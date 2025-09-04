/*package com.tendering.service;

import com.tendering.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordMigrationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public void migrateAllPasswords() {
        List<User> users = userRepository.findAll();

        for (User user : users) {
            // Sadece hash'lenmemiş parolaları işle
            if (!user.getPassword().startsWith("$2a$")) {
                String hashedPassword = passwordEncoder.encode(user.getPassword());
                user.setPassword(hashedPassword);
                userRepository.save(user);
            }
        }
    }
}*/
