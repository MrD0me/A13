package com.groom.manvsclass.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.groom.manvsclass.api.ApiGatewayClient;
import com.groom.manvsclass.model.Suggestion;
import com.groom.manvsclass.model.SuggestionDifficulty;
import com.groom.manvsclass.model.SuggestionTier;
import com.groom.manvsclass.model.dto.suggestion.AdvancedSuggestionRequestDTO;
import com.groom.manvsclass.model.dto.suggestion.SuggestionAvailabilityRequestDTO;
import com.groom.manvsclass.model.dto.suggestion.SuggestionAvailabilityResponseDTO;
import com.groom.manvsclass.model.dto.suggestion.SuggestionCreateRequestDTO;
import com.groom.manvsclass.model.dto.suggestion.SuggestionImportItemDTO;
import com.groom.manvsclass.model.dto.suggestion.SuggestionImportRequestDTO;
import com.groom.manvsclass.model.dto.suggestion.SuggestionListItemDTO;
import com.groom.manvsclass.model.dto.suggestion.SuggestionRequestDTO;
import com.groom.manvsclass.model.dto.suggestion.SuggestionResponseDTO;
import com.groom.manvsclass.service.SuggestionService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SuggestionController.class)
@AutoConfigureMockMvc(addFilters = false)
class SuggestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SuggestionService suggestionService;

    @MockBean
    private ApiGatewayClient apiGatewayClient;

    @Test
    void requestSuggestion_returnsResponse() throws Exception {
        SuggestionRequestDTO request = new SuggestionRequestDTO();
        request.setClassName("MyClass");
        request.setDifficulty("EASY");
        request.setRemainingSuggestions(1);

        SuggestionResponseDTO response = SuggestionResponseDTO.builder()
                .suggestions(List.of("Suggerimento"))
                .remainingSuggestions(0)
                .suggestionsAvailable(0)
                .suggestionsMax(1)
                .totalAvailableSuggestions(1)
                .noMoreSuggestions(true)
                .tier("BASE")
                .build();

        when(suggestionService.requestSuggestions(any(SuggestionRequestDTO.class)))
                .thenReturn(response);

        mockMvc.perform(post("/suggerimenti/richiedi")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.suggestions", hasSize(1)))
                .andExpect(jsonPath("$.suggestions[0]", is("Suggerimento")))
                .andExpect(jsonPath("$.tier", is("BASE")));
    }

    @Test
    void requestAdvancedSuggestion_returnsResponse() throws Exception {
        AdvancedSuggestionRequestDTO request = new AdvancedSuggestionRequestDTO();
        request.setClassName("MyClass");
        request.setDifficulty("HARD");
        request.setRemainingSuggestions(0);
        request.setPlayerId(9L);
        request.setCost(2);

        SuggestionResponseDTO response = SuggestionResponseDTO.builder()
                .suggestions(List.of("Suggerimento avanzato"))
                .remainingSuggestions(0)
                .suggestionsAvailable(0)
                .suggestionsMax(1)
                .totalAvailableSuggestions(1)
                .noMoreSuggestions(true)
                .creditsLeft(3)
                .creditsSpent(2)
                .suggestionCost(2)
                .tier("ADVANCED")
                .build();

        when(suggestionService.requestAdvancedSuggestions(any(AdvancedSuggestionRequestDTO.class)))
                .thenReturn(response);

        mockMvc.perform(post("/suggerimenti/avanzati/richiedi")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.suggestions", hasSize(1)))
                .andExpect(jsonPath("$.tier", is("ADVANCED")))
                .andExpect(jsonPath("$.creditsLeft", is(3)))
                .andExpect(jsonPath("$.creditsSpent", is(2)))
                .andExpect(jsonPath("$.suggestionCost", is(2)));
    }

    @Test
    void getAvailability_returnsResponse() throws Exception {
        SuggestionAvailabilityRequestDTO request = new SuggestionAvailabilityRequestDTO();
        request.setClassName("MyClass");
        request.setDifficulty("EASY");
        request.setTier("BASE");
        request.setGameId(1L);

        SuggestionAvailabilityResponseDTO response = SuggestionAvailabilityResponseDTO.builder()
                .availableSuggestions(2)
                .suggestionsMax(3)
                .totalAvailableSuggestions(3)
                .deliveredSuggestions(List.of("Suggerimento 1"))
                .build();

        when(suggestionService.getAvailability("EASY", "MyClass", "BASE", 1L))
                .thenReturn(response);

        mockMvc.perform(post("/suggerimenti/disponibilita")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableSuggestions", is(2)))
                .andExpect(jsonPath("$.suggestionsMax", is(3)))
                .andExpect(jsonPath("$.deliveredSuggestions", hasSize(1)));
    }

    @Test
    void importSuggestions_returnsNoContent() throws Exception {
        SuggestionImportItemDTO item = new SuggestionImportItemDTO();
        item.setDifficulty("EASY");
        item.setText("Suggerimento");
        item.setTier("BASE");

        SuggestionImportRequestDTO request = new SuggestionImportRequestDTO();
        request.setClassName("MyClass");
        request.setSuggestions(List.of(item));

        doNothing().when(suggestionService).replaceSuggestions(any(SuggestionImportRequestDTO.class));

        mockMvc.perform(post("/suggerimenti/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }

    @Test
    void createSuggestion_returnsCreated() throws Exception {
        SuggestionCreateRequestDTO request = new SuggestionCreateRequestDTO();
        request.setClassName("MyClass");
        request.setDifficulty("MEDIUM");
        request.setText("Suggerimento");
        request.setTier("BASE");
        request.setLanguage("it");

        mockMvc.perform(post("/suggerimenti/admin/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void getCaps_returnsConfig() throws Exception {
        when(suggestionService.getCaps()).thenReturn(Map.of(
                "EASY", 10,
                "MEDIUM", 5,
                "HARD", 2
        ));

        mockMvc.perform(post("/suggerimenti/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.EASY", is(10)))
                .andExpect(jsonPath("$.MEDIUM", is(5)))
                .andExpect(jsonPath("$.HARD", is(2)));
    }

    @Test
    void listSuggestions_returnsItems() throws Exception {
        List<SuggestionListItemDTO> response = List.of(
                new SuggestionListItemDTO(1L, "Suggerimento", "MyClass", "EASY", "BASE", "it")
        );

        when(suggestionService.listSuggestions(eq("MyClass"), eq("EASY"), eq("BASE")))
                .thenReturn(response);

        mockMvc.perform(get("/suggerimenti/admin/list")
                        .param("className", "MyClass")
                        .param("difficulty", "EASY")
                        .param("tier", "BASE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].className", is("MyClass")))
                .andExpect(jsonPath("$[0].tier", is("BASE")));
    }

    @Test
    void deleteSuggestion_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/suggerimenti/admin/7"))
                .andExpect(status().isNoContent());
    }

        @Test
        void requestSuggestion_missingRequiredFields_returnsBadRequest() throws Exception {
                mockMvc.perform(post("/suggerimenti/richiedi")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content("{}"))
                                .andExpect(status().isBadRequest());

                verify(suggestionService, never()).requestSuggestions(any(SuggestionRequestDTO.class));
        }

        @Test
        void requestAdvancedSuggestion_missingPlayerId_returnsBadRequest() throws Exception {
                AdvancedSuggestionRequestDTO request = new AdvancedSuggestionRequestDTO();
                request.setClassName("MyClass");
                request.setDifficulty("HARD");
                request.setRemainingSuggestions(0);

                mockMvc.perform(post("/suggerimenti/avanzati/richiedi")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());

                verify(suggestionService, never()).requestAdvancedSuggestions(any(AdvancedSuggestionRequestDTO.class));
        }

        @Test
        void getAvailability_missingDifficulty_returnsBadRequest() throws Exception {
                SuggestionAvailabilityRequestDTO request = new SuggestionAvailabilityRequestDTO();
                request.setClassName("MyClass");

                mockMvc.perform(post("/suggerimenti/disponibilita")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());

                verify(suggestionService, never()).getAvailability(any(), any(), any(), any());
        }

        @Test
        void importSuggestions_emptyPayload_returnsBadRequest() throws Exception {
                SuggestionImportRequestDTO request = new SuggestionImportRequestDTO();
                request.setClassName("MyClass");
                request.setSuggestions(List.of());

                mockMvc.perform(post("/suggerimenti/import")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());

                verify(suggestionService, never()).replaceSuggestions(any(SuggestionImportRequestDTO.class));
        }

            @Test
            void listSuggestions_missingRequiredParams_returnsBadRequest() throws Exception {
                mockMvc.perform(get("/suggerimenti/admin/list")
                                .param("className", "MyClass"))
                        .andExpect(status().isBadRequest());

                verify(suggestionService, never()).listSuggestions(any(), any(), any());
            }

            @Test
            void requestSuggestion_serviceNotFound_returnsNotFound() throws Exception {
                SuggestionRequestDTO request = new SuggestionRequestDTO();
                request.setClassName("MyClass");
                request.setDifficulty("EASY");
                request.setRemainingSuggestions(1);

                when(suggestionService.requestSuggestions(any(SuggestionRequestDTO.class)))
                        .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Nessun suggerimento"));

                mockMvc.perform(post("/suggerimenti/richiedi")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isNotFound());
            }

            @Test
            void requestAdvancedSuggestion_insufficientCredits_returnsPaymentRequired() throws Exception {
                AdvancedSuggestionRequestDTO request = new AdvancedSuggestionRequestDTO();
                request.setClassName("MyClass");
                request.setDifficulty("HARD");
                request.setRemainingSuggestions(0);
                request.setPlayerId(9L);
                request.setCost(2);

                when(suggestionService.requestAdvancedSuggestions(any(AdvancedSuggestionRequestDTO.class)))
                        .thenThrow(new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED, "Crediti insufficienti"));

                mockMvc.perform(post("/suggerimenti/avanzati/richiedi")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isPaymentRequired());
            }

            @Test
            void listSuggestions_missingClassName_returnsBadRequest() throws Exception {
                mockMvc.perform(get("/suggerimenti/admin/list")
                                .param("difficulty", "EASY"))
                        .andExpect(status().isBadRequest());

                verify(suggestionService, never()).listSuggestions(any(), any(), any());
            }

            @Test
            void getCaps_serviceError_returnsBadGateway() throws Exception {
                when(suggestionService.getCaps())
                        .thenThrow(new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Service down"));

                mockMvc.perform(post("/suggerimenti/config"))
                        .andExpect(status().isBadGateway());
            }

            @Test
            void requestSuggestion_verifiesAllResponseFields() throws Exception {
                SuggestionRequestDTO request = new SuggestionRequestDTO();
                request.setClassName("MyClass");
                request.setDifficulty("EASY");
                request.setRemainingSuggestions(2);

                SuggestionResponseDTO response = SuggestionResponseDTO.builder()
                        .suggestions(List.of("Suggerimento 1", "Suggerimento 2"))
                        .remainingSuggestions(3)
                        .suggestionsAvailable(3)
                        .suggestionsMax(10)
                        .totalAvailableSuggestions(10)
                        .noMoreSuggestions(false)
                        .tier("BASE")
                        .message(null)
                        .build();

                when(suggestionService.requestSuggestions(any(SuggestionRequestDTO.class)))
                        .thenReturn(response);

                mockMvc.perform(post("/suggerimenti/richiedi")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.suggestions", hasSize(2)))
                        .andExpect(jsonPath("$.remainingSuggestions", is(3)))
                        .andExpect(jsonPath("$.suggestionsAvailable", is(3)))
                        .andExpect(jsonPath("$.suggestionsMax", is(10)))
                        .andExpect(jsonPath("$.totalAvailableSuggestions", is(10)))
                        .andExpect(jsonPath("$.noMoreSuggestions", is(false)))
                        .andExpect(jsonPath("$.tier", is("BASE")));
            }

            @Test
            void requestAdvancedSuggestion_verifiesCreditsFields() throws Exception {
                AdvancedSuggestionRequestDTO request = new AdvancedSuggestionRequestDTO();
                request.setClassName("MyClass");
                request.setDifficulty("HARD");
                request.setRemainingSuggestions(0);
                request.setPlayerId(5L);
                request.setCost(3);

                SuggestionResponseDTO response = SuggestionResponseDTO.builder()
                        .suggestions(List.of("Suggerimento avanzato"))
                        .remainingSuggestions(0)
                        .suggestionsAvailable(0)
                        .suggestionsMax(1)
                        .totalAvailableSuggestions(1)
                        .noMoreSuggestions(true)
                        .tier("ADVANCED")
                        .creditsLeft(7)
                        .creditsSpent(3)
                        .suggestionCost(3)
                        .build();

                when(suggestionService.requestAdvancedSuggestions(any(AdvancedSuggestionRequestDTO.class)))
                        .thenReturn(response);

                mockMvc.perform(post("/suggerimenti/avanzati/richiedi")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.creditsLeft", is(7)))
                        .andExpect(jsonPath("$.creditsSpent", is(3)))
                        .andExpect(jsonPath("$.suggestionCost", is(3)))
                        .andExpect(jsonPath("$.tier", is("ADVANCED")))
                        .andExpect(jsonPath("$.noMoreSuggestions", is(true)));
            }

            @Test
            void getAvailability_verifiesDeliveredSuggestionsArray() throws Exception {
                SuggestionAvailabilityRequestDTO request = new SuggestionAvailabilityRequestDTO();
                request.setClassName("MyClass");
                request.setDifficulty("MEDIUM");
                request.setTier("BASE");
                request.setGameId(1L);

                SuggestionAvailabilityResponseDTO response = SuggestionAvailabilityResponseDTO.builder()
                        .availableSuggestions(3)
                        .suggestionsMax(5)
                        .totalAvailableSuggestions(5)
                        .deliveredSuggestions(List.of("Suggerimento 1", "Suggerimento 2"))
                        .build();

                when(suggestionService.getAvailability("MEDIUM", "MyClass", "BASE", 1L))
                        .thenReturn(response);

                mockMvc.perform(post("/suggerimenti/disponibilita")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.availableSuggestions", is(3)))
                        .andExpect(jsonPath("$.suggestionsMax", is(5)))
                        .andExpect(jsonPath("$.totalAvailableSuggestions", is(5)))
                        .andExpect(jsonPath("$.deliveredSuggestions", hasSize(2)))
                        .andExpect(jsonPath("$.deliveredSuggestions[0]", is("Suggerimento 1")))
                        .andExpect(jsonPath("$.deliveredSuggestions[1]", is("Suggerimento 2")));
            }

            @Test
            void createSuggestion_nullClassName_returnsBadRequest() throws Exception {
                SuggestionCreateRequestDTO request = new SuggestionCreateRequestDTO();
                request.setDifficulty("EASY");
                request.setText("Suggerimento");
                request.setTier("BASE");

                mockMvc.perform(post("/suggerimenti/admin/create")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest());

                verify(suggestionService, never()).createSuggestion(any(SuggestionCreateRequestDTO.class));
            }

            @Test
            void createSuggestion_nullText_returnsBadRequest() throws Exception {
                SuggestionCreateRequestDTO request = new SuggestionCreateRequestDTO();
                request.setClassName("MyClass");
                request.setDifficulty("EASY");
                request.setTier("BASE");

                mockMvc.perform(post("/suggerimenti/admin/create")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest());

                verify(suggestionService, never()).createSuggestion(any(SuggestionCreateRequestDTO.class));
            }

            @Test
            void deleteSuggestion_withZeroId_processesRequest() throws Exception {
                doNothing().when(suggestionService).deleteSuggestion(0L);

                mockMvc.perform(delete("/suggerimenti/admin/0"))
                        .andExpect(status().isNoContent());

                verify(suggestionService).deleteSuggestion(0L);
            }

            @Test
            void listSuggestions_withInvalidTier_propagatesBadRequest() throws Exception {
                when(suggestionService.listSuggestions(eq("MyClass"), eq("EASY"), eq("INVALID")))
                        .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tier invalido"));

                mockMvc.perform(get("/suggerimenti/admin/list")
                                .param("className", "MyClass")
                                .param("difficulty", "EASY")
                                .param("tier", "INVALID"))
                        .andExpect(status().isBadRequest());
            }

            @Test
            void requestSuggestion_verifiesServiceInvocationWithCorrectParameters() throws Exception {
                SuggestionRequestDTO request = new SuggestionRequestDTO();
                request.setClassName("TestClass");
                request.setDifficulty("MEDIUM");
                request.setRemainingSuggestions(5);
                request.setGameId(42L);

                SuggestionResponseDTO response = SuggestionResponseDTO.builder()
                        .suggestions(List.of("Test"))
                        .remainingSuggestions(4)
                        .suggestionsAvailable(4)
                        .suggestionsMax(5)
                        .totalAvailableSuggestions(5)
                        .noMoreSuggestions(false)
                        .tier("BASE")
                        .build();

                when(suggestionService.requestSuggestions(any(SuggestionRequestDTO.class)))
                        .thenReturn(response);

                mockMvc.perform(post("/suggerimenti/richiedi")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk());

                ArgumentCaptor<SuggestionRequestDTO> captor = ArgumentCaptor.forClass(SuggestionRequestDTO.class);
                verify(suggestionService, times(1)).requestSuggestions(captor.capture());
                
                SuggestionRequestDTO captured = captor.getValue();
                assertEquals("TestClass", captured.getClassName());
                assertEquals("MEDIUM", captured.getDifficulty());
                assertEquals(Integer.valueOf(5), captured.getRemainingSuggestions());
                assertEquals(Long.valueOf(42L), captured.getGameId());
            }

            @Test
            void getAvailability_verifiesServiceInvocationWithCorrectParameters() throws Exception {
                SuggestionAvailabilityRequestDTO request = new SuggestionAvailabilityRequestDTO();
                request.setClassName("VerifyClass");
                request.setDifficulty("HARD");
                request.setTier("ADVANCED");
                request.setGameId(99L);

                SuggestionAvailabilityResponseDTO response = SuggestionAvailabilityResponseDTO.builder()
                        .availableSuggestions(1)
                        .suggestionsMax(2)
                        .totalAvailableSuggestions(2)
                        .deliveredSuggestions(List.of())
                        .build();

                when(suggestionService.getAvailability("HARD", "VerifyClass", "ADVANCED", 99L))
                        .thenReturn(response);

                mockMvc.perform(post("/suggerimenti/disponibilita")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk());

                verify(suggestionService, times(1))
                        .getAvailability("HARD", "VerifyClass", "ADVANCED", 99L);
            }

            @Test
            void importSuggestions_withLargeList_processesSuccessfully() throws Exception {
                SuggestionImportRequestDTO request = new SuggestionImportRequestDTO();
                request.setClassName("LargeClass");
                
                List<SuggestionImportItemDTO> largeSuggestionsList = new java.util.ArrayList<>();
                for (int i = 1; i <= 50; i++) {
                    SuggestionImportItemDTO item = new SuggestionImportItemDTO();
                    item.setDifficulty("EASY");
                    item.setText("Suggerimento numero " + i);
                    item.setTier("BASE");
                    largeSuggestionsList.add(item);
                }
                request.setSuggestions(largeSuggestionsList);

                doNothing().when(suggestionService).replaceSuggestions(any(SuggestionImportRequestDTO.class));

                mockMvc.perform(post("/suggerimenti/import")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isNoContent());

                ArgumentCaptor<SuggestionImportRequestDTO> captor = 
                        ArgumentCaptor.forClass(SuggestionImportRequestDTO.class);
                verify(suggestionService, times(1)).replaceSuggestions(captor.capture());
                assertEquals(50, captor.getValue().getSuggestions().size());
            }

            @Test
            void deleteSuggestion_verifiesCorrectIdPassedToService() throws Exception {
                doNothing().when(suggestionService).deleteSuggestion(123L);

                mockMvc.perform(delete("/suggerimenti/admin/123"))
                        .andExpect(status().isNoContent());

                verify(suggestionService, times(1)).deleteSuggestion(123L);
                verify(suggestionService, never()).deleteSuggestion(eq(122L));
                verify(suggestionService, never()).deleteSuggestion(eq(124L));
            }

            @Test
            void listSuggestions_verifiesAllParametersPassedToService() throws Exception {
                List<SuggestionListItemDTO> response = List.of(
                        new SuggestionListItemDTO(1L, "Sugg1", "TargetClass", "MEDIUM", "ADVANCED", "it"),
                        new SuggestionListItemDTO(2L, "Sugg2", "TargetClass", "MEDIUM", "ADVANCED", "en")
                );

                when(suggestionService.listSuggestions(eq("TargetClass"), eq("MEDIUM"), eq("ADVANCED")))
                        .thenReturn(response);

                mockMvc.perform(get("/suggerimenti/admin/list")
                                .param("className", "TargetClass")
                                .param("difficulty", "MEDIUM")
                                .param("tier", "ADVANCED"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(2)))
                        .andExpect(jsonPath("$[0].className", is("TargetClass")))
                        .andExpect(jsonPath("$[0].difficulty", is("MEDIUM")))
                        .andExpect(jsonPath("$[0].tier", is("ADVANCED")))
                        .andExpect(jsonPath("$[0].language", is("it")))
                        .andExpect(jsonPath("$[1].language", is("en")));

                verify(suggestionService, times(1)).listSuggestions("TargetClass", "MEDIUM", "ADVANCED");
            }

            @Test
            void createSuggestion_success_returns201Created() throws Exception {
                SuggestionCreateRequestDTO request = new SuggestionCreateRequestDTO();
                request.setClassName("NewClass");
                request.setDifficulty("HARD");
                request.setText("Nuovo suggerimento");
                request.setTier("BASE");
                request.setLanguage("it");

                Suggestion created = Suggestion.builder()
                        .id(100L)
                        .className("NewClass")
                        .difficulty(SuggestionDifficulty.HARD)
                        .text("Nuovo suggerimento")
                        .tier(SuggestionTier.BASE)
                        .language("it")
                        .build();

                when(suggestionService.createSuggestion(any(SuggestionCreateRequestDTO.class)))
                        .thenReturn(created);

                mockMvc.perform(post("/suggerimenti/admin/create")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isCreated());

                verify(suggestionService, times(1)).createSuggestion(any(SuggestionCreateRequestDTO.class));
            }

            @Test
            void getCaps_verifiesCompleteMapReturned() throws Exception {
                when(suggestionService.getCaps()).thenReturn(Map.of(
                        "EASY", 10,
                        "MEDIUM", 5,
                        "HARD", 2
                ));

                mockMvc.perform(post("/suggerimenti/config"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.EASY", is(10)))
                        .andExpect(jsonPath("$.MEDIUM", is(5)))
                        .andExpect(jsonPath("$.HARD", is(2)))
                        .andExpect(jsonPath("$.length()", is(3)));

                verify(suggestionService, times(1)).getCaps();
            }
}
