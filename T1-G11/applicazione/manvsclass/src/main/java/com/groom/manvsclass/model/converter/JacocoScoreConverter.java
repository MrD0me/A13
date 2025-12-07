package com.groom.manvsclass.model.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import testrobotchallenge.commons.models.score.JacocoScore;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class JacocoScoreConverter implements AttributeConverter<JacocoScore, String> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(JacocoScore attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialize JacocoScore", e);
        }
    }

    @Override
    public JacocoScore convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(dbData, JacocoScore.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to deserialize JacocoScore", e);
        }
    }
}
