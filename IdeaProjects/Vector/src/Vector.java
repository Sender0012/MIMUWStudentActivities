

import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

@SuppressWarnings("unused")
public class Vector {
    private static final int SUM_CHUNK_LENGTH = 10;
    private static final int DOT_CHUNK_LENGTH = 10;

    private final int[] elements;

    public Vector(int length) {
        elements = new int[length];
    }

    public Vector(int[] elements) {
        this.elements = Arrays.copyOf(elements, elements.length);
    }

    private final Vector sumSequential(Vector other) {
        if (this.elements.length != other.elements.length) {
            throw new IllegalArgumentException("Vector lengths differ.");
        }
        Vector result = new Vector(this.elements.length);
        for (int i = 0; i < result.elements.length; ++i) {
            result.elements[i] = this.elements[i] + other.elements[i];
        }
        return result;
    }

    private final int dotSequential(Vector other) {
        if (this.elements.length != other.elements.length) {
            throw new IllegalArgumentException("Vector lengths differ.");
        }
        int result = 0;
        for (int i = 0; i < this.elements.length; ++i) {
            result += this.elements[i] * other.elements[i];
        }
        return result;
    }

    private static class SumHelper implements Runnable {
        private final Vector left;
        private final Vector right;
        private final Vector result;
        private final int begin;
        private final int end;

        public SumHelper(Vector left, Vector right, Vector result, int begin, int end) {
            this.left = left;
            this.right = right;
            this.result = result;
            this.begin = begin;
            this.end = end;
        }

        @Override
        public void run() {
            // FIXME: implement
            for (int i = begin ; i <= end; i++) {
                result.elements[i] = left.elements[i] + right.elements[i];
            }

        }
    }

    public Vector sum(Vector v) throws InterruptedException {
        if (elements.length != v.elements.length) {
            throw new IllegalArgumentException("Vector lengths differ.");
        }
        Vector result = new Vector(elements.length);

        ArrayList<Thread> threads = new ArrayList<>();

        // FIXME: implement
        Thread newThread;
        int threadsNum = 0;
        for (int i = 0; i < elements.length; i += SUM_CHUNK_LENGTH) {
            int end = Math.min(i + SUM_CHUNK_LENGTH - 1, elements.length - 1);
            SumHelper task = new SumHelper(this, v, result, i, end);
            Thread t = new Thread(task);
            threads.add(t);
            t.start();
        }

        try {
            for(Thread thread : threads) {
                thread.join();
            }
        }catch (InterruptedException e ) {
            System.err.println(e.getMessage());
            throw new InterruptedException("Sum was interrupted. " + e.getMessage());
        }

        return result;
    }

    private static class DotHelper implements Runnable {

        private final Vector left;
        private final Vector right;
        private final int begin;
        private final int end;
        private final int[] result;
        private final int resultPosition;

        public DotHelper(Vector left, Vector right, int begin, int end, int[] result, int resultPosition) {
            this.left = left;
            this.right = right;
            this.begin = begin;
            this.end = end;
            this.result = result;
            this.resultPosition = resultPosition;
        }

        @Override
        public void run() {
            // FIXME: implement
            for (int i = begin ; i <= end; i++) {
                result[resultPosition] += left.elements[i] * right.elements[i];
            }
        }
    }

    public int dot(Vector v) throws InterruptedException {
        if (elements.length != v.elements.length) {
            throw new IllegalArgumentException("Vector lengths differ.");
        }

        int nThreads = elements.length / DOT_CHUNK_LENGTH; // This part was changed
        int[] partialResults = new int[nThreads];
        int total = 0;

        // FIXME: implement
        Thread newThread;
        for (int i = 0; i < nThreads; i++) {
            newThread = new Thread(new DotHelper(this,
                    v,
                    i *  DOT_CHUNK_LENGTH,
                    i * DOT_CHUNK_LENGTH + 9,
                    partialResults,
                    i));
            newThread.start();
            newThread.join();
        }

        for(int i = nThreads * DOT_CHUNK_LENGTH ; i < elements.length; i++) {
            total += elements[i] * v.elements[i];
        }
        for(int i : partialResults) {
            total += i;
        }


        return total;
    }

    // ----------------------- TESTS -----------------------

    @Override
    public String toString() {
        return Arrays.toString(elements);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Vector)) {
            return false;
        }
        Vector other = (Vector) obj;
        return Arrays.equals(this.elements, other.elements);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.elements);
    }

    private static final Random random = new Random(42);

    private static Vector generateRandomVector(int length) {
        int[] a = new int[length];
        for (int i = 0; i < length; ++i) {
            a[i] = random.nextInt(10);
        }
        return new Vector(a);
    }

    public static void main(String[] args) {
        try {
            for (int length : new int[] { 33, 30 }) {
                Vector a = generateRandomVector(length);
                System.out.println("A:        " + a);
                Vector b = generateRandomVector(length);
                System.out.println("B:        " + b);

                Vector cSequential = a.sumSequential(b);
                Vector c = a.sum(b);

                if (!c.equals(cSequential)) {
                    System.out.println("Sum error!");
                    System.out.println("Expected: " + cSequential);
                    System.out.println("Got:      " + c);
                } else {
                    System.out.println("Sum OK:   " + c);
                }

                int d = a.dot(b);
                int dotSequential = a.dotSequential(b);
                if (d != dotSequential) {
                    System.out.println("Dot error! Expected " + dotSequential + ", got " + d + ".");
                } else {
                    System.out.println("Dot OK: " + d);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("computations interrupted");
        }
    }
}