package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.IntBinaryOperator;


public class MatrixRowSumsPooled {
    private static final int N_ROWS = 10;
    private static final int N_COLUMNS = 100;
    private static final int N_THREADS = 4;

    private static final IntBinaryOperator matrixDefinition = (row, col) -> {
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

    public static void printRowSumsInParallel() throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(N_THREADS);
        try {
            // FIXME: implement
            List<Future<Integer>> futures = new ArrayList<>();
            for (int i = 0; i < N_ROWS; i++) {
                for (int j = 0; j < N_COLUMNS; j++) {
                    Callable<Integer> count = new Task(i, j);
                    futures.add(pool.submit(count));
                }
            }

            int index = 0;
            int[] rowSums = new int[N_ROWS];
            for(Future<Integer> future : futures) {
                int r = index / N_COLUMNS;
                rowSums[r] += future.get();
                index++;
            }

            for (int i = 0; i < N_ROWS; i++) {
                System.out.println(i + " -> " + rowSums[i]);
            }
//            throw new RuntimeException("not implemented");
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } finally {
            pool.shutdown();
        }
    }

    private static class Task implements Callable<Integer> {
        private final int rowNo;
        private final int columnNo;

        private Task(int rowNo, int columnNo) {
            this.rowNo = rowNo;
            this.columnNo = columnNo;
        }

        @Override
        public Integer call() {
            // FIXME: implement
            return matrixDefinition.applyAsInt(rowNo, columnNo);
//            throw new RuntimeException("not implemented");
        }
    }

    public static void main(String[] args) {
        try {
            System.out.println("-- Sequentially --");
            printRowSumsSequentially();
            System.out.println("-- In a FixedThreadPool --");
            printRowSumsInParallel();
            System.out.println("-- End --");
        } catch (InterruptedException e) {
            System.err.println("Main interrupted.");
        }
    }
}