package niulai.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

/**
 * Parses and validates commands entered by the user.
 */
public class Parser {
    /** Matches the description and /by field of a deadline command. */
    private static final Pattern DEADLINE_PATTERN =
            Pattern.compile("^(.+?)\\s+/by\\s+(.+)$");

    /** Matches the description, /from field, and /to field of an event command. */
    private static final Pattern EVENT_PATTERN =
            Pattern.compile("^(.+?)\\s+/from\\s+(.+?)\\s+/to\\s+(.+)$");

    /** Parses the date format accepted by the find command. */
    private static final DateTimeFormatter INPUT_DATE_FORMATTER =
            DateTimeFormatter.ISO_LOCAL_DATE;

    /** Recognizes the date-shaped arguments reserved for the date-search variant of find. */
    private static final Pattern DATE_ARGUMENT_PATTERN =
            Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");

    /**
     * Parses complete user input into an executable command.
     *
     * @param input the complete user input
     * @param taskCount the number of tasks currently available
     * @return the executable command represented by the input
     * @throws NiuLaiException if the input is unknown or malformed
     */
    public Command parseCommand(String input, int taskCount) throws NiuLaiException {
        if (Command.Type.BYE.matchesExactly(input)) {
            return new ExitCommand();
        }

        if (Command.Type.LIST.matchesExactly(input)) {
            return new ListCommand();
        }

        if (Command.Type.FIND.matches(input)) {
            return parseFindCommand(input);
        }

        if (Command.Type.MARK.matches(input)
                || Command.Type.UNMARK.matches(input)
                || Command.Type.DELETE.matches(input)) {
            return parseTaskIndexCommand(input, taskCount);
        }

        if (Command.Type.TODO.matches(input)
                || Command.Type.DEADLINE.matches(input)
                || Command.Type.EVENT.matches(input)) {
            return new AddCommand(parseTaskCreation(input));
        }

        throw new NiuLaiException(
                "NOOO!!! I don't recognize that command. Try 'list' to view your tasks."
        );
    }

    /** Parses a find command into a date search or a keyword search. */
    private Command parseFindCommand(String input) throws NiuLaiException {
        String argument = getArgument(input, Command.Type.FIND);
        if (argument.isEmpty()) {
            throw invalidFindArgumentError();
        }
        if (DATE_ARGUMENT_PATTERN.matcher(argument).matches()) {
            return new FindCommand(parseDateArgument(input, Command.Type.FIND));
        }
        return new FindCommand(argument);
    }

    /** Parses a mark, unmark, or delete command. */
    private Command parseTaskIndexCommand(String input, int taskCount) throws NiuLaiException {
        if (Command.Type.MARK.matches(input)) {
            return new MarkCommand(parseTaskIndex(input, Command.Type.MARK, taskCount));
        }

        if (Command.Type.UNMARK.matches(input)) {
            return new UnmarkCommand(parseTaskIndex(input, Command.Type.UNMARK, taskCount));
        }

        return new DeleteCommand(parseTaskIndex(input, Command.Type.DELETE, taskCount));
    }

    /**
     * Parses the date argument of a command.
     *
     * @param input the complete user input
     * @param command the command whose date argument should be parsed
     * @return the parsed date
     * @throws NiuLaiException if the date is missing or invalid
     */
    public LocalDate parseDateArgument(String input, Command.Type command)
            throws NiuLaiException {
        String argument = getArgument(input, command);

        if (argument.isEmpty()) {
            throw invalidDateError();
        }

        try {
            return LocalDate.parse(argument, INPUT_DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw invalidDateError();
        }
    }

    /**
     * Parses a one-based task number into a zero-based task-list index.
     *
     * @param input the complete user input
     * @param command the command containing the task number
     * @param taskCount the number of tasks currently available
     * @return the zero-based task index
     * @throws NiuLaiException if the task number is missing, invalid, or out of range
     */
    public int parseTaskIndex(String input, Command.Type command, int taskCount)
            throws NiuLaiException {
        String argument = getArgument(input, command);

        if (argument.isEmpty()) {
            throw new NiuLaiException(
                    "NOOO!!! '" + command.getKeyword() + "' needs a task number, such as '"
                            + command.getKeyword() + " 1'."
            );
        }

        int taskNumber;
        try {
            taskNumber = Integer.parseInt(argument);
        } catch (NumberFormatException e) {
            throw new NiuLaiException(
                    "NOOO!!! Task numbers must be positive whole numbers, such as '"
                            + command.getKeyword() + " 1'."
            );
        }

        if (taskNumber < 1 || taskNumber > taskCount) {
            throw new NiuLaiException(
                    "NOOO!!! Task " + taskNumber
                            + " does not exist. Use 'list' to see your tasks."
            );
        }

        return taskNumber - 1;
    }

    /**
     * Parses a todo, deadline, or event creation command.
     *
     * @param input the complete user input
     * @return the newly constructed task
     * @throws NiuLaiException if the command's fields are missing or malformed
     */
    public Task parseTaskCreation(String input) throws NiuLaiException {
        if (Command.Type.TODO.matches(input)) {
            return parseTodo(input);
        }

        if (Command.Type.DEADLINE.matches(input)) {
            return parseDeadline(input);
        }

        if (Command.Type.EVENT.matches(input)) {
            return parseEvent(input);
        }

        throw new NiuLaiException(
                "NOOO!!! I don't recognize that command. Try 'list' to view your tasks."
        );
    }

    /** Parses a todo creation command. */
    private Task parseTodo(String input) throws NiuLaiException {
        String description = getArgument(input, Command.Type.TODO);
        if (description.isEmpty()) {
            throw new NiuLaiException(
                    "NOOO!!! A todo needs a description. Try: todo <description>."
            );
        }
        return new Todo(description);
    }

    /** Parses a deadline creation command. */
    private Task parseDeadline(String input) throws NiuLaiException {
        String details = getArgument(input, Command.Type.DEADLINE);
        Matcher matcher = DEADLINE_PATTERN.matcher(details);

        if (!matcher.matches()) {
            throw new NiuLaiException(
                    "NOOO!!! A deadline must look like: deadline <description> /by <date or time>."
            );
        }

        String description = matcher.group(1).strip();
        String by = matcher.group(2).strip();
        if (description.isEmpty() || by.isEmpty()) {
            throw new NiuLaiException(
                    "NOOO!!! A deadline needs both a description and a /by date or time."
            );
        }

        return new Deadline(description, by);
    }

    /** Parses an event creation command. */
    private Task parseEvent(String input) throws NiuLaiException {
        String details = getArgument(input, Command.Type.EVENT);
        Matcher matcher = EVENT_PATTERN.matcher(details);

        if (!matcher.matches()) {
            throw new NiuLaiException(
                    "NOOO!!! An event must look like: event <description> /from <start> /to <end>."
            );
        }

        String description = matcher.group(1).strip();
        String from = matcher.group(2).strip();
        String to = matcher.group(3).strip();
        if (description.isEmpty() || from.isEmpty() || to.isEmpty()) {
            throw new NiuLaiException(
                    "NOOO!!! An event needs a description, a /from time, and a /to time."
            );
        }

        return new Event(description, from, to);
    }

    /** Returns the trimmed text after a command name. */
    private String getArgument(String input, Command.Type command) {
        return input.substring(command.getKeyword().length()).strip();
    }

    /** Creates the standard invalid-date error. */
    private NiuLaiException invalidDateError() {
        return new NiuLaiException(
                "NOOO!!! 'find' needs a date in yyyy-mm-dd format, such as 'find 2026-08-25'."
        );
    }

    /** Creates the standard error for a find command without a search term. */
    private NiuLaiException invalidFindArgumentError() {
        return new NiuLaiException(
                "NOOO!!! 'find' needs a keyword, such as 'find book', or a date in yyyy-mm-dd format."
        );
    }
}
