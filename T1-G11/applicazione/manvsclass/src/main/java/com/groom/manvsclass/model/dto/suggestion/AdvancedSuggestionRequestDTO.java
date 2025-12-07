package com.groom.manvsclass.model.dto.suggestion;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * Richiesta di suggerimenti avanzati, acquistati tramite crediti.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AdvancedSuggestionRequestDTO extends SuggestionRequestDTO {

    @NotNull
    private Long playerId;

    /**
     * Costo richiesto dal client (facoltativo). Se nullo, verra usato il default lato server.
     */
    @Min(1)
    private Integer cost;
}
