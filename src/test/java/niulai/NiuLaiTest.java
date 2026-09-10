package niulai;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import niulai.gui.GuiUi;

/** Tests session behavior around recoverable and unrecoverable storage failures. */
class NiuLaiTest {
    /** Temporary directory isolated from the application's real data file. */
    @TempDir
    Path temporaryDirectory;

    /** Verifies that a partly corrupt file reports skipped lines and retains valid tasks. */
    @Test
    void startSession_partlyCorruptFile_reportsSkippedLinesAndKeepsValidTasks()
            throws IOException {
        Path dataFile = temporaryDirectory.resolve("niulai.txt");
        Files.writeString(dataFile, "T | 0 | valid\nmalformed");
        GuiUi ui = new GuiUi();
        NiuLai chatbot = new NiuLai(dataFile.toString(), ui);

        chatbot.startSession();
        String startup = ui.consumeOutput();
        chatbot.processCommand("list");
        String listOutput = ui.consumeOutput();

        assertTrue(startup.contains("line 2"));
        assertTrue(startup.contains("backed up"));
        assertTrue(listOutput.contains("1.[T][ ] valid"));
    }

    /** Verifies that an unreadable path blocks mutations while read-only commands still work. */
    @Test
    void startSession_directoryPath_blocksMutationsButAllowsReadOnlyCommands() {
        GuiUi ui = new GuiUi();
        NiuLai chatbot = new NiuLai(temporaryDirectory.toString(), ui);

        chatbot.startSession();
        assertTrue(ui.consumeOutput().contains("changes are disabled"));

        chatbot.processCommand("todo new task");
        assertTrue(ui.consumeOutput().contains("changes are disabled"));
        chatbot.processCommand("list");
        String listOutput = ui.consumeOutput();

        assertTrue(listOutput.contains("Here are the tasks"));
        assertTrue(!listOutput.contains("new task"));
    }

    /** Verifies that an external file edit prevents a task command from overwriting it. */
    @Test
    void processCommand_externalFileChange_rollsBackMutationAndPreservesExternalBytes()
            throws IOException {
        Path dataFile = temporaryDirectory.resolve("niulai.txt");
        Files.writeString(dataFile, "T | 0 | original task");
        GuiUi ui = new GuiUi();
        NiuLai chatbot = new NiuLai(dataFile.toString(), ui);
        chatbot.startSession();
        ui.consumeOutput();
        byte[] externalBytes = "T | 0 | externally changed"
                .getBytes(StandardCharsets.UTF_8);
        Files.write(dataFile, externalBytes);

        chatbot.processCommand("todo new task");
        String addOutput = ui.consumeOutput();
        chatbot.processCommand("list");
        String listOutput = ui.consumeOutput();

        assertTrue(addOutput.contains("changed outside NiuLai"));
        assertTrue(!listOutput.contains("new task"));
        assertArrayEquals(externalBytes, Files.readAllBytes(dataFile));
    }
}
