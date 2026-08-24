import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

/**
 * Saves and loads the task list from the application's data file.
 */
public class Storage {
    /** The file used to store tasks relative to the project root. */
    private static final Path FILE_PATH = Path.of("data", "niulai.txt");

    /**
     * Writes the complete task list to disk, replacing the previous contents.
     *
     * @param tasks the current task list
     * @throws IOException if the data directory or file cannot be written
     */
    public static void save(ArrayList<Task> tasks) throws IOException {
        ArrayList<String> lines = new ArrayList<>();
        for (Task task : tasks) {
            lines.add(task.toStorageString());
        }

        Files.createDirectories(FILE_PATH.getParent());
        Files.write(
                FILE_PATH,
                lines,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        );
    }

    /**
     * Reads the saved task list from disk.
     *
     * @return the saved tasks, or an empty list when no data file exists
     * @throws IOException if the data file cannot be read or contains an invalid task line
     */
    public static ArrayList<Task> load() throws IOException {
        ArrayList<Task> tasks = new ArrayList<>();

        if (!Files.exists(FILE_PATH)) {
            return tasks;
        }

        for (String line : Files.readAllLines(FILE_PATH, StandardCharsets.UTF_8)) {
            if (!line.isBlank()) {
                tasks.add(parseTask(line));
            }
        }

        return tasks;
    }

    /**
     * Converts one saved line into a task object.
     *
     * @param line the saved task line
     * @return the reconstructed task
     * @throws IOException if the line does not follow the save format
     */
    private static Task parseTask(String line) throws IOException {
        String[] fields = line.split(" \\| ", -1);

        if (fields.length < 3) {
            throw new IOException("Invalid task line: " + line);
        }

        String type = fields[0].trim();
        String description = fields[2].trim();
        Task task;

        try {
            int completionState = Integer.parseInt(fields[1].trim());

            if (completionState != 0 && completionState != 1) {
                throw new IOException("Invalid completion state: " + line);
            }

            task = switch (type) {
            case "T" -> {
                requireFieldCount(fields, 3, line);
                yield new Todo(description);
            }
            case "D" -> {
                requireFieldCount(fields, 4, line);
                yield new Deadline(description, fields[3].trim());
            }
            case "E" -> {
                requireFieldCount(fields, 5, line);
                yield new Event(description, fields[3].trim(), fields[4].trim());
            }
            default -> throw new IOException("Invalid task type: " + line);
            };

            if (completionState == 1) {
                task.markAsDone();
            }
        } catch (NumberFormatException e) {
            throw new IOException("Invalid completion state: " + line, e);
        }

        return task;
    }

    /**
     * Checks that a saved line has the number of fields required by its task type.
     *
     * @param fields the fields parsed from the line
     * @param expectedCount the required number of fields
     * @param line the original saved line
     * @throws IOException if the field count is incorrect
     */
    private static void requireFieldCount(String[] fields, int expectedCount, String line)
            throws IOException {
        if (fields.length != expectedCount) {
            throw new IOException("Invalid task line: " + line);
        }
    }
}
