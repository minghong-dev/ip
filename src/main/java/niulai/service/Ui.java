package niulai.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Scanner;

import niulai.model.Deadline;
import niulai.model.Event;
import niulai.model.Task;
import niulai.model.TaskList;

/**
 * Handles interaction between NiuLai and the user.
 */
public class Ui {
    /** The banner shown when NiuLai starts. */
    private static final String BANNER = "|\\ | | |  | |     /\\  |\n"
            + "| \\| | \\__/ |___ /~~\\ |\n";

    /** The separator printed around command responses. */
    private static final String LINE = "    ____________________________________________________________";

    /** Formats dates used by the find command in chatbot output. */
    private static final DateTimeFormatter DISPLAY_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("MMM dd uuuu", Locale.ENGLISH);

    /** Reads commands from the user's standard input. */
    private final Scanner scanner;

    /** Creates a UI connected to standard input and output. */
    public Ui() {
        scanner = new Scanner(System.in);
    }

    /** Shows the startup banner and greeting. */
    public void showWelcome() {
        System.out.println(BANNER);
        System.out.println(LINE);
        System.out.println("     Hello! I'm NiuLai!");
        System.out.println("     What can I do for you?");
        System.out.println(LINE + "\n");
    }

    /** @return whether another user command is available */
    public boolean hasNextLine() {
        return scanner.hasNextLine();
    }

    /**
     * Reads and trims the next user command.
     *
     * @return the next command without surrounding whitespace
     */
    public String readCommand() {
        return scanner.nextLine().strip();
    }

    /** Shows the separator before a command response. */
    public void showSeparator() {
        System.out.println(LINE);
    }

    /** Shows the farewell message. */
    public void showBye() {
        System.out.println("     Bye. Hope not to see you again.");
        System.out.println(LINE);
    }

    /** Shows every task in display order. */
    public void showList(TaskList tasks) {
        System.out.println("     Here are the tasks in your list:");
        for (int i = 0; i < tasks.size(); i++) {
            System.out.println("     " + (i + 1) + "." + tasks.get(i));
        }
        showSeparatorAndBlankLine();
    }

    /** Shows deadlines and events that occur on a specified date. */
    public void showTasksOnDate(TaskList tasks, LocalDate date) {
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

        showSeparatorAndBlankLine();
    }

    /** Shows tasks whose descriptions contain a keyword, ignoring letter case. */
    public void showTasksContaining(TaskList tasks, String keyword) {
        System.out.println("     Here are the matching tasks in your list:");

        var matchingTasks = tasks.findByDescription(keyword);
        int matches = 0;
        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            if (matchingTasks.contains(task)) {
                System.out.println("     " + (i + 1) + "." + task);
                matches++;
            }
        }

        if (matches == 0) {
            System.out.println("     No matching tasks found.");
        }

        showSeparatorAndBlankLine();
    }

    /** Shows the confirmation for marking a task as done. */
    public void showTaskMarked(Task task) {
        System.out.println("     Nice! I've marked this task as done:");
        System.out.println("       " + task);
        showSeparatorAndBlankLine();
    }

    /** Shows the confirmation for marking a task as not done. */
    public void showTaskUnmarked(Task task) {
        System.out.println("     OK, I've marked this task as not done yet:");
        System.out.println("       " + task);
        showSeparatorAndBlankLine();
    }

    /** Shows the confirmation for deleting a task. */
    public void showTaskDeleted(Task task, int remainingTaskCount) {
        System.out.println("     Noted. I've removed this task:");
        System.out.println("       " + task);
        System.out.println("     Now you have " + remainingTaskCount + " tasks in the list.");
        showSeparatorAndBlankLine();
    }

    /** Shows the confirmation for adding a task. */
    public void showTaskAdded(Task task, int taskCount) {
        System.out.println("     Got it. I've added this task:");
        System.out.println("       " + task);
        System.out.println("     Now you have " + taskCount + " tasks in the list.");
        showSeparatorAndBlankLine();
    }

    /** Shows a command-processing error. */
    public void showError(String message) {
        System.out.println("     " + message);
        showSeparatorAndBlankLine();
    }

    /** Shows the error produced when saved tasks cannot be loaded. */
    public void showLoadingError() {
        showSeparator();
        System.out.println("     NOOO!!! I couldn't load your tasks from disk.");
        showSeparatorAndBlankLine();
    }

    /** Shows a separator followed by a blank line. */
    private void showSeparatorAndBlankLine() {
        System.out.println(LINE + "\n");
    }
}
