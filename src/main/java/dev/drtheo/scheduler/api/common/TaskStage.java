package dev.drtheo.scheduler.api.common;

import dev.drtheo.scheduler.api.task.Task;
import net.minecraft.server.world.ServerWorld;

@FunctionalInterface
public interface TaskStage {

    TaskStage END_SERVER_TICK = (scheduler, task) -> scheduler.endServerTickTasks.add(task);
    TaskStage START_SERVER_TICK = (scheduler, task) -> scheduler.startServerTickTasks.add(task);

    static TaskStage startWorldTick(ServerWorld world) {
        return (scheduler, task) ->
                Scheduler.getDeque(world, scheduler.startWorldTickTasks).add(task);
    }

    static TaskStage endWorldTick(ServerWorld world) {
        return (scheduler, task) ->
                Scheduler.getDeque(world, scheduler.endWorldTickTasks).add(task);
    }

    void apply(Scheduler scheduler, Task<?> task);

    default Task<?> add(Scheduler scheduler, Task<?> task) {
        this.apply(scheduler, task);
        return task;
    }
}
