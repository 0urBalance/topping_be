package org.balanceus.topping.presentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Optional;
import java.util.UUID;

import org.balanceus.topping.application.service.ImageUploadService;
import org.balanceus.topping.application.service.ProductService;
import org.balanceus.topping.application.service.StoreService;
import org.balanceus.topping.domain.model.Store;
import org.balanceus.topping.domain.model.User;
import org.balanceus.topping.infrastructure.security.Role;
import org.balanceus.topping.domain.repository.CollaborationRepository;
import org.balanceus.topping.domain.repository.ProductRepository;
import org.balanceus.topping.domain.repository.ReviewRepository;
import org.balanceus.topping.domain.repository.StoreLikeRepository;
import org.balanceus.topping.domain.repository.WishlistRepository;
import org.balanceus.topping.infrastructure.security.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = StoreController.class)
@ActiveProfiles("test")
@DisplayName("Store Registration Controller Tests")
class StoreRegistrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StoreService storeService;

    @MockBean
    private ProductService productService;

    @MockBean
    private ImageUploadService imageUploadService;

    @MockBean
    private ProductRepository productRepository;

    @MockBean
    private CollaborationRepository collaborationRepository;

    @MockBean
    private ReviewRepository reviewRepository;

    @MockBean
    private StoreLikeRepository storeLikeRepository;

    @MockBean
    private WishlistRepository wishlistRepository;

    private User testUser;
    private UserDetailsImpl userDetails;

    @BeforeEach
    void setUp() {
        // Create test user with BUSINESS_OWNER role
        testUser = new User();
        testUser.setUuid(UUID.randomUUID());
        testUser.setEmail("test@business.com");
        testUser.setUsername("testowner");
        testUser.setRole(Role.ROLE_BUSINESS_OWNER);
        
        userDetails = new UserDetailsImpl(testUser);
    }

    @Test
    @DisplayName("Store registration form should be accessible for business owners")
    @WithMockUser(roles = "BUSINESS_OWNER")
    void shouldShowRegistrationFormForBusinessOwner() throws Exception {
        // Given
        when(storeService.getStoreByUser(any(UUID.class))).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/stores/register")
                .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(view().name("store/register"))
                .andExpect(model().attributeExists("storeForm"));
    }

    @Test
    @DisplayName("Store registration form should redirect regular users")
    @WithMockUser(roles = "USER")
    void shouldRedirectRegularUsersFromRegistrationForm() throws Exception {
        // Given
        User regularUser = new User();
        regularUser.setUuid(UUID.randomUUID());
        regularUser.setEmail("regular@user.com");
        regularUser.setRole(Role.ROLE_USER);
        UserDetailsImpl regularUserDetails = new UserDetailsImpl(regularUser);

        // When & Then
        mockMvc.perform(get("/stores/register")
                .with(user(regularUserDetails)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/mypage?error=access_denied"));
    }

    @Test
    @DisplayName("Store registration should work with valid data")
    @WithMockUser(roles = "BUSINESS_OWNER")
    void shouldRegisterStoreWithValidData() throws Exception {
        // Given
        when(storeService.getStoreByUser(any(UUID.class))).thenReturn(Optional.empty());
        
        Store newStore = new Store();
        newStore.setUuid(UUID.randomUUID());
        newStore.setName("Test Café");
        newStore.setUser(testUser);
        
        when(storeService.getStoreByUser(eq(testUser.getUuid()))).thenReturn(Optional.of(newStore));

        // When & Then
        mockMvc.perform(post("/stores/register")
                .with(user(userDetails))
                .with(csrf())
                .param("name", "Test Café")
                .param("category", "CAFE")
                .param("address", "서울시 강남구 테스트로 123")
                .param("contactNumber", "010-1234-5678")
                .param("businessHours", "평일 09:00-22:00")
                .param("description", "테스트 카페입니다")
                .param("tags", "카페, 디저트, 강남")
                .param("snsOrWebsiteLink", "https://instagram.com/testcafe")
                .param("isCollaborationOpen", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/stores/my-store"));
    }

    @Test
    @DisplayName("Store registration should fail with invalid data")
    @WithMockUser(roles = "BUSINESS_OWNER")
    void shouldFailRegistrationWithInvalidData() throws Exception {
        // Given
        when(storeService.getStoreByUser(any(UUID.class))).thenReturn(Optional.empty());

        // When & Then - Missing required fields
        mockMvc.perform(post("/stores/register")
                .with(user(userDetails))
                .with(csrf())
                .param("name", "") // Empty name
                .param("category", "")
                .param("address", "")
                .param("contactNumber", "invalid-phone")
                .param("businessHours", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("store/register"))
                .andExpect(model().attributeHasErrors("storeForm"));
    }

    @Test
    @DisplayName("Store registration should validate phone number format")
    @WithMockUser(roles = "BUSINESS_OWNER")
    void shouldValidatePhoneNumberFormat() throws Exception {
        // Given
        when(storeService.getStoreByUser(any(UUID.class))).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(post("/stores/register")
                .with(user(userDetails))
                .with(csrf())
                .param("name", "Valid Store Name")
                .param("category", "CAFE")
                .param("address", "Valid Address")
                .param("contactNumber", "123-456-7890") // Invalid format
                .param("businessHours", "Valid Hours"))
                .andExpect(status().isOk())
                .andExpect(view().name("store/register"))
                .andExpect(model().attributeHasFieldErrors("storeForm", "contactNumber"));
    }

    @Test
    @DisplayName("Store registration should handle long descriptions")
    @WithMockUser(roles = "BUSINESS_OWNER")
    void shouldHandleLongDescriptions() throws Exception {
        // Given
        when(storeService.getStoreByUser(any(UUID.class))).thenReturn(Optional.empty());
        
        // Create a description over 1000 characters
        String longDescription = "A".repeat(1001);

        // When & Then
        mockMvc.perform(post("/stores/register")
                .with(user(userDetails))
                .with(csrf())
                .param("name", "Valid Store")
                .param("category", "CAFE")
                .param("address", "Valid Address")
                .param("contactNumber", "010-1234-5678")
                .param("businessHours", "Valid Hours")
                .param("description", longDescription))
                .andExpect(status().isOk())
                .andExpect(view().name("store/register"))
                .andExpect(model().attributeHasFieldErrors("storeForm", "description"));
    }

    @Test
    @DisplayName("Should redirect existing store owners to my-store page")
    @WithMockUser(roles = "BUSINESS_OWNER")
    void shouldRedirectExistingStoreOwners() throws Exception {
        // Given
        Store existingStore = new Store();
        existingStore.setUuid(UUID.randomUUID());
        existingStore.setName("Existing Store");
        existingStore.setUser(testUser);
        
        when(storeService.getStoreByUser(eq(testUser.getUuid()))).thenReturn(Optional.of(existingStore));

        // When & Then
        mockMvc.perform(get("/stores/register")
                .with(user(userDetails)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/stores/my-store"));
    }

    @Test  
    @DisplayName("Should handle tag parsing correctly")
    @WithMockUser(roles = "BUSINESS_OWNER")
    void shouldParseTagsCorrectly() throws Exception {
        // Given
        when(storeService.getStoreByUser(any(UUID.class))).thenReturn(Optional.empty());
        
        Store newStore = new Store();
        newStore.setUuid(UUID.randomUUID());
        newStore.setName("Tag Test Store");
        newStore.setUser(testUser);
        
        when(storeService.getStoreByUser(eq(testUser.getUuid()))).thenReturn(Optional.of(newStore));

        // When & Then - Test various tag formats
        mockMvc.perform(post("/stores/register")
                .with(user(userDetails))
                .with(csrf())
                .param("name", "Tag Test Store")
                .param("category", "CAFE")
                .param("address", "Test Address")
                .param("contactNumber", "010-1234-5678")
                .param("businessHours", "Test Hours")
                .param("tags", "#café, dessert, #맛집, 강남역 ")) // Mixed format
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/stores/my-store"));
    }
}