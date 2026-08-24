import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

/**
 * Saves the current task list to the application's data file.
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
}
