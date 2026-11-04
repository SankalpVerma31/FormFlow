package com.formaccess.repository;

import com.formaccess.entity.Event;
import com.formaccess.entity.Submission;
import com.formaccess.entity.User;
import com.formaccess.entity.enums.SubmissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    boolean existsByEventAndUserAndStatus(Event event, User user, SubmissionStatus status);

    long countByEventAndStatus(Event event, SubmissionStatus status);

    long countByEvent(Event event);

    List<Submission> findByUserOrderBySubmittedAtDesc(User user);

    List<Submission> findByEventAndStatusOrderBySubmittedAtDesc(Event event, SubmissionStatus status);

    List<Submission> findByEventAndUserOrderBySubmittedAtDesc(Event event, User user);

    /** Highest attempt number so far (0 if none). JPQL works on classes/fields, not tables. */
    @Query("select coalesce(max(s.attemptNo), 0) from Submission s where s.event = :event and s.user = :user")
    int findMaxAttempt(@Param("event") Event event, @Param("user") User user);

    /** IDs of users who have submitted this event. */
    @Query("select distinct s.user.id from Submission s where s.event = :event and s.status = :status")
    List<Long> findSubmittedUserIds(@Param("event") Event event, @Param("status") SubmissionStatus status);
}
