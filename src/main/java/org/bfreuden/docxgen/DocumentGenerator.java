package org.bfreuden.docxgen;

import javafx.beans.property.DoubleProperty;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DocumentGenerator {

    private static final Logger LOGGER = Logger.getLogger( "DocumentGenerator" );
    private ExecutorService imageConversionExecutor;
    private ExecutorService documentWriterExecutor;

    public DocumentGenerator(ExecutorService imageConversionExecutor, ExecutorService documentWriterExecutor) {
        this.imageConversionExecutor = imageConversionExecutor;
        this.documentWriterExecutor = documentWriterExecutor;
    }

    public void generate(Configuration configuration, File selectedDirectory, DoubleProperty progress) {
        LOGGER.info("generating document...");
        try {
            File[] images = selectedDirectory.listFiles((dir, name) -> name.toLowerCase().endsWith(".jpg"));
            ArrayList<Future<BufferedImage>> futures = new ArrayList<>();
            AtomicInteger counter = new AtomicInteger();
            for (int i=0 ; i<images.length ; i++) {
                File image = images[i];
                futures.add(imageConversionExecutor.submit(() -> {
                    try {
                        BufferedImage resized = ImageResizer.resizeJPG(image, configuration.getImageSize(), configuration.getDPI(), true);
                        int currentProgress = counter.incrementAndGet();
                        synchronized (progress) {
                            progress.setValue((1.0f * currentProgress / images.length));
                        }
                        return resized;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }));

            }
            // https://stackoverflow.com/questions/8082980/inserting-image-into-docx-using-openxml-and-setting-the-size
            // emus per mm : 36000
            for (Future<?> future: futures) {
                future.get();
            }
            LOGGER.info("document generation complete!");
        } catch (Throwable ex) {
            LOGGER.log(Level.SEVERE, "unable to generate document", ex);
        }
    }

}
