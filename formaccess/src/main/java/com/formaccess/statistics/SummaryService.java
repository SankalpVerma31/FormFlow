package com.formaccess.statistics;

import com.formaccess.dto.EventStats;
import com.formaccess.dto.PublicResult;
import com.formaccess.dto.QuestionStat;
import com.formaccess.dto.SummaryOption;
import com.formaccess.entity.Event;
import com.formaccess.entity.PublicSummary;
import com.formaccess.entity.Question;
import com.formaccess.entity.User;
import com.formaccess.entity.enums.EventStatus;
import com.formaccess.entity.enums.QuestionType;
import com.formaccess.entity.enums.SummaryType;
import com.formaccess.entity.enums.Visibility;
import com.formaccess.repository.PublicSummaryRepository;
import com.formaccess.service.AuditService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Public / private summaries.
 *   PRIVATE               -> only the creator sees it
 *   PUBLIC_ALWAYS         -> participants can see it any time
 *   PUBLIC_AFTER_DEADLINE -> participants see it once the event is closed
 * Only aggregated numbers are ever returned – never individual answers.
 */
@Service
public class SummaryService {

    private final PublicSummaryRepository summaryRepository;
    private final StatisticsService statisticsService;
    private final AuditService auditService;

    public SummaryService(PublicSummaryRepository summaryRepository, StatisticsService statisticsService,
                          AuditService auditService) {
        this.summaryRepository = summaryRepository;
        this.statisticsService = statisticsService;
        this.auditService = auditService;
    }

    /** Every summary that can exist for this event, with its current visibility. */
    public List<SummaryOption> options(Event event) {
        List<PublicSummary> saved = summaryRepository.findByEvent(event);
        List<SummaryOption> list = new ArrayList<>();
        for (SummaryType t : List.of(SummaryType.RESPONSE_COUNT, SummaryType.SUBMISSION_PERCENTAGE, SummaryType.BY_DEPARTMENT)) {
            list.add(new SummaryOption(t, null, null, visibilityOf(saved, t, null)));
        }
        List<Question> questions = new ArrayList<>(event.getQuestions());
        questions.sort(Comparator.comparingInt(Question::getDisplayOrder));
        for (Question q : questions) {
            for (SummaryType t : typesFor(q.getQuestionType())) {
                list.add(new SummaryOption(t, q.getId(), q.getQuestionText(), visibilityOf(saved, t, q.getId())));
            }
        }
        return list;
    }

    @Transactional
    public void save(Event event, Map<String, String> params, User actor) {
        List<PublicSummary> saved = summaryRepository.findByEvent(event);
        Map<Long, Question> questions = new HashMap<>();
        for (Question q : event.getQuestions()) questions.put(q.getId(), q);

        for (SummaryOption opt : options(event)) {
            String raw = params.get(opt.getFieldName());
            if (raw == null) continue;
            Visibility vis;
            try { vis = Visibility.valueOf(raw); } catch (IllegalArgumentException e) { continue; }

            PublicSummary row = find(saved, opt.getType(), opt.getQuestionId());
            if (row == null) {
                row = new PublicSummary();
                row.setEvent(event);
                row.setSummaryType(opt.getType());
                row.setQuestion(opt.getQuestionId() == null ? null : questions.get(opt.getQuestionId()));
            }
            row.setVisibility(vis);
            summaryRepository.save(row);
        }
        auditService.log(event, actor, "SUMMARIES_UPDATED", "Public summary settings changed");
    }

    public static boolean isVisibleNow(Visibility v, Event event, LocalDateTime now) {
        return switch (v) {
            case PUBLIC_ALWAYS -> true;
            case PUBLIC_AFTER_DEADLINE -> event.getStatus() == EventStatus.CLOSED || !now.isBefore(event.getDeadlineAt());
            case PRIVATE -> false;
        };
    }

    /** The statistics participants are allowed to see right now. */
    @Transactional(readOnly = true)
    public List<PublicResult> publicResults(Event event) {
        LocalDateTime now = LocalDateTime.now();
        List<PublicSummary> visible = summaryRepository.findByEvent(event).stream()
                .filter(s -> isVisibleNow(s.getVisibility(), event, now))
                .toList();
        if (visible.isEmpty()) return List.of();

        EventStats stats = statisticsService.stats(event);
        Map<Long, QuestionStat> byQuestion = new HashMap<>();
        for (QuestionStat qs : stats.getQuestions()) byQuestion.put(qs.getQuestionId(), qs);

        List<PublicResult> results = new ArrayList<>();
        for (PublicSummary s : visible) {
            switch (s.getSummaryType()) {
                case RESPONSE_COUNT -> results.add(new PublicResult("Total responses", String.valueOf(stats.getSubmitted()), List.of()));
                case SUBMISSION_PERCENTAGE -> results.add(new PublicResult("Submission rate", stats.getPercentage() + "%", List.of()));
                case BY_DEPARTMENT -> results.add(new PublicResult("Responses by department", null, stats.getByDepartment()));
                default -> {
                    if (s.getQuestion() == null) continue;
                    QuestionStat qs = byQuestion.get(s.getQuestion().getId());
                    if (qs == null) continue;
                    String title = s.getQuestion().getQuestionText() + " – " + s.getSummaryType().getLabel();
                    switch (s.getSummaryType()) {
                        case AVERAGE -> results.add(new PublicResult(title, fmt(qs.getAverage()), List.of()));
                        case MINIMUM -> results.add(new PublicResult(title, fmt(qs.getMinimum()), List.of()));
                        case MAXIMUM -> results.add(new PublicResult(title, fmt(qs.getMaximum()), List.of()));
                        default -> results.add(new PublicResult(title, null, qs.getBreakdown()));
                    }
                }
            }
        }
        return results;
    }

    private List<SummaryType> typesFor(QuestionType type) {
        if (type.isNumeric()) return List.of(SummaryType.AVERAGE, SummaryType.MINIMUM, SummaryType.MAXIMUM);
        if (type.isChoice()) return List.of(SummaryType.OPTION_COUNT, SummaryType.OPTION_PERCENTAGE);
        if (type == QuestionType.YES_NO) return List.of(SummaryType.YES_NO_COUNT);
        return List.of();                       // free text is never summarised publicly
    }

    private Visibility visibilityOf(List<PublicSummary> saved, SummaryType t, Long questionId) {
        PublicSummary s = find(saved, t, questionId);
        return s == null ? Visibility.PRIVATE : s.getVisibility();
    }

    private PublicSummary find(List<PublicSummary> saved, SummaryType t, Long questionId) {
        for (PublicSummary s : saved) {
            Long qid = s.getQuestion() == null ? null : s.getQuestion().getId();
            if (s.getSummaryType() == t && Objects.equals(qid, questionId)) return s;
        }
        return null;
    }

    private String fmt(Double d) {
        return d == null ? "No data yet" : String.valueOf(d);
    }
}
