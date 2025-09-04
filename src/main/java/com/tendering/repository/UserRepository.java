package com.tendering.repository;

import com.tendering.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Telefon numarası ile arama (primary identifier)
    Optional<User> findByPhoneNumber(String phoneNumber);
    boolean existsByPhoneNumber(String phoneNumber);

    // Email ile arama
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    // Public ID ile arama (API için)
    Optional<User> findByPublicId(UUID publicId);

    // Firebase UID ile arama
    Optional<User> findByFirebaseUid(String firebaseUid);
}