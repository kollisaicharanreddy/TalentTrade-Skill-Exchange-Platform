package com.talenttrade.service;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.*;
import com.talenttrade.entity.CalendarProvider;
import com.talenttrade.entity.Session;
import com.talenttrade.entity.UserGoogleCredential;
import com.talenttrade.repository.UserGoogleCredentialRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleCalendarService implements CalendarService {

    private final UserGoogleCredentialRepository credentialRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Value("${app.timezone:Asia/Kolkata}")
    private String appTimezone;

    @Override
    public Session createCalendarEvent(Session session) {
        // We synchronize based on the organizer/scheduler credentials.
        // Usually, the mentor is considered the organizer hosting the session.
        String organizerEmail = session.getMentor().getEmail();
        Optional<UserGoogleCredential> credentialOpt = credentialRepository.findByUserEmail(organizerEmail);
        
        if (credentialOpt.isEmpty()) {
            // If the organizer hasn't connected Google Calendar, skip integration silently
            log.info("Organizer {} does not have Google Calendar connected. Skipping event creation.", organizerEmail);
            return session;
        }

        UserGoogleCredential credential = credentialOpt.get();
        try {
            Calendar calendarService = getCalendarService(credential);
            Event event = buildGoogleCalendarEvent(session);

            // Request Google Meet creation
            ConferenceSolutionKey conferenceSolutionKey = new ConferenceSolutionKey().setType("hangoutsMeet");
            CreateConferenceRequest createConferenceRequest = new CreateConferenceRequest()
                    .setRequestId(UUID.randomUUID().toString())
                    .setConferenceSolutionKey(conferenceSolutionKey);
            ConferenceData conferenceData = new ConferenceData()
                    .setCreateRequest(createConferenceRequest);
            event.setConferenceData(conferenceData);

            Event createdEvent = calendarService.events().insert("primary", event)
                    .setConferenceDataVersion(1)
                    .setSendUpdates("all") // Automatically send email invitations
                    .execute();

            log.info("Successfully created Google Calendar Event. ID: {}", createdEvent.getId());

            session.setGoogleEventId(createdEvent.getId());
            session.setCalendarProvider(CalendarProvider.GOOGLE);
            session.setMeetingStatus("CONFIRMED");
            session.setLastSynced(LocalDateTime.now());

            // Extract Google Meet Link
            if (createdEvent.getConferenceData() != null && createdEvent.getConferenceData().getEntryPoints() != null) {
                for (EntryPoint entryPoint : createdEvent.getConferenceData().getEntryPoints()) {
                    if ("video".equals(entryPoint.getEntryPointType())) {
                        session.setMeetingLink(entryPoint.getUri());
                        log.info("Google Meet Link Generated: {}", entryPoint.getUri());
                        break;
                    }
                }
            }

        } catch (Exception e) {
            log.error("Failed to create Google Calendar event for session ID: {}", session.getId(), e);
        }

        return session;
    }

    @Override
    public Session updateCalendarEvent(Session session) {
        if (session.getGoogleEventId() == null || session.getCalendarProvider() != CalendarProvider.GOOGLE) {
            return session;
        }

        String organizerEmail = session.getMentor().getEmail();
        Optional<UserGoogleCredential> credentialOpt = credentialRepository.findByUserEmail(organizerEmail);

        if (credentialOpt.isEmpty()) {
            log.warn("Organizer {} credentials not found during calendar update.", organizerEmail);
            return session;
        }

        UserGoogleCredential credential = credentialOpt.get();
        try {
            Calendar calendarService = getCalendarService(credential);
            Event event = buildGoogleCalendarEvent(session);

            Event updatedEvent = calendarService.events().update("primary", session.getGoogleEventId(), event)
                    .setSendUpdates("all")
                    .execute();

            log.info("Successfully updated Google Calendar Event. ID: {}", updatedEvent.getId());
            session.setLastSynced(LocalDateTime.now());
        } catch (Exception e) {
            log.error("Failed to update Google Calendar event for session ID: {}", session.getId(), e);
        }

        return session;
    }

    @Override
    public void deleteCalendarEvent(Session session) {
        if (session.getGoogleEventId() == null || session.getCalendarProvider() != CalendarProvider.GOOGLE) {
            return;
        }

        String organizerEmail = session.getMentor().getEmail();
        Optional<UserGoogleCredential> credentialOpt = credentialRepository.findByUserEmail(organizerEmail);

        if (credentialOpt.isEmpty()) {
            log.warn("Organizer {} credentials not found during calendar deletion.", organizerEmail);
            return;
        }

        UserGoogleCredential credential = credentialOpt.get();
        try {
            Calendar calendarService = getCalendarService(credential);
            calendarService.events().delete("primary", session.getGoogleEventId())
                    .setSendUpdates("all")
                    .execute();
            log.info("Successfully deleted Google Calendar Event. ID: {}", session.getGoogleEventId());
        } catch (Exception e) {
            log.error("Failed to delete Google Calendar event for session ID: {}", session.getId(), e);
        }
    }

    private Calendar getCalendarService(UserGoogleCredential credential) throws GeneralSecurityException, IOException {
        String accessToken = getOrRefreshAccessToken(credential);
        Credential oauthCredential = new Credential(BearerToken.authorizationHeaderAccessMethod())
                .setAccessToken(accessToken);

        return new Calendar.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                oauthCredential)
                .setApplicationName("TalentTrade")
                .build();
    }

    private synchronized String getOrRefreshAccessToken(UserGoogleCredential credential) {
        // If expired or within 1 minute of expiring
        if (credential.getTokenExpiresAt().isBefore(LocalDateTime.now().plusMinutes(1))) {
            log.info("Google Access Token expired/expiring for user ID: {}. Refreshing...", credential.getUser().getId());
            if (credential.getRefreshToken() == null) {
                throw new IllegalStateException("Refresh token is missing. User must reconnect calendar.");
            }

            try {
                String tokenUrl = "https://oauth2.googleapis.com/token";
                Map<String, String> request = new HashMap<>();
                request.put("client_id", clientId);
                request.put("client_secret", clientSecret);
                request.put("refresh_token", credential.getRefreshToken());
                request.put("grant_type", "refresh_token");

                ResponseEntity<Map> responseEntity = restTemplate.postForEntity(tokenUrl, request, Map.class);
                if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
                    Map<String, Object> body = responseEntity.getBody();
                    String newAccessToken = (String) body.get("access_token");
                    Integer expiresIn = (Integer) body.get("expires_in");
                    
                    credential.setAccessToken(newAccessToken);
                    credential.setTokenExpiresAt(LocalDateTime.now().plusSeconds(expiresIn));
                    credentialRepository.save(credential);
                    log.info("Google Access Token successfully refreshed.");
                } else {
                    throw new RuntimeException("Failed to refresh token, response code: " + responseEntity.getStatusCode());
                }
            } catch (Exception e) {
                log.error("Error refreshing Google Access Token", e);
                throw new RuntimeException("Could not refresh Google authorization token.", e);
            }
        }
        return credential.getAccessToken();
    }

    private Event buildGoogleCalendarEvent(Session session) {
        Event event = new Event()
                .setSummary("TalentTrade Skill Exchange: " + (session.getNotes() != null ? session.getNotes() : "Scheduled Session"))
                .setDescription("TalentTrade Virtual Session\n" +
                        "Mentor: " + session.getMentor().getFullName() + "\n" +
                        "Learner: " + session.getLearner().getFullName() + "\n" +
                        "Exchange Notes: " + (session.getNotes() != null ? session.getNotes() : "None"));

        // Setup Event times using configured App Timezone (e.g. Asia/Kolkata)
        ZoneId zoneId = ZoneId.of(appTimezone);
        
        LocalDateTime startDateTime = LocalDateTime.of(session.getScheduledDate(), session.getStartTime());
        LocalDateTime endDateTime = LocalDateTime.of(session.getScheduledDate(), session.getEndTime());

        java.time.ZonedDateTime zonedDateTimeStart = startDateTime.atZone(zoneId);
        java.time.ZonedDateTime zonedDateTimeEnd = endDateTime.atZone(zoneId);

        event.setStart(new EventDateTime()
                .setDateTime(new DateTime(Date.from(zonedDateTimeStart.toInstant())))
                .setTimeZone(appTimezone));
        event.setEnd(new EventDateTime()
                .setDateTime(new DateTime(Date.from(zonedDateTimeEnd.toInstant())))
                .setTimeZone(appTimezone));

        // Setup Attendees
        List<EventAttendee> attendees = new ArrayList<>();
        attendees.add(new EventAttendee().setEmail(session.getMentor().getEmail()).setResponseStatus("accepted"));
        attendees.add(new EventAttendee().setEmail(session.getLearner().getEmail()));
        event.setAttendees(attendees);

        return event;
    }
}
