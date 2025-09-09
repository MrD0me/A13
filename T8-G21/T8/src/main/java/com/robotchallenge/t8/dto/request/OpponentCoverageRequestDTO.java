package com.robotchallenge.t8.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OpponentCoverageRequestDTO {
    @JsonProperty("classUTName")
    private String classUTName;

    @JsonProperty("classUTPackage")
    private String classUTPackage;

    @Override
    public String toString() {
        return "OpponentCoverageRequestDTO{" +
                "classUTName='" + classUTName + '\'' +
                ", classUTPackage='" + classUTPackage + '\'' +
                '}';
    }
}
