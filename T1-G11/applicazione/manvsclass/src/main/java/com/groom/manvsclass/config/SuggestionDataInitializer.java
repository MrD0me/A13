package com.groom.manvsclass.config;

import com.groom.manvsclass.model.Suggestion;
import com.groom.manvsclass.model.repository.SuggestionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class SuggestionDataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(SuggestionDataInitializer.class);

    private final SuggestionRepository suggestionRepository;

    public SuggestionDataInitializer(SuggestionRepository suggestionRepository) {
        this.suggestionRepository = suggestionRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        long count = suggestionRepository.count();
        if (count > 0) {
            logger.info("Suggestion collection already contains {} items - skipping initialization.", count);
            return;
        }

        List<Suggestion> seed = Arrays.asList(
                new Suggestion("Aggiungi commenti alle funzioni complesse", "style", "EASY"),
                new Suggestion("Nomi di variabili chiari e descrittivi", "naming", "EASY"),
                new Suggestion("Controlla i valori di input prima di usarli", "validation", "EASY"),

                new Suggestion("Gestisci le eccezioni e logga gli errori", "error-handling", "MEDIUM"),
                new Suggestion("Evita code duplication con funzioni", "refactor", "MEDIUM"),
                new Suggestion("Rimuovi chiamate sincrone non necessarie", "performance", "MEDIUM"),

                new Suggestion("Ottimizza il percorso critico dell'algoritmo", "performance", "HARD"),
                new Suggestion("Analizza l'uso della memoria per ridurre i picchi", "performance", "HARD")
        );

        suggestionRepository.saveAll(seed);
        logger.info("Initialized {} suggestions in the database.", seed.size());
    }
}
