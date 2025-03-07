package org.bfreuden.docxgen;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;

public class ImageMetadataReaderTest {

    @Test
    public void test() {
        ImageMetadataReader reader = new ImageMetadataReader();
        ImageMetadata metadata = reader.getMetadata(new File("src/test/data/P1250845.JPG"));
        Assertions.assertEquals(8, metadata.orientation());
        Assertions.assertEquals(5184, metadata.width());
        Assertions.assertEquals(3888, metadata.height());
    }
}
