package dev.drtheo.scheduler.api;

public enum TimeUnit {

    TICKS(TimeUnit.TICK_SCALE),
    SECONDS(TimeUnit.SECOND_SCALE),
    MINUTES(TimeUnit.MINUTE_SCALE);

    private static final long TICK_SCALE = 1;
    private static final long SECOND_SCALE = 20 * TICK_SCALE;
    private static final long MINUTE_SCALE = 60 * SECOND_SCALE;

    private final long scale;

    TimeUnit(long scale) {
        this.scale = scale;
    }

    public long from(TimeUnit from, long time) {
        if (this == from)
            return time;

        return from.scale * time / this.scale;
    }
}