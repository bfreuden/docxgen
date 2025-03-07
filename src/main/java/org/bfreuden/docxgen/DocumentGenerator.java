package org.bfreuden.docxgen;

import javafx.beans.property.DoubleProperty;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DocumentGenerator {

    private static final Logger LOGGER = Logger.getLogger( "DocumentGenerator" );
    private final ExecutorService service;
    private final ImageMetadataReader metadataReader = new ImageMetadataReader();

    public DocumentGenerator(ExecutorService service) {
        this.service = service;
    }

    public void generate(Configuration configuration, File selectedDirectory, DoubleProperty progress) {
        LOGGER.info("generate");
        new Thread(() -> {
            try {
                File[] images = selectedDirectory.listFiles((dir, name) -> name.toLowerCase().endsWith(".jpg"));
                ArrayList<Future<?>> futures = new ArrayList<>();
                for (int i=0 ; i<images.length ; i++) {
                    int index = i;
                    futures.add(service.submit(() -> {
                        ImageMetadata metadata = metadataReader.getMetadata(images[index]);
                        synchronized (progress) {
                            progress.setValue((1.0f * (index + 1) / images.length));
                        }
                    }));

                }
                for (Future<?> future: futures) {
                    future.get();
                }
                LOGGER.info("generate done");
            } catch (Throwable ex) {
                LOGGER.log(Level.SEVERE, "unable to generate ", ex);
            }
        }).start();
    }

}
