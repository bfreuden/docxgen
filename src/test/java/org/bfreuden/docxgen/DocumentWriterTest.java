package org.bfreuden.docxgen;

import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class DocumentWriterTest {

    @Test
    public void test() throws IOException {
        try (DocumentWriter documentWriter = new DocumentWriter(new File("data/constat Isabelle Commissaire 2.docx"))) {
            documentWriter.partiallyRewrite(new File("target/out.docx"));

            resizeAndAddImage(documentWriter, "src/test/data/P1250845.JPG");
            resizeAndAddImage(documentWriter, "src/test/data/P1250845-2.JPG");
            resizeAndAddImage(documentWriter, "src/test/data/P1250845.JPG");
            resizeAndAddImage(documentWriter, "src/test/data/P1250845-2.JPG");
            documentWriter.finalizeDocument();
        }

    }

    private static void resizeAndAddImage(DocumentWriter documentWriter, String filename) throws IOException {
        int targetMaxDimensionInMillimeter = 60;
        BufferedImage target = ImageResizer.resizeJPG(
                new File(filename),
                targetMaxDimensionInMillimeter,
                220,
                true
        );
        documentWriter.appendImage(filename, target, targetMaxDimensionInMillimeter, 0.95f);
    }
}
