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

            String filename = "src/test/data/P1250845.JPG";
            int targetMaxDimensionInMillimeter = 60;
            BufferedImage target = ImageResizer.resizeJPG(
                    new File(filename),
                    targetMaxDimensionInMillimeter,
                    220,
                    true
            );
            documentWriter.appendImage(filename, target, targetMaxDimensionInMillimeter, 0.95f);
            documentWriter.finalizeDocument();
        }

    }
}
