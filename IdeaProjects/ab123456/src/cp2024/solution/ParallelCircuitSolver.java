package cp2024.solution;

import cp2024.circuit.*;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class ParallelCircuitSolver implements CircuitSolver {


    private static final Integer FIXED_NUM_OF_THREADS = 20;
    private final ExecutorService pool = Executors.newCachedThreadPool();
    private Boolean isStopped = false;


    @Override
    public CircuitValue solve(Circuit c) {
        /* FIX ME */
        if (!isStopped){
            Callable<Boolean> solve = () -> {
                return solver(c.getRoot());
            };
            Future<Boolean> boolResult = pool.submit(solve);

            return new ParallelCircuitValue(boolResult);
        }
        // if the solver was stopped we return always interrupted value
        return new ParallelCircuitValue();

    }

    @Override
    public void stop() {
        /*FIX ME*/
        if (isStopped) return;

        isStopped = true;

        pool.shutdownNow(); // Shutting down all created threads


    }

    //Checking each node type and sending to appropriate method
    public Boolean solver(CircuitNode root) throws InterruptedException {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }
        if (root.getType() == NodeType.LEAF)
            return ((LeafNode) root).getValue();

        CircuitNode[] args = root.getArgs();
        return switch (root.getType()) {
            case IF -> solveIF(args);
            case NOT -> solveNOT(args);
            case AND, OR, GT, LT -> solveFirstOccurrenceOfValue(root);
            default -> throw new RuntimeException();
        };
    }

    // the same consider next argument
    private Boolean solveNOT(CircuitNode[] args) throws InterruptedException {
        // not nodes have exactly one argument
        boolean result = solver(args[0]);

        return !result;
    }

    // calculating AND, OR, GT, LT
    private Boolean solveFirstOccurrenceOfValue(CircuitNode root) throws InterruptedException {

        if (Thread.currentThread().isInterrupted())
            throw new InterruptedException();

        // we want to check threads that finish work first
        CompletionService<Boolean> service = new ExecutorCompletionService<>(pool);
        CircuitNode[] args = root.getArgs();

        List<Future<Boolean>> futureResults = new ArrayList<>();
        try{
            CountDownLatch latch;

            Boolean wantedResult;
            boolean finalResult;
            AtomicInteger resultCounter = new AtomicInteger(0);
            int x = 1;

            switch (root.getType()) {
                case AND:
                    // the same method to calculate as OR,but we are looking for false to return false
                    wantedResult = false;
                    finalResult = true;
                    break;
                case OR:
                    wantedResult = true;
                    finalResult = false;
                    break;
                case GT:
                    // the same method to calculate as OR,but we must repeat it until we find all "trues" needed
                    x = ((ThresholdNode)root).getThreshold() + 1;
                    if(x <= 0)
                        return true;
                    if (x > args.length)
                        return false;
                    wantedResult = true;
                    finalResult = false;
                    break;
                case LT:
                    // the same method to calculate as GT,but now we are looking for false's to return true
                    x = ((ThresholdNode)root).getThreshold() - 1;
                    if(x < 0)
                        return false;
                    if (x >= args.length)
                        return true;

                    x = args.length - x;
                    wantedResult = false;
                    finalResult = false;
                    break;
                default:
                    throw new RuntimeException();
            }

            for (CircuitNode arg : args) {

                Callable<Boolean> result = () -> solver(arg);
                // remembering all Futures<> for possible cleanup later
                futureResults.add(service.submit(result));
            }

            for (int i = 0; i < args.length; i++) {
                if (Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException();
                }
                Future<Boolean> firstResult = service.take();
                if (Objects.equals(firstResult.get(), wantedResult)) {
                    x--;
                }
                // if we find enough values we change final result
                if (x == 0) {
                    finalResult = !finalResult;
                    for (Future<Boolean> futureResult : futureResults) {
                        futureResult.cancel(true);
                    }
                    break;
                }
            }

            return finalResult;
        } catch (ExecutionException e) {
            throw new InterruptedException();
        } finally {
            // cleanup
            for (Future<Boolean> futureResult : futureResults) {
                futureResult.cancel(true);
            }
        }
    }

    private Boolean solveIF(CircuitNode[] args) throws InterruptedException {
        if(Thread.currentThread().isInterrupted())
            throw new InterruptedException();
        CompletionService<PositionBoolean> service = new ExecutorCompletionService<>(pool);
        Future<PositionBoolean>[] results = new Future[3];
        try {

            boolean finalResult = false;

            for (int i = 0; i < args.length;i++) {
                if(Thread.currentThread().isInterrupted())
                    throw new InterruptedException();

                int finalI = i;

                Callable<PositionBoolean> result = () -> {
                    return new PositionBoolean(solver(args[finalI]), finalI);
                };
                results[i] = service.submit(result);
            }
            int countAB = 2;
            for (int i = 0; i < args.length;i++) {
                if(Thread.currentThread().isInterrupted())
                    throw new InterruptedException();

                Future<PositionBoolean> firstResult = service.take();
                if (firstResult.get().num() == 0) {
                    if (firstResult.get().bool1()) {
                        results[2].cancel(true);
                        if(Thread.currentThread().isInterrupted())
                            throw new InterruptedException();

                        finalResult = results[1].get().bool1();
                    }else {
                        results[1].cancel(true);
                        if(Thread.currentThread().isInterrupted())
                            throw new InterruptedException();

                        finalResult = results[2].get().bool1();
                    }
                    i = args.length;
                }else {
                    countAB--;
                    if(countAB == 0 && results[1].get().bool1() == results[2].get().bool1()) {
                        results[0].cancel(true);
                        finalResult = results[1].get().bool1();
                        i = args.length;
                    }
                }
            }

            return finalResult;
        } catch (ExecutionException e) {
            throw new InterruptedException();
        }finally {
            for (Future<PositionBoolean> result : results) {
                result.cancel(true);
            }

        }

    }


}
