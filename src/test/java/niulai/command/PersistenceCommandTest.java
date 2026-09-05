package niulai.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import niulai.NiuLaiException;
import niulai.model.Task;
import niulai.model.TaskList;
import niulai.model.TaskStatus;
import niulai.model.Todo;
import niulai.service.Storage;
import niulai.service.Ui;

/** Tests that persistence commands preserve in-memory state when saving fails. */
class PersistenceCommandTest {
    /** A storage implementation that always fails before writing task data. */
    private static class FailingStorage extends Storage {
        /** Creates storage that fails every save attempt. */
        FailingStorage() {
            super("unused.txt");
        }

        /** Always simulates a save failure. */
        @Override
        public void save(TaskList tasks) throws IOException {
            throw new IOException("Simulated save failure.");
        }
    }

    /** Verifies that a failed add removes only the task that was just appended. */
    @Test
    void execute_addSaveFails_removesAddedTask() {
        Task originalTask = new Todo("existing task");
        Task addedTask = new Todo("new task");
        TaskList tasks = new TaskList(originalTask);

        assertThrows(NiuLaiException.class,
                () -> new AddCommand(addedTask).execute(tasks, new Ui(), new FailingStorage()));

        assertEquals(1, tasks.size());
        assertSame(originalTask, tasks.get(0));
    }

    /** Verifies that a failed delete restores the exact task at its original position. */
    @Test
    void execute_deleteSaveFails_restoresDeletedTask() {
        Task deletedTask = new Todo("first task");
        Task followingTask = new Todo("second task");
        TaskList tasks = new TaskList(deletedTask, followingTask);

        assertThrows(NiuLaiException.class,
                () -> new DeleteCommand(0).execute(tasks, new Ui(), new FailingStorage()));

        assertEquals(2, tasks.size());
        assertSame(deletedTask, tasks.get(0));
        assertSame(followingTask, tasks.get(1));
    }

    /** Verifies that a failed mark restores the task's pending status. */
    @Test
    void execute_markSaveFails_restoresPendingStatus() {
        Task task = new Todo("read book");
        TaskList tasks = new TaskList(task);

        assertThrows(NiuLaiException.class,
                () -> new MarkCommand(0).execute(tasks, new Ui(), new FailingStorage()));

        assertEquals(TaskStatus.PENDING, task.getStatus());
    }

    /** Verifies that a failed unmark restores the task's completed status. */
    @Test
    void execute_unmarkSaveFails_restoresCompletedStatus() {
        Task task = new Todo("read book");
        task.markAsDone();
        TaskList tasks = new TaskList(task);

        assertThrows(NiuLaiException.class,
                () -> new UnmarkCommand(0).execute(tasks, new Ui(), new FailingStorage()));

        assertEquals(TaskStatus.COMPLETED, task.getStatus());
    }
}
