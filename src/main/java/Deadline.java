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
        this.by = by;
    }

    @Override
    public String getTypeIcon() {
        return "D";
    }

    @Override
    public String toString() {
        return super.toString() + " (by: " + by + ")";
    }
}
