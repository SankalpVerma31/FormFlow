package com.formaccess.statistics;

import com.formaccess.dto.EventStats;
import com.formaccess.dto.LabelCount;
import com.formaccess.dto.QuestionStat;
import com.formaccess.eligibility.EligibilityService;
import com.formaccess.entity.Event;
import com.formaccess.entity.Question;
import com.formaccess.entity.QuestionOption;
import com.formaccess.entity.Submission;
import com.formaccess.entity.enums.QuestionType;
import com.formaccess.entity.enums.SubmissionStatus;
import com.formaccess.repository.AnswerRepository;
import com.formaccess.repository.SubmissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

/**
 * Statistics: the heavy counting is done by SQL (GROUP BY / AVG / MIN / MAX in AnswerRepository),
 * and Java fills in zero counts, percentages and rounding.
 */
@Service
public class StatisticsService {

    private static final SubmissionStatus OK = SubmissionStatus.SUBMITTED;

    private final EligibilityService eligibilityService;
    private final SubmissionRepository submissionRepository;
    private final AnswerRepository answerRepository;

    public StatisticsService(EligibilityService eligibilityService, SubmissionRepository submissionRepository,
                             AnswerRepository answerRepository) {
        this.eligibilityService = eligibilityService;
        this.submissionRepository = submissionRepository;
        this.answerRepository = answerRepository;
    }

    @Transactional(readOnly = true)
    public EventStats stats(Event event) {
        List<Submission> submissions = submissionRepository.findByEventAndStatusOrderBySubmittedAtDesc(event, OK);
        List<QuestionStat> questionStats = new ArrayList<>();
        List<Question> questions = new ArrayList<>(event.getQuestions());
        questions.sort(Comparator.comparingInt(Question::getDisplayOrder));
        for (Question q : questions) questionStats.add(questionStat(q));
        return new EventStats(eligibilityService.countEligible(event), submissions.size(),
                timeline(submissions), byDepartment(submissions), questionStats);
    }

    public QuestionStat questionStat(Question q) {
        QuestionStat s = new QuestionStat();
        s.setQuestionId(q.getId());
        s.setQuestionText(q.getQuestionText());
        s.setType(q.getQuestionType().name());
        long responses = answerRepository.countAnswers(q.getId(), OK);
        s.setResponses(responses);

        if (q.getQuestionType().isNumeric()) {
            List<Object[]> rows = answerRepository.numericStats(q.getId(), OK);
            if (!rows.isEmpty() && rows.get(0)[3] != null && ((Number) rows.get(0)[3]).longValue() > 0) {
                Object[] r = rows.get(0);
                s.setAverage(round(((Number) r[0]).doubleValue()));
                s.setMinimum(round(((Number) r[1]).doubleValue()));
                s.setMaximum(round(((Number) r[2]).doubleValue()));
            }
        } else if (q.getQuestionType().isChoice()) {
            Map<Long, Long> counts = new HashMap<>();
            for (Object[] r : answerRepository.optionCounts(q.getId(), OK)) {
                counts.put(((Number) r[0]).longValue(), ((Number) r[1]).longValue());
            }
            List<LabelCount> breakdown = new ArrayList<>();
            for (QuestionOption o : q.getOptions()) {            // every option, even with 0 votes
                long c = counts.getOrDefault(o.getId(), 0L);
                breakdown.add(new LabelCount(o.getOptionText(), c, percent(c, responses)));
            }
            s.setBreakdown(breakdown);
        } else if (q.getQuestionType() == QuestionType.YES_NO) {
            long yes = 0, no = 0;
            for (Object[] r : answerRepository.textCounts(q.getId(), OK)) {
                if ("YES".equals(r[0])) yes = ((Number) r[1]).longValue();
                if ("NO".equals(r[0])) no = ((Number) r[1]).longValue();
            }
            s.setBreakdown(List.of(new LabelCount("YES", yes, percent(yes, responses)),
                                   new LabelCount("NO", no, percent(no, responses))));
        }
        return s;
    }

    /** Submissions per day, oldest first. */
    private List<LabelCount> timeline(List<Submission> submissions) {
        TreeMap<LocalDate, Long> perDay = new TreeMap<>();
        for (Submission s : submissions) perDay.merge(s.getSubmittedAt().toLocalDate(), 1L, Long::sum);
        List<LabelCount> list = new ArrayList<>();
        perDay.forEach((day, count) -> list.add(new LabelCount(day.toString(), count, percent(count, submissions.size()))));
        return list;
    }

    private List<LabelCount> byDepartment(List<Submission> submissions) {
        TreeMap<String, Long> perDept = new TreeMap<>();
        for (Submission s : submissions) {
            String code = s.getUser().getDepartment() == null ? "None" : s.getUser().getDepartment().getCode();
            perDept.merge(code, 1L, Long::sum);
        }
        List<LabelCount> list = new ArrayList<>();
        perDept.forEach((dept, count) -> list.add(new LabelCount(dept, count, percent(count, submissions.size()))));
        return list;
    }

    private static double percent(long part, long total) {
        return total == 0 ? 0 : round(part * 100.0 / total);
    }

    private static double round(double v) {
        return Math.round(v * 100) / 100.0;
    }
}
