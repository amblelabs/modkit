package dev.drtheo.queue.api;

import java.util.Deque;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;
import java.util.function.Supplier;

import dev.drtheo.scheduler.api.common.Scheduler;
import dev.drtheo.scheduler.api.TimeUnit;
import dev.drtheo.scheduler.api.common.TaskStage;
import org.jetbrains.annotations.Nullable;

public class ActionQueue implements Finishable {

    private final Deque<Consumer<Finishable>> steps = new ConcurrentLinkedDeque<>();

    public ActionQueue() {

    }

    public ActionQueue thenRun(ActionQueue other) {
        return thenRun(f -> other.thenRun(f::finish).execute());
    }

    public ActionQueue thenRun(Optional<ActionQueue> other) {
        if (other.isEmpty())
            return this;

        return thenRun(other.get());
    }

    public ActionQueue thenRun(Consumer<Finishable> consumer) {
        this.steps.add(consumer);
        return this;
    }

    public ActionQueue apply(Consumer<ActionQueue> consumer) {
        consumer.accept(this);
        return this;
    }

    public ActionQueue thenRun(@Nullable Runnable runnable) {
        if (runnable == null)
            return this;

        return this.thenRun(f -> {
            runnable.run();
            f.finish();
        });
    }

    public ActionQueue firstRun(Consumer<Finishable> consumer) {
        this.steps.addFirst(consumer);
        return this;
    }

    public ActionQueue firstRun(@Nullable Runnable runnable) {
        if (runnable == null)
            return this;

        return this.firstRun(f -> {
            runnable.run();
            f.finish();
        });
    }

    public ActionQueue thenRunSteps(Supplier<Boolean> step, TaskStage stage, TimeUnit unit, int period, int maxTime) {
        return this.thenRun(f -> Scheduler.get().runTaskTimer(t -> {
            boolean shouldContinue = true;
            long start = System.currentTimeMillis();

            while (shouldContinue) {
                shouldContinue = System.currentTimeMillis() - start < maxTime;

                if (step.get()) {
                    t.cancel();

                    if (f != null)
                        f.finish();

                    return;
                }
            }
        }, stage, unit, period));
    }

    public ActionQueue thenRunSteps(Supplier<Boolean> step, TaskStage stage, TimeUnit unit, int period) {
        return this.thenRunSteps(step, stage, unit, period, 2);
    }

    public ActionQueue execute() {
        Consumer<Finishable> consumer = this.steps.poll();

        if (consumer != null)
            consumer.accept(this);

        return this;
    }

    @Override
    public void finish() {
        this.execute();
    }
}