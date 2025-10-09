package org.balanceus.topping.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("StoreCategoryConverter Tests")
class StoreCategoryConverterTest {

    private StoreCategoryConverter converter;

    @BeforeEach
    void setUp() {
        converter = new StoreCategoryConverter();
    }

    @Test
    @DisplayName("Should convert enum to database string")
    void shouldConvertEnumToDatabaseString() {
        // Test all enum values
        assertEquals("CAFE", converter.convertToDatabaseColumn(StoreCategory.CAFE));
        assertEquals("RESTAURANT", converter.convertToDatabaseColumn(StoreCategory.RESTAURANT));
        assertEquals("BOOKSTORE", converter.convertToDatabaseColumn(StoreCategory.BOOKSTORE));
        assertEquals("GYM", converter.convertToDatabaseColumn(StoreCategory.GYM));
        assertEquals("OTHER", converter.convertToDatabaseColumn(StoreCategory.OTHER));

        // Test null
        assertNull(converter.convertToDatabaseColumn(null));
    }

    @Test
    @DisplayName("Should convert database string to enum - uppercase")
    void shouldConvertDatabaseStringToEnumUppercase() {
        assertEquals(StoreCategory.CAFE, converter.convertToEntityAttribute("CAFE"));
        assertEquals(StoreCategory.RESTAURANT, converter.convertToEntityAttribute("RESTAURANT"));
        assertEquals(StoreCategory.BOOKSTORE, converter.convertToEntityAttribute("BOOKSTORE"));
        assertEquals(StoreCategory.GYM, converter.convertToEntityAttribute("GYM"));
        assertEquals(StoreCategory.OTHER, converter.convertToEntityAttribute("OTHER"));
    }

    @Test
    @DisplayName("Should handle mixed case database values")
    void shouldHandleMixedCaseDatabaseValues() {
        // This is the key test - handling "Cafe" from database
        assertEquals(StoreCategory.CAFE, converter.convertToEntityAttribute("Cafe"));
        assertEquals(StoreCategory.CAFE, converter.convertToEntityAttribute("cafe"));
        assertEquals(StoreCategory.RESTAURANT, converter.convertToEntityAttribute("Restaurant"));
        assertEquals(StoreCategory.RESTAURANT, converter.convertToEntityAttribute("restaurant"));
    }

    @Test
    @DisplayName("Should handle legacy database values")
    void shouldHandleLegacyDatabaseValues() {
        // Test legacy string mappings from fromString method
        assertEquals(StoreCategory.RESTAURANT, converter.convertToEntityAttribute("FOOD"));
        assertEquals(StoreCategory.BOOKSTORE, converter.convertToEntityAttribute("BOOK"));
    }

    @Test
    @DisplayName("Should handle null and empty values")
    void shouldHandleNullAndEmptyValues() {
        assertEquals(StoreCategory.OTHER, converter.convertToEntityAttribute(null));
        assertEquals(StoreCategory.OTHER, converter.convertToEntityAttribute(""));
        assertEquals(StoreCategory.OTHER, converter.convertToEntityAttribute("   "));
    }

    @Test
    @DisplayName("Should handle invalid values")
    void shouldHandleInvalidValues() {
        // Invalid values should default to OTHER
        assertEquals(StoreCategory.OTHER, converter.convertToEntityAttribute("INVALID"));
        assertEquals(StoreCategory.OTHER, converter.convertToEntityAttribute("XYZ"));
    }
}
