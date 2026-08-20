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
            String command = scanner.nextLine();

            System.out.println(line);

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

            if (command.startsWith("mark ")) {
                try {
                    int taskNumber = Integer.parseInt(command.substring(5).trim());
                    int taskIndex = taskNumber - 1;

                    if (taskIndex < 0 || taskIndex >= count) {
                        System.out.println("     That task number does not exist.");
                    } else {
                        tasks[taskIndex].markAsDone();
                        System.out.println("     Nice! I've marked this task as done:");
                        System.out.println("       " + tasks[taskIndex]);
                    }
                } catch (NumberFormatException e) {
                    System.out.println("     Please provide a valid task number.");
                }
                System.out.println(line + "\n");
                continue;
            }

            if (command.startsWith("unmark ")) {
                try {
                    int taskNumber = Integer.parseInt(command.substring(7).trim());
                    int taskIndex = taskNumber - 1;

                    if (taskIndex < 0 || taskIndex >= count) {
                        System.out.println("     That task number does not exist.");
                    } else {
                        tasks[taskIndex].markAsNotDone();
                        System.out.println("     OK, I've marked this task as not done yet:");
                        System.out.println("       " + tasks[taskIndex]);
                    }
                } catch (NumberFormatException e) {
                    System.out.println("     Please provide a valid task number.");
                }
                System.out.println(line + "\n");
                continue;
            }

            if (command.startsWith("todo ")) {
                String description = command.substring(5).trim();
                if (!description.isEmpty()) {
                    tasks[count++] = new Todo(description);
                    printTaskAdded(tasks[count - 1], count, line);
                }
                continue;
            }

            if (command.startsWith("deadline ")) {
                String details = command.substring(9).trim();
                int byIndex = details.indexOf(" /by ");

                if (byIndex > 0) {
                    String description = details.substring(0, byIndex).trim();
                    String by = details.substring(byIndex + 5).trim();
                    
                    if (!description.isEmpty() && !by.isEmpty()) {
                        tasks[count++] = new Deadline(description, by);
                        printTaskAdded(tasks[count - 1], count, line);
                        continue;
                    }
                }
            }

            if (command.startsWith("event ")) {
                String details = command.substring(6).trim();
                int fromIndex = details.indexOf(" /from ");
                int toIndex = details.indexOf(" /to ", fromIndex + 7);

                if (fromIndex > 0 && toIndex > fromIndex) {
                    String description = details.substring(0, fromIndex).trim();
                    String from = details.substring(fromIndex + 7, toIndex).trim();
                    String to = details.substring(toIndex + 5).trim();
                    if (!description.isEmpty() && !from.isEmpty() && !to.isEmpty()) {
                        tasks[count++] = new Event(description, from, to);
                        printTaskAdded(tasks[count - 1], count, line);
                        continue;
                    }
                }
            }

            System.out.println("     I don't understand that command.");
            System.out.println(line + "\n");
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
