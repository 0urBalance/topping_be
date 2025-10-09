package org.balanceus.topping.domain.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA converter for StoreCategory enum to handle legacy database values
 * and case-insensitive conversion while keeping the domain independent of infrastructure packages.
 */
@Converter(autoApply = true)
public class StoreCategoryConverter implements AttributeConverter<StoreCategory, String> {

    @Override
    public String convertToDatabaseColumn(StoreCategory attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name();
    }

    @Override
    public StoreCategory convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return StoreCategory.OTHER;
        }

        return StoreCategory.fromString(dbData);
    }
}
