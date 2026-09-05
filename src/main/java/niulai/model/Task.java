package niulai.model;

/**
 * Represents a task in the NiuLai task list.
 */
public class Task {
    /** The text describing the task. */
    private final String description;

    /** The completion state of the task. */
    private TaskStatus status;

    /**
     * Creates a new incomplete task.
     *
     * @param description the text describing the task
     */
    public Task(String description) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Task description cannot be blank.");
        }

        this.description = description.strip();
        this.status = TaskStatus.PENDING;
    }

    /**
     * Returns the status icon used when displaying this task.
     *
     * @return {@code X} if the task is completed, otherwise a blank space
     */
    public String getStatusIcon() {
        return status == TaskStatus.COMPLETED ? "X" : " ";
    }

    /** Marks this task as done. */
    public void markAsDone() {
        status = TaskStatus.COMPLETED;
    }

    /** Marks this task as not done. */
    public void markAsNotDone() {
        status = TaskStatus.PENDING;
    }

    /**
     * Returns the completion state of this task.
     *
     * @return the task status
     */
    public TaskStatus getStatus() {
        return status;
    }

    /**
     * Returns the task description.
     *
     * @return the task description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the one-letter type marker used when displaying this task.
     *
     * @return the task type marker
     */
    public String getTypeIcon() {
        return getType().getIcon();
    }

    /** @return the finite type represented by this task */
    public TaskType getType() {
        return TaskType.TODO;
    }

    /**
     * Returns the line used to save this task to disk.
     *
     * @return the task type, completion state, and description
     */
    public String toStorageString() {
        return getTypeIcon() + " | " + status.getStorageValue() + " | "
                + escapeStorageField(description);
    }

    /**
     * Escapes characters that have a special meaning in the storage format.
     *
     * @param value the field to escape
     * @return the escaped field
     */
    protected static String escapeStorageField(String value) {
        return value.replace("\\", "\\\\").replace("|", "\\|");
    }

    /**
     * Returns the display representation of this task.
     *
     * @return the status icon and task description
     */
    @Override
    public String toString() {
        return "[" + getTypeIcon() + "][" + getStatusIcon() + "] " + description;
    }

    /** Represents the task types supported by the storage and display formats. */
    public enum TaskType {
        /** A task without an attached date or time. */
        TODO("T"),

        /** A task with a completion deadline. */
        DEADLINE("D"),

        /** A task with a start and end time. */
        EVENT("E");

        /** The marker used to persist and display this task type. */
        private final String icon;

        /** Creates a task type with its storage and display marker. */
        TaskType(String icon) {
            this.icon = icon;
        }

        /** @return the marker used to persist and display this task type */
        public String getIcon() {
            return icon;
        }

        /**
         * Converts a persisted type marker into a task type.
         *
         * @param icon the persisted task-type marker
         * @return the corresponding task type
         * @throws IllegalArgumentException if the marker is not supported
         */
        public static TaskType fromIcon(String icon) {
            for (TaskType type : values()) {
                if (type.icon.equals(icon)) {
                    return type;
                }
            }

            throw new IllegalArgumentException("Unknown task type icon: " + icon);
        }
    }
}
