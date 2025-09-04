package com.tendering.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);

    @PostConstruct
    public void initialize() {
        try {
            // "firebase-service-account.json" dosyasının resources klasöründe olduğundan emin olun.
            InputStream serviceAccount = new ClassPathResource("firebase-service-account.json").getInputStream();

            // DÜZELTME: Builder() yerine newBuilder() kullanılıyor
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            // Zaten başlatılmış bir uygulama yoksa yenisini başlat.
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                logger.info("Firebase application has been initialized successfully.");
            } else {
                logger.info("Firebase application is already initialized.");
            }
        } catch (IOException e) {
            logger.error("Failed to initialize Firebase application: {}", e.getMessage());
            // Geliştirme ortamında Firebase olmadan da çalışması için exception fırlatmıyoruz
            logger.warn("Application will continue without Firebase integration.");
        }
    }
}