package niulai;

import java.io.IOException;
import java.util.Objects;

import niulai.command.Command;
import niulai.model.TaskList;
import niulai.service.Parser;
import niulai.service.Storage;
import niulai.service.Ui;

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
        this(filePath, new Ui());
    }

    /** Creates a chatbot backed by the default task data file. */
    public NiuLai() {
        this("data/niulai.txt");
    }

    /**
     * Creates a chatbot with a caller-provided user-interface component.
     *
     * <p>This constructor lets the command-processing logic be reused by interfaces other than
     * the command line, such as the JavaFX interface.</p>
     *
     * @param filePath the path of the task data file
     * @param ui the component used to present chatbot responses
     */
    public NiuLai(String filePath, Ui ui) {
        this.ui = Objects.requireNonNull(ui, "ui");
        parser = new Parser();
        storage = new Storage(filePath);
        tasks = new TaskList();
    }

    /** Runs the chatbot until the user enters {@code bye} or input ends. */
    public void run() {
        startSession();

        while (ui.hasNextLine()) {
            String command = ui.readCommand();

            if (processCommand(command)) {
                break;
            }
        }
    }

    /** Starts a chatbot session by showing the welcome message and loading saved tasks. */
    public void startSession() {
        ui.showWelcome();
        tasks = loadTasks();
    }

    /**
     * Processes one command using the current task list.
     *
     * @param input the command entered by the user
     * @return whether the command ends the chatbot session
     */
    public boolean processCommand(String input) {
        String command = Objects.requireNonNull(input, "input").strip();
        ui.showSeparator();

        try {
            Command parsedCommand = parser.parseCommand(command, tasks.size());
            parsedCommand.execute(tasks, ui, storage);
            return parsedCommand.isExit();
        } catch (NiuLaiException e) {
            ui.showError(e.getMessage());
            return false;
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
