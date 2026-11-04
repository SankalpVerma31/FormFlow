package com.formaccess.eligibility;

import com.formaccess.entity.enums.RuleAttribute;
import com.formaccess.entity.enums.RuleOperator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Unit tests for the eligibility engine. Run with:  mvn test */
class RuleEvaluatorTest {

    private final RuleEvaluator evaluator = new RuleEvaluator();

    // (DEPARTMENT = CSE AND YEAR >= 2 AND ROLE = PARTICIPANT) OR (DEPARTMENT = IT AND ROLE = PARTICIPANT)
    private final List<RuleCondition> hackathonRules = List.of(
            new RuleCondition(1, RuleAttribute.DEPARTMENT, RuleOperator.EQUALS, "CSE"),
            new RuleCondition(1, RuleAttribute.YEAR, RuleOperator.GREATER_OR_EQUAL, "2"),
            new RuleCondition(1, RuleAttribute.ROLE, RuleOperator.EQUALS, "PARTICIPANT"),
            new RuleCondition(2, RuleAttribute.DEPARTMENT, RuleOperator.EQUALS, "IT"),
            new RuleCondition(2, RuleAttribute.ROLE, RuleOperator.EQUALS, "PARTICIPANT"));

    private UserAttributes user(String dept, Integer year, String role, String status) {
        return new UserAttributes(dept, dept + " department", year, role, status);
    }

    @Test
    void cseSecondYearIsEligible() {
        assertTrue(evaluator.isEligible(user("CSE", 2, "PARTICIPANT", "ACTIVE"), hackathonRules));
    }

    @Test
    void cseFirstYearIsNotEligible() {
        assertFalse(evaluator.isEligible(user("CSE", 1, "PARTICIPANT", "ACTIVE"), hackathonRules));
    }

    @Test
    void itAnyYearIsEligibleThroughSecondGroup() {
        assertTrue(evaluator.isEligible(user("IT", 1, "PARTICIPANT", "ACTIVE"), hackathonRules));
    }

    @Test
    void otherDepartmentIsNotEligible() {
        assertFalse(evaluator.isEligible(user("MECH", 3, "PARTICIPANT", "ACTIVE"), hackathonRules));
    }

    @Test
    void creatorIsNotEligibleWhenRoleMustBeParticipant() {
        assertFalse(evaluator.isEligible(user("IT", null, "CREATOR", "ACTIVE"), hackathonRules));
    }

    @Test
    void inactiveUserIsNeverEligible() {
        assertFalse(evaluator.isEligible(user("CSE", 2, "PARTICIPANT", "INACTIVE"), hackathonRules));
        assertFalse(evaluator.isEligible(user("CSE", 2, "PARTICIPANT", "INACTIVE"), List.of()));
    }

    @Test
    void noRulesMeansEveryActiveUser() {
        assertTrue(evaluator.isEligible(user("CIVIL", 4, "PARTICIPANT", "ACTIVE"), List.of()));
    }

    @Test
    void inOperatorMatchesAnyValueIgnoringCase() {
        List<RuleCondition> rules = List.of(new RuleCondition(1, RuleAttribute.DEPARTMENT, RuleOperator.IN, "CSE, it"));
        assertTrue(evaluator.isEligible(user("IT", 3, "PARTICIPANT", "ACTIVE"), rules));
        assertFalse(evaluator.isEligible(user("ECE", 3, "PARTICIPANT", "ACTIVE"), rules));
    }

    @Test
    void containsSearchesDepartmentName() {
        List<RuleCondition> rules = List.of(new RuleCondition(1, RuleAttribute.DEPARTMENT, RuleOperator.CONTAINS, "electronics"));
        UserAttributes ece = new UserAttributes("ECE", "Electronics and Communication Engineering", 2, "PARTICIPANT", "ACTIVE");
        assertTrue(evaluator.isEligible(ece, rules));
    }

    @Test
    void yearComparisons() {
        UserAttributes y3 = user("CSE", 3, "PARTICIPANT", "ACTIVE");
        assertTrue(evaluator.isEligible(y3, List.of(new RuleCondition(1, RuleAttribute.YEAR, RuleOperator.GREATER_THAN, "2"))));
        assertFalse(evaluator.isEligible(y3, List.of(new RuleCondition(1, RuleAttribute.YEAR, RuleOperator.LESS_THAN, "3"))));
        assertTrue(evaluator.isEligible(y3, List.of(new RuleCondition(1, RuleAttribute.YEAR, RuleOperator.LESS_OR_EQUAL, "3"))));
        assertTrue(evaluator.isEligible(y3, List.of(new RuleCondition(1, RuleAttribute.YEAR, RuleOperator.NOT_EQUALS, "1"))));
    }

    @Test
    void badNumberNeverGrantsAccess() {
        List<RuleCondition> rules = List.of(new RuleCondition(1, RuleAttribute.YEAR, RuleOperator.GREATER_THAN, "abc"));
        assertFalse(evaluator.isEligible(user("CSE", 4, "PARTICIPANT", "ACTIVE"), rules));
    }

    @Test
    void accountStatusRule() {
        List<RuleCondition> rules = List.of(
                new RuleCondition(1, RuleAttribute.ROLE, RuleOperator.EQUALS, "PARTICIPANT"),
                new RuleCondition(1, RuleAttribute.ACCOUNT_STATUS, RuleOperator.EQUALS, "ACTIVE"));
        assertTrue(evaluator.isEligible(user("CSE", 1, "PARTICIPANT", "ACTIVE"), rules));
        assertFalse(evaluator.isEligible(user("CSE", 1, "ADMIN", "ACTIVE"), rules));
    }
}
