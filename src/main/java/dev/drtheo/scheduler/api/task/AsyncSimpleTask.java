package dev.drtheo.scheduler.api.task;

import java.util.concurrent.ExecutorService;

public class AsyncSimpleTask extends SimpleTask {

    private final ExecutorService service;

    public AsyncSimpleTask(ExecutorService service, Runnable runnable, long delay) {
        super(runnable, delay);

        this.service = service;
    }

    @Override
    public boolean run() {
        this.service.submit(super::run);
        return true;
    }
}