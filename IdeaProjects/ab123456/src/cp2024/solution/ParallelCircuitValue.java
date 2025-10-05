package cp2024.solution;

import cp2024.circuit.CircuitValue;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class ParallelCircuitValue implements CircuitValue {

    private final Future<Boolean> value;
    private boolean stopped;
    ParallelCircuitValue(Future<Boolean> value) {
        this.value = value;
        stopped = false;
    }

    // struct for creating values for stopped solver
    ParallelCircuitValue() {
        value = null;
        stopped = true;
    }

    @Override
    public boolean getValue() throws InterruptedException {
        /* FIX ME */
        if (stopped){
            throw new InterruptedException();
        }

        try {
            assert value != null; // ensuring that value is not null
            return value.get();
        } catch (ExecutionException e) {
            throw new InterruptedException();
        }


    }

    public void stopWaitingForValue() {
        stopped = true;
    }
}
