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

    private static final Iterable<JpegSegmentMetadataReader> READERS = List.of(new ExifReader());


    public static ImageMetadata getMetadata(File file) {
        try {
            Metadata metadata = JpegMetadataReader.readMetadata(file, READERS);
            Integer orientation = null;
            Integer width = null;
            Integer height = null;
            for (Directory directory : metadata.getDirectories()) {
                if (directory instanceof ExifIFD0Directory) {
                    orientation = directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
                } else if (directory instanceof ExifSubIFDDirectory) {
                    width = directory.getInt(ExifSubIFDDirectory.TAG_EXIF_IMAGE_WIDTH);
                    height = directory.getInt(ExifSubIFDDirectory.TAG_EXIF_IMAGE_HEIGHT);
                }
            }
            if (orientation == null)
                throw new IllegalStateException("impossible to read image orientation");
            if (width == null)
                throw new IllegalStateException("impossible to read image size");
            return new ImageMetadata(new ImageSize(width, height), ImageOrientation.fromExif(orientation));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}
