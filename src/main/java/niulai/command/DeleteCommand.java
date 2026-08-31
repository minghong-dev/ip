package niulai.command;

import java.io.IOException;

import niulai.NiuLaiException;
import niulai.model.Task;
import niulai.model.TaskList;
import niulai.service.Storage;
import niulai.service.Ui;

/**
 * Deletes a task and persists the updated task list.
 */
public class DeleteCommand extends Command {
    /** The zero-based index of the task to delete. */
    private final int taskIndex;

    /**
     * Creates a delete command for a task-list index.
     *
     * @param taskIndex the zero-based index of the task to delete
     */
    public DeleteCommand(int taskIndex) {
        super(Type.DELETE);
        this.taskIndex = taskIndex;
    }

    /**
     * Removes the task, saves the updated list, and restores the task if saving fails.
     *
     * @param tasks the current task list
     * @param ui the user-interface component
     * @param storage the task storage component
     * @throws NiuLaiException if the updated list cannot be saved
     */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws NiuLaiException {
        Task deletedTask = tasks.remove(taskIndex);
        try {
            storage.save(tasks);
        } catch (IOException | SecurityException e) {
            tasks.add(taskIndex, deletedTask);
            throw new NiuLaiException("NOOO!!! I couldn't save your tasks to disk.");
        }
        ui.showTaskDeleted(deletedTask, tasks.size());
    }
}
