package org.example;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;
import java.util.function.IntBinaryOperator;

public class VectorStream {
    private static final int STREAM_LENGTH = 10;
    private static final int VECTOR_LENGTH = 100;

    private static final CyclicBarrier barrier = new CyclicBarrier(VECTOR_LENGTH);
    private static final Semaphore sumMutex = new Semaphore(1, true);
    private static final Semaphore newSumMutex = new Semaphore(1, true);

    private static Integer sum = 0;
    private static Integer newSum = 0;
    /**
     * Function that defines how vectors are computed: the i-th element depends on
     * the previous sum and the index i.
     * The sum of elements in the previous vector is initially given as zero.
     */
    private final static IntBinaryOperator vectorDefinition = (previousSum, i) -> {
        int a = 2 * i + 1;
        return (previousSum / VECTOR_LENGTH + 1) * (a % 4 - 2) * a;
    };

    private static void computeVectorStreamSequentially() {
        int[] vector = new int[VECTOR_LENGTH];
        int sum = 0;
        for (int vectorNo = 0; vectorNo < STREAM_LENGTH; ++vectorNo) {
            for (int i = 0; i < VECTOR_LENGTH; ++i) {
                vector[i] = vectorDefinition.applyAsInt(sum, i);
            }
            sum = 0;
            for (int x : vector) {
                sum += x;
            }
            System.out.println(vectorNo + " -> " + sum);
        }
    }

    private static class Counter implements Runnable {
        private final Integer vectorSlot;

        private Counter(Integer vectorSlot) {
            this.vectorSlot = vectorSlot;
        }

        @Override
        public void run() {
            try {
                for (int vectorNo = 0; vectorNo < STREAM_LENGTH; vectorNo++) {
                    sumMutex.acquire();
                    int tempSum = sum;
                    sumMutex.release();
                    int newValue = vectorDefinition.applyAsInt(tempSum, vectorSlot);
                    newSumMutex.acquire();
                    newSum += newValue;
                    newSumMutex.release();
                    barrier.await(); // waiting for all parts of the vector to calculate

                    if(vectorSlot == 0) {
                        System.out.println(vectorNo + " -> " + newSum);
                        sum = newSum;
                        newSum = 0;
                    }
                    barrier.await();
                }
            }catch (BrokenBarrierException | InterruptedException e) {
                System.err.println(Thread.currentThread().getName() + " interrupted.");
            }

        }
    }

    private static void computeVectorStreamInParallel() throws InterruptedException {
        // FIXME: implement, using VECTOR_LENGTH threads.
        if (VECTOR_LENGTH <= 0) return;

        Thread[] threads = new Thread[VECTOR_LENGTH];


        for (int i = 0 ; i < VECTOR_LENGTH; i++) {
            threads[i] = new Thread(new Counter(i));
        }

        for (int i = 0 ; i < VECTOR_LENGTH; i++) {
            threads[i].start();
        }

        try {
            for (int i = 0 ; i < VECTOR_LENGTH; i++) {
                threads[i].join();
            }
        }catch (InterruptedException e) {
            System.err.println("Main interrupted!");
        }
    }

    public static void main(String[] args) {
        try {
            System.out.println("-- Sequentially --");
            computeVectorStreamSequentially();
            System.out.println("-- Parallel --");
            computeVectorStreamInParallel();
            System.out.println("-- End --");
        } catch (InterruptedException e) {
            System.err.println("Main interrupted.");
        }
    }
}