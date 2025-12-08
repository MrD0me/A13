package com.groom.manvsclass.model.repository;

import com.groom.manvsclass.model.Opponent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import testrobotchallenge.commons.models.opponent.OpponentDifficulty;

import java.util.List;
import java.util.Optional;

@Repository
public interface OpponentRepository extends JpaRepository<Opponent, String> {

    Optional<Opponent> findFirstByClassUTAndOpponentTypeAndOpponentDifficultyOrderByCreatedAtDesc(String classUT,
                                                                                                  String opponentType,
                                                                                                  OpponentDifficulty difficulty);

    void deleteByClassUT(String classUT);
}
