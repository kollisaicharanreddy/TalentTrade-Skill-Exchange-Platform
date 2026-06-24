package com.talenttrade.service;

import com.talenttrade.dto.ExchangeRequestDTO;
import com.talenttrade.dto.ExchangeRequestResponseDTO;
import com.talenttrade.dto.UserResponse;
import com.talenttrade.entity.ExchangeRequest;
import com.talenttrade.entity.RequestStatus;
import com.talenttrade.entity.User;
import com.talenttrade.exception.InvalidRequestException;
import com.talenttrade.exception.RequestAlreadyExistsException;
import com.talenttrade.exception.ResourceNotFoundException;
import com.talenttrade.exception.UnauthorizedException;
import com.talenttrade.repository.ExchangeRequestRepository;
import com.talenttrade.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExchangeRequestService {

    private final ExchangeRequestRepository exchangeRequestRepository;
    private final UserRepository userRepository;

    @Transactional
    public ExchangeRequestResponseDTO createRequest(String senderEmail, ExchangeRequestDTO requestDTO) {
        log.info("User {} is attempting to send exchange request to user ID: {}", senderEmail, requestDTO.getReceiverId());

        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Sender user not found: " + senderEmail));

        User receiver = userRepository.findById(requestDTO.getReceiverId())
                .orElseThrow(() -> new ResourceNotFoundException("Receiver user not found with ID: " + requestDTO.getReceiverId()));

        if (sender.getId().equals(receiver.getId())) {
            log.warn("User {} attempted to send exchange request to themselves", senderEmail);
            throw new InvalidRequestException("Cannot send exchange request to yourself");
        }

        // Prevent duplicate pending requests
        boolean pendingExists = exchangeRequestRepository.existsBySenderAndReceiverAndStatus(sender, receiver, RequestStatus.PENDING);
        if (pendingExists) {
            log.warn("Exchange request creation failed - pending request already exists from {} to {}", sender.getId(), receiver.getId());
            throw new RequestAlreadyExistsException("A pending exchange request already exists between you and this user");
        }

        ExchangeRequest request = ExchangeRequest.builder()
                .sender(sender)
                .receiver(receiver)
                .message(requestDTO.getMessage())
                .status(RequestStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        ExchangeRequest savedRequest = exchangeRequestRepository.save(request);
        log.info("Request sent successfully: ID={}, sender={}, receiver={}, status=PENDING", 
                savedRequest.getId(), sender.getEmail(), receiver.getEmail());

        return mapToResponseDTO(savedRequest);
    }

    @Transactional(readOnly = true)
    public List<ExchangeRequestResponseDTO> getAllRequests(String email) {
        log.debug("Fetching all requests involving user: {}", email);
        return exchangeRequestRepository.findBySenderEmailOrReceiverEmail(email, email).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ExchangeRequestResponseDTO> getSentRequests(String email) {
        log.debug("Fetching sent requests for user: {}", email);
        return exchangeRequestRepository.findBySenderEmail(email).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ExchangeRequestResponseDTO> getReceivedRequests(String email) {
        log.debug("Fetching received requests for user: {}", email);
        return exchangeRequestRepository.findByReceiverEmail(email).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ExchangeRequestResponseDTO acceptRequest(Long requestId, String email) {
        log.info("User {} is attempting to accept request ID: {}", email, requestId);

        ExchangeRequest request = exchangeRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Exchange request not found with ID: " + requestId));

        if (!request.getReceiver().getEmail().equals(email)) {
            log.warn("Unauthorized attempt to accept request ID: {} by user: {}", requestId, email);
            throw new UnauthorizedException("Only the receiver can accept the exchange request");
        }

        if (request.getStatus() != RequestStatus.PENDING) {
            log.warn("Request ID: {} cannot be accepted, current status is {}", requestId, request.getStatus());
            throw new InvalidRequestException("Request cannot be accepted with status: " + request.getStatus());
        }

        request.setStatus(RequestStatus.ACCEPTED);
        ExchangeRequest updatedRequest = exchangeRequestRepository.save(request);
        log.info("Request accepted successfully: ID={}", requestId);

        return mapToResponseDTO(updatedRequest);
    }

    @Transactional
    public ExchangeRequestResponseDTO rejectRequest(Long requestId, String email) {
        log.info("User {} is attempting to reject request ID: {}", email, requestId);

        ExchangeRequest request = exchangeRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Exchange request not found with ID: " + requestId));

        if (!request.getReceiver().getEmail().equals(email)) {
            log.warn("Unauthorized attempt to reject request ID: {} by user: {}", requestId, email);
            throw new UnauthorizedException("Only the receiver can reject the exchange request");
        }

        if (request.getStatus() != RequestStatus.PENDING) {
            log.warn("Request ID: {} cannot be rejected, current status is {}", requestId, request.getStatus());
            throw new InvalidRequestException("Request cannot be rejected with status: " + request.getStatus());
        }

        request.setStatus(RequestStatus.REJECTED);
        ExchangeRequest updatedRequest = exchangeRequestRepository.save(request);
        log.info("Request rejected successfully: ID={}", requestId);

        return mapToResponseDTO(updatedRequest);
    }

    private ExchangeRequestResponseDTO mapToResponseDTO(ExchangeRequest request) {
        return ExchangeRequestResponseDTO.builder()
                .id(request.getId())
                .sender(mapToUserResponse(request.getSender()))
                .receiver(mapToUserResponse(request.getReceiver()))
                .message(request.getMessage())
                .status(request.getStatus())
                .createdAt(request.getCreatedAt())
                .build();
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .username(user.getUsernameValue())
                .email(user.getEmail())
                .bio(user.getBio())
                .location(user.getLocation())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
