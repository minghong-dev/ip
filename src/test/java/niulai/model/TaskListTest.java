package niulai.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

/** Tests task-list operations that support task searching. */
class TaskListTest {
    /** Verifies that description searches are case-insensitive and preserve list order. */
    @Test
    void findByDescription_keywordMatchesInOriginalOrder() {
        Task firstMatch = new Todo("Read a book");
        Task nonMatch = new Todo("Buy groceries");
        Task secondMatch = new Deadline("Return book", "June 6th");
        TaskList tasks = new TaskList(firstMatch, nonMatch, secondMatch);

        List<Task> matches = tasks.findByDescription("BOOK");

        assertEquals(2, matches.size());
        assertSame(firstMatch, matches.get(0));
        assertSame(secondMatch, matches.get(1));
    }

    /** Verifies that a keyword with no matches produces an empty result. */
    @Test
    void findByDescription_noMatch_returnsEmptyList() {
        TaskList tasks = new TaskList(new Todo("Read a book"));

        assertEquals(List.of(), tasks.findByDescription("holiday"));
    }

    /** Verifies that case and spacing variants of one task are duplicates. */
    @Test
    void add_sameNormalizedIdentity_exceptionThrownWithoutMutation() {
        Task first = new Todo("Read   Book");
        TaskList tasks = new TaskList(first);

        assertThrows(DuplicateTaskException.class,
                () -> tasks.add(new Todo("  read book  ")));
        assertEquals(1, tasks.size());
        assertSame(first, tasks.get(0));
    }

    /** Verifies that task type and temporal details remain part of identity. */
    @Test
    void add_differentTypeOrDetails_tasksRemainDistinct() {
        TaskList tasks = new TaskList(new Todo("read book"));

        tasks.add(new Deadline("read book", "tomorrow"));
        tasks.add(new Deadline("read book", "Friday"));

        assertEquals(3, tasks.size());
    }
}
