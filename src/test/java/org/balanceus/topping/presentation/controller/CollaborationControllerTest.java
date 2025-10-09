package org.balanceus.topping.presentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.UUID;

import org.balanceus.topping.application.service.CollaborationApplicationException;
import org.balanceus.topping.application.service.CollaborationApplicationService;
import org.balanceus.topping.presentation.dto.collaboration.CollaborationApplyViewModel;
import org.balanceus.topping.presentation.dto.collaboration.StoreOptionView;
import org.balanceus.topping.presentation.dto.collaboration.UserSummaryView;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CollaborationController.class)
class CollaborationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CollaborationApplicationService collaborationApplicationService;

    @Test
    @WithMockUser(username = "test@example.com")
    void applyForm_withAuthentication_returnsApplyPage() throws Exception {
        CollaborationApplyViewModel viewModel = CollaborationApplyViewModel.builder()
                .user(UserSummaryView.builder()
                        .uuid(UUID.randomUUID())
                        .username("testuser")
                        .email("test@example.com")
                        .role("ROLE_USER")
                        .build())
                .businessOwner(false)
                .storeDataJson("{}")
                .build();

        when(collaborationApplicationService.prepareApplyForm(any(), any(), eq("test@example.com")))
                .thenReturn(viewModel);

        mockMvc.perform(get("/collaborations/apply"))
                .andExpect(status().isOk())
                .andExpect(view().name("collaborations/apply"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attribute("isBusinessOwner", false));

        verify(collaborationApplicationService)
                .prepareApplyForm(any(), any(), eq("test@example.com"));
    }

    @Test
    void applyForm_withoutAuthentication_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/collaborations/apply"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=authentication_required"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void submitProposal_validData_redirectsToApplications() throws Exception {
        when(collaborationApplicationService.submitProposal(any(), eq("test@example.com")))
                .thenReturn(UUID.randomUUID());

        mockMvc.perform(post("/collaborations/apply")
                .param("collaborationTitle", "Test Collaboration")
                .param("description", "Test Description")
                .param("startDate", "2025-08-01")
                .param("endDate", "2025-08-15")
                .param("targetStoreId", UUID.randomUUID().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/mypage/applications?success=proposal_submitted"));

        verify(collaborationApplicationService)
                .submitProposal(any(), eq("test@example.com"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void submitProposal_missingTitle_redirectsWithError() throws Exception {
        doThrow(new CollaborationApplicationException(
                "title_required",
                "redirect:/collaborations/apply?error=title_required"))
                .when(collaborationApplicationService)
                .submitProposal(any(), eq("test@example.com"));

        mockMvc.perform(post("/collaborations/apply")
                .param("description", "Test Description")
                .param("startDate", "2025-08-01")
                .param("endDate", "2025-08-15")
                .param("targetStoreId", UUID.randomUUID().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/collaborations/apply?error=title_required"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void submitProposal_invalidDateRange_redirectsWithError() throws Exception {
        doThrow(new CollaborationApplicationException(
                "invalid_date_range",
                "redirect:/collaborations/apply?error=invalid_date_range"))
                .when(collaborationApplicationService)
                .submitProposal(any(), eq("test@example.com"));

        mockMvc.perform(post("/collaborations/apply")
                .param("collaborationTitle", "Test Collaboration")
                .param("description", "Test Description")
                .param("startDate", "2025-08-15")
                .param("endDate", "2025-08-01")
                .param("targetStoreId", UUID.randomUUID().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/collaborations/apply?error=invalid_date_range"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void submitProposal_storeNotFound_redirectsWithError() throws Exception {
        doThrow(new CollaborationApplicationException(
                "target_store_not_found",
                "redirect:/collaborations/apply?error=target_store_not_found"))
                .when(collaborationApplicationService)
                .submitProposal(any(), eq("test@example.com"));

        mockMvc.perform(post("/collaborations/apply")
                .param("collaborationTitle", "Test Collaboration")
                .param("description", "Test Description")
                .param("startDate", "2025-08-01")
                .param("endDate", "2025-08-15")
                .param("targetStoreId", UUID.randomUUID().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/collaborations/apply?error=target_store_not_found"));
    }

    @Test
    @WithMockUser(username = "owner@example.com", roles = "BUSINESS_OWNER")
    void applyForm_businessOwner_populatesOwnerData() throws Exception {
        CollaborationApplyViewModel viewModel = CollaborationApplyViewModel.builder()
                .user(UserSummaryView.builder()
                        .uuid(UUID.randomUUID())
                        .username("owner")
                        .email("owner@example.com")
                        .role("ROLE_BUSINESS_OWNER")
                        .build())
                .businessOwner(true)
                .userStore(StoreOptionView.builder()
                        .uuid(UUID.randomUUID())
                        .name("Owner Store")
                        .category("CAFE")
                        .build())
                .storeDataJson("{}")
                .build();

        when(collaborationApplicationService.prepareApplyForm(any(), any(), eq("owner@example.com")))
                .thenReturn(viewModel);

        mockMvc.perform(get("/collaborations/apply"))
                .andExpect(status().isOk())
                .andExpect(view().name("collaborations/apply"))
                .andExpect(model().attribute("isBusinessOwner", true))
                .andExpect(model().attributeExists("userStore"));
    }
}
