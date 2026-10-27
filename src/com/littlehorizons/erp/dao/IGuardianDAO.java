package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Guardian;
import com.littlehorizons.erp.model.GuardianLink;

import java.util.List;

public interface IGuardianDAO {
    void addGuardian(Guardian guardian);

    List<Guardian> listAllGuardians();

    Guardian findGuardianById(int id);

    Guardian findGuardianByDocument(String documentNumber);

    List<Guardian> findGuardiansByStudent(int studentId);

    void updateGuardian(Guardian guardian);

    void deleteGuardian(int id);

    void linkGuardian(GuardianLink link);

    void unlinkGuardian(int studentId, int guardianId);

    boolean isLinked(int studentId, int guardianId);

    int countGuardiansOf(int studentId);
}
