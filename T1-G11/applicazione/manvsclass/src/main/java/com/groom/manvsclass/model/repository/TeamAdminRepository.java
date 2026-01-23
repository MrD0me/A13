package com.groom.manvsclass.model.repository;

import com.groom.manvsclass.model.TeamAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamAdminRepository extends JpaRepository<TeamAdmin, Long> {

    TeamAdmin findByAdminId(String adminId);

    TeamAdmin findByTeamId(String teamId);

    List<TeamAdmin> findAllByAdminId(String adminId);
}
