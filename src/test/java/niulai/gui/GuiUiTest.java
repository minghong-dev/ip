package niulai.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import niulai.NiuLai;
import niulai.model.Todo;

/** Tests the JavaFX interface's output adapter and chatbot integration boundary. */
class GuiUiTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void consumeOutput_removesConsoleFormatting() {
        GuiUi ui = new GuiUi();

        ui.showTaskAdded(new Todo("read book"), 1);

        assertEquals(
                "Got it. I've added this task:\n[T][ ] read book\nNow you have 1 tasks in the list.",
                ui.consumeOutput()
        );
        assertEquals("", ui.consumeOutput());
    }

    @Test
    void processCommand_reusesExistingChatbotLogic() {
        GuiUi ui = new GuiUi();
        Path dataFile = temporaryDirectory.resolve("niulai.txt");
        NiuLai chatbot = new NiuLai(dataFile.toString(), ui);

        chatbot.startSession();
        ui.consumeOutput();

        assertTrue(!chatbot.processCommand("todo read book"));
        assertTrue(ui.consumeOutput().contains("I've added this task"));
        assertTrue(!chatbot.processCommand("list"));
        assertTrue(ui.consumeOutput().contains("1.[T][ ] read book"));
    }

    /** Verifies that keyword search uses the keyword mode of a find command. */
    @Test
    void processCommand_keywordFind_displaysMatchingTasks() {
        GuiUi ui = new GuiUi();
        Path dataFile = temporaryDirectory.resolve("niulai.txt");
        NiuLai chatbot = new NiuLai(dataFile.toString(), ui);

        chatbot.startSession();
        ui.consumeOutput();
        chatbot.processCommand("todo read book");
        ui.consumeOutput();

        assertTrue(!chatbot.processCommand("find read"));
        assertTrue(ui.consumeOutput().contains("1.[T][ ] read book"));
    }
}
