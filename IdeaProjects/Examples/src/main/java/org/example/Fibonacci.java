package org.example;


import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class Fibonacci {
    private static class Computation extends RecursiveTask<Integer> {
        private final int argument;

        public Computation(int argument) {
            this.argument = argument;
        }

        @Override
        protected Integer compute() {
            if (argument <= 1) {
                return argument;
            } else {
                Computation right = new Computation(argument - 2);
                right.fork();
                Computation left = new Computation(argument - 1);
                return left.compute() + right.join();
            }
        }
    }

    public static void main(String[] args) {
        ForkJoinPool pool = new ForkJoinPool();
        try {
            for (int i = 0; i < 20; ++i) {
                int result = pool.invoke(new Computation(i));
                System.out.println(i + " " + result);
            }
        } finally {
            pool.shutdown();
        }
    }
}
