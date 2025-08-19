package org.balanceus.topping.domain.model;

/**
 * Store category enumeration with Korean display names
 */
public enum StoreCategory {
    RESTAURANT("음식점"),
    CAFE("카페"),
    BOOKSTORE("책방"),
    GYM("헬스장"),
    OTHER("기타");

    private final String displayName;

    StoreCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Convert string value to StoreCategory enum for backward compatibility
     * @param value String value to convert
     * @return StoreCategory enum or OTHER if not found
     */
    public static StoreCategory fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return OTHER;
        }
        
        String upperValue = value.trim().toUpperCase();
        
        // Handle legacy string values from database
        switch (upperValue) {
            case "FOOD":
                return RESTAURANT;
            case "CAFE":
                return CAFE;
            case "BOOK":
                return BOOKSTORE;
            case "GYM":
                return GYM;
            case "OTHER":
                return OTHER;
            // Handle enum names directly
            case "RESTAURANT":
                return RESTAURANT;
            case "BOOKSTORE":
                return BOOKSTORE;
            default:
                // Try to find by enum name
                try {
                    return StoreCategory.valueOf(upperValue);
                } catch (IllegalArgumentException e) {
                    return OTHER;
                }
        }
    }

    /**
     * Convert string value to StoreCategory enum with strict validation
     * @param value String value to convert
     * @return StoreCategory enum
     * @throws IllegalArgumentException if value is not valid
     */
    public static StoreCategory fromStringStrict(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Category value cannot be null or empty");
        }
        
        StoreCategory result = fromString(value);
        if (result == OTHER && !value.trim().equalsIgnoreCase("OTHER")) {
            throw new IllegalArgumentException("Invalid category value: " + value);
        }
        
        return result;
    }
}