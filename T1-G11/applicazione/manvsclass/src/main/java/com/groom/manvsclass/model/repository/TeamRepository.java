package com.groom.manvsclass.model.repository;

import com.groom.manvsclass.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamRepository extends JpaRepository<Team, String> {
    //MODIFICA 02/12/2024: aggiutna verifica se esiste un team con il nome specificato
    boolean existsByName(String name);

    Team findByIdStudenti(String idStudente);
}
