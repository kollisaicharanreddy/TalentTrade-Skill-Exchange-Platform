package com.talenttrade.integration;

import com.talenttrade.BaseIntegrationTest;
import com.talenttrade.dto.ExchangeRequestDTO;
import com.talenttrade.dto.ExchangeRequestResponseDTO;
import com.talenttrade.entity.*;
import com.talenttrade.repository.ExchangeRequestRepository;
import com.talenttrade.repository.UserRepository;
import com.talenttrade.service.ExchangeRequestService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

public class ExchangeRequestWorkflowIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ExchangeRequestService exchangeRequestService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExchangeRequestRepository exchangeRequestRepository;

    @Test
    @DisplayName("Workflow 4 Integration: Send Exchange Request -> Accept Request -> Verify Status")
    void sendAcceptExchangeRequestWorkflow() {
        // 1. Create Users
        User sender = User.builder()
                .fullName("Sender User")
                .email("sender@example.com")
                .username("sender_user")
                .password("password")
                .role(Role.USER)
                .emailVerified(true)
                .enabled(true)
                .build();
        sender = userRepository.save(sender);

        User receiver = User.builder()
                .fullName("Receiver User")
                .email("receiver@example.com")
                .username("receiver_user")
                .password("password")
                .role(Role.USER)
                .emailVerified(true)
                .enabled(true)
                .build();
        receiver = userRepository.save(receiver);

        // 2. Send request
        ExchangeRequestDTO requestDTO = ExchangeRequestDTO.builder()
                .receiverId(receiver.getId())
                .message("Hello! Let's swap skills.")
                .build();

        ExchangeRequestResponseDTO response = exchangeRequestService.createRequest("sender@example.com", requestDTO);
        assertNotNull(response);
        assertEquals(RequestStatus.PENDING, response.getStatus());

        // 3. Accept request
        ExchangeRequestResponseDTO accepted = exchangeRequestService.acceptRequest(response.getId(), "receiver@example.com");
        assertNotNull(accepted);
        assertEquals(RequestStatus.ACCEPTED, accepted.getStatus());

        // 4. Verify stored status
        ExchangeRequest stored = exchangeRequestRepository.findById(response.getId()).orElseThrow();
        assertEquals(RequestStatus.ACCEPTED, stored.getStatus());
    }
}
