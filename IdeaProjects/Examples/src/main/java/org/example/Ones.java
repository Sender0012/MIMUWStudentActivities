package org.example;


import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class Ones {
    private static final int ARRAY_SIZE = 1000000;
    private static final int MAX_WORK_UNFORKED = 10;

    private static class Action extends RecursiveAction {
        private final int[] array;
        private final int begin;
        private final int end;

        public Action(int[] array, int begin, int end) {
            this.array = array;
            this.begin = begin;
            this.end = end;
        }

        @Override
        protected void compute() {
            if (end - begin < MAX_WORK_UNFORKED) {
                for (int i = begin; i < end; ++i) {
                    array[i] = 1;
                }
            } else {
                int middle = (end + begin) / 2;
                Action right = new Action(array, middle, end);
                right.fork();
                Action left = new Action(array, begin, middle);
                left.compute();
                right.join();
            }
        }
    }

    public static void main(String[] args) {
        int[] array = new int[ARRAY_SIZE];
        ForkJoinPool pool = new ForkJoinPool();
        try {
            pool.invoke(new Action(array, 0, ARRAY_SIZE));
            int sum = 0;
            for (int x : array) {
                sum += x;
            }
            System.out.println(sum);
        } finally {
            pool.shutdown();
        }
    }
}