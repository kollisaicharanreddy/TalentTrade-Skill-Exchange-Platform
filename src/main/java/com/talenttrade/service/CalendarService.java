package com.talenttrade.service;

import com.talenttrade.entity.Session;

public interface CalendarService {
    /**
     * Creates a calendar event for the scheduled session and sets Google Meet link on the session.
     * @param session the scheduled session
     * @return updated session details containing meeting link and event credentials
     */
    Session createCalendarEvent(Session session);

    /**
     * Updates an existing calendar event to keep it synchronized.
     * @param session the updated session
     * @return updated session details
     */
    Session updateCalendarEvent(Session session);

    /**
     * Deletes / cancels an event from the calendar provider.
     * @param session the session to delete
     */
    void deleteCalendarEvent(Session session);
}
