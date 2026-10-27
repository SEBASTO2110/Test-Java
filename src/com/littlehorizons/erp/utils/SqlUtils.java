package com.littlehorizons.erp.utils;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;

/**
 * Helpers to save and read optional values (NULL) with JDBC.
 */
public class SqlUtils {

    private SqlUtils() {
    }

    public static void setText(PreparedStatement pst, int index, String value) throws SQLException {
        if (value == null || value.isBlank()) {
            pst.setNull(index, Types.VARCHAR);
        } else {
            pst.setString(index, value);
        }
    }

    public static void setInteger(PreparedStatement pst, int index, Integer value) throws SQLException {
        if (value == null) {
            pst.setNull(index, Types.INTEGER);
        } else {
            pst.setInt(index, value);
        }
    }

    public static void setDate(PreparedStatement pst, int index, LocalDate value) throws SQLException {
        if (value == null) {
            pst.setNull(index, Types.DATE);
        } else {
            pst.setDate(index, Date.valueOf(value));
        }
    }

    public static LocalDate getDate(ResultSet rs, String column) throws SQLException {
        Date date = rs.getDate(column);
        return date == null ? null : date.toLocalDate();
    }

    public static Integer getInteger(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }

    /**
     * SQL expression that joins the names of a person table (estudiantes, acudientes, profesores).
     */
    public static String fullName(String alias) {
        return "CONCAT_WS(' ', " + alias + ".primer_nombre, " + alias + ".segundo_nombre, "
                + alias + ".primer_apellido, " + alias + ".segundo_apellido)";
    }
}
