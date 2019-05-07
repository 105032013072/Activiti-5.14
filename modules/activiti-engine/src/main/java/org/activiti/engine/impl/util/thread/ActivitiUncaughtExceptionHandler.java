package org.activiti.engine.impl.util.thread;

import org.slf4j.Logger;

public class ActivitiUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    private Logger logger;

    public ActivitiUncaughtExceptionHandler(Logger logger){
        this.logger = logger;
    }

    public void uncaughtException(Thread t, Throwable e) {
        logger.error("uncaught exception", e);
    }
}