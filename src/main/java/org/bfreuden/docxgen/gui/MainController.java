package org.bfreuden.docxgen.gui;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.stage.DirectoryChooser;
import org.bfreuden.docxgen.Configuration;
import org.bfreuden.docxgen.DaemonBlockingExecutorServiceFactory;
import org.bfreuden.docxgen.DocumentGenerator;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;

public class MainController implements Initializable {

	private StageController stageController;

	private BooleanProperty isDirectorySelected = new SimpleBooleanProperty(false);
	private BooleanProperty isProcessStarted = new SimpleBooleanProperty(false);
	private Configuration configuration;
	private DoubleProperty progressIndicator = new SimpleDoubleProperty(0);
	@FXML
	private ProgressBar progressBar;
	@FXML
	private Button createDocumentBtn;
	private ExecutorService executor;
	private File selectedDirectory;


	public void initialize(StageController stageController) {
		this.stageController = stageController;
	}

	private synchronized ExecutorService getOrCreateExecutor() {
		if (executor == null)
			executor = DaemonBlockingExecutorServiceFactory.create(Runtime.getRuntime().availableProcessors());
		return executor;
	}

	@FXML
	private void selectPhotoDirectory() throws IOException {
		var dirChooser = new DirectoryChooser();
		var lastDir = this.configuration.getLastPhotoDirectory();
		var lastDirFile = lastDir == null ? null : new File(lastDir);
		if (lastDirFile != null && lastDirFile.exists()) {
			dirChooser.setInitialDirectory(lastDirFile);
		}
		File selectedDirectory = dirChooser.showDialog(stageController.getStage());
		if (selectedDirectory != null) {
			this.configuration.setLastPhotoDirectory(selectedDirectory.getAbsolutePath());
		}
		this.isDirectorySelected.setValue(selectedDirectory != null);
		this.selectedDirectory = selectedDirectory;
	}

	@FXML
	public void openParameters(ActionEvent actionEvent) {
		this.stageController.showConfigScene();
	}

	@FXML
	public void createDocument(ActionEvent actionEvent) throws IOException {
		isProcessStarted.setValue(true);
		getOrCreateExecutor();

		var generator = new DocumentGenerator(getOrCreateExecutor());
		generator.generate(this.configuration, this.selectedDirectory, progressIndicator);
	}

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            this.configuration = Configuration.configuration();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
		createDocumentBtn.disableProperty().bind(isDirectorySelected.not());

		progressBar.visibleProperty().bind(isProcessStarted);
		progressBar.progressProperty().bind(progressIndicator);
    }

	@FXML
	public void exit(ActionEvent actionEvent) throws Exception {
		stageController.exit();
	}
}