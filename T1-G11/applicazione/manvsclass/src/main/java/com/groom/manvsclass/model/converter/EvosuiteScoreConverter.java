package com.groom.manvsclass.model.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import testrobotchallenge.commons.models.score.Coverage;
import testrobotchallenge.commons.models.score.EvosuiteScore;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.StringJoiner;

@Converter
public class EvosuiteScoreConverter implements AttributeConverter<EvosuiteScore, String> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final int EXPECTED_SEGMENTS = 8;

    @Override
    public String convertToDatabaseColumn(EvosuiteScore attribute) {
        if (attribute == null) {
            return null;
        }
        // Compact string (covered|missed;...) to avoid exceeding VARCHAR limits on legacy schemas.
        return new StringJoiner(";")
                .add(coverageToString(attribute.getLineCoverage()))
                .add(coverageToString(attribute.getBranchCoverage()))
                .add(coverageToString(attribute.getExceptionCoverage()))
                .add(coverageToString(attribute.getWeakMutationCoverage()))
                .add(coverageToString(attribute.getOutputCoverage()))
                .add(coverageToString(attribute.getMethodCoverage()))
                .add(coverageToString(attribute.getMethodNoExceptionCoverage()))
                .add(coverageToString(attribute.getCBranchCoverage()))
                .toString();
    }

    private String coverageToString(Coverage coverage) {
        int covered = coverage != null ? coverage.getCovered() : 0;
        int missed = coverage != null ? coverage.getMissed() : 0;
        return covered + "|" + missed;
    }

    private EvosuiteScore fromJson(String dbData) {
        try {
            return OBJECT_MAPPER.readValue(dbData, EvosuiteScore.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to deserialize EvosuiteScore", e);
        }
    }

    private Coverage coverageFromString(String value) {
        String[] parts = value.split("\\|");
        if (parts.length != 2) {
            throw new IllegalStateException("Invalid coverage segment: " + value);
        }
        try {
            return new Coverage(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Unable to parse coverage numbers from: " + value, e);
        }
    }

    @Override
    public EvosuiteScore convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }

        // Backward compatibility: old records are stored as JSON.
        if (dbData.trim().startsWith("{")) {
            return fromJson(dbData);
        }

        String[] segments = dbData.split(";");
        if (segments.length != EXPECTED_SEGMENTS) {
            throw new IllegalStateException("Unexpected evosuite score format");
        }

        EvosuiteScore score = new EvosuiteScore();
        score.setLineCoverage(coverageFromString(segments[0]));
        score.setBranchCoverage(coverageFromString(segments[1]));
        score.setExceptionCoverage(coverageFromString(segments[2]));
        score.setWeakMutationCoverage(coverageFromString(segments[3]));
        score.setOutputCoverage(coverageFromString(segments[4]));
        score.setMethodCoverage(coverageFromString(segments[5]));
        score.setMethodNoExceptionCoverage(coverageFromString(segments[6]));
        score.setCBranchCoverage(coverageFromString(segments[7]));
        return score;
    }
}
