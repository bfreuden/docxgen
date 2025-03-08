package org.bfreuden.docxgen;

import javafx.util.Pair;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ImageResizerWriter {

    public static void resizeAndWriteJPG(File sourceImage, File targetImage, int targetMaxDimensionInMillimeter, int dpi, float compressionQuality, boolean rotate) throws IOException {
        ImageMetadata metadata = ImageMetadataReader.getMetadata(sourceImage);
        ImageSize targetImageSize = ImageResizer.getTargetImageSize(targetMaxDimensionInMillimeter, dpi, metadata);
        resizeAndWriteJPG(sourceImage, metadata.orientation(), targetImage, targetImageSize, compressionQuality, rotate);
    }



    public static void resizeAndWriteJPG(File sourceImage, File targetImage, ImageSize targetImageSize, float compressionQuality, boolean rotate) throws IOException {
        ImageMetadata metadata = ImageMetadataReader.getMetadata(sourceImage);
        resizeAndWriteJPG(sourceImage, metadata.orientation(), targetImage, targetImageSize, compressionQuality, rotate);
    }

    public static void resizeAndWriteJPG(File sourceImage, ImageOrientation sourceImageOrientation, File targetImage, ImageSize targetImageSize, float compressionQuality, boolean rotate) throws IOException {
        try (OutputStream outputStream = new FileOutputStream(targetImage)) {
            resizeAndWriteJPG(sourceImage, sourceImageOrientation, outputStream, targetImageSize, compressionQuality, rotate);
        }
    }

    public static void resizeAndWriteJPG(File sourceImage, ImageOrientation sourceImageOrientation, OutputStream targetImage, ImageSize targetImageSize, float compressionQuality, boolean rotate) throws IOException {
        // read source image
        BufferedImage source = ImageIO.read(sourceImage);
        // resize image
        BufferedImage target = ImageResizer.resizeJPG(source, sourceImageOrientation, targetImageSize, rotate);
        // write image
        ImageWriter.writeJPG(target, targetImage, compressionQuality);
    }


}
