import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Scanner;
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

    /** Formats dates used by the find command in chatbot output. */
    private static final DateTimeFormatter DISPLAY_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("MMM dd uuuu", Locale.ENGLISH);

    public static void main(String[] args) {
        String banner = "|\\ | | |  | |     /\\  |\n"
                + "| \\| | \\__/ |___ /~~\\ |\n";
        System.out.println(banner);

        String line = "    ____________________________________________________________";
        System.out.println(line);
        System.out.println("     Hello! I'm NiuLai!");
        System.out.println("     What can I do for you?");
        System.out.println(line + "\n");

        TaskList tasks = loadTasks(line);

        Scanner scanner = new Scanner(System.in);

        while (scanner.hasNextLine()) {
            String command = scanner.nextLine().strip();

            System.out.println(line);

            try {
                if (Command.BYE.matchesExactly(command)) {
                    System.out.println("     Bye. Hope not to see you again.");
                    System.out.println(line);
                    break;
                }

                if (Command.LIST.matchesExactly(command)) {
                    System.out.println("     Here are the tasks in your list:");

                    for (int i = 0; i < tasks.size(); i++) {
                        System.out.println("     " + (i + 1) + "." + tasks.get(i));
                    }

                    System.out.println(line + "\n");
                    continue;
                }

                if (Command.FIND.matches(command)) {
                    LocalDate date = getDateArgument(command, Command.FIND);
                    printTasksOnDate(tasks, date, line);
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
                    System.out.println("     Nice! I've marked this task as done:");
                    System.out.println("       " + task);
                    System.out.println(line + "\n");
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
                    System.out.println("     OK, I've marked this task as not done yet:");
                    System.out.println("       " + task);
                    System.out.println(line + "\n");
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
                    System.out.println("     Noted. I've removed this task:");
                    System.out.println("       " + deletedTask);
                    System.out.println("     Now you have " + tasks.size() + " tasks in the list.");
                    System.out.println(line + "\n");
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
                    printTaskAdded(tasks.get(tasks.size() - 1), tasks.size(), line);
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
                    printTaskAdded(tasks.get(tasks.size() - 1), tasks.size(), line);
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
                    printTaskAdded(tasks.get(tasks.size() - 1), tasks.size(), line);
                    continue;
                }

                throw new NiuLaiException(
                        "NOOO!!! I don't recognize that command. Try 'list' to view your tasks."
                );
            } catch (NiuLaiException e) {
                System.out.println("     " + e.getMessage());
                System.out.println(line + "\n");
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

    /** Prints deadlines and events that occur on a date. */
    private static void printTasksOnDate(TaskList tasks, LocalDate date, String line) {
        System.out.println("     Here are the deadlines and events on "
                + date.format(DISPLAY_DATE_FORMATTER) + ":");

        int matches = 0;
        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            boolean occursOnDate = task instanceof Deadline deadline && deadline.occursOn(date)
                    || task instanceof Event event && event.occursOn(date);

            if (occursOnDate) {
                System.out.println("     " + (i + 1) + "." + task);
                matches++;
            }
        }

        if (matches == 0) {
            System.out.println("     No deadlines or events found.");
        }

        System.out.println(line + "\n");
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
    private static TaskList loadTasks(String line) {
        try {
            return Storage.load();
        } catch (IOException | SecurityException e) {
            System.out.println(line);
            System.out.println("     NOOO!!! I couldn't load your tasks from disk.");
            System.out.println(line + "\n");
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

    /**
     * Prints the confirmation shown after a task has been added.
     *
     * @param task the task that was added
     * @param count the new number of tasks
     * @param line the separator line used by the user interface
     */
    private static void printTaskAdded(Task task, int count, String line) {
        System.out.println("     Got it. I've added this task:");
        System.out.println("       " + task);
        System.out.println("     Now you have " + count + " tasks in the list.");
        System.out.println(line + "\n");
    }
}
