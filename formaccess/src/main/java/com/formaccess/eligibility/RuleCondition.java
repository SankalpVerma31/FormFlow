package com.formaccess.eligibility;

import com.formaccess.entity.enums.RuleAttribute;
import com.formaccess.entity.enums.RuleOperator;

/** One condition, e.g. (group 1) YEAR GREATER_OR_EQUAL "2". */
public record RuleCondition(int groupNo, RuleAttribute attribute, RuleOperator operator, String value) {
}
