import java.util.Scanner;

/**
 * Runs the NiuLai command-line chatbot.
 */
public class NiuLai {
    public static void main(String[] args) {
        String banner = "|\\ | | |  | |     /\\  |\n"
                + "| \\| | \\__/ |___ /~~\\ |\n";
        System.out.println(banner);

        String line = "    ____________________________________________________________";
        System.out.println(line);
        System.out.println("     Hello! I'm NiuLai!");
        System.out.println("     What can I do for you?");
        System.out.println(line + "\n");

        Task[] tasks = new Task[100];
        int count = 0;

        Scanner scanner = new Scanner(System.in);

        while (scanner.hasNextLine()) {
            String command = scanner.nextLine().trim();

            System.out.println(line);

            try {
                if (command.equals("bye")) {
                    System.out.println("     Bye. Hope not to see you again.");
                    System.out.println(line);
                    break;
                }

                if (command.equals("list")) {
                    System.out.println("     Here are the tasks in your list:");

                    for (int i = 0; i < count; i++) {
                        System.out.println("     " + (i + 1) + "." + tasks[i]);
                    }

                    System.out.println(line + "\n");
                    continue;
                }

                if (isCommand(command, "mark")) {
                    int taskIndex = getTaskIndex(command, "mark", count);
                    tasks[taskIndex].markAsDone();
                    System.out.println("     Nice! I've marked this task as done:");
                    System.out.println("       " + tasks[taskIndex]);
                    System.out.println(line + "\n");
                    continue;
                }

                if (isCommand(command, "unmark")) {
                    int taskIndex = getTaskIndex(command, "unmark", count);
                    tasks[taskIndex].markAsNotDone();
                    System.out.println("     OK, I've marked this task as not done yet:");
                    System.out.println("       " + tasks[taskIndex]);
                    System.out.println(line + "\n");
                    continue;
                }

                if (isCommand(command, "todo")) {
                    String description = getArgument(command, "todo");
                    if (description.isEmpty()) {
                        throw new NiuLaiException(
                                "NOOO!!! A todo needs a description. Try: todo <description>."
                        );
                    }
                    ensureSpaceForTask(count);
                    tasks[count++] = new Todo(description);
                    printTaskAdded(tasks[count - 1], count, line);
                    continue;
                }

                if (isCommand(command, "deadline")) {
                    String details = getArgument(command, "deadline");
                    int byIndex = details.indexOf(" /by ");

                    if (byIndex <= 0) {
                        throw new NiuLaiException(
                                "NOOO!!! A deadline must look like: deadline <description> /by <date or time>."
                        );
                    }

                    String description = details.substring(0, byIndex).trim();
                    String by = details.substring(byIndex + 5).trim();

                    if (description.isEmpty() || by.isEmpty()) {
                        throw new NiuLaiException(
                                "NOOO!!! A deadline needs both a description and a /by date or time."
                        );
                    }

                    ensureSpaceForTask(count);
                    tasks[count++] = new Deadline(description, by);
                    printTaskAdded(tasks[count - 1], count, line);
                    continue;
                }

                if (isCommand(command, "event")) {
                    String details = getArgument(command, "event");
                    int fromIndex = details.indexOf(" /from ");
                    int toIndex = details.indexOf(" /to ", fromIndex + 7);

                    if (fromIndex <= 0 || toIndex <= fromIndex) {
                        throw new NiuLaiException(
                                "NOOO!!! An event must look like: event <description> /from <start> /to <end>."
                        );
                    }

                    String description = details.substring(0, fromIndex).trim();
                    String from = details.substring(fromIndex + 7, toIndex).trim();
                    String to = details.substring(toIndex + 5).trim();

                    if (description.isEmpty() || from.isEmpty() || to.isEmpty()) {
                        throw new NiuLaiException(
                                "NOOO!!! An event needs a description, a /from time, and a /to time."
                        );
                    }

                    ensureSpaceForTask(count);
                    tasks[count++] = new Event(description, from, to);
                    printTaskAdded(tasks[count - 1], count, line);
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
     * Checks whether an input is a command or starts with a command and its argument.
     *
     * @param command the complete user input
     * @param commandName the command to check
     * @return whether the input belongs to the command
     */
    private static boolean isCommand(String command, String commandName) {
        return command.equals(commandName) || command.startsWith(commandName + " ");
    }

    /**
     * Returns the text after a command name.
     *
     * @param command the complete user input
     * @param commandName the command whose argument should be returned
     * @return the trimmed command argument
     */
    private static String getArgument(String command, String commandName) {
        return command.substring(commandName.length()).trim();
    }

    /**
     * Parses and validates a task number from a mark or unmark command.
     *
     * @param command the complete user input
     * @param commandName the command being processed
     * @param count the number of tasks currently in the list
     * @return the zero-based task index
     * @throws NiuLaiException if the task number is missing, invalid, or out of range
     */
    private static int getTaskIndex(String command, String commandName, int count)
            throws NiuLaiException {
        String argument = getArgument(command, commandName);

        if (argument.isEmpty()) {
            throw new NiuLaiException(
                    "NOOO!!! '" + commandName + "' needs a task number, such as '" + commandName + " 1'."
            );
        }

        int taskNumber;

        try {
            taskNumber = Integer.parseInt(argument);
        } catch (NumberFormatException e) {
            throw new NiuLaiException(
                    "NOOO!!! Task numbers must be positive whole numbers, such as '" + commandName + " 1'."
            );
        }

        if (taskNumber < 1 || taskNumber > count) {
            throw new NiuLaiException(
                    "NOOO!!! Task " + taskNumber + " does not exist. Use 'list' to see your tasks."
            );
        }

        return taskNumber - 1;
    }

    /**
     * Prevents the fixed-size task list from overflowing.
     *
     * @param count the current number of tasks
     * @throws NiuLaiException if no more tasks can be added
     */
    private static void ensureSpaceForTask(int count) throws NiuLaiException {
        if (count >= 100) {
            throw new NiuLaiException("NOOO!!! Your task list is full. Remove a task before adding another.");
        }
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
