package org.balanceus.topping.presentation.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.balanceus.topping.application.service.KakaoService;
import org.balanceus.topping.application.service.KakaoService.KakaoLoginResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 카카오 로그인 컨트롤러 테스트
 * 컨트롤러 레벨에서 카카오 로그인 플로우를 테스트
 */
@WebMvcTest(UserController.class)
@ActiveProfiles("test")
class KakaoLoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private KakaoService kakaoService;

    @Test
    void testSuccessfulKakaoCallback() throws Exception {
        // Given
        String authCode = "test-auth-code";
        KakaoLoginResult successResult = KakaoLoginResult.success(false, false, "카카오 로그인에 성공했습니다.");
        
        when(kakaoService.processKakaoLogin(eq(authCode), any())).thenReturn(successResult);

        // When & Then
        mockMvc.perform(get("/api/user/kakao/callback")
                .param("code", authCode))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(kakaoService).processKakaoLogin(eq(authCode), any());
    }

    @Test
    void testKakaoCallbackWithPlaceholderEmail() throws Exception {
        // Given
        String authCode = "test-auth-code";
        KakaoLoginResult successResult = KakaoLoginResult.success(
            true, false, "카카오 로그인은 완료되었지만 이메일 제공 권한이 없어 임시 이메일이 생성되었습니다.");
        
        when(kakaoService.processKakaoLogin(eq(authCode), any())).thenReturn(successResult);

        // When & Then
        mockMvc.perform(get("/api/user/kakao/callback")
                .param("code", authCode))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute("kakaoLoginMessage", successResult.getMessage()))
                .andExpect(flash().attribute("kakaoEmailNotice", true));

        verify(kakaoService).processKakaoLogin(eq(authCode), any());
    }

    @Test
    void testKakaoCallbackWithLinkedAccount() throws Exception {
        // Given
        String authCode = "test-auth-code";
        KakaoLoginResult successResult = KakaoLoginResult.success(
            false, true, "기존 계정과 카카오 계정이 성공적으로 연결되었습니다.");
        
        when(kakaoService.processKakaoLogin(eq(authCode), any())).thenReturn(successResult);

        // When & Then
        mockMvc.perform(get("/api/user/kakao/callback")
                .param("code", authCode))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute("kakaoLoginMessage", successResult.getMessage()));

        verify(kakaoService).processKakaoLogin(eq(authCode), any());
    }

    @Test
    void testFailedKakaoCallback() throws Exception {
        // Given
        String authCode = "invalid-auth-code";
        KakaoLoginResult failureResult = KakaoLoginResult.failure("카카오 로그인 처리 중 오류가 발생했습니다.");
        
        when(kakaoService.processKakaoLogin(eq(authCode), any())).thenReturn(failureResult);

        // When & Then
        mockMvc.perform(get("/api/user/kakao/callback")
                .param("code", authCode))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login?error=kakao_login_failed"))
                .andExpect(flash().attribute("kakaoLoginError", failureResult.getMessage()));

        verify(kakaoService).processKakaoLogin(eq(authCode), any());
    }

    @Test
    void testKakaoCallbackWithException() throws Exception {
        // Given
        String authCode = "test-auth-code";
        when(kakaoService.processKakaoLogin(eq(authCode), any()))
            .thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        mockMvc.perform(get("/api/user/kakao/callback")
                .param("code", authCode))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login?error=kakao_login_error"));

        verify(kakaoService).processKakaoLogin(eq(authCode), any());
    }

    @Test
    void testKakaoLoginStatusSuccess() throws Exception {
        // Given
        String authCode = "test-auth-code";
        KakaoLoginResult successResult = KakaoLoginResult.success(false, false, "카카오 로그인에 성공했습니다.");
        
        when(kakaoService.processKakaoLogin(eq(authCode), any())).thenReturn(successResult);

        // When & Then
        mockMvc.perform(get("/api/user/kakao/login-status")
                .param("code", authCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(successResult.getMessage()));

        verify(kakaoService).processKakaoLogin(eq(authCode), any());
    }

    @Test
    void testKakaoLoginStatusFailure() throws Exception {
        // Given
        String authCode = "invalid-auth-code";
        KakaoLoginResult failureResult = KakaoLoginResult.failure("카카오 로그인 처리 중 오류가 발생했습니다.");
        
        when(kakaoService.processKakaoLogin(eq(authCode), any())).thenReturn(failureResult);

        // When & Then
        mockMvc.perform(get("/api/user/kakao/login-status")
                .param("code", authCode))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value(failureResult.getMessage()));

        verify(kakaoService).processKakaoLogin(eq(authCode), any());
    }

    @Test
    @WithMockUser
    void testGetUserInfoWhenAuthenticated() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/user/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.role").value("ROLE_USER"));
    }

    @Test
    void testGetUserInfoWhenNotAuthenticated() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/user/info"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(401));
    }
}