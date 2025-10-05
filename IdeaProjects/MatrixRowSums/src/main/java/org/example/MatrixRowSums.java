package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.function.IntBinaryOperator;

public class MatrixRowSums {
    private static final int N_ROWS = 10;
    private static final int N_COLUMNS = 100;

    private static IntBinaryOperator matrixDefinition = (row, col) -> {
        int a = 2 * col + 1;
        return (row + 1) * (a % 4 - 2) * a;
    };

    private static void printRowSumsSequentially() {
        for (int r = 0; r < N_ROWS; ++r) {
            int sum = 0;
            for (int c = 0; c < N_COLUMNS; ++c) {
                sum += matrixDefinition.applyAsInt(r, c);
            }
            System.out.println(r + " -> " + sum);
        }
    }

    private static void printRowSumsInParallel() throws InterruptedException {
        List<Thread> threads = new ArrayList<>();
        ConcurrentHashMap<Integer, AtomicInteger> sums = new ConcurrentHashMap<>();
        AtomicIntegerArray computed = new AtomicIntegerArray(N_ROWS);
        for (int r = 0; r < N_ROWS; r++) {
            sums.put(r,new AtomicInteger());
        }
        for (int c = 0; c < N_COLUMNS; ++c) {
            final int myColumn = c;
            threads.add(new Thread(() -> {
                // TODO
                for (int r = 0; r < N_ROWS; r++) {
                    sums.computeIfPresent(r, (key, value) -> {
                        value.addAndGet(matrixDefinition.applyAsInt(key, myColumn));
                        return value;
                    });

                    if (computed.incrementAndGet(r) == N_COLUMNS) {
                        System.out.println(r + " -> " + sums.get(r));
                    }
                }
            }));
        }
        for (Thread t : threads) {
            t.start();
        }

        // TODO

        try {
            for (Thread t : threads) {
                t.join();
            }
        } catch (InterruptedException e) {
            for (Thread t : threads) {
                t.interrupt();
            }
            throw e;
        }

    }

    public static void main(String[] args) {
        try {
            System.out.println("-- Sequentially --");
            printRowSumsSequentially();
            System.out.println("-- In parallel --");
            printRowSumsInParallel();
            System.out.println("-- End --");
        } catch (InterruptedException e) {
            System.err.println("Main interrupted.");
        }
    }
}