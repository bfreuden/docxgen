package org.bfreuden.docxgen;

import javafx.beans.property.DoubleProperty;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DocumentGenerator {

    private static class ResizedImage {
        public final File image;
        public final int number;
        public final BufferedImage resized;

        public ResizedImage(File image, int number, BufferedImage resized) {
            this.image = image;
            this.number = number;
            this.resized = resized;
        }
    }
    private static final Logger LOGGER = Logger.getLogger( "DocumentGenerator" );
    private ExecutorService imageConversionExecutor;
    private ExecutorService documentWriterExecutor;

    public DocumentGenerator(ExecutorService imageConversionExecutor, ExecutorService documentWriterExecutor) {
        this.imageConversionExecutor = imageConversionExecutor;
        this.documentWriterExecutor = documentWriterExecutor;
    }

    public void generate(Configuration configuration, File selectedDirectory, DoubleProperty progress) {
        LOGGER.info("generating document...");
        try (DocumentWriter documentWriter = new DocumentWriter(new File(configuration.getTemplate()))) {
            documentWriter.partiallyRewrite(new File(selectedDirectory.getName() + ".docx"));
            File[] images = selectedDirectory.listFiles((dir, name) -> name.toLowerCase().endsWith(".jpg"));
            if (images == null)
                return;
            ArrayList<Future<ResizedImage>> futures = new ArrayList<>();
            AtomicInteger counter = new AtomicInteger();
            for (int i=0 ; i<images.length ; i++) {
                File image = images[i];
                int number = i;
                futures.add(imageConversionExecutor.submit(() -> {
                    try {
                        LOGGER.info("resizing image: " + image);
                        BufferedImage resized = ImageResizer.resizeJPG(image, configuration.getImageSize(), configuration.getDPI(), true);
                        int currentProgress = counter.incrementAndGet();
                        synchronized (progress) {
                            progress.setValue((1.0f * currentProgress / images.length));
                        }
                        return new ResizedImage(image, number, resized);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }));

            }
            // preserve image order
            int waitingForImageNumber = 0;
            HashMap<Integer, ResizedImage> resizedImages = new HashMap<>();
            for (Future<ResizedImage> future: futures) {
                ResizedImage currentResizedImage = future.get();
                LOGGER.info("image is resized: " + currentResizedImage.image);
                resizedImages.put(currentResizedImage.number, currentResizedImage);
                ResizedImage resizedImage;
                while ((resizedImage=resizedImages.get(waitingForImageNumber)) != null) {
                    LOGGER.info("appending image to doc: " + currentResizedImage.image);
                    documentWriter.appendImage(resizedImage.image.getName(), resizedImage.resized, configuration.getImageSize(), configuration.getCompressionQuality());
                    resizedImages.remove(waitingForImageNumber);
                    waitingForImageNumber++;
                }
            }
            LOGGER.info("finalizing document");
            documentWriter.finalizeDocument();
            LOGGER.info("document generation complete!");
        } catch (Throwable ex) {
            // FIXME manage errors
            LOGGER.log(Level.SEVERE, "unable to generate document", ex);
        }
    }

}
