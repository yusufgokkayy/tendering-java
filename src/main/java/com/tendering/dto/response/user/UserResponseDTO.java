package com.tendering.dto.response.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {
    private String id; // public_id
    private String phoneNumber;
    private String email;
    private String name;
    private String surname;
    private LocalDate birthDate;
    private String gender;
    private String role;
    private String profilePhotoUrl;
    private Boolean isActive;
    private Boolean phoneNumberVerified;
    private Boolean emailVerified;
}