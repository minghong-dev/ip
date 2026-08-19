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

        Scanner scanner = new Scanner(System.in);

        while (scanner.hasNextLine()) {
            String command = scanner.nextLine();

            System.out.println(line);

            if (command.equals("bye")) {
                System.out.println("     Bye. Hope not to see you again.");
                System.out.println(line);
                break;
            }

            System.out.println("     " + command);
            System.out.println(line + "\n");
        }
    }
}
