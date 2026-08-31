/**
 * Exits the NiuLai command-line application.
 */
public class ExitCommand extends Command {
    /** Creates an exit command. */
    public ExitCommand() {
        super(Type.BYE);
    }

    /** Shows the farewell message. */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        ui.showBye();
    }

    /** @return true because this command ends the application */
    @Override
    public boolean isExit() {
        return true;
    }
}
