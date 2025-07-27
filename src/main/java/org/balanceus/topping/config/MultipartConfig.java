package org.balanceus.topping.config;

import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import jakarta.servlet.MultipartConfigElement;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class MultipartConfig {

    @Bean
    public MultipartResolver multipartResolver() {
        log.info("Configuring custom StandardServletMultipartResolver");
        StandardServletMultipartResolver multipartResolver = new StandardServletMultipartResolver();
        multipartResolver.setResolveLazily(true);
        return multipartResolver;
    }

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        log.info("Configuring MultipartConfigElement with custom settings");
        
        MultipartConfigFactory factory = new MultipartConfigFactory();
        
        // Set file size limits
        factory.setMaxFileSize(DataSize.ofMegabytes(10));
        factory.setMaxRequestSize(DataSize.ofMegabytes(50));
        factory.setFileSizeThreshold(DataSize.ofMegabytes(1));
        
        // Set temporary location for file uploads
        String tempDir = System.getProperty("java.io.tmpdir");
        log.info("Using temp directory for multipart uploads: {}", tempDir);
        factory.setLocation(tempDir);
        
        MultipartConfigElement config = factory.createMultipartConfig();
        log.info("MultipartConfig created - MaxFileSize: {}, MaxRequestSize: {}, Location: {}", 
                config.getMaxFileSize(), config.getMaxRequestSize(), config.getLocation());
        
        return config;
    }
}