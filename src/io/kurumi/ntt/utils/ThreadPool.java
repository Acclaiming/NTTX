package io.kurumi.ntt.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPool {

    private static ExecutorService pool;

    static {
		
        pool = Executors.newFixedThreadPool(9);

    }

    public static void exec(Runnable runnable) {

        pool.execute(runnable);

    }

}
