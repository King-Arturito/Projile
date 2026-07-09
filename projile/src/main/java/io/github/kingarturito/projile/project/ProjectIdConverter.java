package io.github.kingarturito.projile.project;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.UUID;

@Converter(autoApply = true)
public class ProjectIdConverter implements AttributeConverter<ProjectId, UUID> {

    @Override
    public UUID convertToDatabaseColumn(ProjectId attribute) {
        return attribute == null ? null : attribute.value();
    }
    @Override
    public ProjectId convertToEntityAttribute(UUID dbData) {
        return dbData == null ? null : new ProjectId(dbData);
    }

}
