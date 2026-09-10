package niulai.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

/** Tests strict recognition and preservation of temporal values. */
class TemporalValueTest {
    @Test
    void parse_supportedFullDate_returnsDate() {
        TemporalValue value = TemporalValue.parse("2026-09-10");

        assertEquals(LocalDate.of(2026, 9, 10), value.getDate());
        assertEquals("2026-09-10", value.toString());
    }

    @Test
    void parse_impossibleRecognizableDate_exceptionThrown() {
        assertThrows(IllegalArgumentException.class,
                () -> TemporalValue.parse("2026-02-30"));
        assertThrows(IllegalArgumentException.class,
                () -> TemporalValue.parse("30/2/2026"));
        assertThrows(IllegalArgumentException.class,
                () -> TemporalValue.parse("Feb 30"));
    }

    @Test
    void parse_impossibleRecognizableTime_exceptionThrown() {
        assertThrows(IllegalArgumentException.class, () -> TemporalValue.parse("24:30"));
        assertThrows(IllegalArgumentException.class, () -> TemporalValue.parse("13pm"));
    }

    @Test
    void parse_freeFormValue_preservesTextWithoutComparableDate() {
        TemporalValue value = TemporalValue.parse("tomorrow morning");

        assertEquals("tomorrow morning", value.toString());
        assertNull(value.getDate());
    }
}
