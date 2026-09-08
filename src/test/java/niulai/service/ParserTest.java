package niulai.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import niulai.NiuLaiException;
import niulai.command.AddCommand;
import niulai.command.Command;
import niulai.command.DeleteCommand;
import niulai.command.ExitCommand;
import niulai.command.FindCommand;
import niulai.command.ListCommand;
import niulai.command.MarkCommand;
import niulai.command.UnmarkCommand;
import niulai.model.Deadline;
import niulai.model.Event;
import niulai.model.Task;
import niulai.model.Todo;

/** Tests command parsing, validation, and task construction in {@link Parser}. */
class ParserTest {
    /** The parser under test. */
    private final Parser parser = new Parser();

    /** Verifies that every supported command is dispatched to the right command class. */
    @Test
    void parseCommand_supportedCommands_returnsMatchingCommandTypes() throws NiuLaiException {
        assertInstanceOf(ExitCommand.class, parser.parseCommand("bye", 0));
        assertInstanceOf(ListCommand.class, parser.parseCommand("list", 0));
        assertEquals("undo", parser.parseCommand("undo", 0).getKeyword());
        assertInstanceOf(FindCommand.class, parser.parseCommand("find 2026-08-31", 0));
        assertInstanceOf(FindCommand.class, parser.parseCommand("find book", 0));
        assertInstanceOf(MarkCommand.class, parser.parseCommand("mark 1", 1));
        assertInstanceOf(UnmarkCommand.class, parser.parseCommand("unmark 1", 1));
        assertInstanceOf(DeleteCommand.class, parser.parseCommand("delete 1", 1));
        assertInstanceOf(AddCommand.class, parser.parseCommand("todo read book", 0));
        assertInstanceOf(AddCommand.class,
                parser.parseCommand("deadline submit report /by tomorrow", 0));
        assertInstanceOf(AddCommand.class,
                parser.parseCommand("event meeting /from 10am /to 11am", 0));
    }

    /** Verifies that unknown commands produce a useful parsing error. */
    @Test
    void parseCommand_unknownCommand_exceptionThrown() {
        NiuLaiException exception = assertThrows(NiuLaiException.class,
                () -> parser.parseCommand("archive tasks", 0));
        assertEquals("NOOO!!! I don't recognize that command. Try 'list' to view your tasks.",
                exception.getMessage());
    }

    /** Verifies that a valid ISO date is parsed into the expected date value. */
    @Test
    void parseDateArgument_validDate_returnsLocalDate() throws NiuLaiException {
        assertEquals(LocalDate.of(2026, 8, 31),
                parser.parseDateArgument("find 2026-08-31", Command.Type.FIND));
    }

    /** Verifies that missing and malformed dates are rejected consistently. */
    @Test
    void parseDateArgument_missingOrMalformedDate_exceptionThrown() {
        String expectedMessage =
                "NOOO!!! 'find' needs a date in yyyy-mm-dd format, such as 'find 2026-08-25'.";

        NiuLaiException missing = assertThrows(NiuLaiException.class,
                () -> parser.parseDateArgument("find", Command.Type.FIND));
        assertEquals(expectedMessage, missing.getMessage());

        NiuLaiException malformed = assertThrows(NiuLaiException.class,
                () -> parser.parseDateArgument("find 2026-02-30", Command.Type.FIND));
        assertEquals(expectedMessage, malformed.getMessage());
    }

    /** Verifies that a non-date find argument is accepted as a description keyword. */
    @Test
    void parseCommand_keywordFind_returnsFindCommand() throws NiuLaiException {
        assertInstanceOf(FindCommand.class, parser.parseCommand("find return book", 0));
    }

    /** Verifies that each supported task syntax creates the correct task details. */
    @Test
    void parseTaskCreation_supportedTaskTypes_returnsExpectedTasks() throws NiuLaiException {
        Task todo = parser.parseTaskCreation("todo  read book");
        assertInstanceOf(Todo.class, todo);
        assertEquals("read book", todo.getDescription());

        Task deadline = parser.parseTaskCreation("deadline submit report /by tomorrow");
        assertInstanceOf(Deadline.class, deadline);
        assertEquals("submit report", deadline.getDescription());
        assertEquals("[D][ ] submit report (by: tomorrow)", deadline.toString());

        Task event = parser.parseTaskCreation("event project meeting /from 10am /to 11am");
        assertInstanceOf(Event.class, event);
        assertEquals("project meeting", event.getDescription());
        assertEquals("[E][ ] project meeting (from: 10am to: 11am)", event.toString());
    }

    /** Verifies that unknown task syntax and incomplete task fields are rejected. */
    @Test
    void parseTaskCreation_malformedTask_exceptionThrown() {
        assertThrows(NiuLaiException.class,
                () -> parser.parseTaskCreation("archive old task"));
        assertThrows(NiuLaiException.class,
                () -> parser.parseTaskCreation("todo"));
        assertThrows(NiuLaiException.class,
                () -> parser.parseTaskCreation("deadline report"));
        assertThrows(NiuLaiException.class,
                () -> parser.parseTaskCreation("event meeting /from 10am"));
    }

    /** Verifies that the first task number is converted to the first zero-based index. */
    @Test
    void parseTaskIndex_firstTask_returnsZero() throws NiuLaiException {
        assertEquals(0, parser.parseTaskIndex("mark 1", Command.Type.MARK, 3));
    }

    /** Verifies that a task number in the middle of the list is converted correctly. */
    @Test
    void parseTaskIndex_middleTask_returnsZeroBasedIndex() throws NiuLaiException {
        assertEquals(1, parser.parseTaskIndex("delete 2", Command.Type.DELETE, 3));
    }

    /** Verifies that the last valid task number is accepted. */
    @Test
    void parseTaskIndex_lastTask_returnsLastIndex() throws NiuLaiException {
        assertEquals(2, parser.parseTaskIndex("unmark 3", Command.Type.UNMARK, 3));
    }

    /** Verifies that a missing task number is rejected. */
    @Test
    void parseTaskIndex_missingTaskNumber_exceptionThrown() {
        assertInvalidTaskIndex("mark", Command.Type.MARK, 3,
                "NOOO!!! 'mark' needs a task number, such as 'mark 1'.");
    }

    /** Verifies that a non-numeric task number is rejected. */
    @Test
    void parseTaskIndex_nonNumericTaskNumber_exceptionThrown() {
        assertInvalidTaskIndex("delete abc", Command.Type.DELETE, 3,
                "NOOO!!! Task numbers must be positive whole numbers, such as 'delete 1'.");
    }

    /** Verifies that zero and negative task numbers are rejected. */
    @Test
    void parseTaskIndex_nonPositiveTaskNumber_exceptionThrown() {
        assertInvalidTaskIndex("mark 0", Command.Type.MARK, 3,
                "NOOO!!! Task 0 does not exist. Use 'list' to see your tasks.");
        assertInvalidTaskIndex("mark -1", Command.Type.MARK, 3,
                "NOOO!!! Task -1 does not exist. Use 'list' to see your tasks.");
    }

    /** Verifies that task numbers beyond the available list are rejected. */
    @Test
    void parseTaskIndex_taskNumberBeyondList_exceptionThrown() {
        assertInvalidTaskIndex("unmark 4", Command.Type.UNMARK, 3,
                "NOOO!!! Task 4 does not exist. Use 'list' to see your tasks.");
    }

    /** Verifies that every task number is invalid when the list is empty. */
    @Test
    void parseTaskIndex_emptyTaskList_exceptionThrown() {
        assertInvalidTaskIndex("delete 1", Command.Type.DELETE, 0,
                "NOOO!!! Task 1 does not exist. Use 'list' to see your tasks.");
    }

    /** Asserts that an invalid task number produces the expected user-facing error. */
    private void assertInvalidTaskIndex(String input, Command.Type command, int taskCount,
                                        String expectedMessage) {
        NiuLaiException exception = assertThrows(NiuLaiException.class,
                () -> parser.parseTaskIndex(input, command, taskCount));
        assertEquals(expectedMessage, exception.getMessage());
    }
}
