package org.balanceus.topping.presentation.controller;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PhoneValidationTest {

    private boolean isValidPhone(String phoneNumber) {
        // Korean phone number format: 010-1234-5678 (11 digits) or 010-123-4567 (10 digits)
        String phoneRegex = "^01[0-9]-\\d{3,4}-\\d{4}$";
        return phoneNumber.matches(phoneRegex);
    }
    
    private String formatPhoneNumber(String phoneNumber) {
        if (phoneNumber.length() >= 10 && phoneNumber.length() <= 11) {
            if (phoneNumber.length() == 10) {
                return phoneNumber.replaceFirst("(\\d{3})(\\d{3})(\\d{4})", "$1-$2-$3");
            } else {
                return phoneNumber.replaceFirst("(\\d{3})(\\d{4})(\\d{4})", "$1-$2-$3");
            }
        }
        return phoneNumber;
    }

    @Test
    void testPhoneValidation_11Digits_ShouldPass() {
        String phoneNumber = "01012345678";
        String formatted = formatPhoneNumber(phoneNumber);
        assertEquals("010-1234-5678", formatted);
        assertTrue(isValidPhone(formatted));
    }

    @Test
    void testPhoneValidation_10Digits_ShouldPass() {
        String phoneNumber = "0101234567";
        String formatted = formatPhoneNumber(phoneNumber);
        assertEquals("010-123-4567", formatted);
        assertTrue(isValidPhone(formatted));
    }

    @Test
    void testPhoneValidation_InvalidFormat_ShouldFail() {
        assertFalse(isValidPhone("010-12-34567"));
        assertFalse(isValidPhone("010-12345-678"));
        assertFalse(isValidPhone("02-1234-5678"));
        assertFalse(isValidPhone("010-abcd-efgh"));
    }

    @Test
    void testPhoneValidation_DifferentPrefixes_ShouldPass() {
        assertTrue(isValidPhone("010-1234-5678"));
        assertTrue(isValidPhone("011-123-4567"));
        assertTrue(isValidPhone("016-1234-5678"));
        assertTrue(isValidPhone("017-123-4567"));
        assertTrue(isValidPhone("018-1234-5678"));
        assertTrue(isValidPhone("019-123-4567"));
    }
}