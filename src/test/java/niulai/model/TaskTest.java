package niulai.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/** Tests task state changes and the common task representation. */
class TaskTest {
    /** Verifies that a task starts pending and can be marked and unmarked. */
    @Test
    void taskStatus_markAndUnmark_updatesStatusAndIcon() {
        Task task = new Todo("read book");

        assertEquals(TaskStatus.PENDING, task.getStatus());
        assertEquals(" ", task.getStatusIcon());

        task.markAsDone();
        assertEquals(TaskStatus.COMPLETED, task.getStatus());
        assertEquals("X", task.getStatusIcon());

        task.markAsNotDone();
        assertEquals(TaskStatus.PENDING, task.getStatus());
        assertEquals(" ", task.getStatusIcon());
    }

    /** Verifies that descriptions are trimmed and special storage characters are escaped. */
    @Test
    void taskRepresentation_trimmedDescriptionAndEscapedStorage() {
        Task task = new Todo("  read | review \\ draft  ");

        assertEquals("read | review \\ draft", task.getDescription());
        assertEquals("[T][ ] read | review \\ draft", task.toString());
        assertEquals("T | 0 | read \\| review \\\\ draft", task.toStorageString());
    }

    /** Verifies that blank descriptions are rejected. */
    @Test
    void task_blankDescription_exceptionThrown() {
        assertThrows(IllegalArgumentException.class, () -> new Todo("  "));
    }
}
