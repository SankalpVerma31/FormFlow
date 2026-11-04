package com.formaccess.dto;

import com.formaccess.entity.enums.RuleAttribute;
import com.formaccess.entity.enums.RuleOperator;
import jakarta.validation.constraints.*;

/** One condition built with the rule builder dropdowns. */
public class RuleForm {

    @NotNull @Min(1) @Max(20)
    private Integer groupNo = 1;

    @NotNull
    private RuleAttribute attribute = RuleAttribute.DEPARTMENT;

    @NotNull
    private RuleOperator operator = RuleOperator.EQUALS;

    @NotBlank(message = "Enter a value")
    @Size(max = 255)
    private String ruleValue;

    public Integer getGroupNo() { return groupNo; }
    public void setGroupNo(Integer groupNo) { this.groupNo = groupNo; }
    public RuleAttribute getAttribute() { return attribute; }
    public void setAttribute(RuleAttribute attribute) { this.attribute = attribute; }
    public RuleOperator getOperator() { return operator; }
    public void setOperator(RuleOperator operator) { this.operator = operator; }
    public String getRuleValue() { return ruleValue; }
    public void setRuleValue(String ruleValue) { this.ruleValue = ruleValue; }
}
