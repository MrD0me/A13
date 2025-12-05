package com.example.db_setup.model.dto.gamification;

import lombok.Data;

import javax.validation.constraints.Min;

@Data
public class HintCreditUpdateDTO {
    @Min(1)
    private int credits;
}
