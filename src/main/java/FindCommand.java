import java.time.LocalDate;
import java.util.Objects;

/**
 * Displays deadlines and events occurring on a specified date.
 */
public class FindCommand extends Command {
    /** The date whose matching tasks should be displayed. */
    private final LocalDate date;

    /**
     * Creates a find command for a date.
     *
     * @param date the date to search for
     */
    public FindCommand(LocalDate date) {
        super(Type.FIND);
        this.date = Objects.requireNonNull(date, "date");
    }

    /**
     * Displays the tasks occurring on the requested date.
     *
     * @param tasks the current task list
     * @param ui the user-interface component
     * @param storage the task storage component
     */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        ui.showTasksOnDate(tasks, date);
    }
}
