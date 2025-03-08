package org.bfreuden.docxgen;

import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageResizerTest {

    @Test
    public void testAllInOne() throws IOException {
        ImageResizerWriter.resizeAndWriteJPG(
                new File("src/test/data/P1250845.JPG"),
                new File("src/test/data/P1250845-small.JPG"),
                60,
                220,
                0.95f,
                true
        );
    }

    @Test
    public void testResizeAndWrite() throws IOException {
        BufferedImage target = ImageResizer.resizeJPG(
                new File("src/test/data/P1250845.JPG"),
                60,
                220,
                true
        );
        ImageWriter.writeJPG(target, new File("src/test/data/P1250845-small-2.JPG"), 0.95f);
    }
}
