package com.tendering.dto.response.auth;

import com.tendering.dto.response.user.UserResponseDTO;
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