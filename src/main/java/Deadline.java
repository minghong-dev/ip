/**
 * Represents a task that must be completed before a specified date or time.
 */
public class Deadline extends Task {
    /** The date or time by which the task should be completed. */
    private final String by;

    /**
     * Creates a new incomplete deadline task.
     *
     * @param description the text describing the task
     * @param by the date or time by which the task should be completed
     */
    public Deadline(String description, String by) {
        super(description);
        if (by == null || by.isBlank()) {
            throw new IllegalArgumentException("Deadline time cannot be blank.");
        }

        this.by = by.strip();
    }

    @Override
    public String getTypeIcon() {
        return "D";
    }

    @Override
    public String toStorageString() {
        return super.toStorageString() + " | " + escapeStorageField(by);
    }

    @Override
    public String toString() {
        return super.toString() + " (by: " + by + ")";
    }
}
