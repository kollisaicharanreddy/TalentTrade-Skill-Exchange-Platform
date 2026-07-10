package com.talenttrade.service;

import com.talenttrade.dto.ExchangeRequestDTO;
import com.talenttrade.dto.ExchangeRequestResponseDTO;
import com.talenttrade.entity.*;
import com.talenttrade.exception.InvalidRequestException;
import com.talenttrade.exception.RequestAlreadyExistsException;
import com.talenttrade.exception.UnauthorizedException;
import com.talenttrade.repository.ExchangeRequestRepository;
import com.talenttrade.repository.UserRepository;
import com.talenttrade.util.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExchangeRequestServiceTest {

        @Mock
        private ExchangeRequestRepository exchangeRequestRepository;

        @Mock
        private UserRepository userRepository;

        @Mock
        private NotificationService notificationService;

        @InjectMocks
        private ExchangeRequestService exchangeRequestService;

        @Test
        @DisplayName("Test: Successfully send exchange request\n" +
                        "Why: Verifies request state is saved in PENDING status and target user is notified.\n" +
                        "Expected: Request saved and response mapping contains receiver and PENDING status.")
        void createRequest_success() {
                User sender = TestDataFactory.createUser(1L, "sender@example.com", "sender", Role.USER);
                User receiver = TestDataFactory.createUser(2L, "receiver@example.com", "receiver", Role.USER);
                ExchangeRequestDTO dto = ExchangeRequestDTO.builder()
                                .receiverId(2L)
                                .message("Let's swap java for python!")
                                .build();

                ExchangeRequest savedRequest = TestDataFactory.createExchangeRequest(1L, sender, receiver,
                                RequestStatus.PENDING);

                when(userRepository.findByEmail("sender@example.com")).thenReturn(Optional.of(sender));
                when(userRepository.findById(2L)).thenReturn(Optional.of(receiver));
                when(exchangeRequestRepository.existsBySenderAndReceiverAndStatusIn(any(), any(), any()))
                                .thenReturn(false);
                when(exchangeRequestRepository.save(any(ExchangeRequest.class))).thenReturn(savedRequest);

                ExchangeRequestResponseDTO response = exchangeRequestService.createRequest("sender@example.com", dto);

                assertNotNull(response);
                assertEquals(RequestStatus.PENDING, response.getStatus());
                verify(notificationService).createNotification(eq(receiver), any(), any(),
                                eq(NotificationType.REQUEST_RECEIVED));
        }

        @Test
        @DisplayName("Test: Send exchange request fails if sender attempts to request themselves\n" +
                        "Why: Business rule checks to avoid self-requests.\n" +
                        "Expected: InvalidRequestException is thrown.")
        void createRequest_fail_selfRequest() {
                User sender = TestDataFactory.createUser(1L, "sender@example.com", "sender", Role.USER);
                ExchangeRequestDTO dto = ExchangeRequestDTO.builder()
                                .receiverId(1L)
                                .message("Self message")
                                .build();

                when(userRepository.findByEmail("sender@example.com")).thenReturn(Optional.of(sender));
                when(userRepository.findById(1L)).thenReturn(Optional.of(sender));

                assertThrows(InvalidRequestException.class,
                                () -> exchangeRequestService.createRequest("sender@example.com", dto));
                verify(exchangeRequestRepository, never()).save(any());
        }

        @Test
        @DisplayName("Test: Send exchange request fails if an active request already exists\n" +
                        "Why: Enforces that redundant duplicate connection requests cannot clog the database.\n" +
                        "Expected: RequestAlreadyExistsException is thrown.")
        void createRequest_fail_duplicateRequest() {
                User sender = TestDataFactory.createUser(1L, "sender@example.com", "sender", Role.USER);
                User receiver = TestDataFactory.createUser(2L, "receiver@example.com", "receiver", Role.USER);
                ExchangeRequestDTO dto = ExchangeRequestDTO.builder()
                                .receiverId(2L)
                                .message("Another request")
                                .build();

                when(userRepository.findByEmail("sender@example.com")).thenReturn(Optional.of(sender));
                when(userRepository.findById(2L)).thenReturn(Optional.of(receiver));
                // Simulate pre-existing request in active status
                when(exchangeRequestRepository.existsBySenderAndReceiverAndStatusIn(eq(sender), eq(receiver),
                                anyList()))
                                .thenReturn(true);

                assertThrows(RequestAlreadyExistsException.class,
                                () -> exchangeRequestService.createRequest("sender@example.com", dto));
                verify(exchangeRequestRepository, never()).save(any());
        }

        @Test
        @DisplayName("Test: Accept exchange request success\n" +
                        "Why: Verifies that status updates to ACCEPTED and sender is notified.\n" +
                        "Expected: Status is ACCEPTED.")
        void acceptRequest_success() {
                User sender = TestDataFactory.createUser(1L, "sender@example.com", "sender", Role.USER);
                User receiver = TestDataFactory.createUser(2L, "receiver@example.com", "receiver", Role.USER);
                ExchangeRequest request = TestDataFactory.createExchangeRequest(1L, sender, receiver,
                                RequestStatus.PENDING);

                when(exchangeRequestRepository.findById(1L)).thenReturn(Optional.of(request));
                when(exchangeRequestRepository.save(any(ExchangeRequest.class))).thenAnswer(inv -> inv.getArgument(0));

                ExchangeRequestResponseDTO response = exchangeRequestService.acceptRequest(1L, "receiver@example.com");

                assertNotNull(response);
                assertEquals(RequestStatus.ACCEPTED, response.getStatus());
                verify(notificationService).createNotification(eq(sender), any(), any(),
                                eq(NotificationType.REQUEST_ACCEPTED));
        }

        @Test
        @DisplayName("Test: Accept exchange request fails if unauthorized user attempts to accept\n" +
                        "Why: Ensures only the request recipient can accept the exchange request.\n" +
                        "Expected: UnauthorizedException is thrown.")
        void acceptRequest_fail_unauthorized() {
                User sender = TestDataFactory.createUser(1L, "sender@example.com", "sender", Role.USER);
                User receiver = TestDataFactory.createUser(2L, "receiver@example.com", "receiver", Role.USER);
                ExchangeRequest request = TestDataFactory.createExchangeRequest(1L, sender, receiver,
                                RequestStatus.PENDING);

                when(exchangeRequestRepository.findById(1L)).thenReturn(Optional.of(request));

                assertThrows(UnauthorizedException.class,
                                () -> exchangeRequestService.acceptRequest(1L, "sender@example.com"));
        }
}
