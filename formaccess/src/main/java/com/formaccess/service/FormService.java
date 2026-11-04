package com.formaccess.service;

import com.formaccess.dto.QuestionForm;
import com.formaccess.entity.Event;
import com.formaccess.entity.Question;
import com.formaccess.entity.QuestionOption;
import com.formaccess.entity.User;
import com.formaccess.entity.enums.QuestionType;
import com.formaccess.exception.BusinessException;
import com.formaccess.exception.NotFoundException;
import com.formaccess.repository.EventRepository;
import com.formaccess.repository.QuestionRepository;
import com.formaccess.validation.AnswerValidationException;
import com.formaccess.validation.AnswerValidator;
import com.formaccess.validation.QuestionRules;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * THE DYNAMIC FORM BUILDER.
 * Questions are rows in the database, not HTML. The form page is generated from them.
 * Questions can only be changed while the event is a DRAFT, so answers always match their questions.
 */
@Service
public class FormService {

    private final QuestionRepository questionRepository;
    private final EventRepository eventRepository;
    private final AuditService auditService;
    private final AnswerValidator answerValidator = new AnswerValidator();

    public FormService(QuestionRepository questionRepository, EventRepository eventRepository,
                       AuditService auditService) {
        this.questionRepository = questionRepository;
        this.eventRepository = eventRepository;
        this.auditService = auditService;
    }

    /** Finds a question and makes sure it belongs to the given event (no editing other people's forms). */
    public Question getQuestion(Event event, Long questionId) {
        Question q = questionRepository.findById(questionId).orElseThrow(() -> new NotFoundException("Question not found"));
        if (!q.getEvent().getId().equals(event.getId())) throw new NotFoundException("Question not found");
        return q;
    }

    @Transactional
    public void addQuestion(Event event, QuestionForm form, User actor) {
        requireDraft(event);
        Question q = new Question();
        q.setEvent(event);
        q.setDisplayOrder(event.getQuestions().size() + 1);
        apply(q, form);
        event.getQuestions().add(q);
        questionRepository.save(q);
        auditService.log(event, actor, "QUESTION_ADDED", q.getQuestionText());
    }

    @Transactional
    public void updateQuestion(Event event, Question q, QuestionForm form, User actor) {
        requireDraft(event);
        apply(q, form);
        questionRepository.save(q);
        auditService.log(event, actor, "QUESTION_UPDATED", q.getQuestionText());
    }

    @Transactional
    public void deleteQuestion(Event event, Question q, User actor) {
        requireDraft(event);
        event.getQuestions().remove(q);       // orphanRemoval deletes the row
        renumber(event);
        eventRepository.save(event);
        auditService.log(event, actor, "QUESTION_DELETED", q.getQuestionText());
    }

    /** Swap a question with its neighbour (up = -1, down = +1). */
    @Transactional
    public void move(Event event, Question q, int direction) {
        requireDraft(event);
        List<Question> list = new ArrayList<>(event.getQuestions());
        list.sort(Comparator.comparingInt(Question::getDisplayOrder));
        int index = list.indexOf(q);
        int target = index + direction;
        if (index < 0 || target < 0 || target >= list.size()) return;
        Question other = list.get(target);
        int tmp = q.getDisplayOrder();
        q.setDisplayOrder(other.getDisplayOrder());
        other.setDisplayOrder(tmp);
        questionRepository.save(q);
        questionRepository.save(other);
    }

    public QuestionForm toForm(Question q) {
        QuestionForm f = new QuestionForm();
        f.setQuestionText(q.getQuestionText());
        f.setHelpText(q.getHelpText());
        f.setQuestionType(q.getQuestionType());
        f.setRequired(q.isRequired());
        f.setDefaultValue(q.getDefaultValue());
        f.setMinLength(q.getMinLength());
        f.setMaxLength(q.getMaxLength());
        f.setMinValue(q.getMinValue());
        f.setMaxValue(q.getMaxValue());
        f.setMinDate(q.getMinDate());
        f.setMaxDate(q.getMaxDate());
        f.setRatingMax(q.getRatingMax() == null ? 5 : q.getRatingMax());
        StringBuilder sb = new StringBuilder();
        for (QuestionOption o : q.getOptions()) sb.append(o.getOptionText()).append('\n');
        f.setOptionsText(sb.toString().trim());
        return f;
    }

    // ------------------------------------------------------------------ helpers

    private void requireDraft(Event event) {
        if (!event.isEditable()) {
            throw new BusinessException("Questions can only be changed while the event is a DRAFT.");
        }
    }

    private void renumber(Event event) {
        List<Question> list = new ArrayList<>(event.getQuestions());
        list.sort(Comparator.comparingInt(Question::getDisplayOrder));
        for (int i = 0; i < list.size(); i++) list.get(i).setDisplayOrder(i + 1);
    }

    /** Copies the form into the entity, keeping only the settings that make sense for the type. */
    private void apply(Question q, QuestionForm f) {
        QuestionType type = f.getQuestionType();
        q.setQuestionText(f.getQuestionText().trim());
        q.setHelpText(blankToNull(f.getHelpText()));
        q.setQuestionType(type);
        q.setRequired(f.isRequired());

        boolean text = type == QuestionType.SHORT_TEXT || type == QuestionType.LONG_TEXT || type == QuestionType.EMAIL;
        boolean number = type == QuestionType.INTEGER || type == QuestionType.DECIMAL;
        boolean date = type == QuestionType.DATE;

        q.setMinLength(text ? f.getMinLength() : null);
        q.setMaxLength(text ? f.getMaxLength() : null);
        q.setMinValue(number ? f.getMinValue() : null);
        q.setMaxValue(number ? f.getMaxValue() : null);
        q.setMinDate(date ? f.getMinDate() : null);
        q.setMaxDate(date ? f.getMaxDate() : null);
        q.setRatingMax(type == QuestionType.RATING ? f.getRatingMax() : null);

        if (q.getMinLength() != null && q.getMinLength() < 0) throw new BusinessException("Minimum length cannot be negative.");
        if (q.getMaxLength() != null && q.getMaxLength() < 1) throw new BusinessException("Maximum length must be at least 1.");
        if (q.getMinLength() != null && q.getMaxLength() != null && q.getMinLength() > q.getMaxLength())
            throw new BusinessException("Minimum length cannot be greater than maximum length.");
        if (q.getMinValue() != null && q.getMaxValue() != null && q.getMinValue().compareTo(q.getMaxValue()) > 0)
            throw new BusinessException("Minimum value cannot be greater than maximum value.");
        if (q.getMinDate() != null && q.getMaxDate() != null && q.getMinDate().isAfter(q.getMaxDate()))
            throw new BusinessException("Earliest date cannot be after latest date.");
        if (type == QuestionType.RATING && (q.getRatingMax() == null || (q.getRatingMax() != 5 && q.getRatingMax() != 10)))
            throw new BusinessException("Rating scale must be 1–5 or 1–10.");

        if (type.isChoice()) {
            q.setDefaultValue(null);
            syncOptions(q, parseOptions(f.getOptionsText()));
        } else {
            q.getOptions().clear();
            q.setDefaultValue(checkDefault(q, blankToNull(f.getDefaultValue())));
        }
    }

    /** The default value must itself be a valid answer. */
    private String checkDefault(Question q, String value) {
        if (value == null) return null;
        QuestionRules rules = new QuestionRules(q.getQuestionText(), q.getQuestionType(), false,
                q.getMinLength(), q.getMaxLength(), q.getMinValue(), q.getMaxValue(),
                q.getMinDate(), q.getMaxDate(), q.getRatingMax(), Set.of());
        try {
            answerValidator.validate(rules, List.of(value));
        } catch (AnswerValidationException e) {
            throw new BusinessException("Default value is not valid: " + e.getMessage());
        }
        return value;
    }

    private List<String> parseOptions(String text) {
        LinkedHashMap<String, String> unique = new LinkedHashMap<>();
        if (text != null) {
            for (String line : text.split("\\r?\\n")) {
                String opt = line.trim();
                if (opt.isEmpty()) continue;
                if (opt.length() > 200) throw new BusinessException("Each option can have at most 200 characters.");
                unique.putIfAbsent(opt.toLowerCase(), opt);
            }
        }
        if (unique.size() < 2) throw new BusinessException("Choice questions need at least 2 options (one per line).");
        if (unique.size() > 50) throw new BusinessException("At most 50 options are allowed.");
        return new ArrayList<>(unique.values());
    }

    /**
     * Keeps existing options whose text is unchanged, removes the others and adds new ones.
     * (Deleting and re-inserting the same text would clash with the UNIQUE(question_id, option_text) key.)
     */
    private void syncOptions(Question q, List<String> wanted) {
        Map<String, QuestionOption> existing = new HashMap<>();
        for (QuestionOption o : q.getOptions()) existing.put(o.getOptionText().toLowerCase(), o);

        Set<String> wantedKeys = new HashSet<>();
        for (String w : wanted) wantedKeys.add(w.toLowerCase());
        q.getOptions().removeIf(o -> !wantedKeys.contains(o.getOptionText().toLowerCase()));

        for (int i = 0; i < wanted.size(); i++) {
            String text = wanted.get(i);
            QuestionOption o = existing.get(text.toLowerCase());
            if (o != null && q.getOptions().contains(o)) {
                o.setDisplayOrder(i + 1);
            } else {
                q.getOptions().add(new QuestionOption(q, text, i + 1));
            }
        }
        q.getOptions().sort(Comparator.comparingInt(QuestionOption::getDisplayOrder));
    }

    private String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
