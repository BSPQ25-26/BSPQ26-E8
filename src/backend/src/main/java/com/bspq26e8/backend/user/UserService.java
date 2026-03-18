package com.bspq26e8.backend.user;

import org.springframework.stereotype.Service;

@Service
public class UserService {

    public boolean isValidUsername(String username) {
        
        if (username == null)
            return false;

        String trimmed = username.trim();

        if (trimmed.length() < 3 || trimmed.length() > 30)
            return false;

        return trimmed.matches("^[a-zA-Z0-9_]+$");
    }
}
