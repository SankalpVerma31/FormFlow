package com.formaccess.security;

import com.formaccess.entity.User;
import com.formaccess.entity.enums.AccountStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/** What Spring Security keeps in the session for a logged-in user. */
public class AppUserPrincipal implements UserDetails {

    private final Long userId;
    private final String email;
    private final String passwordHash;
    private final String fullName;
    private final String role;
    private final boolean active;

    public AppUserPrincipal(User user) {
        this.userId = user.getId();
        this.email = user.getEmail();
        this.passwordHash = user.getPasswordHash();
        this.fullName = user.getFullName();
        this.role = user.getRole().name();
        this.active = user.getAccountStatus() == AccountStatus.ACTIVE;
    }

    public Long getUserId() { return userId; }
    public String getFullName() { return fullName; }
    public String getRole() { return role; }

    /** Spring expects roles as "ROLE_ADMIN", "ROLE_CREATOR" ... */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override public String getPassword() { return passwordHash; }
    @Override public String getUsername() { return email; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    /** Deactivated users cannot log in. */
    @Override public boolean isEnabled() { return active; }
}
