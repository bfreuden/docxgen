package org.bfreuden.docxgen;

import javafx.application.Application;

import java.util.Locale;
import java.util.Map;

public class Main {
	public static void main(final String[] args) {

		// Better looking
		System.setProperty("prism.lcdtext", "false");

		Locale.setDefault(Locale.FRANCE);

		Application.launch(JavaFXApplication.class, args);
	}
}
