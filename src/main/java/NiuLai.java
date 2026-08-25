import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Runs the NiuLai command-line chatbot.
 */
public class NiuLai {
    /** Matches the description and /by field of a deadline command. */
    private static final Pattern DEADLINE_PATTERN =
            Pattern.compile("^(.+?)\\s+/by\\s+(.+)$");

    /** Matches the description, /from field, and /to field of an event command. */
    private static final Pattern EVENT_PATTERN =
            Pattern.compile("^(.+?)\\s+/from\\s+(.+?)\\s+/to\\s+(.+)$");

    public static void main(String[] args) {
        Ui ui = new Ui();
        ui.showWelcome();

        TaskList tasks = loadTasks(ui);

        while (ui.hasNextLine()) {
            String command = ui.readCommand();

            ui.showSeparator();

            try {
                if (Command.BYE.matchesExactly(command)) {
                    ui.showBye();
                    break;
                }

                if (Command.LIST.matchesExactly(command)) {
                    ui.showList(tasks);
                    continue;
                }

                if (Command.FIND.matches(command)) {
                    LocalDate date = getDateArgument(command, Command.FIND);
                    ui.showTasksOnDate(tasks, date);
                    continue;
                }

                if (Command.MARK.matches(command)) {
                    int taskIndex = getTaskIndex(command, Command.MARK, tasks.size());
                    Task task = tasks.get(taskIndex);
                    TaskStatus previousStatus = task.getStatus();
                    task.markAsDone();
                    try {
                        saveTasks(tasks);
                    } catch (NiuLaiException e) {
                        restoreStatus(task, previousStatus);
                        throw e;
                    }
                    ui.showTaskMarked(task);
                    continue;
                }

                if (Command.UNMARK.matches(command)) {
                    int taskIndex = getTaskIndex(command, Command.UNMARK, tasks.size());
                    Task task = tasks.get(taskIndex);
                    TaskStatus previousStatus = task.getStatus();
                    task.markAsNotDone();
                    try {
                        saveTasks(tasks);
                    } catch (NiuLaiException e) {
                        restoreStatus(task, previousStatus);
                        throw e;
                    }
                    ui.showTaskUnmarked(task);
                    continue;
                }

                if (Command.DELETE.matches(command)) {
                    int taskIndex = getTaskIndex(command, Command.DELETE, tasks.size());
                    Task deletedTask = tasks.remove(taskIndex);
                    try {
                        saveTasks(tasks);
                    } catch (NiuLaiException e) {
                        tasks.add(taskIndex, deletedTask);
                        throw e;
                    }
                    ui.showTaskDeleted(deletedTask, tasks.size());
                    continue;
                }

                if (Command.TODO.matches(command)) {
                    String description = getArgument(command, Command.TODO);

                    if (description.isEmpty()) {
                        throw new NiuLaiException(
                                "NOOO!!! A todo needs a description. Try: todo <description>."
                        );
                    }

                    addTaskAndSave(tasks, new Todo(description));
                    ui.showTaskAdded(tasks.get(tasks.size() - 1), tasks.size());
                    continue;
                }

                if (Command.DEADLINE.matches(command)) {
                    String details = getArgument(command, Command.DEADLINE);
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

                    addTaskAndSave(tasks, new Deadline(description, by));
                    ui.showTaskAdded(tasks.get(tasks.size() - 1), tasks.size());
                    continue;
                }

                if (Command.EVENT.matches(command)) {
                    String details = getArgument(command, Command.EVENT);
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

                    addTaskAndSave(tasks, new Event(description, from, to));
                    ui.showTaskAdded(tasks.get(tasks.size() - 1), tasks.size());
                    continue;
                }

                throw new NiuLaiException(
                        "NOOO!!! I don't recognize that command. Try 'list' to view your tasks."
                );
            } catch (NiuLaiException e) {
                ui.showError(e.getMessage());
            }
        }
    }

    /**
     * Returns the text after a command name.
     *
     * @param input the complete user input
     * @param command the command whose argument should be returned
     * @return the trimmed command argument
     */
    private static String getArgument(String input, Command command) {
        return input.substring(command.getKeyword().length()).strip();
    }

    /**
     * Parses the date argument used by the find command.
     *
     * @param input the complete user input
     * @param command the command whose date argument should be returned
     * @return the parsed date
     * @throws NiuLaiException if the date is missing or not in ISO format
     */
    private static LocalDate getDateArgument(String input, Command command)
            throws NiuLaiException {
        String argument = getArgument(input, command);

        if (argument.isEmpty()) {
            throw new NiuLaiException(
                    "NOOO!!! 'find' needs a date in yyyy-mm-dd format, such as 'find 2026-08-25'."
            );
        }

        try {
            return LocalDate.parse(argument, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            throw new NiuLaiException(
                    "NOOO!!! 'find' needs a date in yyyy-mm-dd format, such as 'find 2026-08-25'."
            );
        }
    }

    /**
     * Adds a task and rolls the addition back if saving fails.
     *
     * @param tasks the current task list
     * @param task the task to add
     * @throws NiuLaiException if the updated list cannot be saved
     */
    private static void addTaskAndSave(TaskList tasks, Task task) throws NiuLaiException {
        tasks.add(task);
        try {
            saveTasks(tasks);
        } catch (NiuLaiException e) {
            tasks.remove(tasks.size() - 1);
            throw e;
        }
    }

    /**
     * Restores a task's status after a failed save.
     *
     * @param task the task whose status should be restored
     * @param status the previous status
     */
    private static void restoreStatus(Task task, TaskStatus status) {
        if (status == TaskStatus.COMPLETED) {
            task.markAsDone();
        } else {
            task.markAsNotDone();
        }
    }

    /**
     * Saves the current task list and turns file-system failures into a chatbot error.
     *
     * @param tasks the current task list
     * @throws NiuLaiException if the task list cannot be written to disk
     */
    private static void saveTasks(TaskList tasks) throws NiuLaiException {
        try {
            Storage.save(tasks);
        } catch (IOException | SecurityException e) {
            throw new NiuLaiException("NOOO!!! I couldn't save your tasks to disk.");
        }
    }

    /**
     * Loads the saved task list, starting with an empty list if loading fails.
     *
     * @return the saved task list or an empty list when no usable data is available
     */
    private static TaskList loadTasks(Ui ui) {
        try {
            return Storage.load();
        } catch (IOException | SecurityException e) {
            ui.showLoadingError();
            return new TaskList();
        }
    }

    /**
     * Parses and validates a task number from a mark, unmark, or delete command.
     *
     * @param input the complete user input
     * @param command the command being processed
     * @param taskCount the number of tasks currently in the list
     * @return the zero-based task index
     * @throws NiuLaiException if the task number is missing, invalid, or out of range
     */
    private static int getTaskIndex(String input, Command command, int taskCount)
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
                    "NOOO!!! Task " + taskNumber + " does not exist. Use 'list' to see your tasks."
            );
        }

        return taskNumber - 1;
    }

}
