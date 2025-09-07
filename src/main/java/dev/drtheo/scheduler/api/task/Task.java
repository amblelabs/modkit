package dev.drtheo.scheduler.api.task;

public abstract class Task<R> {

    protected final R runnable;
    protected boolean cancelled = false;

    public Task(R runnable) {
        this.runnable = runnable;
    }

    /**
     * @return {@literal true} if the task is finished
     */
    public boolean tick() {
        return this.cancelled;
    }

    public void cancel() {
        this.cancelled = true;
    }

    /**
     * @return {@literal true} if the task should be finished
     */
    public abstract boolean run();
}