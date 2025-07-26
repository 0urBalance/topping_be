package org.balanceus.topping.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Static resource mapping for images
        registry.addResourceHandler("/image/**")
                .addResourceLocations("classpath:/static/image/");
        
        // Legacy mappings for backward compatibility
        registry.addResourceHandler("/store-images/**")
                .addResourceLocations("file:./store-images/", "classpath:/static/store-images/");
        
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:./uploads/", "classpath:/static/uploads/");
    }
}