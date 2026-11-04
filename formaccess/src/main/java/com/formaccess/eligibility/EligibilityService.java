package com.formaccess.eligibility;

import com.formaccess.entity.EligibilityRule;
import com.formaccess.entity.Event;
import com.formaccess.entity.User;
import com.formaccess.entity.enums.AccountStatus;
import com.formaccess.entity.enums.SubmissionStatus;
import com.formaccess.repository.SubmissionRepository;
import com.formaccess.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Spring service around the pure RuleEvaluator.
 *   EligibilityService.isEligible(user, event) -> true / false
 * Called when a participant opens a form, submits it, or views results.
 */
@Service
public class EligibilityService {

    private final RuleEvaluator evaluator = new RuleEvaluator();
    private final UserRepository userRepository;
    private final SubmissionRepository submissionRepository;

    public EligibilityService(UserRepository userRepository, SubmissionRepository submissionRepository) {
        this.userRepository = userRepository;
        this.submissionRepository = submissionRepository;
    }

    public boolean isEligible(User user, Event event) {
        return evaluator.isEligible(toAttributes(user), toConditions(event));
    }

    /** All ACTIVE users who satisfy the event's rules. */
    public List<User> findEligibleUsers(Event event) {
        List<RuleCondition> conditions = toConditions(event);
        return userRepository.findByAccountStatus(AccountStatus.ACTIVE).stream()
                .filter(u -> evaluator.isEligible(toAttributes(u), conditions))
                .toList();
    }

    public long countEligible(Event event) {
        return findEligibleUsers(event).size();
    }

    /** Eligible users who have NOT submitted yet. */
    public List<User> findPendingUsers(Event event) {
        Set<Long> submitted = new HashSet<>(
                submissionRepository.findSubmittedUserIds(event, SubmissionStatus.SUBMITTED));
        return findEligibleUsers(event).stream()
                .filter(u -> !submitted.contains(u.getId()))
                .toList();
    }

    private UserAttributes toAttributes(User u) {
        return new UserAttributes(
                u.getDepartment() == null ? null : u.getDepartment().getCode(),
                u.getDepartment() == null ? null : u.getDepartment().getName(),
                u.getYearOfStudy(),
                u.getRole().name(),
                u.getAccountStatus().name());
    }

    private List<RuleCondition> toConditions(Event event) {
        return event.getRules().stream()
                .map((EligibilityRule r) -> new RuleCondition(r.getGroupNo(), r.getAttribute(), r.getOperator(), r.getRuleValue()))
                .toList();
    }
}
