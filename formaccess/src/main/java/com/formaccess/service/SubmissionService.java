package com.formaccess.service;

import com.formaccess.dto.AnswerLine;
import com.formaccess.dto.QuestionInputView;
import com.formaccess.eligibility.EligibilityService;
import com.formaccess.entity.*;
import com.formaccess.entity.enums.EventStatus;
import com.formaccess.entity.enums.QuestionType;
import com.formaccess.entity.enums.Role;
import com.formaccess.entity.enums.SubmissionStatus;
import com.formaccess.exception.FormValidationException;
import com.formaccess.exception.NotFoundException;
import com.formaccess.exception.SubmissionBlockedException;
import com.formaccess.repository.SubmissionRepository;
import com.formaccess.validation.AnswerValidationException;
import com.formaccess.validation.AnswerValidator;
import com.formaccess.validation.QuestionRules;
import com.formaccess.validation.ValidatedAnswer;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Opening and submitting forms. Every check is done HERE on the server:
 *   1. account active   2. eligible   3. event open   4. before deadline
 *   5. not a duplicate  6. every answer valid
 * Then the submission and all answers are saved in ONE transaction.
 */
@Service
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final EligibilityService eligibilityService;
    private final DeadlineService deadlineService;
    private final AnswerValidator answerValidator = new AnswerValidator();

    public SubmissionService(SubmissionRepository submissionRepository, EligibilityService eligibilityService,
                             DeadlineService deadlineService) {
        this.submissionRepository = submissionRepository;
        this.eligibilityService = eligibilityService;
        this.deadlineService = deadlineService;
    }

    /** May this user fill this form right now? */
    @Transactional
    public AccessResult checkAccess(User user, Event event) {
        deadlineService.refresh(event);           // deadline check that does not depend on the scheduler
        if (event.getStatus() == EventStatus.DRAFT) return AccessResult.NOT_PUBLISHED;
        if (!user.isActive()) return AccessResult.ACCOUNT_INACTIVE;
        if (!eligibilityService.isEligible(user, event)) return AccessResult.NOT_ELIGIBLE;
        if (event.getStatus() == EventStatus.SCHEDULED) return AccessResult.NOT_STARTED;
        if (event.getStatus() == EventStatus.CLOSED || event.isDeadlinePassed(LocalDateTime.now())) return AccessResult.CLOSED;
        if (!event.isAllowMultipleSubmissions() && hasSubmitted(user, event)) return AccessResult.ALREADY_SUBMITTED;
        return AccessResult.ALLOWED;
    }

    public boolean hasSubmitted(User user, Event event) {
        return submissionRepository.existsByEventAndUserAndStatus(event, user, SubmissionStatus.SUBMITTED);
    }

    /** Builds the input list for the form page. values == null means "fresh form, use defaults". */
    public List<QuestionInputView> inputViews(Event event, MultiValueMap<String, String> values, Map<Long, String> errors) {
        List<QuestionInputView> views = new ArrayList<>();
        for (Question q : sortedQuestions(event)) {
            List<String> raw = values == null ? null : values.get(fieldName(q));
            String value;
            Set<Long> selected = new HashSet<>();
            if (values == null) {
                value = q.getDefaultValue() == null ? "" : q.getDefaultValue();
            } else {
                value = raw == null || raw.isEmpty() ? "" : raw.get(0);
                if (raw != null) {
                    for (String r : raw) {
                        try { selected.add(Long.parseLong(r)); } catch (NumberFormatException ignored) { }
                    }
                }
            }
            views.add(new QuestionInputView(q, value, selected, errors == null ? null : errors.get(q.getId())));
        }
        return views;
    }

    /** The HTML input name for a question, e.g. "q_12". */
    public static String fieldName(Question q) {
        return "q_" + q.getId();
    }

    @Transactional
    public Submission submit(User user, Event event, MultiValueMap<String, String> params) {
        AccessResult access = checkAccess(user, event);
        if (access != AccessResult.ALLOWED) throw new SubmissionBlockedException(access);

        // Validate every answer first; collect all problems so the user sees them together.
        Map<Long, String> errors = new LinkedHashMap<>();
        Map<Question, ValidatedAnswer> valid = new LinkedHashMap<>();
        for (Question q : sortedQuestions(event)) {
            try {
                valid.put(q, answerValidator.validate(rulesOf(q), params.get(fieldName(q))));
            } catch (AnswerValidationException e) {
                errors.put(q.getId(), e.getMessage());
            }
        }
        if (!errors.isEmpty()) throw new FormValidationException(errors);

        Submission submission = new Submission();
        submission.setEvent(event);
        submission.setUser(user);
        submission.setAttemptNo(event.isAllowMultipleSubmissions()
                ? submissionRepository.findMaxAttempt(event, user) + 1
                : 1);                                       // always 1 -> UNIQUE key blocks duplicates
        submission.setSubmittedAt(LocalDateTime.now());

        for (Map.Entry<Question, ValidatedAnswer> e : valid.entrySet()) {
            ValidatedAnswer va = e.getValue();
            if (va.isEmpty()) continue;                     // optional question left blank
            Answer a = new Answer();
            a.setQuestion(e.getKey());
            a.setAnswerText(va.text());
            a.setAnswerNumber(va.number());
            for (Long optionId : va.optionIds()) {
                e.getKey().getOptions().stream()
                        .filter(o -> o.getId().equals(optionId))
                        .findFirst()
                        .ifPresent(a.getSelectedOptions()::add);
            }
            submission.addAnswer(a);
        }

        try {
            return submissionRepository.saveAndFlush(submission);
        } catch (DataIntegrityViolationException ex) {
            // Two requests at the same moment: the database UNIQUE key stopped the second one.
            throw new SubmissionBlockedException(AccessResult.ALREADY_SUBMITTED);
        }
    }

    /** A participant may only open their OWN submission. */
    public Submission getOwnSubmission(User user, Long submissionId) {
        Submission s = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new NotFoundException("Submission not found"));
        if (!s.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You can only view your own submissions.");
        }
        return s;
    }

    /** The creator (or admin) may open any submission of their event. */
    public Submission getSubmissionForManager(Event event, Long submissionId) {
        Submission s = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new NotFoundException("Submission not found"));
        if (!s.getEvent().getId().equals(event.getId())) throw new NotFoundException("Submission not found");
        return s;
    }

    public List<Submission> history(User user) {
        return submissionRepository.findByUserOrderBySubmittedAtDesc(user);
    }

    public List<Submission> submissionsOf(Event event) {
        return submissionRepository.findByEventAndStatusOrderBySubmittedAtDesc(event, SubmissionStatus.SUBMITTED);
    }

    public List<Submission> mySubmissionsFor(User user, Event event) {
        return submissionRepository.findByEventAndUserOrderBySubmittedAtDesc(event, user);
    }

    public boolean canManage(User user, Event event) {
        return user.getRole() == Role.ADMIN || event.getCreator().getId().equals(user.getId());
    }

    /** Turns stored answers back into readable text, in question order. */
    public List<AnswerLine> answerLines(Submission s) {
        Map<Long, Answer> byQuestion = new HashMap<>();
        for (Answer a : s.getAnswers()) byQuestion.put(a.getQuestion().getId(), a);
        List<AnswerLine> lines = new ArrayList<>();
        for (Question q : sortedQuestions(s.getEvent())) {
            Answer a = byQuestion.get(q.getId());
            lines.add(new AnswerLine(q.getQuestionText(), a == null ? "—" : display(q, a)));
        }
        return lines;
    }

    private String display(Question q, Answer a) {
        if (q.getQuestionType().isChoice()) {
            return a.getSelectedOptions().stream()
                    .sorted(Comparator.comparingInt(QuestionOption::getDisplayOrder))
                    .map(QuestionOption::getOptionText)
                    .collect(Collectors.joining(", "));
        }
        if (a.getAnswerNumber() != null) {
            String n = a.getAnswerNumber().stripTrailingZeros().toPlainString();
            return q.getQuestionType() == QuestionType.RATING ? n + " / " + q.getRatingMax() : n;
        }
        return a.getAnswerText() == null ? "—" : a.getAnswerText();
    }

    private List<Question> sortedQuestions(Event event) {
        List<Question> list = new ArrayList<>(event.getQuestions());
        list.sort(Comparator.comparingInt(Question::getDisplayOrder));
        return list;
    }

    private QuestionRules rulesOf(Question q) {
        Set<Long> optionIds = new HashSet<>();
        for (QuestionOption o : q.getOptions()) optionIds.add(o.getId());
        return new QuestionRules(q.getQuestionText(), q.getQuestionType(), q.isRequired(),
                q.getMinLength(), q.getMaxLength(), q.getMinValue(), q.getMaxValue(),
                q.getMinDate(), q.getMaxDate(), q.getRatingMax(), optionIds);
    }

}
