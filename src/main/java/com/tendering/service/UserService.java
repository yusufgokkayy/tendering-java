package com.tendering.service;

import com.tendering.dto.response.user.UserResponseDTO;
import com.tendering.exceptionHandlers.ResourceNotFoundException;
import com.tendering.model.User;
import com.tendering.repository.UserRepository;
import com.tendering.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    // İzin verilen dosya formatları
    private static final Set<String> ALLOWED_IMAGE_TYPES = new HashSet<>(
            Arrays.asList("image/jpeg", "image/png", "image/gif", "image/webp")
    );

    private final UserRepository userRepository;
    private final FileService fileService;
    private final UserMapper userMapper;

    @Value("${app.profile-photo.max-size:5242880}") // Varsayılan 5MB
    private long maxProfilePhotoSize;

    @Autowired
    public UserService(UserRepository userRepository, FileService fileService, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.fileService = fileService;
        this.userMapper = userMapper;
    }

    /**
     * ID ile kullanıcıyı bulur
     */
    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + userId));
    }

    /**
     * Public ID (UUID) ile kullanıcıyı bulur
     */
    private User getUserByPublicId(UUID userId) {
        return userRepository.findByPublicId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + userId));
    }

    /**
     * UUID ile profil fotoğrafı yükleme - Ana metod
     */
    @Transactional
    public UserResponseDTO uploadProfilePhoto(UUID userId, MultipartFile file) {
        logger.info("Profil fotoğrafı yükleme isteği: userId={}, fileName={}, size={}",
                userId, file.getOriginalFilename(), file.getSize());

        // Kullanıcıyı bul
        User user = getUserByPublicId(userId);

        try {
            // Dosya kontrolü
            validateProfilePhoto(file);

            // Eski fotoğrafı sil
            removeExistingProfilePhoto(user);

            // Yeni fotoğrafı kaydet
            String filename = fileService.storeProfilePhoto(file, userId.toString());
            String photoUrl = fileService.getProfilePhotoUrl(filename);

            // Kullanıcıyı güncelle
            updateUserProfilePhoto(user, filename, photoUrl);

            logger.info("Profil fotoğrafı başarıyla güncellendi: userId={}, photoUrl={}", userId, photoUrl);
            return userMapper.convertToDTO(user);

        } catch (IllegalArgumentException e) {
            logger.warn("Geçersiz dosya: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Beklenmeyen hata: {}", e.getMessage(), e);
            throw new RuntimeException("Profil fotoğrafı yüklenemedi: " + e.getMessage(), e);
        }
    }

    /**
     * Geriye dönük uyumluluk için Long ID metodu
     */
    @Transactional
    public UserResponseDTO uploadProfilePhoto(Long userId, MultipartFile file) {
        logger.debug("Long ID ile profil fotoğrafı yükleniyor, UUID'ye dönüştürülecek");

        // Long ID ile kullanıcıyı bul ve UUID'sini kullan
        User user = getUserById(userId);
        return uploadProfilePhoto(user.getPublicId(), file);
    }

    /**
     * Profil fotoğrafının geçerli olup olmadığını kontrol eder
     */
    private void validateProfilePhoto(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Dosya boş");
        }

        // Boyut kontrolü
        if (file.getSize() > maxProfilePhotoSize) {
            throw new IllegalArgumentException(String.format(
                    "Dosya boyutu çok büyük. Maksimum izin verilen boyut: %d MB",
                    maxProfilePhotoSize / (1024 * 1024)
            ));
        }

        // Format kontrolü
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw new IllegalArgumentException(
                    "Desteklenmeyen dosya formatı. İzin verilen formatlar: JPEG, PNG, GIF, WEBP"
            );
        }
    }

    /**
     * Mevcut profil fotoğrafını siler
     */
    private void removeExistingProfilePhoto(User user) {
        if (user.getProfilePhotoFilename() != null && !user.getProfilePhotoFilename().isEmpty()) {
            try {
                logger.debug("Eski profil fotoğrafı siliniyor: {}", user.getProfilePhotoFilename());
                fileService.deleteProfilePhoto(user.getProfilePhotoFilename());
            } catch (Exception e) {
                // Silme hatası yeni fotoğraf yüklemeyi engellemesin
                logger.warn("Eski profil fotoğrafı silinirken hata: {}", e.getMessage());
            }
        }
    }

    /**
     * Kullanıcının profil fotoğrafı bilgilerini günceller
     */
    private void updateUserProfilePhoto(User user, String filename, String photoUrl) {
        user.setProfilePhotoFilename(filename);
        user.setProfilePhotoUrl(photoUrl);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }
}