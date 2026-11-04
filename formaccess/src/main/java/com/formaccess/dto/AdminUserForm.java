package com.formaccess.dto;

import com.formaccess.entity.enums.AccountStatus;
import com.formaccess.entity.enums.Role;
import jakarta.validation.constraints.*;

/** Used by the admin to add or edit a user. Password may be blank when editing. */
public class AdminUserForm {

    private Long id;

    @NotBlank(message = "Name is required")
    @Size(max = 100)
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email")
    private String email;

    @Size(max = 72)
    private String password;

    private Integer deptId;

    @Min(1) @Max(5)
    private Integer yearOfStudy;

    @NotNull
    private Role role = Role.PARTICIPANT;

    @NotNull
    private AccountStatus accountStatus = AccountStatus.ACTIVE;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Integer getDeptId() { return deptId; }
    public void setDeptId(Integer deptId) { this.deptId = deptId; }
    public Integer getYearOfStudy() { return yearOfStudy; }
    public void setYearOfStudy(Integer yearOfStudy) { this.yearOfStudy = yearOfStudy; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public AccountStatus getAccountStatus() { return accountStatus; }
    public void setAccountStatus(AccountStatus accountStatus) { this.accountStatus = accountStatus; }
}
