package org.bfreuden.docxgen;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.imaging.jpeg.JpegSegmentMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifReader;

public class ImageOrientation {

    public static void main(String[] args) throws ImageProcessingException, IOException {
        var file = new File("P1250845.JPG");
        Iterable<JpegSegmentMetadataReader> readers = List.of(new ExifReader());

        Metadata metadata = JpegMetadataReader.readMetadata(file, readers);
        for (Directory directory : metadata.getDirectories()) {

            //
            // Each Directory stores values in Tag objects
            //
            for (Tag tag : directory.getTags()) {
                System.out.println(tag);
            }
        }


    }
}
