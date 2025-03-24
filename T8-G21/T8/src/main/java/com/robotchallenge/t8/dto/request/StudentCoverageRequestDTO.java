package com.robotchallenge.t8.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class StudentCoverageRequestDTO {
    @JsonProperty("testClassName")
    String testClassName;

    @JsonProperty("testClassCode")
    String testClassCode;

    @JsonProperty("classUTName")
    private String classUTName;

    @JsonProperty("classUTCode")
    private String classUTCode;

    @JsonProperty("classUTPackage")
    private String classUTPackage;

    public StudentCoverageRequestDTO() {}
}
