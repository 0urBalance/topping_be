package org.balanceus.topping.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.path}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Static resource mapping for root-level files (ads.txt, robots.txt, sitemap.xml, etc.)
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(3600);
        
        // Static resource mapping for images - both classpath and external uploads
        registry.addResourceHandler("/image/**")
                .addResourceLocations("classpath:/static/image/", "file:" + uploadPath + "/image/")
                .setCachePeriod(3600);
        
        // Legacy mappings for backward compatibility
        registry.addResourceHandler("/store-images/**")
                .addResourceLocations("file:" + uploadPath + "/stores/", "classpath:/static/store-images/")
                .setCachePeriod(3600);
        
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath + "/uploads/")
                .setCachePeriod(3600);
    }
}