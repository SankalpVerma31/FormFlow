package com.formaccess.service;

import com.formaccess.dto.AdminUserForm;
import com.formaccess.dto.PasswordForm;
import com.formaccess.dto.ProfileForm;
import com.formaccess.dto.RegisterForm;
import com.formaccess.entity.Department;
import com.formaccess.entity.User;
import com.formaccess.entity.enums.AccountStatus;
import com.formaccess.entity.enums.Role;
import com.formaccess.exception.BusinessException;
import com.formaccess.exception.NotFoundException;
import com.formaccess.repository.DepartmentRepository;
import com.formaccess.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Registration, profile updates and admin user management. */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, DepartmentRepository departmentRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Department> allDepartments() {
        return departmentRepository.findAllByOrderByCodeAsc();
    }

    public List<User> allUsers() {
        return userRepository.findAllByOrderByIdAsc();
    }

    public User getById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
    }

    public boolean emailTaken(String email) {
        return email != null && userRepository.existsByEmailIgnoreCase(email.trim());
    }

    /** Self-registration always creates a PARTICIPANT. Only an admin can promote. */
    @Transactional
    public User register(RegisterForm form) {
        if (emailTaken(form.getEmail())) throw new BusinessException("That email is already registered.");
        User user = new User();
        user.setFullName(form.getFullName().trim());
        user.setEmail(form.getEmail().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        user.setDepartment(findDepartment(form.getDeptId()));
        user.setYearOfStudy(form.getYearOfStudy());
        user.setRole(Role.PARTICIPANT);
        user.setAccountStatus(AccountStatus.ACTIVE);
        return userRepository.save(user);
    }

    @Transactional
    public void updateProfile(User user, ProfileForm form) {
        user.setFullName(form.getFullName().trim());
        user.setDepartment(findDepartment(form.getDeptId()));
        user.setYearOfStudy(form.getYearOfStudy());
        userRepository.save(user);
    }

    @Transactional
    public void changePassword(User user, PasswordForm form) {
        if (!passwordEncoder.matches(form.getCurrentPassword(), user.getPasswordHash())) {
            throw new BusinessException("Current password is incorrect.");
        }
        if (!form.getNewPassword().equals(form.getConfirmPassword())) {
            throw new BusinessException("New passwords do not match.");
        }
        user.setPasswordHash(passwordEncoder.encode(form.getNewPassword()));
        userRepository.save(user);
    }

    /** Admin: create (id == null) or update a user. */
    @Transactional
    public User adminSave(AdminUserForm form, User admin) {
        User user;
        String email = form.getEmail().trim().toLowerCase();
        if (form.getId() == null) {
            if (emailTaken(email)) throw new BusinessException("That email is already registered.");
            if (form.getPassword() == null || form.getPassword().length() < 8) {
                throw new BusinessException("A new user needs a password of at least 8 characters.");
            }
            user = new User();
        } else {
            user = getById(form.getId());
            userRepository.findByEmailIgnoreCase(email).ifPresent(other -> {
                if (!other.getId().equals(form.getId())) throw new BusinessException("That email belongs to another user.");
            });
            if (user.getId().equals(admin.getId())
                    && (form.getRole() != Role.ADMIN || form.getAccountStatus() != AccountStatus.ACTIVE)) {
                throw new BusinessException("You cannot remove your own admin access or deactivate yourself.");
            }
        }
        user.setFullName(form.getFullName().trim());
        user.setEmail(email);
        user.setDepartment(findDepartment(form.getDeptId()));
        user.setYearOfStudy(form.getYearOfStudy());
        user.setRole(form.getRole());
        user.setAccountStatus(form.getAccountStatus());
        if (form.getPassword() != null && !form.getPassword().isBlank()) {
            if (form.getPassword().length() < 8) throw new BusinessException("Password must be at least 8 characters.");
            user.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        }
        return userRepository.save(user);
    }

    @Transactional
    public void toggleStatus(Long userId, User admin) {
        User user = getById(userId);
        if (user.getId().equals(admin.getId())) throw new BusinessException("You cannot deactivate yourself.");
        user.setAccountStatus(user.isActive() ? AccountStatus.INACTIVE : AccountStatus.ACTIVE);
        userRepository.save(user);
    }

    public AdminUserForm toAdminForm(User user) {
        AdminUserForm form = new AdminUserForm();
        form.setId(user.getId());
        form.setFullName(user.getFullName());
        form.setEmail(user.getEmail());
        form.setDeptId(user.getDepartment() == null ? null : user.getDepartment().getId());
        form.setYearOfStudy(user.getYearOfStudy());
        form.setRole(user.getRole());
        form.setAccountStatus(user.getAccountStatus());
        return form;
    }

    public ProfileForm toProfileForm(User user) {
        ProfileForm form = new ProfileForm();
        form.setFullName(user.getFullName());
        form.setDeptId(user.getDepartment() == null ? null : user.getDepartment().getId());
        form.setYearOfStudy(user.getYearOfStudy());
        return form;
    }

    private Department findDepartment(Integer id) {
        if (id == null) return null;
        return departmentRepository.findById(id).orElseThrow(() -> new BusinessException("Unknown department."));
    }
}
