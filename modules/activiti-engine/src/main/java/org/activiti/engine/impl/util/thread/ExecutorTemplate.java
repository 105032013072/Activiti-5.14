package org.activiti.engine.impl.util.thread;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ExecutorTemplate {

    private volatile ExecutorCompletionService comletions = null;
    
    private volatile List<Future>              futures    = null;

    private static ThreadPoolExecutor sharedThreadPoolExecutor = new ThreadPoolExecutor(2,
			20,
            60,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue(2 * 2),
            new NamedThreadFactory(),
            new ThreadPoolExecutor.CallerRunsPolicy());
    
    public ExecutorTemplate(){
    	this(sharedThreadPoolExecutor);
    }

    public ExecutorTemplate(ThreadPoolExecutor executor){
        futures = Collections.synchronizedList(new ArrayList<Future>());
        comletions = new ExecutorCompletionService(executor);
    }

    public void submit(Runnable task) {
        Future future = comletions.submit(task, null);
        futures.add(future);
        check(future);
    }

    public void submit(Callable task) {
        Future future = comletions.submit(task);
        futures.add(future);
        check(future);
    }

    private void check(Future future) {
        if (future.isDone()) {
            // 立即判断一次，因为使用了CallerRun可能当场跑出结果，针对有异常时快速响应，而不是等跑完所有的才抛异常
            try {
                future.get();
            } catch (Throwable e) {
                // 取消完之后立马退出
            	cancelAllFutures();
                throw new RuntimeException(e);
            }
        }
    }

    public synchronized List<?> waitForResult() {
        List result = new ArrayList();
        RuntimeException exception = null;
        // 开始处理结果
        int index = 0;
        while (index < futures.size()) { // 循环处理发出去的所有任务
            try {
                Future future = comletions.take();// 它也可能被打断
                result.add(future.get());
            } catch (Throwable e) {
                exception = new RuntimeException(e);
                // 如何一个future出现了异常，就退出
                break;
            }

            index++;
        }

        if (exception != null) {
            // 小于代表有错误，需要对未完成的记录进行cancel操作，对已完成的结果进行收集，做重复录入过滤记录
        	cancelAllFutures();
            throw exception;
        } else {
            return result;
        }
    }

    public void cancelAllFutures() {
        for (Future future : futures) {
            if (!future.isDone() && !future.isCancelled()) {
                future.cancel(true);
            }
        }
    }

    public void clear() {
        futures.clear();
    }

}