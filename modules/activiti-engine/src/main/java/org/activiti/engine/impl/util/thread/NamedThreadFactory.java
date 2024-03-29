package org.activiti.engine.impl.util.thread;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NamedThreadFactory implements ThreadFactory {

    private static final Logger                   logger                   = LoggerFactory.getLogger(NamedThreadFactory.class);
    private final static UncaughtExceptionHandler uncaughtExceptionHandler = new ActivitiUncaughtExceptionHandler(logger);
    private final static String                   DEFAULT_NAME             = "ActivitiExecutor";
    private String                                threadName;
    private AtomicInteger                         threadNumber             = new AtomicInteger(0);
    private boolean                               daemon                   = true;

    public NamedThreadFactory(){
        this(DEFAULT_NAME, true);
    }

    public NamedThreadFactory(String name){
        this(name, true);
    }

    public NamedThreadFactory(String name, boolean daemon){
        this.threadName = name;
        this.daemon = daemon;
    }

    public Thread newThread(Runnable r) {
        Thread t = new Thread(r, threadName + "-" + threadNumber.getAndIncrement());
        t.setDaemon(daemon);
        t.setUncaughtExceptionHandler(uncaughtExceptionHandler);
        return t;
    }

}