package com.bspq26e8.backend.submission.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class SubmissionStatusConverter implements AttributeConverter<SubmissionStatus, String> {

    @Override
    public String convertToDatabaseColumn(SubmissionStatus attribute) {
        if (attribute == null) {
            return null;
        }

        return attribute.name().toLowerCase();
    }

    @Override
    public SubmissionStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }

        return SubmissionStatus.valueOf(dbData.toUpperCase());
    }
}
