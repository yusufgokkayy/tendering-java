package com.tendering.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "file.upload.profile-photos")
public class FileUploadConfig {

    private String directory;
    private long maxSize;
    private String[] allowedExtensions;
    private String baseUrl;
    private String staticUrl; // ✅ Bu field var mı?

    // Getters and Setters
    public String getDirectory() { return directory; }
    public void setDirectory(String directory) { this.directory = directory; }

    public long getMaxSize() { return maxSize; }
    public void setMaxSize(long maxSize) { this.maxSize = maxSize; }

    public String[] getAllowedExtensions() { return allowedExtensions; }
    public void setAllowedExtensions(String[] allowedExtensions) { this.allowedExtensions = allowedExtensions; }

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    // ✅ Bu getter/setter'ları ekleyin:
    public String getStaticUrl() {
        return staticUrl;
    }

    public void setStaticUrl(String staticUrl) {
        this.staticUrl = staticUrl;
    }
}