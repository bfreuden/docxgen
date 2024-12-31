package org.bfreuden.docxgen;

import javafx.application.Application;
import org.bfreuden.docxgen.gui.ConfigController;
import org.bfreuden.docxgen.gui.MainController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.bfreuden.docxgen.gui.StageController;

import java.util.Objects;

public class JavaFXApplication extends Application {

	static MainController guiMainController;
	static ConfigController guiConfigController;

	public JavaFXApplication() {
		super();
	}

	@Override
	public void start(Stage stage) throws Exception {
		var configuration = Configuration.configuration();
		var stageController = new StageController(this, stage);

		final FXMLLoader configViewLoader = new FXMLLoader(JavaFXApplication.class.getResource("config-view.fxml"));
		final Parent configParent = configViewLoader.load();
		final Scene configScene = new Scene(configParent, -1f, -1f, false, SceneAntialiasing.BALANCED);
		stageController.setConfigScene(configScene);
		guiConfigController = configViewLoader.getController();
		guiConfigController.initialize(stageController);
		guiConfigController.setGetHostController(getHostServices());

		final FXMLLoader mainView = new FXMLLoader(JavaFXApplication.class.getResource("main-view.fxml"));
		final Parent mainParent = mainView.load();
		final Scene mainScene = new Scene(mainParent, -1f, -1f, false, SceneAntialiasing.BALANCED);
		guiMainController = mainView.getController();
		guiMainController.initialize(stageController);
		stageController.setMainScene(mainScene);

		stage.setResizable(false);
		stage.setTitle("Constat Photo");
		stage.getIcons().add(new Image(Objects.requireNonNull(JavaFXApplication.class.getResourceAsStream("icon.png"))));
		stage.setScene(configuration.getTemplate() == null ? configScene : mainScene);

		stage.show();
	}
}