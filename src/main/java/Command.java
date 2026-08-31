/**
 * Represents an executable command entered in the NiuLai command-line interface.
 */
public abstract class Command {
    /** The command keyword and matching rules used by the parser. */
    private final Type type;

    /** Creates a command backed by the specified command type. */
    protected Command(Type type) {
        this.type = type;
    }

    /**
     * Executes this command against the current application state.
     *
     * @param tasks the current task list
     * @param ui the user-interface component
     * @param storage the task storage component
     * @throws NiuLaiException if the command cannot be completed
     */
    public abstract void execute(TaskList tasks, Ui ui, Storage storage)
            throws NiuLaiException;

    /** @return whether executing this command should end the application */
    public boolean isExit() {
        return false;
    }

    /** @return the command keyword */
    public String getKeyword() {
        return type.getKeyword();
    }

    /** @return whether the input exactly matches this command */
    public boolean matchesExactly(String input) {
        return type.matchesExactly(input);
    }

    /** @return whether the input belongs to this command */
    public boolean matches(String input) {
        return type.matches(input);
    }

    /** Describes the keywords understood by the parser. */
    public enum Type {
        /** Exits the application. */
        BYE("bye"),

        /** Lists all tasks. */
        LIST("list"),

        /** Lists deadlines and events occurring on a date. */
        FIND("find"),

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

        /** Creates a command type with its user-facing keyword. */
        Type(String keyword) {
            this.keyword = keyword;
        }

        /** @return the command keyword */
        public String getKeyword() {
            return keyword;
        }

        /** @return whether the input exactly matches this command */
        public boolean matchesExactly(String input) {
            return input.equals(keyword);
        }

        /** @return whether the input belongs to this command */
        public boolean matches(String input) {
            if (input.equals(keyword)) {
                return true;
            }

            if (!input.startsWith(keyword) || input.length() == keyword.length()) {
                return false;
            }

            char separator = input.charAt(keyword.length());
            return Character.isWhitespace(separator) || Character.isSpaceChar(separator);
        }
    }
}
