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

        String[] commands = new String[100];
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
                for (int i = 0; i < count; i++) {
                    System.out.println("     " + (i + 1) + ". " + commands[i]);
                }
                System.out.println(line + "\n");
                continue;
            }

            System.out.println("     added: " + command);
            commands[count++] = command;
            System.out.println(line + "\n");
        }
    }
}
