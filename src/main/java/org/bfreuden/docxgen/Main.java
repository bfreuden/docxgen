package org.bfreuden.docxgen;

import javafx.application.Application;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Main {

	public static void main(final String[] args) throws IOException {

		// Better looking
		System.setProperty("prism.lcdtext", "false");

		final InputStream inputStream = Main.class.getResourceAsStream("/logging.properties");
		LogManager.getLogManager().readConfiguration(inputStream);
		FileHandler fileHandler = new FileHandler();
		Locale.setDefault(Locale.FRANCE);

		Application.launch(JavaFXApplication.class, args);
	}
}
