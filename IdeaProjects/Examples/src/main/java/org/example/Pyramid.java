package org.example;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Pyramid {
    private static final int N_TASKS = 101;
    private static final int N_WORKERS = 8;

    public static void main(String[] args) {
        ExecutorService pool = Executors.newFixedThreadPool(N_WORKERS);
        List<Callable<Integer>> calculations = new ArrayList<>();
        for (int i = 1; i <= N_TASKS; ++i) {
            final int myId = i;
            Callable<Integer> task = () -> { return myId * myId; };
            calculations.add(task);
        }
        try {
            List<Future<Integer>> futures = pool.invokeAll(calculations);
            int sum = 0;
            for (Future<Integer> futureResult : futures) {
                sum += futureResult.get();
            }
            System.out.println(sum + " " + N_TASKS * (N_TASKS + 1) * (2 * N_TASKS + 1) / 6);
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Calculations interrupted.");
        } finally {
            pool.shutdown();
        }
    }
}