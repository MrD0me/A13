package com.example.db_setup.model.repository;

import com.example.db_setup.model.DeliveredSuggestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface DeliveredSuggestionRepository extends JpaRepository<DeliveredSuggestion, Long> {

    List<DeliveredSuggestion> findBySessionKey(String sessionKey);

    void deleteBySessionKey(String sessionKey);

    void deleteBySessionKeyAndSuggestionIdIn(String sessionKey, Collection<Long> suggestionIds);
}
