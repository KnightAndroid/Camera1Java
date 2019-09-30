package com.knight.cameraone.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author created by knight
 * @organize
 * @Date 2019/9/29 9:31
 * @descript:线程帮助类
 */

public class ThreadPoolUtil {
    private static ExecutorService threadPool = Executors.newCachedThreadPool();

    /**
     * 在线程池执行一个任务
     * @param runnable 任务
     */
    public static void execute(Runnable runnable){
        threadPool.execute(runnable);
    }

}
