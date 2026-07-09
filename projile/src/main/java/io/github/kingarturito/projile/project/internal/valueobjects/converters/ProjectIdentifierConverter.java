package io.github.kingarturito.projile.project.internal.valueobjects.converters;

import io.github.kingarturito.projile.project.internal.valueobjects.ProjectIdentifier;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ProjectIdentifierConverter implements AttributeConverter<ProjectIdentifier, String> {

    @Override
    public String convertToDatabaseColumn(ProjectIdentifier attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public ProjectIdentifier convertToEntityAttribute(String dbData) {
        return dbData == null ? null : new ProjectIdentifier(dbData);
    }
}
