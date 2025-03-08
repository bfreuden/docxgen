package org.bfreuden.docxgen;

import javafx.util.Pair;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageResizer {

    static ImageSize getTargetImageSize(int targetMaxDimensionInMillimeter, int dpi, ImageMetadata metadata) {
        int targetWidth, targetHeight;
        if (metadata.size().width() > metadata.size().height()) {
            targetWidth = Math.round(1.0f * targetMaxDimensionInMillimeter * dpi / 2.54f / 10);
            targetHeight = Math.round(1.0f * metadata.size().height() / metadata.size().width() * targetWidth);

        } else {
            targetHeight = Math.round(1.0f * targetMaxDimensionInMillimeter * dpi / 2.54f / 10);
            targetWidth = Math.round(1.0f * metadata.size().width() / metadata.size().height() * targetHeight);
        }
        return new ImageSize(targetWidth, targetHeight);
    }

    public static BufferedImage resizeJPG(File sourceImage, int targetMaxDimensionInMillimeter, int dpi, boolean rotate) throws IOException {
        ImageMetadata metadata = ImageMetadataReader.getMetadata(sourceImage);
        ImageSize targetImageSize = getTargetImageSize(targetMaxDimensionInMillimeter, dpi, metadata);
        return resizeJPG(sourceImage, metadata.orientation(), targetImageSize, rotate);
    }


    public static BufferedImage resizeJPG(File sourceImage, ImageOrientation sourceImageOrientation, ImageSize targetImageSize, boolean rotate) throws IOException {
        // read source image
        BufferedImage source = ImageIO.read(sourceImage);
        // resize image
        return resizeJPG(source, sourceImageOrientation, targetImageSize, rotate);
    }


    static BufferedImage resizeJPG(BufferedImage sourceImage, ImageOrientation sourceImageOrientation, ImageSize targetImageSize, boolean rotate) {
        int targetWidth = targetImageSize.width();
        int targetHeight = targetImageSize.height();
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = resizedImage.createGraphics();
        graphics2D.drawImage(sourceImage, 0, 0, targetWidth, targetHeight,null);
        graphics2D.dispose();
        return rotate ? rotate(resizedImage, sourceImageOrientation) : resizedImage;
    }

    private static BufferedImage rotate(BufferedImage source, ImageOrientation sourceImageOrientation) {
        if (sourceImageOrientation == null || sourceImageOrientation.equals(ImageOrientation.STRAIGHT))
            return source;
        int width = source.getWidth();
        int height = source.getHeight();
        Pair<Integer, Integer> rotatedSize = sourceImageOrientation.rotatedImageSize(width, height);
        Integer newWidth = rotatedSize.getKey();
        Integer newHeight = rotatedSize.getValue();
        BufferedImage rotated = new BufferedImage(newWidth, newHeight, source.getType());
        Graphics2D graphic = rotated.createGraphics();
        graphic.translate((newWidth-width)/2, (newHeight-height)/2);
        graphic.rotate(sourceImageOrientation.rotationAngle, 1.0f*width/2, 1.0f*height/2);
        graphic.drawRenderedImage(source, null);
        graphic.dispose();
        return rotated;
    }

}
