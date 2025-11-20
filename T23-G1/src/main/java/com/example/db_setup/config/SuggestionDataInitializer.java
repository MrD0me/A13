//inizializza 1 suggerimento nel db

/*
package com.example.db_setup.config;

import com.example.db_setup.model.Suggestion;
import com.example.db_setup.model.SuggestionDifficulty;
import com.example.db_setup.model.repository.SuggestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@Slf4j
public class SuggestionDataInitializer implements CommandLineRunner {

    private final SuggestionRepository suggestionRepository;

    @Override
    public void run(String... args) {
        if (suggestionRepository.count() > 0) {
            return;
        }
        suggestionRepository.save(
                Suggestion.builder()
                        .text("test_suggerimento_preso_da_db")
                        .className("DEFAULT_CLASS")
                        .difficulty(SuggestionDifficulty.EASY)
                        .language("it")
                        .build()
        );
        log.info("Suggerimento di default inserito nel database");
    }
}*/
