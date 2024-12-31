package org.bfreuden.docxgen.gui;

import javafx.application.HostServices;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.bfreuden.docxgen.Configuration;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ConfigController implements Initializable {

	private StageController stageController;
	private Configuration configuration;
	@FXML
	private Button selectTemplateBtn;
	@FXML
	private Button openTemplateBtn;
	@FXML
	private Button closeParametersBtn;

	public BooleanProperty isTemplateSelected = new SimpleBooleanProperty(false);
	private HostServices hostServices;

	public void initialize(StageController stageController) {
		this.stageController = stageController;
	}

	@FXML
	private void selectTemplate() {
		FileChooser docxChooser = new FileChooser();
		docxChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Fichiers Word", "*.docx"));
		docxChooser.setInitialDirectory(new File(System.getProperty("user.home")));
		File selectedDocx = docxChooser.showOpenDialog(stageController.getStage());
		if (selectedDocx != null) {
            try {
                this.configuration.setTemplate(selectedDocx.getAbsolutePath());
				this.isTemplateSelected.setValue(true);
				selectTemplateBtn.setText("Sélectionner une autre matrice");

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
	}

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            this.configuration = Configuration.configuration();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
		isTemplateSelected.setValue(this.configuration.getTemplate() != null);

		openTemplateBtn.visibleProperty().bind(isTemplateSelected);

		selectTemplateBtn.setText(this.configuration.getTemplate() != null ? "Sélectionner une autre matrice" : "Sélectionner une matrice");

		closeParametersBtn.disableProperty().bind(isTemplateSelected.not());
		closeParametersBtn.getStyleClass().add(this.configuration.getTemplate() != null ? "btn-enabled" : "btn-disabled");
    }

	public void openTemplate(ActionEvent actionEvent) {
		hostServices.showDocument(this.configuration.getTemplate());
	}

	public void setGetHostController(HostServices hostServices) {
		this.hostServices = hostServices;
	}

	public void closeParameters(ActionEvent actionEvent) {
		this.stageController.showMainScene();
	}
}