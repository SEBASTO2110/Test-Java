package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.LoanStatus;
import com.littlehorizons.erp.model.Loan;
import com.littlehorizons.erp.utils.DBConnectionSingleton;
import com.littlehorizons.erp.utils.ErrorLogger;
import com.littlehorizons.erp.utils.SqlUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Shared code of the two loan tables (prestamos_estudiante and prestamos_profesor).
 * Each subclass only says which table, borrower table and id column it uses.
 */
public abstract class LoanDaoImpl implements ILoanDAO {
    protected Connection con;

    protected LoanDaoImpl() {
        this.con = DBConnectionSingleton.getInstance();
    }

    protected abstract String getLoanTable();

    protected abstract String getBorrowerTable();

    protected abstract String getBorrowerIdColumn();

    protected abstract Loan createEmptyLoan();

    private String getSelect() {
        return "SELECT l.*, l." + getBorrowerIdColumn() + " AS id_prestatario, r.codigo_recurso, r.nombre_recurso,"
                + " CONCAT_WS(' ', b.primer_nombre, b.primer_apellido, b.segundo_apellido) AS nombre_prestatario"
                + " FROM " + getLoanTable() + " l"
                + " JOIN recursos r ON r.id_recurso = l.id_recurso"
                + " JOIN " + getBorrowerTable() + " b ON b." + getBorrowerIdColumn() + " = l." + getBorrowerIdColumn();
    }

    @Override
    public boolean addLoan(Loan loan) {
        String reserveSql = "UPDATE recursos SET estado_recurso = 'Prestado'"
                + " WHERE id_recurso = ? AND estado_recurso = 'Disponible'";
        String insertSql = "INSERT INTO " + getLoanTable() + " (id_recurso, " + getBorrowerIdColumn()
                + ", fecha_prestamo, fecha_devolucion_esperada, estado_prestamo, observaciones)"
                + " VALUES (?, ?, ?, ?, ?, ?)";

        try {
            con.setAutoCommit(false);
            try (PreparedStatement reserve = con.prepareStatement(reserveSql);
                 PreparedStatement insert = con.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                reserve.setInt(1, loan.getResourceId());
                if (reserve.executeUpdate() == 0) {
                    con.rollback();
                    return false;
                }
                insert.setInt(1, loan.getResourceId());
                insert.setInt(2, loan.getBorrowerId());
                SqlUtils.setDate(insert, 3, loan.getLoanDate());
                SqlUtils.setDate(insert, 4, loan.getExpectedReturnDate());
                insert.setString(5, LoanStatus.ACTIVE.getDbValue());
                SqlUtils.setText(insert, 6, loan.getNotes());
                insert.executeUpdate();
                try (ResultSet keys = insert.getGeneratedKeys()) {
                    if (keys.next()) {
                        loan.setId(keys.getInt(1));
                    }
                }
                con.commit();
                loan.setStatus(LoanStatus.ACTIVE);
                return true;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("registrar el préstamo", e);
        }
    }

    @Override
    public boolean registerReturn(Loan loan, LocalDate returnDate) {
        String returnSql = "UPDATE " + getLoanTable() + " SET fecha_devolucion_real = ?, estado_prestamo = 'Devuelto'"
                + " WHERE id_prestamo = ? AND estado_prestamo <> 'Devuelto'";
        String releaseSql = "UPDATE recursos SET estado_recurso = 'Disponible'"
                + " WHERE id_recurso = ? AND estado_recurso = 'Prestado'";

        try {
            con.setAutoCommit(false);
            try (PreparedStatement returnStatement = con.prepareStatement(returnSql);
                 PreparedStatement release = con.prepareStatement(releaseSql)) {
                SqlUtils.setDate(returnStatement, 1, returnDate);
                returnStatement.setInt(2, loan.getId());
                if (returnStatement.executeUpdate() == 0) {
                    con.rollback();
                    return false;
                }
                release.setInt(1, loan.getResourceId());
                release.executeUpdate();
                con.commit();
                return true;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("registrar la devolución", e);
        }
    }

    @Override
    public int markOverdueLoans(LocalDate today) {
        String sql = "UPDATE " + getLoanTable() + " SET estado_prestamo = 'Vencido'"
                + " WHERE estado_prestamo = 'Activo' AND fecha_devolucion_esperada < ?";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            SqlUtils.setDate(pst, 1, today);
            return pst.executeUpdate();
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("actualizar los préstamos vencidos", e);
        }
    }

    @Override
    public Loan findLoanById(int id) {
        List<Loan> loans = listLoans(getSelect() + " WHERE l.id_prestamo = ?", id);
        return loans.isEmpty() ? null : loans.get(0);
    }

    @Override
    public List<Loan> findLoansByBorrower(int borrowerId) {
        return listLoans(getSelect() + " WHERE l." + getBorrowerIdColumn() + " = ? ORDER BY l.fecha_prestamo DESC",
                borrowerId);
    }

    @Override
    public List<Loan> findLoansByResource(int resourceId) {
        return listLoans(getSelect() + " WHERE l.id_recurso = ? ORDER BY l.fecha_prestamo DESC", resourceId);
    }

    @Override
    public List<Loan> findOpenLoans() {
        return listLoans(getSelect() + " WHERE l.estado_prestamo <> 'Devuelto' ORDER BY l.fecha_devolucion_esperada",
                null);
    }

    private List<Loan> listLoans(String sql, Integer param) {
        List<Loan> loans = new ArrayList<>();

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            if (param != null) {
                pst.setInt(1, param);
            }
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Loan loan = createEmptyLoan();
                    loan.setId(rs.getInt("id_prestamo"));
                    loan.setResourceId(rs.getInt("id_recurso"));
                    loan.setResourceCode(rs.getString("codigo_recurso"));
                    loan.setResourceName(rs.getString("nombre_recurso"));
                    loan.setBorrowerId(rs.getInt("id_prestatario"));
                    loan.setBorrowerName(rs.getString("nombre_prestatario"));
                    loan.setLoanDate(SqlUtils.getDate(rs, "fecha_prestamo"));
                    loan.setExpectedReturnDate(SqlUtils.getDate(rs, "fecha_devolucion_esperada"));
                    loan.setActualReturnDate(SqlUtils.getDate(rs, "fecha_devolucion_real"));
                    loan.setStatus(LoanStatus.fromDbValue(rs.getString("estado_prestamo")));
                    loan.setNotes(rs.getString("observaciones"));
                    loans.add(loan);
                }
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("consultar los préstamos", e);
        }
        return loans;
    }
}
