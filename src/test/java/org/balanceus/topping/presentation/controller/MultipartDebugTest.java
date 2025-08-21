package org.balanceus.topping.presentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;
import java.util.UUID;

import org.balanceus.topping.application.service.ImageUploadService;
import org.balanceus.topping.application.service.ProductService;
import org.balanceus.topping.application.service.StoreService;
import org.balanceus.topping.domain.model.User;
import org.balanceus.topping.domain.repository.CollaborationRepository;
import org.balanceus.topping.domain.repository.ProductRepository;
import org.balanceus.topping.domain.repository.ReviewRepository;
import org.balanceus.topping.domain.repository.StoreLikeRepository;
import org.balanceus.topping.domain.repository.WishlistRepository;
import org.balanceus.topping.infrastructure.security.Role;
import org.balanceus.topping.infrastructure.security.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@WebMvcTest(controllers = StoreController.class)
@ActiveProfiles("test")
@DisplayName("Multipart Form Debug Tests")
class MultipartDebugTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

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
        testUser = new User();
        testUser.setUuid(UUID.randomUUID());
        testUser.setEmail("test@business.com");
        testUser.setUsername("testowner");
        testUser.setRole(Role.ROLE_BUSINESS_OWNER);
        
        userDetails = new UserDetailsImpl(testUser);
    }

    @Test
    @DisplayName("Test 1: Simple multipart endpoint without form binding")
    void testSimpleMultipartEndpoint() throws Exception {
        MockMultipartFile testFile = new MockMultipartFile(
                "image", 
                "test.jpg", 
                "image/jpeg", 
                "test image content".getBytes()
        );

        mockMvc.perform(multipart("/stores/test-multipart")
                .file(testFile)
                .param("name", "Test Store")
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("Multipart parsing successful"));
    }

    @Test
    @DisplayName("Test 2: Form-based multipart without file")
    void testFormMultipartWithoutFile() throws Exception {
        when(storeService.getStoreByUser(any(UUID.class))).thenReturn(Optional.empty());

        mockMvc.perform(post("/stores/register")
                .with(user(userDetails))
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("name", "Test Café")
                .param("category", "CAFE")
                .param("address", "서울시 강남구 테스트로 123")
                .param("contactNumber", "010-1234-5678")
                .param("businessHours", "평일 09:00-22:00")
                .param("description", "테스트 카페")
                .param("tags", "카페, 디저트")
                .param("snsOrWebsiteLink", "https://instagram.com/test")
                .param("isCollaborationOpen", "true"))
                .andDo(print())
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("Test 3: Multipart form with file upload")
    void testMultipartFormWithFile() throws Exception {
        when(storeService.getStoreByUser(any(UUID.class))).thenReturn(Optional.empty());

        MockMultipartFile testFile = new MockMultipartFile(
                "image", 
                "store-image.jpg", 
                "image/jpeg", 
                "store image content".getBytes()
        );

        mockMvc.perform(multipart("/stores/register")
                .file(testFile)
                .param("name", "Test Café with Image")
                .param("category", "CAFE")
                .param("address", "서울시 강남구 테스트로 123")
                .param("contactNumber", "010-1234-5678")
                .param("businessHours", "평일 09:00-22:00")
                .param("description", "이미지가 있는 테스트 카페")
                .param("tags", "카페, 디저트, 이미지")
                .param("snsOrWebsiteLink", "https://instagram.com/testimage")
                .param("isCollaborationOpen", "true")
                .with(user(userDetails))
                .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("Test 4: Multipart without CSRF token - Should fail gracefully")
    void testMultipartWithoutCSRF() throws Exception {
        MockMultipartFile testFile = new MockMultipartFile(
                "image", 
                "test.jpg", 
                "image/jpeg", 
                "test content".getBytes()
        );

        mockMvc.perform(multipart("/stores/register")
                .file(testFile)
                .param("name", "Test Store")
                .with(user(userDetails)))
                .andDo(print())
                .andExpect(status().isForbidden()); // Should fail due to CSRF
    }

    @Test
    @DisplayName("Test 5: Test empty multipart file")
    void testEmptyMultipartFile() throws Exception {
        when(storeService.getStoreByUser(any(UUID.class))).thenReturn(Optional.empty());

        MockMultipartFile emptyFile = new MockMultipartFile(
                "image", 
                "", 
                "image/jpeg", 
                new byte[0]
        );

        mockMvc.perform(multipart("/stores/register")
                .file(emptyFile)
                .param("name", "Store with Empty File")
                .param("category", "CAFE")
                .param("address", "Test Address")
                .param("contactNumber", "010-1234-5678")
                .param("businessHours", "Test Hours")
                .with(user(userDetails))
                .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("Test 6: Large file upload - Should respect size limits")
    void testLargeFileUpload() throws Exception {
        // Create a file larger than configured limit (10MB)
        byte[] largeContent = new byte[11 * 1024 * 1024]; // 11MB
        
        MockMultipartFile largeFile = new MockMultipartFile(
                "image", 
                "large-image.jpg", 
                "image/jpeg", 
                largeContent
        );

        mockMvc.perform(multipart("/stores/register")
                .file(largeFile)
                .param("name", "Store with Large File")
                .param("category", "CAFE")
                .param("address", "Test Address")
                .param("contactNumber", "010-1234-5678")
                .param("businessHours", "Test Hours")
                .with(user(userDetails))
                .with(csrf()))
                .andDo(print())
                .andExpect(status().is4xxClientError()); // Should fail due to size limit
    }
}