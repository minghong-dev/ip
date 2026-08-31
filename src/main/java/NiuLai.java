import java.io.IOException;
/**
 * Runs the NiuLai command-line chatbot.
 */
public class NiuLai {
    /** The component that persists tasks between chatbot sessions. */
    private final Storage storage;

    /** The tasks currently managed by the chatbot. */
    private TaskList tasks;

    /** The component that handles console interaction. */
    private final Ui ui;

    /** The component that interprets user commands. */
    private final Parser parser;

    /**
     * Creates a chatbot backed by the specified task data file.
     *
     * @param filePath the path of the task data file
     */
    public NiuLai(String filePath) {
        ui = new Ui();
        parser = new Parser();
        storage = new Storage(filePath);
        tasks = new TaskList();
    }

    /** Creates a chatbot backed by the default task data file. */
    public NiuLai() {
        this("data/niulai.txt");
    }

    /** Runs the chatbot until the user enters {@code bye} or input ends. */
    public void run() {
        ui.showWelcome();
        tasks = loadTasks();

        while (ui.hasNextLine()) {
            String command = ui.readCommand();

            ui.showSeparator();

            try {
                Command parsedCommand = parser.parseCommand(command, tasks.size());
                parsedCommand.execute(tasks, ui, storage);
                if (parsedCommand.isExit()) {
                    break;
                }
            } catch (NiuLaiException e) {
                ui.showError(e.getMessage());
            }
        }
    }

    /**
     * Loads the saved task list, starting with an empty list if loading fails.
     *
     * @return the saved task list or an empty list when no usable data is available
     */
    private TaskList loadTasks() {
        try {
            return storage.load();
        } catch (IOException | SecurityException e) {
            ui.showLoadingError();
            return new TaskList();
        }
    }

    /** Starts the chatbot with its default data file. */
    public static void main(String[] args) {
        new NiuLai().run();
    }

}
