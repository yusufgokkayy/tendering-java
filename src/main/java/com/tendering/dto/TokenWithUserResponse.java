package com.tendering.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TokenWithUserResponse {
    private String accessToken;
    private String refreshToken;
    private UserResponseDTO user;
}