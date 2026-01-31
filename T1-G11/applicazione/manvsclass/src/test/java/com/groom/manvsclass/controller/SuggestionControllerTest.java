package com.groom.manvsclass.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.groom.manvsclass.api.ApiGatewayClient;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
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
}
