import java.util.ArrayList;
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

        ArrayList<Task> tasks = new ArrayList<>();

        Scanner scanner = new Scanner(System.in);

        while (scanner.hasNextLine()) {
            String command = scanner.nextLine().trim();

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

                if (Command.MARK.matches(command)) {
                    int taskIndex = getTaskIndex(command, Command.MARK, tasks.size());
                    tasks.get(taskIndex).markAsDone();
                    System.out.println("     Nice! I've marked this task as done:");
                    System.out.println("       " + tasks.get(taskIndex));
                    System.out.println(line + "\n");
                    continue;
                }

                if (Command.UNMARK.matches(command)) {
                    int taskIndex = getTaskIndex(command, Command.UNMARK, tasks.size());
                    tasks.get(taskIndex).markAsNotDone();
                    System.out.println("     OK, I've marked this task as not done yet:");
                    System.out.println("       " + tasks.get(taskIndex));
                    System.out.println(line + "\n");
                    continue;
                }

                if (Command.DELETE.matches(command)) {
                    int taskIndex = getTaskIndex(command, Command.DELETE, tasks.size());
                    Task deletedTask = tasks.remove(taskIndex);
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

                    tasks.add(new Todo(description));
                    printTaskAdded(tasks.get(tasks.size() - 1), tasks.size(), line);
                    continue;
                }

                if (Command.DEADLINE.matches(command)) {
                    String details = getArgument(command, Command.DEADLINE);
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

                    tasks.add(new Deadline(description, by));
                    printTaskAdded(tasks.get(tasks.size() - 1), tasks.size(), line);
                    continue;
                }

                if (Command.EVENT.matches(command)) {
                    String details = getArgument(command, Command.EVENT);
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

                    tasks.add(new Event(description, from, to));
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
        return input.substring(command.getKeyword().length()).trim();
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
