package com.formaccess.entity;

import com.formaccess.entity.enums.RuleAttribute;
import com.formaccess.entity.enums.RuleOperator;
import jakarta.persistence.*;

/**
 * Maps to table "eligibility_rules".
 * Rules with the same groupNo are joined with AND; different groups are joined with OR.
 */
@Entity
@Table(name = "eligibility_rules")
public class EligibilityRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rule_id")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "group_no", nullable = false)
    private int groupNo = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "attribute", nullable = false, length = 30)
    private RuleAttribute attribute;

    @Enumerated(EnumType.STRING)
    @Column(name = "operator", nullable = false, length = 30)
    private RuleOperator operator;

    @Column(name = "rule_value", nullable = false, length = 255)
    private String ruleValue;

    public Long getId() { return id; }
    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }
    public int getGroupNo() { return groupNo; }
    public void setGroupNo(int groupNo) { this.groupNo = groupNo; }
    public RuleAttribute getAttribute() { return attribute; }
    public void setAttribute(RuleAttribute attribute) { this.attribute = attribute; }
    public RuleOperator getOperator() { return operator; }
    public void setOperator(RuleOperator operator) { this.operator = operator; }
    public String getRuleValue() { return ruleValue; }
    public void setRuleValue(String ruleValue) { this.ruleValue = ruleValue; }
}
