package niulai.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

/** Tests deadline date parsing, matching, and formatting. */
class DeadlineTest {
    /** Verifies that a date-only deadline matches only its own date. */
    @Test
    void occursOn_dateOnlyDeadline_matchesExactDate() {
        Deadline deadline = new Deadline("submit report", "2026-08-31");

        assertEquals(LocalDate.of(2026, 8, 31), deadline.getByDate());
        assertTrue(deadline.occursOn(LocalDate.of(2026, 8, 31)));
        assertFalse(deadline.occursOn(LocalDate.of(2026, 9, 1)));
        assertFalse(deadline.occursOn(null));
    }

    /** Verifies that date-time deadlines parse correctly and match by calendar date. */
    @Test
    void occursOn_dateTimeDeadline_matchesCalendarDate() {
        Deadline deadline = new Deadline("submit report", "2/12/2019 1800");

        assertEquals(LocalDateTime.of(2019, 12, 2, 18, 0), deadline.getByDateTime());
        assertTrue(deadline.occursOn(LocalDate.of(2019, 12, 2)));
        assertFalse(deadline.occursOn(LocalDate.of(2019, 12, 3)));
    }

    /** Verifies that free-form deadline text remains usable when it is not a supported date. */
    @Test
    void deadline_freeFormText_preservedForDisplayAndStorage() {
        Deadline deadline = new Deadline("call client", "tomorrow morning");

        assertEquals("tomorrow morning", deadline.toString().substring(
                deadline.toString().indexOf("(by: ") + 5, deadline.toString().length() - 1));
        assertEquals("D | 0 | call client | tomorrow morning", deadline.toStorageString());
        assertFalse(deadline.occursOn(LocalDate.of(2026, 8, 31)));
    }

    /** Verifies that a missing deadline value is rejected. */
    @Test
    void deadline_blankValue_exceptionThrown() {
        assertThrows(IllegalArgumentException.class, () -> new Deadline("task", " "));
    }
}
