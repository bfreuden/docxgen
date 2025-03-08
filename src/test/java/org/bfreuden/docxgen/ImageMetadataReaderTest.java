package org.bfreuden.docxgen;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;

public class ImageMetadataReaderTest {

    @Test
    public void test() {
        ImageMetadata metadata = ImageMetadataReader.getMetadata(new File("src/test/data/P1250845.JPG"));
        Assertions.assertEquals(ImageOrientation.CLOCKWISE_90, metadata.orientation());
        Assertions.assertEquals(5184, metadata.size().width());
        Assertions.assertEquals(3888, metadata.size().height());
    }
}
