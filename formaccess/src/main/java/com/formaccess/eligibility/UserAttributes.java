package com.formaccess.eligibility;

/**
 * The user facts a rule can look at, copied out of the User entity.
 * Keeping this a plain record means the rule engine has no database code
 * and can be unit-tested on its own.
 */
public record UserAttributes(String departmentCode,
                             String departmentName,
                             Integer year,
                             String role,
                             String accountStatus) {
}
