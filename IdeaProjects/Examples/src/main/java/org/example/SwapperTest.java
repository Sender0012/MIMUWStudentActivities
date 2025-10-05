package org.example;


import java.util.ArrayList;
import java.util.List;

public class SwapperTest {
    private static final Swapper<Integer> swapper = new Swapper<>(0);
    private static final int CYCLES = 5;

    private static class Worker implements Runnable {
        private final int expectedValue;
        private final int newValue;

        private Worker(int expectedValue, int newValue) {
            this.expectedValue = expectedValue;
            this.newValue = newValue;
        }

        @Override
        public void run() {
            try {
                for (int i = 0; i < CYCLES; ++i) {
                    swapper.swapValue(expectedValue, newValue);
                }
            } catch (InterruptedException e) {
                System.err.println("Worker interrupted.");
            }
        }

    }

    public static void main(String[] args) {
        List<Thread> workers = new ArrayList<>();

        //  42-->┐
        //  |    |
        //  └<---0---->1
        //       |     |
        //       └<----2

        workers.add(new Thread(new Worker(0, 1)));
        workers.add(new Thread(new Worker(1, 2)));
        workers.add(new Thread(new Worker(2, 0)));
        workers.add(new Thread(new Worker(0, 42)));
        workers.add(new Thread(new Worker(42, 0)));

        for (Thread t : workers) {
            t.start();
        }

        try {
            for (Thread t : workers) {
                t.join();
            }
            for (int n : swapper.getHistory()) {
                System.out.print(" " + n);
            }
            System.out.println();
        } catch (InterruptedException e) {
            for (Thread t : workers) {
                t.interrupt();
            }
            System.err.println("Main interrupted.");
        }
    }

}