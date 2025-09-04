package com.tendering.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.time.Duration;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload.profile-photos.directory}")
    private String uploadDirectory;

    @PostConstruct
    public void init() {
        System.out.println("🔧 WebConfig uploadDirectory: " + uploadDirectory);
        File dir = new File(uploadDirectory);
        System.out.println("🔧 Directory exists: " + dir.exists());
        System.out.println("🔧 Directory readable: " + dir.canRead());
        System.out.println("🔧 Full path: " + dir.getAbsolutePath());
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        System.out.println("🔧 WebConfig: Adding resource handler for: " + uploadDirectory);

        // Profile photos için resource handler
        registry.addResourceHandler("/uploads/profile-photos/**")
                .addResourceLocations("file:" + uploadDirectory + "/")
                .setCacheControl(CacheControl.maxAge(Duration.ofDays(365))
                        .cachePublic()
                        .mustRevalidate())
                .resourceChain(true);

        // Static files için default handler'ı kullan - özel handler kaldırıldı
        System.out.println("✅ WebConfig: Resource handlers added successfully");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/uploads/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "HEAD")
                .allowedHeaders("*")
                .maxAge(3600);
    }

    // addViewControllers metodunu tamamen kaldır
}