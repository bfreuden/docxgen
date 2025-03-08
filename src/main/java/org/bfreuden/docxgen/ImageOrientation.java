package org.bfreuden.docxgen;

import javafx.util.Pair;

public enum ImageOrientation {

    STRAIGHT(0, 1),
    CLOCKWISE_90(Math.PI/2, 8),
    CLOCKWISE_180(Math.PI, 3),
    CLOCKWISE_270(3*Math.PI/2, 6);
    public final double angle;
    public final double rotationAngle;
    public final int exif;

    ImageOrientation(double angle, int exif) {
        this.angle = angle;
        this.rotationAngle = 2*Math.PI - angle;
        this.exif = exif;
    }


    public Pair<Integer, Integer> rotatedImageSize(int width, int height) {
        if (this.equals(STRAIGHT) || this.equals(CLOCKWISE_180))
            return new Pair<>(width, height);
        else
            return new Pair<>(height, width);
    }

    public static ImageOrientation fromExif(int exif) {
        for (ImageOrientation orientation: ImageOrientation.values())
            if (orientation.exif == exif)
                return orientation;
        throw new IllegalArgumentException("unknown exif orientation code: " + exif);
    }
}
