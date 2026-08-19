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

        String[] tasks = new String[100];
        // Stores whether each task in tasks has been marked as done.
        boolean[] completed = new boolean[100];
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
                    String status = completed[i] ? "[X]" : "[ ]";
                    System.out.println("     " + (i + 1) + "." + status + " " + tasks[i]);
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
                        completed[taskIndex] = true;
                        System.out.println("     Nice! I've marked this task as done:");
                        System.out.println("       [X] " + tasks[taskIndex]);
                    }
                } catch (NumberFormatException e) {
                    System.out.println("     Please provide a valid task number.");
                }
                System.out.println(line + "\n");
                continue;
            }

            System.out.println("     added: " + command);
            tasks[count++] = command;
            System.out.println(line + "\n");
        }
    }
}
