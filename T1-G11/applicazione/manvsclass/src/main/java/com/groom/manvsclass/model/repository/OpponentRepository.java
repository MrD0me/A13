package com.groom.manvsclass.model.repository;

import com.groom.manvsclass.model.Opponent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import testrobotchallenge.commons.models.opponent.OpponentDifficulty;
import testrobotchallenge.commons.models.score.EvosuiteScore;
import testrobotchallenge.commons.models.score.JacocoScore;

import java.util.List;
import java.util.Optional;

@Repository
public interface OpponentRepository extends JpaRepository<Opponent, String> {

    Optional<Opponent> findByClassUTAndOpponentTypeAndOpponentDifficulty(String classUT,
                                                                         String opponentType,
                                                                         OpponentDifficulty difficulty);

    @Query("select o.evosuiteScore from Opponent o where o.classUT = :classUT and o.opponentType = :opponentType and o.opponentDifficulty = :difficulty")
    Optional<EvosuiteScore> findEvosuiteScore(@Param("classUT") String classUT,
                                              @Param("opponentType") String opponentType,
                                              @Param("difficulty") OpponentDifficulty difficulty);

    @Query("select o.jacocoScore from Opponent o where o.classUT = :classUT and o.opponentType = :opponentType and o.opponentDifficulty = :difficulty")
    Optional<JacocoScore> findJacocoScore(@Param("classUT") String classUT,
                                          @Param("opponentType") String opponentType,
                                          @Param("difficulty") OpponentDifficulty difficulty);

    @Query("select o.coverage from Opponent o where o.classUT = :classUT and o.opponentType = :opponentType and o.opponentDifficulty = :difficulty")
    Optional<String> findCoverage(@Param("classUT") String classUT,
                                  @Param("opponentType") String opponentType,
                                  @Param("difficulty") OpponentDifficulty difficulty);

    List<Opponent> findAll();

    void deleteByClassUT(String classUT);
}
