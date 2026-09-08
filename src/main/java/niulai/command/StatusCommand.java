package niulai.command;

import java.io.IOException;

import niulai.NiuLaiException;
import niulai.model.Task;
import niulai.model.TaskList;
import niulai.model.TaskStatus;
import niulai.service.Storage;
import niulai.service.Ui;

/** Provides shared execution logic for commands that change task status. */
abstract class StatusCommand extends Command {
    /** The zero-based index of the task whose status should change. */
    private final int taskIndex;

    /** Creates a status-changing command for a task-list index. */
    protected StatusCommand(Type type, int taskIndex) {
        super(type);
        this.taskIndex = taskIndex;
    }

    /** Applies this command's target status to the supplied task. */
    protected abstract void applyStatus(Task task);

    /** Shows this command's successful status-change message. */
    protected abstract void showConfirmation(Ui ui, Task task);

    /**
     * Changes the task status, persists the list, and restores the status on failure.
     *
     * @param tasks the current task list
     * @param ui the user-interface component
     * @param storage the task storage component
     * @throws NiuLaiException if the updated list cannot be saved
     */
    @Override
    public final UndoAction execute(TaskList tasks, Ui ui, Storage storage) throws NiuLaiException {
        Task task = tasks.get(taskIndex);
        TaskStatus previousStatus = task.getStatus();

        applyStatus(task);
        try {
            storage.save(tasks);
        } catch (IOException | SecurityException e) {
            restoreStatus(task, previousStatus);
            // A failed save must leave the task's completion state unchanged.
            assert task.getStatus() == previousStatus
                    : "A failed save must restore the task's original completion state.";
            throw createStorageFailure();
        }
        showConfirmation(ui, task);
        return (currentTasks, currentStorage) -> {
            Task currentTask = currentTasks.get(taskIndex);
            TaskStatus currentStatus = currentTask.getStatus();
            restoreStatus(currentTask, previousStatus);
            try {
                currentStorage.save(currentTasks);
            } catch (IOException | SecurityException e) {
                restoreStatus(currentTask, currentStatus);
                throw createStorageFailure();
            }
        };
    }

    /** Restores the task status that was present before execution. */
    private static void restoreStatus(Task task, TaskStatus status) {
        if (status == TaskStatus.COMPLETED) {
            task.markAsDone();
        } else {
            task.markAsNotDone();
        }
    }
}
