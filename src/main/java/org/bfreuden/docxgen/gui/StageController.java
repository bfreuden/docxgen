package org.bfreuden.docxgen.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class StageController {
    private final Stage stage;
    private Scene mainScene;
    private Scene configScene;
    private final Application app;

    public StageController(Application app, Stage stage) {
        this.app = app;
        this.stage = stage;
    }
    public Stage getStage() {
        return this.stage;
    }

    public void setMainScene(Scene mainScene) {
        this.mainScene = mainScene;
    }

    public void setConfigScene(Scene configScene) {
        this.configScene = configScene;
    }

    public void showMainScene() {
        this.stage.setScene(mainScene);
    }

    public void showConfigScene() {
        this.stage.setScene(configScene);
    }

    public void exit() throws Exception {
        try {
            app.stop();
            Platform.exit();
            System.out.println("ok");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
