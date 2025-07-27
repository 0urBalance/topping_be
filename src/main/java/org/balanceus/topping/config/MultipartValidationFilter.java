package org.balanceus.topping.config;

import java.io.IOException;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Component
@Order(1)
@Slf4j
public class MultipartValidationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String contentType = request.getContentType();
        String requestUri = request.getRequestURI();
        
        // Log multipart request details for debugging
        if (contentType != null && contentType.toLowerCase().startsWith("multipart/")) {
            log.info("Processing multipart request - URI: {}, Content-Type: {}, Content-Length: {}", 
                    requestUri, contentType, request.getContentLength());
            
            // Validate content length
            long contentLength = request.getContentLength();
            if (contentLength > 50 * 1024 * 1024) { // 50MB limit
                log.warn("Request exceeds maximum size limit: {} bytes", contentLength);
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("파일 크기가 너무 큽니다. 최대 50MB까지 업로드 가능합니다.");
                return;
            }
            
            // Validate content type format
            if (!contentType.contains("boundary=")) {
                log.warn("Multipart request missing boundary parameter: {}", contentType);
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("잘못된 multipart 요청 형식입니다.");
                return;
            }
            
            log.debug("Multipart validation passed for request: {}", requestUri);
        }
        
        try {
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error("Error processing request in multipart validation filter", e);
            throw e;
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String contentType = request.getContentType();
        // Only filter multipart requests
        return contentType == null || !contentType.toLowerCase().startsWith("multipart/");
    }
}