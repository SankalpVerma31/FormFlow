package com.formaccess.security;

import com.formaccess.entity.User;
import com.formaccess.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/** Returns the logged-in user as a fresh database object (so status/role are up to date). */
@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User get() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof AppUserPrincipal principal)) {
            throw new AccessDeniedException("Please log in.");
        }
        return userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new AccessDeniedException("Account no longer exists."));
    }
}
