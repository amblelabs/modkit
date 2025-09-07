package dev.drtheo.scheduler;

import dev.drtheo.scheduler.api.common.Scheduler;
import net.fabricmc.api.ModInitializer;

public class SchedulerMod implements ModInitializer {

    @Override
    public void onInitialize() {
        Scheduler.init();
    }
}
