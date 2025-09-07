package dev.drtheo.scheduler.api.task;

import java.util.function.Consumer;

public class RepeatingSimpleTask extends CountUpTask<Consumer<Task<?>>> {

    public RepeatingSimpleTask(Consumer<Task<?>> runnable, long period) {
        super(runnable, period);
    }

    @Override
    public boolean run() {
        this.runnable.accept(this);
        this.counter = 0;

        return this.cancelled;
    }
}