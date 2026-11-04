package com.formaccess.repository;

import com.formaccess.entity.Answer;
import com.formaccess.entity.enums.SubmissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Aggregation queries for statistics. Each returns rows of Object[] because they
 * select several values (e.g. avg, min, max, count) at once.
 */
public interface AnswerRepository extends JpaRepository<Answer, Long> {

    /** [avg, min, max, count] of a numeric question. */
    @Query("select avg(a.answerNumber), min(a.answerNumber), max(a.answerNumber), count(a.answerNumber) " +
           "from Answer a where a.question.id = :questionId and a.submission.status = :status")
    List<Object[]> numericStats(@Param("questionId") Long questionId, @Param("status") SubmissionStatus status);

    /** [optionId, count] for a choice question. Options with 0 votes are added in Java. */
    @Query("select o.id, count(a) from Answer a join a.selectedOptions o " +
           "where a.question.id = :questionId and a.submission.status = :status group by o.id")
    List<Object[]> optionCounts(@Param("questionId") Long questionId, @Param("status") SubmissionStatus status);

    /** [answerText, count] – used for YES/NO questions. */
    @Query("select a.answerText, count(a) from Answer a " +
           "where a.question.id = :questionId and a.submission.status = :status group by a.answerText")
    List<Object[]> textCounts(@Param("questionId") Long questionId, @Param("status") SubmissionStatus status);

    /** Number of submissions that answered this question. */
    @Query("select count(a) from Answer a where a.question.id = :questionId and a.submission.status = :status")
    long countAnswers(@Param("questionId") Long questionId, @Param("status") SubmissionStatus status);
}
