package com.tendering.validator;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PasswordValidator {
    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 100;

    public boolean isValid(String password) {
        return password != null &&
                getValidationErrors(password).isEmpty();
    }

    public List<String> getValidationErrors(String password) {
        List<String> errors = new ArrayList<>();

        if (password == null || password.isEmpty()) {
            errors.add("Password cannot be empty.");
            return errors;
        }

        if (password.length() < MIN_LENGTH) {
            errors.add("Password must be at least 8 characters long.");
        }

        if (password.length() > MAX_LENGTH) {
            errors.add("Password cannot be more than 100 characters long.");
        }

        if (!password.matches(".*[a-z].*")) {
            errors.add("Password must contain at least one lowercase letter.");
        }

        if (!password.matches(".*[A-Z].*")) {
            errors.add("Password must contain at least one uppercase letter.");
        }

        if (!password.matches(".*\\d.*")) {
            errors.add("Password must contain at least one digit.");
        }

        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
            errors.add("Password must contain at least one special character.");
        }

        return errors;
    }
}
