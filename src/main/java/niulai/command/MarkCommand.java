package niulai.command;

import java.io.IOException;

import niulai.NiuLaiException;
import niulai.model.Task;
import niulai.model.TaskList;
import niulai.model.TaskStatus;
import niulai.service.Storage;
import niulai.service.Ui;

/**
 * Marks a task as completed and persists the updated task list.
 */
public class MarkCommand extends Command {
    /** The zero-based index of the task to mark. */
    private final int taskIndex;

    /**
     * Creates a mark command for a task-list index.
     *
     * @param taskIndex the zero-based index of the task to mark
     */
    public MarkCommand(int taskIndex) {
        super(Type.MARK);
        this.taskIndex = taskIndex;
    }

    /**
     * Marks the task, saves the updated list, and restores its status if saving fails.
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
        task.markAsDone();
        try {
            storage.save(tasks);
        } catch (IOException | SecurityException e) {
            restoreStatus(task, previousStatus);
            throw new NiuLaiException("NOOO!!! I couldn't save your tasks to disk.");
        }
        ui.showTaskMarked(task);
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
