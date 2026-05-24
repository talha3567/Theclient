package net.wurstclient.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class MinPriorityThreadFactory
implements ThreadFactory {
    private static final AtomicInteger poolNumber = new AtomicInteger(1);
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;

    public MinPriorityThreadFactory() {
        this.group = Thread.currentThread().getThreadGroup();
        this.namePrefix = "pool-min-" + poolNumber.getAndIncrement() + "-thread-";
    }

    @Override
    public Thread newThread(Runnable r) {
        String name = this.namePrefix + this.threadNumber.getAndIncrement();
        Thread t = new Thread(this.group, r, name);
        t.setDaemon(true);
        t.setPriority(1);
        return t;
    }

    public static ExecutorService newFixedThreadPool() {
        return Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), new MinPriorityThreadFactory());
    }
}
