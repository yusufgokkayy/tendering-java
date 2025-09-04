package com.tendering.service;

import com.tendering.config.FileUploadConfig;
import com.tendering.exceptionHandlers.FileStorageException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.UUID;

@Service
public class FileService {

    private final FileUploadConfig fileUploadConfig;
    private final Path fileStorageLocation;

    @Autowired
    public FileService(FileUploadConfig fileUploadConfig) {
        this.fileUploadConfig = fileUploadConfig;
        this.fileStorageLocation = Paths.get(fileUploadConfig.getDirectory())
                .toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Dosya dizini oluşturulamadı: " + ex.getMessage());
        }
    }

    // Mevcut metod - Long ID için
    public String storeProfilePhoto(MultipartFile file, Long userId) {
        validateFile(file);

        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String newFilename = "user_" + userId + "_" + UUID.randomUUID().toString() + "." + fileExtension;

        try {
            Path targetLocation = this.fileStorageLocation.resolve(newFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return newFilename;
        } catch (IOException ex) {
            throw new FileStorageException("Dosya kaydedilemedi: " + newFilename + ". Hata: " + ex.getMessage());
        }
    }

    // Yeni metod - String ID için (UUID string olarak)
    public String storeProfilePhoto(MultipartFile file, String userIdStr) {
        validateFile(file);

        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String newFilename = "user_" + userIdStr + "_" + UUID.randomUUID().toString() + "." + fileExtension;

        try {
            Path targetLocation = this.fileStorageLocation.resolve(newFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return newFilename;
        } catch (IOException ex) {
            throw new FileStorageException("Dosya kaydedilemedi: " + newFilename + ". Hata: " + ex.getMessage());
        }
    }

    // Yeni metod - UUID için (doğrudan UUID nesnesi)
    public String storeProfilePhoto(MultipartFile file, UUID userId) {
        return storeProfilePhoto(file, userId.toString());
    }

    public void deleteProfilePhoto(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return;
        }

        try {
            Path filePath = this.fileStorageLocation.resolve(filename).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new FileStorageException("Dosya silinemedi: " + filename + ". Hata: " + ex.getMessage());
        }
    }

    // API endpoint URL'i
    public String getProfilePhotoUrl(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return null;
        }
        return fileUploadConfig.getBaseUrl() + "/" + filename;
    }

    // Static serving URL'i
    public String getStaticProfilePhotoUrl(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return null;
        }
        return fileUploadConfig.getStaticUrl() + "/" + filename;
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new FileStorageException("Boş dosya yüklenemez");
        }

        if (file.getSize() > fileUploadConfig.getMaxSize()) {
            throw new FileStorageException("Dosya boyutu çok büyük. Maksimum: " +
                    (fileUploadConfig.getMaxSize() / 1024 / 1024) + "MB");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || filename.contains("..")) {
            throw new FileStorageException("Geçersiz dosya adı: " + filename);
        }

        String extension = getFileExtension(filename).toLowerCase();
        if (!Arrays.asList(fileUploadConfig.getAllowedExtensions()).contains(extension)) {
            throw new FileStorageException("Desteklenmeyen dosya formatı: " + extension +
                    ". İzin verilen formatlar: " + Arrays.toString(fileUploadConfig.getAllowedExtensions()));
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw new FileStorageException("Dosya uzantısı bulunamadı");
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}