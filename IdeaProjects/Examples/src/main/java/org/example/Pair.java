package org.example;

import java.time.Duration;
import java.time.Instant;

public class Pair<T>{
    private T first;
    private T second;

    public Pair(T first, T second) {
        this.first = first;
        this.second = second;
    }

    // These two methods are marked as `synchronized`,
    // meaning that they take a lock on a whole instance
    // for the duration of the call.

    public synchronized void swap() {
        T a = first;
        first = second;
        second = a;
    }

    public synchronized boolean areBothEqual() {
        return first.equals(second);
    }

    /* == Tests == */

    private static final Duration DURATION = Duration.ofSeconds(3);

    public static void main(String[] args) {
        Instant finishTime = Instant.now().plus(DURATION);

        Pair<Integer> pair = new Pair<>(1, 2);

        Thread t = new Thread(() -> {
            while (Instant.now().isBefore(finishTime)) {
                pair.swap();
            }
        });
        t.start();

        while (Instant.now().isBefore(finishTime)) {
            if (pair.areBothEqual()) {
                System.out.println("Swap was unsafe, we saw first==second!.");
            }
        }

        try {
            t.join();
        } catch (InterruptedException e) {
            System.err.println("Main thread interrupted.");
        }
        System.out.println("Finished.");
    }
}
