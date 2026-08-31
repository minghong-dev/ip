import java.io.IOException;
import java.time.LocalDate;
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
                if (Command.Type.BYE.matchesExactly(command)) {
                    Command exitCommand = new ExitCommand();
                    exitCommand.execute(tasks, ui, storage);
                    break;
                }

                if (Command.Type.LIST.matchesExactly(command)) {
                    ui.showList(tasks);
                    continue;
                }

                if (Command.Type.FIND.matches(command)) {
                    LocalDate date = parser.parseDateArgument(command, Command.Type.FIND);
                    ui.showTasksOnDate(tasks, date);
                    continue;
                }

                if (Command.Type.MARK.matches(command)) {
                    int taskIndex = parser.parseTaskIndex(command, Command.Type.MARK, tasks.size());
                    Task task = tasks.get(taskIndex);
                    TaskStatus previousStatus = task.getStatus();
                    task.markAsDone();
                    try {
                        saveTasks();
                    } catch (NiuLaiException e) {
                        restoreStatus(task, previousStatus);
                        throw e;
                    }
                    ui.showTaskMarked(task);
                    continue;
                }

                if (Command.Type.UNMARK.matches(command)) {
                    int taskIndex = parser.parseTaskIndex(command, Command.Type.UNMARK, tasks.size());
                    Task task = tasks.get(taskIndex);
                    TaskStatus previousStatus = task.getStatus();
                    task.markAsNotDone();
                    try {
                        saveTasks();
                    } catch (NiuLaiException e) {
                        restoreStatus(task, previousStatus);
                        throw e;
                    }
                    ui.showTaskUnmarked(task);
                    continue;
                }

                if (Command.Type.DELETE.matches(command)) {
                    int taskIndex = parser.parseTaskIndex(command, Command.Type.DELETE, tasks.size());
                    Task deletedTask = tasks.remove(taskIndex);
                    try {
                        saveTasks();
                    } catch (NiuLaiException e) {
                        tasks.add(taskIndex, deletedTask);
                        throw e;
                    }
                    ui.showTaskDeleted(deletedTask, tasks.size());
                    continue;
                }

                if (Command.Type.TODO.matches(command)
                        || Command.Type.DEADLINE.matches(command)
                        || Command.Type.EVENT.matches(command)) {
                    addTaskAndSave(parser.parseTaskCreation(command));
                    ui.showTaskAdded(tasks.get(tasks.size() - 1), tasks.size());
                    continue;
                }

                throw new NiuLaiException(
                        "NOOO!!! I don't recognize that command. Try 'list' to view your tasks."
                );
            } catch (NiuLaiException e) {
                ui.showError(e.getMessage());
            }
        }
    }

    /**
     * Adds a task and rolls the addition back if saving fails.
     *
     * @param task the task to add
     * @throws NiuLaiException if the updated list cannot be saved
     */
    private void addTaskAndSave(Task task) throws NiuLaiException {
        tasks.add(task);
        try {
            saveTasks();
        } catch (NiuLaiException e) {
            tasks.remove(tasks.size() - 1);
            throw e;
        }
    }

    /**
     * Restores a task's status after a failed save.
     *
     * @param task the task whose status should be restored
     * @param status the previous status
     */
    private static void restoreStatus(Task task, TaskStatus status) {
        if (status == TaskStatus.COMPLETED) {
            task.markAsDone();
        } else {
            task.markAsNotDone();
        }
    }

    /**
     * Saves the current task list and turns file-system failures into a chatbot error.
     *
     * @throws NiuLaiException if the task list cannot be written to disk
     */
    private void saveTasks() throws NiuLaiException {
        try {
            storage.save(tasks);
        } catch (IOException | SecurityException e) {
            throw new NiuLaiException("NOOO!!! I couldn't save your tasks to disk.");
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
