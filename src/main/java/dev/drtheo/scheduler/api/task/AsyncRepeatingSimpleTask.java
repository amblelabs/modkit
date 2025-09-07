package dev.drtheo.scheduler.api.task;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

public class AsyncRepeatingSimpleTask extends RepeatingSimpleTask {

    private final ExecutorService service;

    public AsyncRepeatingSimpleTask(ExecutorService service, Consumer<Task<?>> runnable, long delay) {
        super(runnable, delay);

        this.service = service;
    }

    @Override
    public boolean run() {
        CompletableFuture<Void> future = new CompletableFuture<Boolean>().completeAsync(super::run).thenAccept(success -> {
            if (success)
                this.cancel();
        });

        this.service.submit(() -> future.get());
        return this.cancelled;
    }
}