package org.bfreuden.docxgen;

import java.util.concurrent.*;

public class DaemonBlockingExecutorServiceFactory {

    public static ExecutorService create(int nbThreads) {
        var workQueue = new LinkedBlockingQueue<Runnable>(nbThreads);
        var pool = new ThreadPoolExecutor(nbThreads, nbThreads, 10, TimeUnit.SECONDS, workQueue, r -> {
            Thread t = Executors.defaultThreadFactory().newThread(r);
            t.setDaemon(true);
            return t;
        });
        pool.setRejectedExecutionHandler((r, executor) -> {
            try {
                workQueue.put(r);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        return pool;
    }

}
