package com.talenttrade.repository;

import com.talenttrade.entity.ExchangeRequest;
import com.talenttrade.entity.RequestStatus;
import com.talenttrade.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExchangeRequestRepository extends JpaRepository<ExchangeRequest, Long> {
    List<ExchangeRequest> findBySenderEmailOrReceiverEmail(String senderEmail, String receiverEmail);
    List<ExchangeRequest> findBySenderEmail(String email);
    List<ExchangeRequest> findByReceiverEmail(String email);

    Page<ExchangeRequest> findBySenderEmailOrReceiverEmail(String senderEmail, String receiverEmail, Pageable pageable);
    Page<ExchangeRequest> findBySenderEmail(String email, Pageable pageable);
    Page<ExchangeRequest> findByReceiverEmail(String email, Pageable pageable);
    boolean existsBySenderAndReceiverAndStatus(User sender, User receiver, RequestStatus status);
    boolean existsBySenderAndReceiverAndStatusIn(User sender, User receiver, List<RequestStatus> statuses);
}
