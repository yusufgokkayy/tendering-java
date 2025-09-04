package com.tendering.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class RegisterRequest {
    private String username;
    private String email;
    private String password;
    private String name;
    private String surname;
    private String gender;
    private LocalDate birthDate;
    private String phoneNumber;
}