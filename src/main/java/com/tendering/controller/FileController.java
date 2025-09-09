package com.tendering.controller;

import com.tendering.config.FileUploadConfig;
import com.tendering.dto.common.ApiResponse;
import com.tendering.dto.response.user.UserResponseDTO;
import com.tendering.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "*")
public class FileController {
    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    private final FileUploadConfig fileUploadConfig;
    private final Path fileStorageLocation;
    private final UserService userService;

    @Autowired
    public FileController(FileUploadConfig fileUploadConfig, UserService userService) {
        this.fileUploadConfig = fileUploadConfig;
        this.userService = userService;
        this.fileStorageLocation = Paths.get(fileUploadConfig.getDirectory())
                .toAbsolutePath().normalize();
    }

    /**
     * Profil fotoğrafı yükleme endpointi
     * UUID formatında kullanıcı ID'si alır
     */
    @PostMapping("/profile-photos/upload/{userId}")
    public ResponseEntity<ApiResponse<UserResponseDTO>> uploadProfilePhoto(
            @PathVariable UUID userId,  // Long yerine UUID kullan
            @RequestParam("file") MultipartFile file) {

        logger.debug("Profil fotoğrafı yükleme isteği: userId={}, fileName={}, size={}",
                userId, file.getOriginalFilename(), file.getSize());

        try {
            // Dosya kontrolü
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Lütfen bir dosya seçin"));
            }

            // Dosya tipi kontrolü
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Sadece resim dosyaları yüklenebilir"));
            }

            // UserService üzerinden profil fotoğrafı yükleme
            UserResponseDTO updatedUser = userService.uploadProfilePhoto(userId, file);
            return ResponseEntity.ok(ApiResponse.success("Profil fotoğrafı başarıyla yüklendi", updatedUser));

        } catch (IllegalArgumentException e) {
            logger.warn("Geçersiz kullanıcı ID: {}", userId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Geçersiz kullanıcı ID: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Profil fotoğrafı yüklenirken hata: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Profil fotoğrafı yüklenemedi: " + e.getMessage()));
        }
    }

    /**
     * Profil fotoğrafı görüntüleme endpointi
     */
    @GetMapping("/profile-photos/{filename:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) {
        try {
            Path filePath = this.fileStorageLocation.resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                // Content-type belirleme
                String contentType = getContentType(filename);

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                logger.warn("Dosya bulunamadı: {}", filename);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception ex) {
            logger.error("Dosya indirme hatası: {}", ex.getMessage(), ex);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Dosya uzantısına göre MIME tipini belirler
     */
    private String getContentType(String filename) {
        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        switch (extension) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "webp":
                return "image/webp";
            default:
                return "application/octet-stream";
        }
    }
}