package com.groom.manvsclass.service;

import com.groom.manvsclass.api.UserServiceClient;
import com.groom.manvsclass.model.DeliveredSuggestion;
import com.groom.manvsclass.model.Suggestion;
import com.groom.manvsclass.model.SuggestionDifficulty;
import com.groom.manvsclass.model.SuggestionTier;
import com.groom.manvsclass.model.dto.suggestion.AdvancedSuggestionRequestDTO;
import com.groom.manvsclass.model.dto.suggestion.SuggestionAvailabilityResponseDTO;
import com.groom.manvsclass.model.dto.suggestion.SuggestionCreateRequestDTO;
import com.groom.manvsclass.model.dto.suggestion.SuggestionListItemDTO;
import com.groom.manvsclass.model.dto.suggestion.SuggestionRequestDTO;
import com.groom.manvsclass.model.dto.suggestion.SuggestionResponseDTO;
import com.groom.manvsclass.model.repository.DeliveredSuggestionRepository;
import com.groom.manvsclass.model.repository.SuggestionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SuggestionServiceTest {

    @Mock
    private SuggestionRepository suggestionRepository;

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private DeliveredSuggestionRepository deliveredSuggestionRepository;

    @InjectMocks
    private SuggestionService suggestionService;

    @Test
    void requestSuggestions_baseNoSuggestions_throwsNotFound() {
        SuggestionRequestDTO request = new SuggestionRequestDTO();
        request.setClassName("MyClass");
        request.setDifficulty("EASY");
        request.setRemainingSuggestions(0);

        when(suggestionRepository.findByDifficultyAndClassNameIgnoreCaseAndTier(
                SuggestionDifficulty.EASY,
                "MyClass",
                SuggestionTier.BASE
        )).thenReturn(Collections.emptyList());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> suggestionService.requestSuggestions(request));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        verify(userServiceClient, never()).spendHintCredits(anyLong(), anyInt());
    }

    @Test
    void requestAdvancedSuggestions_noSuggestions_returnsEmptyResponse() {
        AdvancedSuggestionRequestDTO request = new AdvancedSuggestionRequestDTO();
        request.setClassName("MyClass");
        request.setDifficulty("HARD");
        request.setRemainingSuggestions(0);
        request.setPlayerId(7L);

        when(suggestionRepository.findByDifficultyAndClassNameIgnoreCaseAndTier(
                SuggestionDifficulty.HARD,
                "MyClass",
                SuggestionTier.ADVANCED
        )).thenReturn(Collections.emptyList());

        SuggestionResponseDTO response = suggestionService.requestAdvancedSuggestions(request);

        assertTrue(response.isNoMoreSuggestions());
        assertEquals("ADVANCED", response.getTier());
        assertEquals(0, response.getSuggestions().size());
        assertEquals(0, response.getRemainingSuggestions());
        assertEquals(0, response.getSuggestionsAvailable());
        assertEquals(0, response.getSuggestionsMax());
        assertEquals(0, response.getTotalAvailableSuggestions());
        verify(userServiceClient, never()).spendHintCredits(anyLong(), anyInt());
    }

    @Test
    void requestAdvancedSuggestions_withSuggestion_spendsCreditsAndPersistsDelivered() {
        AdvancedSuggestionRequestDTO request = new AdvancedSuggestionRequestDTO();
        request.setClassName("MyClass");
        request.setDifficulty("MEDIUM");
        request.setRemainingSuggestions(0);
        request.setPlayerId(7L);
        request.setCost(3);

        Suggestion suggestion = Suggestion.builder()
                .id(10L)
                .text("Usa un costruttore")
                .className("MyClass")
                .difficulty(SuggestionDifficulty.MEDIUM)
                .tier(SuggestionTier.ADVANCED)
                .build();

        when(suggestionRepository.findByDifficultyAndClassNameIgnoreCaseAndTier(
                SuggestionDifficulty.MEDIUM,
                "MyClass",
                SuggestionTier.ADVANCED
        )).thenReturn(List.of(suggestion));

        when(deliveredSuggestionRepository.findBySessionKey("nogame|myclass|MEDIUM|ADVANCED"))
                .thenReturn(Collections.emptyList());

        when(userServiceClient.spendHintCredits(7L, 3)).thenReturn(5);

        SuggestionResponseDTO response = suggestionService.requestAdvancedSuggestions(request);

        assertEquals(List.of("Usa un costruttore"), response.getSuggestions());
        assertTrue(response.isNoMoreSuggestions());
        assertEquals(Integer.valueOf(5), response.getCreditsLeft());
        assertEquals(Integer.valueOf(3), response.getCreditsSpent());
        assertEquals(Integer.valueOf(3), response.getSuggestionCost());
        assertEquals("ADVANCED", response.getTier());
        assertEquals(0, response.getRemainingSuggestions());

        ArgumentCaptor<DeliveredSuggestion> captor = ArgumentCaptor.forClass(DeliveredSuggestion.class);
        verify(deliveredSuggestionRepository).save(captor.capture());
        DeliveredSuggestion saved = captor.getValue();
        assertNotNull(saved);
        assertEquals("nogame|myclass|MEDIUM|ADVANCED", saved.getSessionKey());
        assertEquals(10L, saved.getSuggestionId());
    }

    @Test
    void getAvailability_filtersInvalidDeliveredSuggestions() {
        Suggestion suggestionOne = Suggestion.builder()
                .id(1L)
                .text("Suggerimento 1")
                .className("MyClass")
                .difficulty(SuggestionDifficulty.EASY)
                .tier(SuggestionTier.BASE)
                .build();
        Suggestion suggestionTwo = Suggestion.builder()
                .id(2L)
                .text("Suggerimento 2")
                .className("MyClass")
                .difficulty(SuggestionDifficulty.EASY)
                .tier(SuggestionTier.BASE)
                .build();

        when(suggestionRepository.findByDifficultyAndClassNameIgnoreCaseAndTier(
                SuggestionDifficulty.EASY,
                "MyClass",
                SuggestionTier.BASE
        )).thenReturn(List.of(suggestionOne, suggestionTwo));

        DeliveredSuggestion deliveredValid = DeliveredSuggestion.builder()
                .sessionKey("nogame|myclass|EASY|BASE")
                .suggestionId(1L)
                .createdAt(Instant.now().minusSeconds(60))
                .build();
        DeliveredSuggestion deliveredInvalid = DeliveredSuggestion.builder()
                .sessionKey("nogame|myclass|EASY|BASE")
                .suggestionId(99L)
                .createdAt(Instant.now())
                .build();

        when(deliveredSuggestionRepository.findBySessionKey("nogame|myclass|EASY|BASE"))
                .thenReturn(List.of(deliveredValid, deliveredInvalid));

        SuggestionAvailabilityResponseDTO response = suggestionService.getAvailability(
                "EASY",
                "MyClass",
                null,
                null
        );

        assertEquals(1, response.getAvailableSuggestions());
        assertEquals(2, response.getSuggestionsMax());
        assertEquals(2, response.getTotalAvailableSuggestions());
        assertEquals(List.of("Suggerimento 1"), response.getDeliveredSuggestions());

        ArgumentCaptor<Collection<Long>> invalidCaptor = ArgumentCaptor.forClass(Collection.class);
        verify(deliveredSuggestionRepository).deleteBySessionKeyAndSuggestionIdIn(
                eq("nogame|myclass|EASY|BASE"),
                invalidCaptor.capture()
        );
        Collection<Long> invalidIds = invalidCaptor.getValue();
        assertTrue(invalidIds.contains(99L));
        assertFalse(invalidIds.contains(1L));
    }

    @Test
    void requestAdvancedSuggestions_missingPlayerId_throwsBadRequest() {
        AdvancedSuggestionRequestDTO request = new AdvancedSuggestionRequestDTO();
        request.setClassName("MyClass");
        request.setDifficulty("MEDIUM");
        request.setRemainingSuggestions(0);

        Suggestion suggestion = Suggestion.builder()
                .id(10L)
                .text("Suggerimento avanzato")
                .className("MyClass")
                .difficulty(SuggestionDifficulty.MEDIUM)
                .tier(SuggestionTier.ADVANCED)
                .build();

        when(suggestionRepository.findByDifficultyAndClassNameIgnoreCaseAndTier(
                SuggestionDifficulty.MEDIUM,
                "MyClass",
                SuggestionTier.ADVANCED
        )).thenReturn(List.of(suggestion));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> suggestionService.requestAdvancedSuggestions(request));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        verify(userServiceClient, never()).spendHintCredits(anyLong(), anyInt());
    }

    @Test
    void requestSuggestions_resetsSessionWhenClientCounterResets() {
        SuggestionRequestDTO request = new SuggestionRequestDTO();
        request.setClassName("MyClass");
        request.setDifficulty("EASY");
        request.setRemainingSuggestions(1);

        Suggestion suggestion = Suggestion.builder()
                .id(1L)
                .text("Suggerimento")
                .className("MyClass")
                .difficulty(SuggestionDifficulty.EASY)
                .tier(SuggestionTier.BASE)
                .build();

        when(suggestionRepository.findByDifficultyAndClassNameIgnoreCaseAndTier(
                SuggestionDifficulty.EASY,
                "MyClass",
                SuggestionTier.BASE
        )).thenReturn(List.of(suggestion));

        when(deliveredSuggestionRepository.findBySessionKey("nogame|myclass|EASY|BASE"))
                .thenReturn(Collections.emptyList());

        SuggestionResponseDTO response = suggestionService.requestSuggestions(request);

        assertEquals(List.of("Suggerimento"), response.getSuggestions());
        verify(deliveredSuggestionRepository).deleteBySessionKey("nogame|myclass|EASY|BASE");
        verify(deliveredSuggestionRepository, times(1)).save(any(DeliveredSuggestion.class));
    }

    @Test
    void createSuggestion_blankText_throwsBadRequest() {
        SuggestionCreateRequestDTO request = new SuggestionCreateRequestDTO();
        request.setClassName("MyClass");
        request.setDifficulty("EASY");
        request.setText("   ");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> suggestionService.createSuggestion(request));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void deleteSuggestion_notFound_throwsNotFound() {
        when(suggestionRepository.existsById(42L)).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> suggestionService.deleteSuggestion(42L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

        @Test
        void listSuggestions_withTier_filtersByTier() {
                Suggestion suggestion = Suggestion.builder()
                                .id(5L)
                                .text("Suggerimento base")
                                .className("MyClass")
                                .difficulty(SuggestionDifficulty.EASY)
                                .tier(SuggestionTier.BASE)
                                .language("it")
                                .build();

                when(suggestionRepository.findByDifficultyAndClassNameIgnoreCaseAndTier(
                                SuggestionDifficulty.EASY,
                                "MyClass",
                                SuggestionTier.BASE
                )).thenReturn(List.of(suggestion));

                List<SuggestionListItemDTO> result = suggestionService.listSuggestions("MyClass", "EASY", "BASE");

                assertEquals(1, result.size());
                assertEquals("MyClass", result.get(0).getClassName());
                assertEquals("EASY", result.get(0).getDifficulty());
                assertEquals("BASE", result.get(0).getTier());
                assertEquals("it", result.get(0).getLanguage());
        }

        @Test
        void listSuggestions_withoutTier_usesFallbackTier() {
                Suggestion suggestion = Suggestion.builder()
                                .id(6L)
                                .text("Suggerimento")
                                .className("MyClass")
                                .difficulty(SuggestionDifficulty.MEDIUM)
                                .tier(null)
                                .language("it")
                                .build();

                when(suggestionRepository.findByClassNameIgnoreCaseAndDifficulty("MyClass", SuggestionDifficulty.MEDIUM))
                                .thenReturn(List.of(suggestion));

                List<SuggestionListItemDTO> result = suggestionService.listSuggestions("MyClass", "MEDIUM", null);

                assertEquals(1, result.size());
                assertEquals("BASE", result.get(0).getTier());
        }

        @Test
        void getCaps_returnsConfiguredValues() {
                java.util.Map<String, Integer> caps = suggestionService.getCaps();

                assertEquals(10, caps.get("EASY"));
                assertEquals(5, caps.get("MEDIUM"));
                assertEquals(2, caps.get("HARD"));
        }

        @Test
        void requestSuggestions_invalidDifficulty_throwsBadRequest() {
                SuggestionRequestDTO request = new SuggestionRequestDTO();
                request.setClassName("MyClass");
                request.setDifficulty("IMPOSSIBLE");
                request.setRemainingSuggestions(0);

                ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                                () -> suggestionService.requestSuggestions(request));

                assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        }

        @Test
        void listSuggestions_invalidTier_throwsBadRequest() {
                ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                                () -> suggestionService.listSuggestions("MyClass", "EASY", "GOLD"));

                assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        }

        @Test
        void createSuggestion_blankClassName_throwsBadRequest() {
                SuggestionCreateRequestDTO request = new SuggestionCreateRequestDTO();
                request.setClassName(" ");
                request.setDifficulty("EASY");
                request.setText("Suggerimento");

                ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                                () -> suggestionService.createSuggestion(request));

                assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        }

        @Test
        void createSuggestion_success_persistsWithCorrectFields() {
                SuggestionCreateRequestDTO request = new SuggestionCreateRequestDTO();
                request.setClassName("MyClass");
                request.setDifficulty("MEDIUM");
                request.setText("  Suggerimento con spazi  ");
                request.setTier("BASE");
                request.setLanguage("en");

                Suggestion savedSuggestion = Suggestion.builder()
                                .id(1L)
                                .className("MyClass")
                                .difficulty(SuggestionDifficulty.MEDIUM)
                                .text("Suggerimento con spazi")
                                .tier(SuggestionTier.BASE)
                                .language("en")
                                .build();

                when(suggestionRepository.save(any(Suggestion.class))).thenReturn(savedSuggestion);

                Suggestion result = suggestionService.createSuggestion(request);

                assertNotNull(result);
                assertEquals("MyClass", result.getClassName());
                assertEquals(SuggestionDifficulty.MEDIUM, result.getDifficulty());
                assertEquals("Suggerimento con spazi", result.getText());
                assertEquals(SuggestionTier.BASE, result.getTier());
                assertEquals("en", result.getLanguage());

                ArgumentCaptor<Suggestion> captor = ArgumentCaptor.forClass(Suggestion.class);
                verify(suggestionRepository).save(captor.capture());
                Suggestion captured = captor.getValue();
                assertEquals("MyClass", captured.getClassName());
                assertEquals("Suggerimento con spazi", captured.getText());
        }

        @Test
        void createSuggestion_nullLanguage_defaultsToItalian() {
                SuggestionCreateRequestDTO request = new SuggestionCreateRequestDTO();
                request.setClassName("MyClass");
                request.setDifficulty("EASY");
                request.setText("Suggerimento");
                request.setTier("BASE");

                Suggestion savedSuggestion = Suggestion.builder()
                                .id(2L)
                                .className("MyClass")
                                .difficulty(SuggestionDifficulty.EASY)
                                .text("Suggerimento")
                                .tier(SuggestionTier.BASE)
                                .language("it")
                                .build();

                when(suggestionRepository.save(any(Suggestion.class))).thenReturn(savedSuggestion);

                Suggestion result = suggestionService.createSuggestion(request);

                assertEquals("it", result.getLanguage());
        }

        @Test
        void replaceSuggestions_success_deletesOldAndSavesNew() {
                com.groom.manvsclass.model.dto.suggestion.SuggestionImportRequestDTO request = 
                        new com.groom.manvsclass.model.dto.suggestion.SuggestionImportRequestDTO();
                request.setClassName("TestClass");

                com.groom.manvsclass.model.dto.suggestion.SuggestionImportItemDTO item1 = 
                        new com.groom.manvsclass.model.dto.suggestion.SuggestionImportItemDTO();
                item1.setDifficulty("EASY");
                item1.setText("Primo suggerimento");
                item1.setTier("BASE");

                com.groom.manvsclass.model.dto.suggestion.SuggestionImportItemDTO item2 = 
                        new com.groom.manvsclass.model.dto.suggestion.SuggestionImportItemDTO();
                item2.setDifficulty("HARD");
                item2.setText("Secondo suggerimento");
                item2.setTier("ADVANCED");

                request.setSuggestions(List.of(item1, item2));

                suggestionService.replaceSuggestions(request);

                verify(suggestionRepository).deleteByClassNameIgnoreCase("TestClass");
                ArgumentCaptor<List<Suggestion>> captor = ArgumentCaptor.forClass(List.class);
                verify(suggestionRepository).saveAll(captor.capture());
                
                List<Suggestion> saved = captor.getValue();
                assertEquals(2, saved.size());
                assertEquals("TestClass", saved.get(0).getClassName());
                assertEquals("Primo suggerimento", saved.get(0).getText());
                assertEquals(SuggestionDifficulty.EASY, saved.get(0).getDifficulty());
                assertEquals("Secondo suggerimento", saved.get(1).getText());
                assertEquals(SuggestionDifficulty.HARD, saved.get(1).getDifficulty());
        }

        @Test
        void requestSuggestions_withValidGameId_doesNotResetSession() {
                SuggestionRequestDTO request = new SuggestionRequestDTO();
                request.setClassName("MyClass");
                request.setDifficulty("EASY");
                request.setRemainingSuggestions(10);
                request.setGameId(42L);

                Suggestion suggestion = Suggestion.builder()
                                .id(1L)
                                .text("Suggerimento")
                                .className("MyClass")
                                .difficulty(SuggestionDifficulty.EASY)
                                .tier(SuggestionTier.BASE)
                                .build();

                when(suggestionRepository.findByDifficultyAndClassNameIgnoreCaseAndTier(
                                SuggestionDifficulty.EASY,
                                "MyClass",
                                SuggestionTier.BASE
                )).thenReturn(List.of(suggestion));

                when(deliveredSuggestionRepository.findBySessionKey("game-42|myclass|EASY|BASE"))
                                .thenReturn(Collections.emptyList());

                SuggestionResponseDTO response = suggestionService.requestSuggestions(request);

                assertEquals(List.of("Suggerimento"), response.getSuggestions());
                verify(deliveredSuggestionRepository, never()).deleteBySessionKey(any());
                
                ArgumentCaptor<DeliveredSuggestion> captor = ArgumentCaptor.forClass(DeliveredSuggestion.class);
                verify(deliveredSuggestionRepository).save(captor.capture());
                assertEquals("game-42|myclass|EASY|BASE", captor.getValue().getSessionKey());
        }

        @Test
        void requestSuggestions_easyCappedAt10_evenWithMoreAvailable() {
                SuggestionRequestDTO request = new SuggestionRequestDTO();
                request.setClassName("MyClass");
                request.setDifficulty("EASY");
                request.setRemainingSuggestions(0);

                List<Suggestion> manySuggestions = new java.util.ArrayList<>();
                for (int i = 1; i <= 15; i++) {
                        manySuggestions.add(Suggestion.builder()
                                        .id((long) i)
                                        .text("Suggerimento " + i)
                                        .className("MyClass")
                                        .difficulty(SuggestionDifficulty.EASY)
                                        .tier(SuggestionTier.BASE)
                                        .build());
                }

                when(suggestionRepository.findByDifficultyAndClassNameIgnoreCaseAndTier(
                                SuggestionDifficulty.EASY,
                                "MyClass",
                                SuggestionTier.BASE
                )).thenReturn(manySuggestions);

                when(deliveredSuggestionRepository.findBySessionKey("nogame|myclass|EASY|BASE"))
                                .thenReturn(Collections.emptyList());

                SuggestionResponseDTO response = suggestionService.requestSuggestions(request);

                assertEquals(10, response.getSuggestionsMax());
                assertEquals(10, response.getTotalAvailableSuggestions());
                assertEquals(9, response.getRemainingSuggestions());
        }

        @Test
        void requestSuggestions_mediumCappedAt5() {
                SuggestionRequestDTO request = new SuggestionRequestDTO();
                request.setClassName("MyClass");
                request.setDifficulty("MEDIUM");
                request.setRemainingSuggestions(0);

                List<Suggestion> manySuggestions = new java.util.ArrayList<>();
                for (int i = 1; i <= 10; i++) {
                        manySuggestions.add(Suggestion.builder()
                                        .id((long) i)
                                        .text("Suggerimento " + i)
                                        .className("MyClass")
                                        .difficulty(SuggestionDifficulty.MEDIUM)
                                        .tier(SuggestionTier.BASE)
                                        .build());
                }

                when(suggestionRepository.findByDifficultyAndClassNameIgnoreCaseAndTier(
                                SuggestionDifficulty.MEDIUM,
                                "MyClass",
                                SuggestionTier.BASE
                )).thenReturn(manySuggestions);

                when(deliveredSuggestionRepository.findBySessionKey("nogame|myclass|MEDIUM|BASE"))
                                .thenReturn(Collections.emptyList());

                SuggestionResponseDTO response = suggestionService.requestSuggestions(request);

                assertEquals(5, response.getSuggestionsMax());
                assertEquals(5, response.getTotalAvailableSuggestions());
                assertEquals(4, response.getRemainingSuggestions());
        }

        @Test
        void requestSuggestions_hardCappedAt2() {
                SuggestionRequestDTO request = new SuggestionRequestDTO();
                request.setClassName("MyClass");
                request.setDifficulty("HARD");
                request.setRemainingSuggestions(0);

                List<Suggestion> manySuggestions = new java.util.ArrayList<>();
                for (int i = 1; i <= 5; i++) {
                        manySuggestions.add(Suggestion.builder()
                                        .id((long) i)
                                        .text("Suggerimento " + i)
                                        .className("MyClass")
                                        .difficulty(SuggestionDifficulty.HARD)
                                        .tier(SuggestionTier.BASE)
                                        .build());
                }

                when(suggestionRepository.findByDifficultyAndClassNameIgnoreCaseAndTier(
                                SuggestionDifficulty.HARD,
                                "MyClass",
                                SuggestionTier.BASE
                )).thenReturn(manySuggestions);

                when(deliveredSuggestionRepository.findBySessionKey("nogame|myclass|HARD|BASE"))
                                .thenReturn(Collections.emptyList());

                SuggestionResponseDTO response = suggestionService.requestSuggestions(request);

                assertEquals(2, response.getSuggestionsMax());
                assertEquals(2, response.getTotalAvailableSuggestions());
                assertEquals(1, response.getRemainingSuggestions());
        }

        @Test
        void requestAdvancedSuggestions_noCap_returnsAllAvailable() {
                AdvancedSuggestionRequestDTO request = new AdvancedSuggestionRequestDTO();
                request.setClassName("MyClass");
                request.setDifficulty("EASY");
                request.setRemainingSuggestions(0);
                request.setPlayerId(7L);
                request.setCost(2);

                List<Suggestion> manySuggestions = new java.util.ArrayList<>();
                for (int i = 1; i <= 20; i++) {
                        manySuggestions.add(Suggestion.builder()
                                        .id((long) i)
                                        .text("Suggerimento avanzato " + i)
                                        .className("MyClass")
                                        .difficulty(SuggestionDifficulty.EASY)
                                        .tier(SuggestionTier.ADVANCED)
                                        .build());
                }

                when(suggestionRepository.findByDifficultyAndClassNameIgnoreCaseAndTier(
                                SuggestionDifficulty.EASY,
                                "MyClass",
                                SuggestionTier.ADVANCED
                )).thenReturn(manySuggestions);

                when(deliveredSuggestionRepository.findBySessionKey("nogame|myclass|EASY|ADVANCED"))
                                .thenReturn(Collections.emptyList());

                when(userServiceClient.spendHintCredits(7L, 2)).thenReturn(10);

                SuggestionResponseDTO response = suggestionService.requestAdvancedSuggestions(request);

                assertEquals(20, response.getSuggestionsMax());
                assertEquals(20, response.getTotalAvailableSuggestions());
                assertEquals(19, response.getRemainingSuggestions());
        }

        @Test
        void requestSuggestions_reachingCap_setsNoMoreSuggestionsTrue() {
                SuggestionRequestDTO request = new SuggestionRequestDTO();
                request.setClassName("MyClass");
                request.setDifficulty("HARD");
                request.setRemainingSuggestions(0);

                Suggestion suggestion1 = Suggestion.builder()
                                .id(1L)
                                .text("Primo")
                                .className("MyClass")
                                .difficulty(SuggestionDifficulty.HARD)
                                .tier(SuggestionTier.BASE)
                                .build();
                Suggestion suggestion2 = Suggestion.builder()
                                .id(2L)
                                .text("Secondo")
                                .className("MyClass")
                                .difficulty(SuggestionDifficulty.HARD)
                                .tier(SuggestionTier.BASE)
                                .build();

                when(suggestionRepository.findByDifficultyAndClassNameIgnoreCaseAndTier(
                                SuggestionDifficulty.HARD,
                                "MyClass",
                                SuggestionTier.BASE
                )).thenReturn(List.of(suggestion1, suggestion2));

                when(deliveredSuggestionRepository.findBySessionKey("nogame|myclass|HARD|BASE"))
                                .thenReturn(List.of(
                                        DeliveredSuggestion.builder()
                                                        .sessionKey("nogame|myclass|HARD|BASE")
                                                        .suggestionId(1L)
                                                        .createdAt(Instant.now())
                                                        .build()
                                ));

                SuggestionResponseDTO response = suggestionService.requestSuggestions(request);

                assertEquals(1, response.getSuggestions().size());
                assertEquals(0, response.getRemainingSuggestions());
                assertTrue(response.isNoMoreSuggestions());
        }

        @Test
        void getAvailability_allSuggestionsDelivered_returnsZeroAvailable() {
                Suggestion suggestion1 = Suggestion.builder()
                                .id(1L)
                                .text("Primo")
                                .className("MyClass")
                                .difficulty(SuggestionDifficulty.HARD)
                                .tier(SuggestionTier.BASE)
                                .build();
                Suggestion suggestion2 = Suggestion.builder()
                                .id(2L)
                                .text("Secondo")
                                .className("MyClass")
                                .difficulty(SuggestionDifficulty.HARD)
                                .tier(SuggestionTier.BASE)
                                .build();

                when(suggestionRepository.findByDifficultyAndClassNameIgnoreCaseAndTier(
                                SuggestionDifficulty.HARD,
                                "MyClass",
                                SuggestionTier.BASE
                )).thenReturn(List.of(suggestion1, suggestion2));

                when(deliveredSuggestionRepository.findBySessionKey("nogame|myclass|HARD|BASE"))
                                .thenReturn(List.of(
                                        DeliveredSuggestion.builder()
                                                        .sessionKey("nogame|myclass|HARD|BASE")
                                                        .suggestionId(1L)
                                                        .createdAt(Instant.now().minusSeconds(120))
                                                        .build(),
                                        DeliveredSuggestion.builder()
                                                        .sessionKey("nogame|myclass|HARD|BASE")
                                                        .suggestionId(2L)
                                                        .createdAt(Instant.now().minusSeconds(60))
                                                        .build()
                                ));

                SuggestionAvailabilityResponseDTO response = suggestionService.getAvailability(
                                "HARD",
                                "MyClass",
                                null,
                                null
                );

                assertEquals(0, response.getAvailableSuggestions());
                assertEquals(2, response.getSuggestionsMax());
                assertEquals(2, response.getDeliveredSuggestions().size());
        }

        @Test
        void getAvailability_withGameId_usesCorrectSessionKey() {
                Suggestion suggestion = Suggestion.builder()
                                .id(1L)
                                .text("Suggerimento")
                                .className("MyClass")
                                .difficulty(SuggestionDifficulty.EASY)
                                .tier(SuggestionTier.BASE)
                                .build();

                when(suggestionRepository.findByDifficultyAndClassNameIgnoreCaseAndTier(
                                SuggestionDifficulty.EASY,
                                "MyClass",
                                SuggestionTier.BASE
                )).thenReturn(List.of(suggestion));

                when(deliveredSuggestionRepository.findBySessionKey("game-99|myclass|EASY|BASE"))
                                .thenReturn(Collections.emptyList());

                SuggestionAvailabilityResponseDTO response = suggestionService.getAvailability(
                                "EASY",
                                "MyClass",
                                null,
                                99L
                );

                assertEquals(1, response.getAvailableSuggestions());
                verify(deliveredSuggestionRepository).findBySessionKey("game-99|myclass|EASY|BASE");
        }
}
