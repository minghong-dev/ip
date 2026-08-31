/**
 * Displays all tasks currently managed by NiuLai.
 */
public class ListCommand extends Command {
    /** Creates a list command. */
    public ListCommand() {
        super(Type.LIST);
    }

    /** Shows every task in display order. */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        ui.showList(tasks);
    }
}
