package niulai.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Objects;

import niulai.model.Deadline;
import niulai.model.Event;
import niulai.model.Task;
import niulai.model.TaskList;
import niulai.model.TaskStatus;
import niulai.model.Todo;

/**
 * Saves and loads the task list from the application's data file.
 */
public class Storage {
    /** The file used to store tasks. */
    private final Path filePath;

    /**
     * Creates a storage component backed by the specified file.
     *
     * @param filePath the path of the task data file
     */
    public Storage(String filePath) {
        this.filePath = Path.of(Objects.requireNonNull(filePath, "filePath"));
    }

    /**
     * Writes the complete task list to disk, replacing the previous contents.
     *
     * @param tasks the current task list
     * @throws IOException if the data directory or file cannot be written
     */
    public void save(TaskList tasks) throws IOException {
        Objects.requireNonNull(tasks, "tasks");

        ArrayList<String> lines = new ArrayList<>();
        for (Task task : tasks) {
            Objects.requireNonNull(task, "tasks cannot contain null");
            lines.add(task.toStorageString());
        }

        Path dataDirectory = filePath.getParent();
        if (dataDirectory == null) {
            dataDirectory = Path.of(".");
        }
        Files.createDirectories(dataDirectory);
        Path temporaryFile = Files.createTempFile(dataDirectory, "niulai-", ".tmp");

        try {
            Files.write(
                    temporaryFile,
                    lines,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );

            try {
                Files.move(
                        temporaryFile,
                        filePath,
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING
                );
            } catch (AtomicMoveNotSupportedException | FileAlreadyExistsException e) {
                Files.move(
                        temporaryFile,
                        filePath,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }
        } finally {
            Files.deleteIfExists(temporaryFile);
        }
    }

    /**
     * Reads the saved task list from disk.
     *
     * @return the saved tasks, or an empty list when no data file exists
     * @throws IOException if the data file cannot be read or contains an invalid task line
     */
    public TaskList load() throws IOException {
        TaskList tasks = new TaskList();

        if (!Files.exists(filePath)) {
            return tasks;
        }

        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (lineNumber == 1 && line.startsWith("\uFEFF")) {
                    line = line.substring(1);
                }
                if (!line.isBlank()) {
                    tasks.add(parseTask(line, lineNumber));
                }
            }
        }

        return tasks;
    }

    /**
     * Converts one saved line into a task object.
     *
     * @param line the saved task line
     * @param lineNumber the line's number in the data file
     * @return the reconstructed task
     * @throws IOException if the line does not follow the save format
     */
    private static Task parseTask(String line, int lineNumber) throws IOException {
        ArrayList<String> fields = splitFields(line, lineNumber);

        if (fields.size() < 3) {
            throw invalidLine(lineNumber);
        }

        String description = requireValue(fields.get(2), lineNumber);
        Task task;

        TaskStatus status = parseStatus(fields.get(1), lineNumber);
        Task.TaskType type;
        try {
            type = Task.TaskType.fromIcon(fields.get(0));
        } catch (IllegalArgumentException e) {
            throw invalidLine(lineNumber, e);
        }

        switch (type) {
            case TODO:
                requireFieldCount(fields, 3, lineNumber);
                task = new Todo(description);
                break;
            case DEADLINE:
                requireFieldCount(fields, 4, lineNumber);
                task = new Deadline(description, requireValue(fields.get(3), lineNumber));
                break;
            case EVENT:
                requireFieldCount(fields, 5, lineNumber);
                task = new Event(
                        description,
                        requireValue(fields.get(3), lineNumber),
                        requireValue(fields.get(4), lineNumber)
                );
                break;
            default:
                throw invalidLine(lineNumber);
        }

        if (status == TaskStatus.COMPLETED) {
            task.markAsDone();
        }

        // Loading must faithfully reconstruct the type and completion state written by save().
        assert task.getTypeIcon().equals(type.getIcon())
                : "A storage type marker must create a task of the same type.";
        assert task.getStatus() == status
                : "Loading must preserve a task's completion state.";

        return task;
    }

    /**
     * Parses a persisted task status.
     *
     * @param value the persisted status value
     * @param lineNumber the value's line number in the data file
     * @return the parsed task status
     * @throws IOException if the value is not a valid task status
     */
    private static TaskStatus parseStatus(String value, int lineNumber) throws IOException {
        try {
            return TaskStatus.fromStorageValue(Integer.parseInt(value));
        } catch (IllegalArgumentException e) {
            throw invalidLine(lineNumber, e);
        }
    }

    /**
     * Splits a saved line while honoring escaped pipes and backslashes.
     *
     * @param line the saved line
     * @param lineNumber the line's number in the data file
     * @return the unescaped, trimmed fields
     * @throws IOException if the line ends with an incomplete escape sequence
     */
    private static ArrayList<String> splitFields(String line, int lineNumber) throws IOException {
        ArrayList<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean escaped = false;

        for (int i = 0; i < line.length(); i++) {
            char current = line.charAt(i);

            if (escaped) {
                if (current == '\\' || current == '|') {
                    field.append(current);
                } else {
                    field.append('\\').append(current);
                }
                escaped = false;
            } else if (current == '\\') {
                escaped = true;
            } else if (current == '|') {
                fields.add(field.toString().strip());
                field.setLength(0);
            } else {
                field.append(current);
            }
        }

        if (escaped) {
            throw invalidLine(lineNumber);
        }

        fields.add(field.toString().strip());
        return fields;
    }

    /**
     * Ensures that a saved field contains meaningful text.
     *
     * @param value the field to validate
     * @param lineNumber the line's number in the data file
     * @return the validated field
     * @throws IOException if the field is blank
     */
    private static String requireValue(String value, int lineNumber) throws IOException {
        if (value.isBlank()) {
            throw invalidLine(lineNumber);
        }
        return value;
    }

    /**
     * Checks that a saved line has the number of fields required by its task type.
     *
     * @param fields the fields parsed from the line
     * @param expectedCount the required number of fields
     * @param lineNumber the line's number in the data file
     * @throws IOException if the field count is incorrect
     */
    private static void requireFieldCount(
            ArrayList<String> fields, int expectedCount, int lineNumber) throws IOException {
        if (fields.size() != expectedCount) {
            throw invalidLine(lineNumber);
        }
    }

    /**
     * Creates a consistent error for malformed storage data.
     *
     * @param lineNumber the invalid line's number
     * @return the storage error
     */
    private static IOException invalidLine(int lineNumber) {
        return new IOException("Invalid task data on line " + lineNumber + ".");
    }

    /**
     * Creates a consistent error for malformed storage data with a cause.
     *
     * @param lineNumber the invalid line's number
     * @param cause the parsing failure
     * @return the storage error
     */
    private static IOException invalidLine(int lineNumber, Exception cause) {
        return new IOException("Invalid task data on line " + lineNumber + ".", cause);
    }
}
