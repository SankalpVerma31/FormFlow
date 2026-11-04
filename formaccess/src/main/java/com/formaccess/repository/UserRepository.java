package com.formaccess.repository;

import com.formaccess.entity.User;
import com.formaccess.entity.enums.AccountStatus;
import com.formaccess.entity.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    /** SELECT * FROM users WHERE LOWER(email) = LOWER(?) */
    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    List<User> findByAccountStatus(AccountStatus status);

    List<User> findAllByOrderByIdAsc();

    long countByRole(Role role);
}
