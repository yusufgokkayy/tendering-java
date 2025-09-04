package com.tendering.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ValidateTokenRequest {

    @NotBlank(message = "Token boş olamaz")
    private String token;
}