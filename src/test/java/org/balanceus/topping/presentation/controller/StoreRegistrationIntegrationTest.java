package org.balanceus.topping.presentation.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.balanceus.topping.application.dto.StoreForm;
import org.balanceus.topping.application.service.StoreService;
import org.balanceus.topping.domain.model.Store;
import org.balanceus.topping.domain.model.StoreCategory;
import org.balanceus.topping.domain.model.User;
import org.balanceus.topping.domain.repository.StoreRepository;
import org.balanceus.topping.domain.repository.UserRepository;
import org.balanceus.topping.domain.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
@DisplayName("Store Registration Integration Tests")
class StoreRegistrationIntegrationTest {

    @Autowired
    private StoreService storeService;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private UserRepository userRepository;

    private User testBusinessOwner;

    @BeforeEach
    void setUp() {
        // Clean up any existing data
        storeRepository.deleteAll();
        userRepository.deleteAll();

        // Create a test business owner
        testBusinessOwner = new User();
        testBusinessOwner.setEmail("business@test.com");
        testBusinessOwner.setUsername("businessowner");
        testBusinessOwner.setPassword("password123");
        testBusinessOwner.setRole(Role.ROLE_BUSINESS_OWNER);
        testBusinessOwner.setTermsAgreement(true);
        testBusinessOwner = userRepository.save(testBusinessOwner);
    }

    @Test
    @DisplayName("Should successfully register a new store with all fields")
    void shouldRegisterStoreWithAllFields() {
        // Given
        StoreForm storeForm = new StoreForm();
        storeForm.setName("Test Café Integration");
        storeForm.setCategory("CAFE");
        storeForm.setAddress("서울시 강남구 테스트로 123");
        storeForm.setDescription("테스트를 위한 카페입니다. 맛있는 커피와 디저트를 제공합니다.");
        storeForm.setContactNumber("010-1234-5678");
        storeForm.setBusinessHours("평일 09:00-22:00, 주말 10:00-23:00");
        storeForm.setTags("카페, 디저트, 강남역, 데이트");
        storeForm.setSnsOrWebsiteLink("https://instagram.com/testcafe");
        storeForm.setIsCollaborationOpen(true);

        // When
        // Simulate the controller flow - convert to StoreRegistrationRequest and register
        var registrationRequest = convertToRegistrationRequest(storeForm);
        storeService.registerStore(registrationRequest, testBusinessOwner.getUuid());

        // Get the created store and update with additional fields
        Optional<Store> createdStoreOpt = storeService.getStoreByUser(testBusinessOwner.getUuid());
        assertTrue(createdStoreOpt.isPresent(), "Store should be created");

        Store createdStore = createdStoreOpt.get();
        updateStoreWithFormData(createdStore, storeForm);

        // Then
        Optional<Store> finalStoreOpt = storeService.getStoreById(createdStore.getUuid());
        assertTrue(finalStoreOpt.isPresent(), "Updated store should exist");

        Store finalStore = finalStoreOpt.get();
        
        // Verify basic fields
        assertEquals("Test Café Integration", finalStore.getName());
        assertEquals(StoreCategory.CAFE, finalStore.getCategory());
        assertEquals("서울시 강남구 테스트로 123", finalStore.getAddress());
        assertEquals("010-1234-5678", finalStore.getContactNumber());
        assertEquals("평일 09:00-22:00, 주말 10:00-23:00", finalStore.getBusinessHours());
        assertEquals("https://instagram.com/testcafe", finalStore.getSnsOrWebsiteLink());
        
        // Verify additional fields
        assertEquals("테스트를 위한 카페입니다. 맛있는 커피와 디저트를 제공합니다.", finalStore.getDescription());
        assertEquals(true, finalStore.getIsCollaborationOpen());
        
        // Verify tags
        List<String> tags = finalStore.getTags();
        assertNotNull(tags, "Tags should not be null");
        assertEquals(4, tags.size(), "Should have 4 tags");
        assertTrue(tags.contains("#카페"), "Should contain #카페 tag");
        assertTrue(tags.contains("#디저트"), "Should contain #디저트 tag");
        assertTrue(tags.contains("#강남역"), "Should contain #강남역 tag");
        assertTrue(tags.contains("#데이트"), "Should contain #데이트 tag");
        
        // Verify user relationship
        assertEquals(testBusinessOwner.getUuid(), finalStore.getUser().getUuid());
    }

    @Test
    @DisplayName("Should handle stores with minimal required fields")
    void shouldRegisterStoreWithMinimalFields() {
        // Given
        StoreForm storeForm = new StoreForm();
        storeForm.setName("Minimal Store");
        storeForm.setCategory("Restaurant");
        storeForm.setAddress("서울시 서초구 최소 주소");
        storeForm.setContactNumber("010-9876-5432");
        storeForm.setBusinessHours("매일 11:00-21:00");
        // Leave optional fields empty
        storeForm.setDescription("");
        storeForm.setTags("");
        storeForm.setSnsOrWebsiteLink("");
        storeForm.setIsCollaborationOpen(false);

        // When
        var registrationRequest = convertToRegistrationRequest(storeForm);
        storeService.registerStore(registrationRequest, testBusinessOwner.getUuid());

        Optional<Store> createdStoreOpt = storeService.getStoreByUser(testBusinessOwner.getUuid());
        assertTrue(createdStoreOpt.isPresent());

        Store createdStore = createdStoreOpt.get();
        updateStoreWithFormData(createdStore, storeForm);

        // Then
        Optional<Store> finalStoreOpt = storeService.getStoreById(createdStore.getUuid());
        assertTrue(finalStoreOpt.isPresent());

        Store finalStore = finalStoreOpt.get();
        assertEquals("Minimal Store", finalStore.getName());
        assertEquals(StoreCategory.RESTAURANT, finalStore.getCategory());
        assertEquals(false, finalStore.getIsCollaborationOpen());
        assertTrue(finalStore.getTags().isEmpty(), "Tags should be empty");
    }

    @Test
    @DisplayName("Should correctly parse and format tags")
    void shouldParseTagsCorrectly() {
        // Given - Test various tag formats
        StoreForm storeForm = createBasicStoreForm();
        storeForm.setTags("#카페, dessert, #맛집   , 강남역, #브런치 ");

        // When
        List<String> parsedTags = storeForm.getTagsList();

        // Then
        assertEquals(5, parsedTags.size());
        assertTrue(parsedTags.contains("#카페"));
        assertTrue(parsedTags.contains("#dessert"));
        assertTrue(parsedTags.contains("#맛집"));
        assertTrue(parsedTags.contains("#강남역"));
        assertTrue(parsedTags.contains("#브런치"));
    }

    @Test
    @DisplayName("Should handle tag conversion both ways")
    void shouldConvertTagsBothWays() {
        // Given
        List<String> originalTags = List.of("#카페", "#디저트", "#강남", "#맛집");
        StoreForm storeForm = new StoreForm();

        // When - Convert list to string
        storeForm.setTagsFromList(originalTags);
        String tagsString = storeForm.getTags();

        // Then - Check string format
        assertEquals("카페, 디저트, 강남, 맛집", tagsString);

        // When - Convert back to list
        List<String> convertedTags = storeForm.getTagsList();

        // Then - Should match original
        assertEquals(originalTags.size(), convertedTags.size());
        for (String tag : originalTags) {
            assertTrue(convertedTags.contains(tag));
        }
    }

    // Helper methods mimicking controller behavior
    private org.balanceus.topping.application.dto.StoreRegistrationRequest convertToRegistrationRequest(StoreForm storeForm) {
        var request = new org.balanceus.topping.application.dto.StoreRegistrationRequest();
        request.setName(storeForm.getName());
        request.setAddress(storeForm.getAddress());
        request.setContactNumber(storeForm.getContactNumber());
        request.setBusinessHours(storeForm.getBusinessHours());
        request.setCategory(storeForm.getCategory());
        request.setMainImageUrl(storeForm.getMainImageUrl());
        request.setSnsOrWebsiteLink(storeForm.getSnsOrWebsiteLink());
        return request;
    }

    private void updateStoreWithFormData(Store store, StoreForm storeForm) {
        if (storeForm.getDescription() != null && !storeForm.getDescription().trim().isEmpty()) {
            store.setDescription(storeForm.getDescription().trim());
        }
        
        if (storeForm.getIsCollaborationOpen() != null) {
            store.setIsCollaborationOpen(storeForm.getIsCollaborationOpen());
        }
        
        // Handle tags
        List<String> tagsList = storeForm.getTagsList();
        if (!tagsList.isEmpty()) {
            store.getTags().clear();
            tagsList.forEach(store::addTag);
        }
        
        // Save the updated store
        storeRepository.save(store);
    }

    private StoreForm createBasicStoreForm() {
        StoreForm form = new StoreForm();
        form.setName("Basic Test Store");
        form.setCategory("Cafe");
        form.setAddress("Test Address");
        form.setContactNumber("010-1234-5678");
        form.setBusinessHours("Test Hours");
        return form;
    }
}