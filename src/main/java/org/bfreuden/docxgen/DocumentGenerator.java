package org.bfreuden.docxgen;

import javafx.beans.property.DoubleProperty;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class DocumentGenerator {

    private final ExecutorService service;

    public DocumentGenerator(ExecutorService service) {
        this.service = service;
    }

    public void generate(Configuration configuration, File selectedDirectory, DoubleProperty progress) {
        new Thread(() -> {
            ArrayList<Future<?>> futures = new ArrayList<>();
            for (int i=0; i<100 ; i++) {
                futures.add(service.submit(() -> {
                    try {
                        synchronized (progress) {
                            progress.wait(500);
                            var value = progress.getValue();
                            progress.setValue(value + 0.01);
                        }
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }));
            }
        }).start();

    }

}
