package org.bfreuden.docxgen;

import javafx.util.Pair;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ImageWriter {


    public static void writeJPG(BufferedImage image, OutputStream outputStream, float compressionQuality) throws IOException {
        // prepare target image writer
        JPEGImageWriteParam jpegParams = new JPEGImageWriteParam(null);
        jpegParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        jpegParams.setCompressionQuality(compressionQuality);
        javax.imageio.ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
        ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(outputStream);
        writer.setOutput(imageOutputStream);
        // write target image
        writer.write(null, new IIOImage(image, null, null), jpegParams);
        writer.dispose();
    }

    public static void writeJPG(BufferedImage image, File outputImage, float compressionQuality) throws IOException {
        try (OutputStream outputStream = new FileOutputStream(outputImage)) {
            // prepare target image writer
            JPEGImageWriteParam jpegParams = new JPEGImageWriteParam(null);
            jpegParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            jpegParams.setCompressionQuality(compressionQuality);
            javax.imageio.ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
            ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(outputStream);
            writer.setOutput(imageOutputStream);
            // write target image
            writer.write(null, new IIOImage(image, null, null), jpegParams);
            writer.dispose();
        }
    }

}
