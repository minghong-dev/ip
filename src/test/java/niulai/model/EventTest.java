package niulai.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

/** Tests event date-range matching and validation. */
class EventTest {
    /** Verifies that an event matches every date in its inclusive range. */
    @Test
    void occursOn_dateRange_matchesInclusiveStartAndEnd() {
        Event event = new Event("conference", "2026-08-30", "2026-09-02");

        assertTrue(event.occursOn(LocalDate.of(2026, 8, 30)));
        assertTrue(event.occursOn(LocalDate.of(2026, 9, 1)));
        assertTrue(event.occursOn(LocalDate.of(2026, 9, 2)));
        assertFalse(event.occursOn(LocalDate.of(2026, 8, 29)));
        assertFalse(event.occursOn(LocalDate.of(2026, 9, 3)));
        assertFalse(event.occursOn(null));
    }

    /** Verifies that date-time endpoints are compared using their calendar dates. */
    @Test
    void occursOn_dateTimeRange_matchesCalendarDateRange() {
        Event event = new Event("meeting", "2026-08-31 0900", "2026-08-31 1700");

        assertTrue(event.occursOn(LocalDate.of(2026, 8, 31)));
        assertFalse(event.occursOn(LocalDate.of(2026, 9, 1)));
    }

    /** Verifies that unsupported endpoint values do not produce false date matches. */
    @Test
    void occursOn_freeFormEndpoints_returnsFalse() {
        Event event = new Event("meeting", "tomorrow morning", "tomorrow afternoon");

        assertFalse(event.occursOn(LocalDate.of(2026, 8, 31)));
    }

    /** Verifies that missing event endpoints are rejected. */
    @Test
    void event_blankEndpoint_exceptionThrown() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> new Event("meeting", "", "10am"));
    }
}
