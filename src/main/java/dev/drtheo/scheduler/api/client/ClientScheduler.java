package dev.drtheo.scheduler.api.client;

import dev.drtheo.scheduler.api.TimeUnit;
import dev.drtheo.scheduler.api.task.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.util.Util;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class ClientScheduler {

    private static final ExecutorService service = Util.getMainWorkerExecutor();

    private static ClientScheduler self;

    private final Deque<Task<?>> tasks = new ConcurrentLinkedDeque<>();

    private ClientScheduler() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> tasks.removeIf(Task::tick));
    }

    public static void init() {
        if (self != null)
            return;

        self = new ClientScheduler();
    }

    public Task<?> runTaskLater(Runnable runnable, TimeUnit unit, long delay) {
        return add(new SimpleTask(runnable, TimeUnit.TICKS.from(unit, delay)));
    }

    public Task<?> runAsyncTaskLater(Runnable runnable, TimeUnit unit, long delay) {
        return add(new AsyncSimpleTask(service, runnable, TimeUnit.TICKS.from(unit, delay)));
    }

    public Task<?> runTaskTimer(Consumer<Task<?>> runnable, TimeUnit unit, long period) {
        return add(new RepeatingSimpleTask(runnable, TimeUnit.TICKS.from(unit, period)));
    }

    public Task<?> runAsyncTaskTimer(Consumer<Task<?>> runnable, TimeUnit unit, long period) {
        return add(new AsyncRepeatingSimpleTask(service, runnable, TimeUnit.TICKS.from(unit, period)));
    }

    protected Task<?> add(Task<?> task) {
        this.tasks.add(task);
        return task;
    }

    public static ClientScheduler get() {
        return self;
    }
}