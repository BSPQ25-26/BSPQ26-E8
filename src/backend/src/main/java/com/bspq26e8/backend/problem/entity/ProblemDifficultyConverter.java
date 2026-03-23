package com.bspq26e8.backend.problem.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class ProblemDifficultyConverter implements AttributeConverter<ProblemDifficulty, String> {

    @Override
    public String convertToDatabaseColumn(ProblemDifficulty attribute) {
        if (attribute == null) {
            return null;
        }

        return attribute.name().toLowerCase();
    }

    @Override
    public ProblemDifficulty convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }

        return ProblemDifficulty.valueOf(dbData.toUpperCase());
    }
}
