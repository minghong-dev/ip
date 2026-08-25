import java.io.IOException;
import java.time.LocalDate;
/**
 * Runs the NiuLai command-line chatbot.
 */
public class NiuLai {
    public static void main(String[] args) {
        Ui ui = new Ui();
        Parser parser = new Parser();
        ui.showWelcome();

        TaskList tasks = loadTasks(ui);

        while (ui.hasNextLine()) {
            String command = ui.readCommand();

            ui.showSeparator();

            try {
                if (Command.BYE.matchesExactly(command)) {
                    ui.showBye();
                    break;
                }

                if (Command.LIST.matchesExactly(command)) {
                    ui.showList(tasks);
                    continue;
                }

                if (Command.FIND.matches(command)) {
                    LocalDate date = parser.parseDateArgument(command, Command.FIND);
                    ui.showTasksOnDate(tasks, date);
                    continue;
                }

                if (Command.MARK.matches(command)) {
                    int taskIndex = parser.parseTaskIndex(command, Command.MARK, tasks.size());
                    Task task = tasks.get(taskIndex);
                    TaskStatus previousStatus = task.getStatus();
                    task.markAsDone();
                    try {
                        saveTasks(tasks);
                    } catch (NiuLaiException e) {
                        restoreStatus(task, previousStatus);
                        throw e;
                    }
                    ui.showTaskMarked(task);
                    continue;
                }

                if (Command.UNMARK.matches(command)) {
                    int taskIndex = parser.parseTaskIndex(command, Command.UNMARK, tasks.size());
                    Task task = tasks.get(taskIndex);
                    TaskStatus previousStatus = task.getStatus();
                    task.markAsNotDone();
                    try {
                        saveTasks(tasks);
                    } catch (NiuLaiException e) {
                        restoreStatus(task, previousStatus);
                        throw e;
                    }
                    ui.showTaskUnmarked(task);
                    continue;
                }

                if (Command.DELETE.matches(command)) {
                    int taskIndex = parser.parseTaskIndex(command, Command.DELETE, tasks.size());
                    Task deletedTask = tasks.remove(taskIndex);
                    try {
                        saveTasks(tasks);
                    } catch (NiuLaiException e) {
                        tasks.add(taskIndex, deletedTask);
                        throw e;
                    }
                    ui.showTaskDeleted(deletedTask, tasks.size());
                    continue;
                }

                if (Command.TODO.matches(command)
                        || Command.DEADLINE.matches(command)
                        || Command.EVENT.matches(command)) {
                    addTaskAndSave(tasks, parser.parseTaskCreation(command));
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
     * @param tasks the current task list
     * @param task the task to add
     * @throws NiuLaiException if the updated list cannot be saved
     */
    private static void addTaskAndSave(TaskList tasks, Task task) throws NiuLaiException {
        tasks.add(task);
        try {
            saveTasks(tasks);
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
     * @param tasks the current task list
     * @throws NiuLaiException if the task list cannot be written to disk
     */
    private static void saveTasks(TaskList tasks) throws NiuLaiException {
        try {
            Storage.save(tasks);
        } catch (IOException | SecurityException e) {
            throw new NiuLaiException("NOOO!!! I couldn't save your tasks to disk.");
        }
    }

    /**
     * Loads the saved task list, starting with an empty list if loading fails.
     *
     * @return the saved task list or an empty list when no usable data is available
     */
    private static TaskList loadTasks(Ui ui) {
        try {
            return Storage.load();
        } catch (IOException | SecurityException e) {
            ui.showLoadingError();
            return new TaskList();
        }
    }

}
