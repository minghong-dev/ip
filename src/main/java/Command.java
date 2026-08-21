/**
 * Represents a command that can be entered in the NiuLai command-line interface.
 */
public enum Command {
    /** Exits the application. */
    BYE("bye"),

    /** Lists all tasks. */
    LIST("list"),

    /** Marks a task as completed. */
    MARK("mark"),

    /** Marks a task as pending. */
    UNMARK("unmark"),

    /** Deletes a task. */
    DELETE("delete"),

    /** Creates a basic todo task. */
    TODO("todo"),

    /** Creates a deadline task. */
    DEADLINE("deadline"),

    /** Creates an event task. */
    EVENT("event");

    private final String keyword;

    /**
     * Creates a command with its user-facing keyword.
     *
     * @param keyword the keyword used to enter the command
     */
    Command(String keyword) {
        this.keyword = keyword;
    }

    /**
     * Returns the keyword used to enter this command.
     *
     * @return the command keyword
     */
    public String getKeyword() {
        return keyword;
    }

    /**
     * Checks whether an input is exactly this command without an argument.
     *
     * @param input the complete user input
     * @return whether the input exactly matches this command
     */
    public boolean matchesExactly(String input) {
        return input.equals(keyword);
    }

    /**
     * Checks whether an input is this command or starts with this command and an argument.
     *
     * @param input the complete user input
     * @return whether the input belongs to this command
     */
    public boolean matches(String input) {
        return input.equals(keyword) || input.startsWith(keyword + " ");
    }
}
