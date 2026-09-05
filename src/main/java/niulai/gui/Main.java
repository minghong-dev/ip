package niulai.gui;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import niulai.NiuLai;

/**
 * JavaFX application for NiuLai.
 *
 * <p>The FXML files define the view while {@link MainWindow} handles user interaction.</p>
 */
public class Main extends Application {
    /**
     * Creates and displays the main NiuLai window.
     *
     * @param stage the primary JavaFX window
     */
    @Override
    public void start(Stage stage) {
        try {
            GuiUi ui = new GuiUi();
            NiuLai chatbot = new NiuLai(NiuLai.DEFAULT_FILE_PATH, ui);
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/view/MainWindow.fxml"));
            BorderPane mainWindow = fxmlLoader.load();
            MainWindow controller = fxmlLoader.getController();
            controller.setChatbot(chatbot, ui);

            chatbot.startSession();
            controller.showBotMessage(ui.consumeOutput());

            Scene scene = new Scene(mainWindow);
            scene.getStylesheets().add(Main.class.getResource("/view/style.css").toExternalForm());
            stage.setTitle("NiuLai");
            stage.setMinWidth(520.0);
            stage.setMinHeight(520.0);
            stage.setScene(scene);
            stage.show();
        } catch (IOException | NullPointerException e) {
            throw new IllegalStateException("Unable to start the NiuLai GUI.", e);
        }
    }
}
