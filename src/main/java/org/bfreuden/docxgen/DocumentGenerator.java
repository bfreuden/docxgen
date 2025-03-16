package org.bfreuden.docxgen;

import javafx.beans.property.DoubleProperty;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
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
        DocumentWriter documentWriter = new DocumentWriter(new File(configuration.getTemplate()));
        try {
            documentWriter.partiallyRewrite(new File(selectedDirectory.getName() + ".docx"));
            File[] images = selectedDirectory.listFiles((dir, name) -> name.toLowerCase().endsWith(".jpg"));
            if (images == null)
                return;
            ArrayList<Future<BufferedImage>> futures = new ArrayList<>();
            AtomicInteger counter = new AtomicInteger();
            for (File image: images) {
                futures.add(imageConversionExecutor.submit(() -> {
                    try {
                        LOGGER.info("resizing image: " + image);
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
            for (Future<?> future: futures) {
                future.get();
            }
            LOGGER.info("document generation complete!");
        } catch (Throwable ex) {
            // FIXME manage errors
            LOGGER.log(Level.SEVERE, "unable to generate document", ex);
        }
    }

}
