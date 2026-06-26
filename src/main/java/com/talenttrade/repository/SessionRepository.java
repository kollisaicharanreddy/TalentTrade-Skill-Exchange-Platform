package com.talenttrade.repository;

import com.talenttrade.entity.Session;
import com.talenttrade.entity.SessionStatus;
import com.talenttrade.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {
    
    boolean existsByExchangeRequestId(Long exchangeRequestId);
    
    Page<Session> findByMentorEmailOrLearnerEmail(String mentorEmail, String learnerEmail, Pageable pageable);

    @Query("SELECT s FROM Session s WHERE (s.mentor.email = :email OR s.learner.email = :email) AND s.status = :status")
    Page<Session> findByUserAndStatus(@Param("email") String email, @Param("status") SessionStatus status, Pageable pageable);

    @Query("SELECT COUNT(s) > 0 FROM Session s WHERE s.status = 'SCHEDULED' AND s.scheduledDate = :date AND " +
           "((s.mentor.id = :userId OR s.learner.id = :userId) AND " +
           "(s.startTime < :endTime AND s.endTime > :startTime))")
    boolean hasTimeConflict(@Param("userId") Long userId, 
                            @Param("date") LocalDate date, 
                            @Param("startTime") LocalTime startTime, 
                            @Param("endTime") LocalTime endTime);

    @Query("SELECT COUNT(s) > 0 FROM Session s WHERE s.id != :excludeSessionId AND s.status = 'SCHEDULED' AND s.scheduledDate = :date AND " +
           "((s.mentor.id = :userId OR s.learner.id = :userId) AND " +
           "(s.startTime < :endTime AND s.endTime > :startTime))")
    boolean hasTimeConflictExcludingSession(@Param("excludeSessionId") Long excludeSessionId,
                                            @Param("userId") Long userId, 
                                            @Param("date") LocalDate date, 
                                            @Param("startTime") LocalTime startTime, 
                                            @Param("endTime") LocalTime endTime);

    @Query("SELECT COUNT(s) FROM Session s WHERE (s.mentor.email = :email OR s.learner.email = :email) AND s.status = :status")
    long countByUserAndStatus(@Param("email") String email, @Param("status") SessionStatus status);

    boolean existsByMentorAndLearnerAndStatus(User mentor, User learner, SessionStatus status);
}
