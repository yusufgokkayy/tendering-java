package com.tendering.mapper;

import com.tendering.dto.UserResponseDTO;
import com.tendering.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponseDTO convertToDTO(User user) {
        if (user == null) return null;

        return UserResponseDTO.builder()
                .id(user.getPublicId().toString())
                .phoneNumber(user.getPhoneNumber())
                .email(user.getEmail())
                .name(user.getName())
                .surname(user.getSurname())
                .birthDate(user.getBirthDate())
                .gender(user.getGender())
                .role(user.getRole())
                .profilePhotoUrl(user.getProfilePhotoUrl())
                .isActive(user.getIsActive())
                .phoneNumberVerified(user.isPhoneNumberVerified())
                .emailVerified(user.getEmailVerified())
                .build();
    }
}