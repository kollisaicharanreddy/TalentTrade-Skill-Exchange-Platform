package com.talenttrade.repository;

import com.talenttrade.entity.ExchangeRequest;
import com.talenttrade.entity.RequestStatus;
import com.talenttrade.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExchangeRequestRepository extends JpaRepository<ExchangeRequest, Long> {
    List<ExchangeRequest> findBySenderEmailOrReceiverEmail(String senderEmail, String receiverEmail);
    List<ExchangeRequest> findBySenderEmail(String email);
    List<ExchangeRequest> findByReceiverEmail(String email);
    boolean existsBySenderAndReceiverAndStatus(User sender, User receiver, RequestStatus status);
}
