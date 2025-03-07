package org.bfreuden.docxgen;

import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.imaging.jpeg.JpegSegmentMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifReader;
import com.drew.metadata.exif.ExifSubIFDDirectory;

import java.io.File;
import java.util.List;

public class ImageMetadataReader {

    private Iterable<JpegSegmentMetadataReader> readers = List.of(new ExifReader());


    public ImageMetadata getMetadata(File file) {
        try {
            Iterable<JpegSegmentMetadataReader> readers = List.of(new ExifReader());
            Metadata metadata = JpegMetadataReader.readMetadata(file, readers);
            Integer orientation = null;
            Integer width = null;
            Integer height = null;
            for (Directory directory : metadata.getDirectories()) {
                if (directory instanceof ExifIFD0Directory) {
                    orientation = directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
                    System.out.println(orientation);
                } else if (directory instanceof ExifSubIFDDirectory) {
                    width = directory.getInt(ExifSubIFDDirectory.TAG_EXIF_IMAGE_WIDTH);
                    height = directory.getInt(ExifSubIFDDirectory.TAG_EXIF_IMAGE_HEIGHT);
                }
            }
            if (orientation == null)
                throw new IllegalStateException("impossible de lire l'orientation de l'image");
            if (width == null)
                throw new IllegalStateException("impossible de lire la largeur de l'image");
            return new ImageMetadata(width, height, orientation);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}
