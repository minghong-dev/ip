package niulai.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import niulai.model.Deadline;
import niulai.model.Event;
import niulai.model.TaskList;
import niulai.model.Todo;

/** Tests persistence, escaping, replacement, and malformed-data handling. */
class StorageTest {
    /** Temporary directory isolated from the application's real data file. */
    @TempDir
    Path temporaryDirectory;

    /** Verifies that all task types and completion states survive a save/load round trip. */
    @Test
    void saveAndLoad_mixedTasks_preservesTaskData() throws IOException {
        Storage storage = new Storage(temporaryDirectory.resolve("niulai.txt").toString());
        TaskList saved = new TaskList();
        saved.add(new Todo("read | review \\ draft"));
        Deadline deadline = new Deadline("submit report", "2026-08-31");
        deadline.markAsDone();
        saved.add(deadline);
        saved.add(new Event("meeting", "2026-08-30", "2026-09-02"));

        storage.save(saved);
        TaskList loaded = storage.load();

        assertEquals(3, loaded.size());
        assertEquals("read | review \\ draft", loaded.get(0).getDescription());
        assertEquals(" ", loaded.get(0).getStatusIcon());
        assertEquals("X", loaded.get(1).getStatusIcon());
        assertEquals("[D][X] submit report (by: Aug 31 2026)", loaded.get(1).toString());
        assertEquals("[E][ ] meeting (from: 2026-08-30 to: 2026-09-02)",
                loaded.get(2).toString());
    }

    /** Verifies that saving replaces an existing file, which is the Windows regression case. */
    @Test
    void save_existingFile_replacesContents() throws IOException {
        Path file = temporaryDirectory.resolve("niulai.txt");
        Files.writeString(file, "T | 0 | old task");
        Storage storage = new Storage(file.toString());
        TaskList tasks = new TaskList();
        tasks.add(new Todo("new task"));

        storage.save(tasks);

        assertEquals("T | 0 | new task", Files.readString(file).strip());
    }

    /** Verifies that malformed saved data reports an I/O error with its line number. */
    @Test
    void load_malformedData_exceptionThrown() throws IOException {
        Path file = temporaryDirectory.resolve("niulai.txt");
        Files.writeString(file, "T | 0 | valid\nD | 0");

        IOException exception = assertThrows(IOException.class,
                () -> new Storage(file.toString()).load());

        assertEquals("Invalid task data on line 2.", exception.getMessage());
    }

    /** Verifies that an unsupported persisted completion state is rejected. */
    @Test
    void load_invalidCompletionState_exceptionThrown() throws IOException {
        Path file = temporaryDirectory.resolve("niulai.txt");
        Files.writeString(file, "T | 2 | invalid state");

        IOException exception = assertThrows(IOException.class,
                () -> new Storage(file.toString()).load());

        assertEquals("Invalid task data on line 1.", exception.getMessage());
    }

    /** Verifies that a missing data file is treated as an empty task list. */
    @Test
    void load_missingFile_returnsEmptyTaskList() throws IOException {
        TaskList tasks = new Storage(temporaryDirectory.resolve("missing.txt").toString()).load();

        assertEquals(0, tasks.size());
    }
}
