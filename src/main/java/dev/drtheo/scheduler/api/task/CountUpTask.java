package dev.drtheo.scheduler.api.task;

public abstract class CountUpTask<R> extends Task<R> {

    protected final long target;
    protected long counter;

    public CountUpTask(R runnable, long target) {
        super(runnable);
        this.target = target;
    }

    @Override
    public boolean tick() {
        if (super.tick())
            return true;

        if (this.counter >= this.target)
            return this.run();

        this.counter++;
        return false;
    }
}