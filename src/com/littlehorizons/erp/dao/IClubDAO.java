package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Club;

import java.util.List;

public interface IClubDAO {
    void addClub(Club club);

    List<Club> listAllClubs();

    Club findClubById(int id);

    boolean existsClubName(String name, int excludedId);

    void updateClub(Club club);

    void deleteClub(int id);
}
