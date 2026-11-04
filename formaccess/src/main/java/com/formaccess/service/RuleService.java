package com.formaccess.service;

import com.formaccess.dto.RuleForm;
import com.formaccess.entity.Department;
import com.formaccess.entity.EligibilityRule;
import com.formaccess.entity.Event;
import com.formaccess.entity.User;
import com.formaccess.entity.enums.*;
import com.formaccess.exception.BusinessException;
import com.formaccess.exception.NotFoundException;
import com.formaccess.repository.DepartmentRepository;
import com.formaccess.repository.EligibilityRuleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/** Adds/removes eligibility conditions and checks that each value makes sense. */
@Service
public class RuleService {

    private final EligibilityRuleRepository ruleRepository;
    private final DepartmentRepository departmentRepository;
    private final AuditService auditService;

    public RuleService(EligibilityRuleRepository ruleRepository, DepartmentRepository departmentRepository,
                       AuditService auditService) {
        this.ruleRepository = ruleRepository;
        this.departmentRepository = departmentRepository;
        this.auditService = auditService;
    }

    /** Rules grouped by group number, for display: {1=[...], 2=[...]} */
    public Map<Integer, List<EligibilityRule>> grouped(Event event) {
        Map<Integer, List<EligibilityRule>> map = new TreeMap<>();
        for (EligibilityRule r : event.getRules()) map.computeIfAbsent(r.getGroupNo(), k -> new ArrayList<>()).add(r);
        return map;
    }

    public int nextGroupNo(Event event) {
        return event.getRules().stream().mapToInt(EligibilityRule::getGroupNo).max().orElse(0) + 1;
    }

    @Transactional
    public void addRule(Event event, RuleForm form, User actor) {
        if (event.getStatus() == EventStatus.CLOSED) throw new BusinessException("Rules of a closed event cannot change.");
        EligibilityRule rule = new EligibilityRule();
        rule.setEvent(event);
        rule.setGroupNo(form.getGroupNo());
        rule.setAttribute(form.getAttribute());
        rule.setOperator(form.getOperator());
        rule.setRuleValue(normalise(form.getAttribute(), form.getOperator(), form.getRuleValue()));
        event.getRules().add(rule);
        ruleRepository.save(rule);
        auditService.log(event, actor, "RULE_ADDED", describe(rule));
    }

    @Transactional
    public void deleteRule(Event event, Long ruleId, User actor) {
        if (event.getStatus() == EventStatus.CLOSED) throw new BusinessException("Rules of a closed event cannot change.");
        EligibilityRule rule = event.getRules().stream().filter(r -> r.getId().equals(ruleId)).findFirst()
                .orElseThrow(() -> new NotFoundException("Rule not found"));
        event.getRules().remove(rule);
        ruleRepository.delete(rule);
        auditService.log(event, actor, "RULE_DELETED", describe(rule));
    }

    public String describe(EligibilityRule r) {
        return r.getAttribute().getLabel() + " " + r.getOperator().getSymbol() + " " + r.getRuleValue();
    }

    /** Validates the value against the attribute and returns a clean version (e.g. "cse" -> "CSE"). */
    private String normalise(RuleAttribute attribute, RuleOperator operator, String raw) {
        String value = raw.trim();
        if (operator.isNumericOnly() && attribute != RuleAttribute.YEAR) {
            throw new BusinessException("Greater/less than can only be used with Year of study.");
        }
        List<String> parts = operator == RuleOperator.IN
                ? Arrays.stream(value.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList()
                : List.of(value);
        if (parts.isEmpty()) throw new BusinessException("Enter at least one value.");
        if (operator == RuleOperator.CONTAINS) return value;

        List<String> clean = new ArrayList<>();
        for (String p : parts) {
            switch (attribute) {
                case YEAR -> {
                    try {
                        int y = Integer.parseInt(p);
                        if (y < 1 || y > 5) throw new NumberFormatException();
                    } catch (NumberFormatException e) {
                        throw new BusinessException("Year must be a number from 1 to 5.");
                    }
                    clean.add(p);
                }
                case DEPARTMENT -> {
                    Set<String> codes = new HashSet<>();
                    for (Department d : departmentRepository.findAll()) codes.add(d.getCode().toUpperCase());
                    if (!codes.contains(p.toUpperCase())) throw new BusinessException("Unknown department code: " + p);
                    clean.add(p.toUpperCase());
                }
                case ROLE -> {
                    try { clean.add(Role.valueOf(p.toUpperCase()).name()); }
                    catch (IllegalArgumentException e) { throw new BusinessException("Role must be ADMIN, CREATOR or PARTICIPANT."); }
                }
                case ACCOUNT_STATUS -> {
                    try { clean.add(AccountStatus.valueOf(p.toUpperCase()).name()); }
                    catch (IllegalArgumentException e) { throw new BusinessException("Status must be ACTIVE or INACTIVE."); }
                }
            }
        }
        return String.join(",", clean);
    }
}
