package com.groom.manvsclass.model.dto.gamification;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HintCreditUpdateDTO {
    @Min(1)
    private int credits;
}
