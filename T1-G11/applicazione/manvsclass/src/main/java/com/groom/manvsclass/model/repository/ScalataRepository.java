package com.groom.manvsclass.model.repository;

import com.groom.manvsclass.model.Scalata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScalataRepository extends JpaRepository<Scalata, String> {

    List<Scalata> findByUsernameContaining(String username);

    List<Scalata> findByNumberOfRounds(int numberOfRounds);

    List<Scalata> findByScalataNameContaining(String scalataName);

}
