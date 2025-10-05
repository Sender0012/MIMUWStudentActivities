package org.example;

import java.util.ArrayList;
import java.util.List;

public class Swapper<T> {
    private T value;
    private final List<T> history;

    public Swapper(T value) {
        this.value = value;
        this.history = new ArrayList<>();
        this.history.add(value);
    }

    public synchronized void swapValue(T expectedValue, T newValue) throws InterruptedException {
        while (!value.equals(expectedValue)) {
            wait();
        }
        value = newValue;
        history.add(newValue);
        notifyAll();
    }

    public synchronized List<T> getHistory() {
        return List.copyOf(history);
    }
}