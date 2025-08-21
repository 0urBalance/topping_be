package org.balanceus.topping.infrastructure.converter;

import org.balanceus.topping.domain.model.StoreCategory;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA converter for StoreCategory enum to handle legacy database values
 * and case-insensitive conversion.
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
        
        // Use the robust fromString method that handles case conversion
        // and legacy values
        return StoreCategory.fromString(dbData);
    }
}