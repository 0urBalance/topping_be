package org.balanceus.topping.presentation.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.balanceus.topping.domain.model.CollaborationProposal;
import org.balanceus.topping.domain.model.Store;
import org.balanceus.topping.domain.model.User;
import org.balanceus.topping.infrastructure.security.Role;
import org.balanceus.topping.domain.repository.CollaborationProposalRepository;
import org.balanceus.topping.domain.repository.CollaborationRepository;
import org.balanceus.topping.domain.repository.ProductRepository;
import org.balanceus.topping.domain.repository.StoreRepository;
import org.balanceus.topping.domain.repository.UserRepository;
import org.balanceus.topping.application.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(CollaborationController.class)
class CollaborationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CollaborationProposalRepository collaborationProposalRepository;
    
    @MockBean
    private CollaborationRepository collaborationRepository;
    
    @MockBean
    private ProductRepository productRepository;
    
    @MockBean
    private StoreRepository storeRepository;
    
    @MockBean
    private UserRepository userRepository;
    
    @MockBean
    private ProductService productService;
    
    @MockBean
    private ObjectMapper objectMapper;

    private User testUser;
    private User targetBusinessOwner;
    private Store targetStore;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User();
        testUser.setUuid(UUID.randomUUID());
        testUser.setEmail("test@example.com");
        testUser.setUsername("testuser");
        testUser.setRole(Role.ROLE_USER);

        // Setup target business owner
        targetBusinessOwner = new User();
        targetBusinessOwner.setUuid(UUID.randomUUID());
        targetBusinessOwner.setEmail("business@example.com");
        targetBusinessOwner.setUsername("businessowner");
        targetBusinessOwner.setRole(Role.ROLE_BUSINESS_OWNER);

        // Setup target store
        targetStore = new Store();
        targetStore.setUuid(UUID.randomUUID());
        targetStore.setName("Test Store");
        targetStore.setCategory("카페");
        targetStore.setUser(targetBusinessOwner);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void testCollaborationApplyForm_WithAuthentication_ShouldReturnApplyPage() throws Exception {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(storeRepository.findAll(any())).thenReturn(Collections.emptyList());
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When & Then
        mockMvc.perform(get("/collaborations/apply"))
                .andExpect(status().isOk())
                .andExpect(view().name("collaborations/apply"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attribute("isBusinessOwner", false));
    }

    @Test
    void testCollaborationApplyForm_WithoutAuthentication_ShouldRedirectToLogin() throws Exception {
        // When & Then
        mockMvc.perform(get("/collaborations/apply"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=authentication_required"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void testCollaborationSubmission_ValidData_ShouldCreateProposal() throws Exception {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(storeRepository.findById(targetStore.getUuid())).thenReturn(Optional.of(targetStore));
        when(collaborationProposalRepository.save(any(CollaborationProposal.class)))
                .thenReturn(new CollaborationProposal());

        // When & Then
        mockMvc.perform(post("/collaborations/apply")
                .param("collaborationTitle", "Test Collaboration")
                .param("description", "Test Description")
                .param("startDate", "2025-08-01")
                .param("endDate", "2025-08-15")
                .param("category", "카페")
                .param("targetStoreId", targetStore.getUuid().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/mypage/applications?success=proposal_submitted"));

        // Verify that save was called
        verify(collaborationProposalRepository, times(1)).save(any(CollaborationProposal.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void testCollaborationSubmission_MissingTitle_ShouldRedirectWithError() throws Exception {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // When & Then
        mockMvc.perform(post("/collaborations/apply")
                .param("description", "Test Description")
                .param("startDate", "2025-08-01")
                .param("endDate", "2025-08-15")
                .param("targetStoreId", targetStore.getUuid().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/collaborations/apply?error=title_required"));

        // Verify that save was NOT called
        verify(collaborationProposalRepository, never()).save(any(CollaborationProposal.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void testCollaborationSubmission_InvalidDateRange_ShouldRedirectWithError() throws Exception {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // When & Then
        mockMvc.perform(post("/collaborations/apply")
                .param("collaborationTitle", "Test Collaboration")
                .param("description", "Test Description")
                .param("startDate", "2025-08-15")  // End date is before start date
                .param("endDate", "2025-08-01")
                .param("targetStoreId", targetStore.getUuid().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/collaborations/apply?error=invalid_date_range"));

        // Verify that save was NOT called
        verify(collaborationProposalRepository, never()).save(any(CollaborationProposal.class));
    }

    @Test
    @WithMockUser(username = "test@example.com") 
    void testCollaborationSubmission_StoreNotFound_ShouldRedirectWithError() throws Exception {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(storeRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(post("/collaborations/apply")
                .param("collaborationTitle", "Test Collaboration")
                .param("description", "Test Description")
                .param("startDate", "2025-08-01")
                .param("endDate", "2025-08-15")
                .param("targetStoreId", UUID.randomUUID().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/collaborations/apply?error=target_store_not_found"));

        // Verify that save was NOT called
        verify(collaborationProposalRepository, never()).save(any(CollaborationProposal.class));
    }
}