package niulai.command;

import java.io.IOException;

import niulai.NiuLaiException;
import niulai.model.Task;
import niulai.model.TaskList;
import niulai.model.TaskStatus;
import niulai.service.Storage;
import niulai.service.Ui;

/**
 * Marks a task as pending and persists the updated task list.
 */
public class UnmarkCommand extends Command {
    /** The zero-based index of the task to mark as pending. */
    private final int taskIndex;

    /**
     * Creates an unmark command for a task-list index.
     *
     * @param taskIndex the zero-based index of the task to mark as pending
     */
    public UnmarkCommand(int taskIndex) {
        super(Type.UNMARK);
        this.taskIndex = taskIndex;
    }

    /**
     * Marks the task as pending, saves the updated list, and restores its status if saving fails.
     *
     * @param tasks the current task list
     * @param ui the user-interface component
     * @param storage the task storage component
     * @throws NiuLaiException if the updated list cannot be saved
     */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws NiuLaiException {
        Task task = tasks.get(taskIndex);
        TaskStatus previousStatus = task.getStatus();
        task.markAsNotDone();
        try {
            storage.save(tasks);
        } catch (IOException | SecurityException e) {
            restoreStatus(task, previousStatus);
            throw new NiuLaiException("NOOO!!! I couldn't save your tasks to disk.");
        }
        ui.showTaskUnmarked(task);
    }

    /** Restores the task status that was present before execution. */
    private void restoreStatus(Task task, TaskStatus status) {
        if (status == TaskStatus.COMPLETED) {
            task.markAsDone();
        } else {
            task.markAsNotDone();
        }
    }
}
