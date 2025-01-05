module org.bfreuden.docxgen {
	requires javafx.base;
	requires javafx.controls;
	requires javafx.fxml;
	requires javafx.swing;
	requires javafx.graphics;
	requires org.controlsfx.controls;
	requires com.dlsc.formsfx;
	requires org.kordamp.bootstrapfx.core;
	requires java.desktop;
    requires metadata.extractor;

    opens org.bfreuden.docxgen to javafx.fxml, java.desktop;
	exports org.bfreuden.docxgen;

	opens org.bfreuden.docxgen.gui to javafx.fxml;
	exports org.bfreuden.docxgen.gui;
}