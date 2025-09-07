package dev.drtheo.scheduler.api.task;

public class SimpleTask extends CountUpTask<Runnable> {

    public SimpleTask(Runnable runnable, long delay) {
        super(runnable, delay);
    }

    @Override
    public boolean run() {
        this.runnable.run();
        return true;
    }
}