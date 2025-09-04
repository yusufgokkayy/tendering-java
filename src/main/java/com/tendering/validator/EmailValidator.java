package com.tendering.validator;

import com.tendering.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class EmailValidator {
    private static final String EMAIL_PATTERN = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    private static final Pattern pattern = Pattern.compile(EMAIL_PATTERN);

    @Autowired
    private UserRepository userRepository;

    public boolean isValid(String email) {
        return email != null &&
                pattern.matcher(email).matches() &&
                !isEmailExists(email);
    }

    public boolean isEmailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public List<String> getValidationErrors(String email) {
        List<String> errors = new ArrayList<>();

        if (email == null || email.isEmpty()) {
            errors.add("Email cannot be empty.");
            return errors;
        }

        if (!pattern.matcher(email).matches()) {
            errors.add("Email format is not valid.");
        }

        if (isEmailExists(email)) {
            errors.add("This email is already registered.");
        }

        return errors;
    }
}