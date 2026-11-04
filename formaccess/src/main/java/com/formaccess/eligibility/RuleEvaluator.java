package com.formaccess.eligibility;

import com.formaccess.entity.enums.RuleAttribute;
import com.formaccess.entity.enums.RuleOperator;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * THE ELIGIBILITY RULE ENGINE (pure Java, no database, no Spring).
 *
 * How rules are combined:
 *   - conditions in the SAME group are joined with AND
 *   - different groups are joined with OR
 *   - no rules at all  -> everyone (who is ACTIVE) is eligible
 *   - INACTIVE users   -> never eligible
 *
 * Example: (DEPARTMENT = CSE AND YEAR >= 2) OR (DEPARTMENT = IT)
 *
 * Creators never write SQL; they only pick an attribute, an operator and a value
 * from fixed lists, and this class compares them with the user's data.
 */
public class RuleEvaluator {

    public boolean isEligible(UserAttributes user, List<RuleCondition> conditions) {
        if (user == null || !"ACTIVE".equalsIgnoreCase(user.accountStatus())) {
            return false;
        }
        if (conditions == null || conditions.isEmpty()) {
            return true;
        }

        // Put conditions into their groups: {1=[...], 2=[...]}
        Map<Integer, List<RuleCondition>> groups = new TreeMap<>();
        for (RuleCondition c : conditions) {
            groups.computeIfAbsent(c.groupNo(), k -> new java.util.ArrayList<>()).add(c);
        }

        // OR between groups: one fully matching group is enough.
        for (List<RuleCondition> group : groups.values()) {
            if (groupMatches(user, group)) {
                return true;
            }
        }
        return false;
    }

    /** AND inside a group: every condition must be true. */
    private boolean groupMatches(UserAttributes user, List<RuleCondition> group) {
        for (RuleCondition c : group) {
            if (!conditionMatches(user, c)) {
                return false;
            }
        }
        return true;
    }

    boolean conditionMatches(UserAttributes user, RuleCondition c) {
        String ruleValue = c.value() == null ? "" : c.value().trim();

        if (c.attribute() == RuleAttribute.YEAR) {
            return compareNumber(user.year(), c.operator(), ruleValue);
        }

        String actual = switch (c.attribute()) {
            case DEPARTMENT -> user.departmentCode();
            case ROLE -> user.role();
            case ACCOUNT_STATUS -> user.accountStatus();
            default -> null;
        };

        // CONTAINS on department also searches the full department name.
        if (c.operator() == RuleOperator.CONTAINS && c.attribute() == RuleAttribute.DEPARTMENT) {
            String both = (user.departmentCode() == null ? "" : user.departmentCode()) + " "
                        + (user.departmentName() == null ? "" : user.departmentName());
            return !ruleValue.isEmpty() && both.toLowerCase().contains(ruleValue.toLowerCase());
        }
        return compareText(actual, c.operator(), ruleValue);
    }

    private boolean compareText(String actual, RuleOperator op, String ruleValue) {
        if (actual == null) {
            // A user without a department is "not equal" to every department.
            return op == RuleOperator.NOT_EQUALS;
        }
        return switch (op) {
            case EQUALS -> actual.equalsIgnoreCase(ruleValue);
            case NOT_EQUALS -> !actual.equalsIgnoreCase(ruleValue);
            case CONTAINS -> !ruleValue.isEmpty() && actual.toLowerCase().contains(ruleValue.toLowerCase());
            case IN -> splitList(ruleValue).stream().anyMatch(actual::equalsIgnoreCase);
            // >, >=, <, <= are not allowed on text
            default -> false;
        };
    }

    private boolean compareNumber(Integer actual, RuleOperator op, String ruleValue) {
        if (actual == null) {
            return op == RuleOperator.NOT_EQUALS;
        }
        if (op == RuleOperator.IN) {
            return splitList(ruleValue).stream().anyMatch(v -> v.equals(String.valueOf(actual)));
        }
        if (op == RuleOperator.CONTAINS) {
            return String.valueOf(actual).contains(ruleValue);
        }
        int expected;
        try {
            expected = Integer.parseInt(ruleValue);
        } catch (NumberFormatException e) {
            return false;   // a bad value can never make someone eligible
        }
        return switch (op) {
            case EQUALS -> actual == expected;
            case NOT_EQUALS -> actual != expected;
            case GREATER_THAN -> actual > expected;
            case GREATER_OR_EQUAL -> actual >= expected;
            case LESS_THAN -> actual < expected;
            case LESS_OR_EQUAL -> actual <= expected;
            default -> false;
        };
    }

    private List<String> splitList(String value) {
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
