package com.formaccess.dto;

import jakarta.validation.constraints.*;

public class ProfileForm {

    @NotBlank(message = "Name is required")
    @Size(max = 100)
    private String fullName;

    private Integer deptId;

    @Min(1) @Max(5)
    private Integer yearOfStudy;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public Integer getDeptId() { return deptId; }
    public void setDeptId(Integer deptId) { this.deptId = deptId; }
    public Integer getYearOfStudy() { return yearOfStudy; }
    public void setYearOfStudy(Integer yearOfStudy) { this.yearOfStudy = yearOfStudy; }
}
