package com.groom.manvsclass.model.repository;

import com.groom.manvsclass.model.interaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InteractionRepository extends JpaRepository<interaction, Long> {

    long countByNameAndType(String name, int type);

    List<interaction> findByType(int type);
}
