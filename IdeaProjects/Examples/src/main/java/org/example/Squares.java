package org.example;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Squares {
    private static final int N_COUNTING_WORKERS = 4;
    private static final int N_COUNTING_TASKS = 10;
    private static final Duration MAX_SLEEP_TIME = Duration.ofMillis(10);
    private static final Random RANDOM = new Random();

    private static void getSomeSleep() throws InterruptedException {
        Thread.sleep(RANDOM.nextLong(MAX_SLEEP_TIME.toMillis()));
    }

    private static class CountingTask implements Callable<Integer> {
        private final int value;

        private CountingTask(int value) {
            this.value = value;
        }

        @Override
        public Integer call() throws InterruptedException {
            getSomeSleep();
            return value * value;
        }
    }

    private static class WritingTask implements Runnable {
        private final Future<Integer> future;

        public WritingTask(Future<Integer> future) {
            this.future = future;
        }

        @Override
        public void run() {
            try {
                getSomeSleep();
                System.out.println(future.get());
            } catch (InterruptedException e) {
                System.err.println("Writing's sleep interrupted.");
            } catch (ExecutionException e) {
                System.err.println("Counting task interrupted.");
            }
        }
    }

    public static void main(String[] args) {
        ExecutorService countingPool = Executors.newFixedThreadPool(N_COUNTING_WORKERS);
        ExecutorService writingPool = Executors.newFixedThreadPool(N_COUNTING_WORKERS);
        try {
            for (int i = 1; i <= N_COUNTING_TASKS; ++i) {
                Callable<Integer> work = new CountingTask(i);
                Future<Integer> future = countingPool.submit(work);
                writingPool.submit(new WritingTask(future));
            }
        } finally {
            countingPool.shutdown();
            writingPool.shutdown();
        }
    }
}