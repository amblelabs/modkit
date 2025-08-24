package dev.amble.lib.client.bedrock;

import dev.amble.lib.AmbleKit;
import dev.drtheo.scheduler.api.TimeUnit;
import dev.drtheo.scheduler.api.client.ClientScheduler;
import dev.drtheo.scheduler.api.common.Scheduler;
import dev.drtheo.scheduler.api.common.TaskStage;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class BedrockAnimationTracker {
	public final BedrockAnimation animation;
	private final Consumer<Runnable> ticker;
	private final boolean reversed;
	private int ticks;

	/**
	 * Creates a new Bedrock Animation Tracker.
	 * @param animation the animation to track
	 * @param nextTick a method that will be called on the next tick to continue the animation.
	 * @param reversed whether the animation should be played in reverse.
	 */
	public BedrockAnimationTracker(BedrockAnimation animation, Consumer<Runnable> nextTick, boolean reversed) {
		this.animation = animation;
		this.ticker = nextTick;
		this.ticks = 0;
		this.reversed = reversed;
	}

	/**
	 * Creates a new Bedrock Animation Tracker using the default scheduler for the given side.
	 * @param animation the animation to track
	 * @param reversed whether the animation should be played in reverse.
	 */
	public BedrockAnimationTracker(BedrockAnimation animation, boolean reversed) {
		this(animation, getSchedulerTicker(true), reversed);
	}

	private static Consumer<Runnable> getSchedulerTicker(boolean isClient) {
		if (isClient) {
			return runnable -> ClientScheduler.get().runTaskLater(runnable, TimeUnit.TICKS, 1);
		}

		return runnable -> Scheduler.get().runTaskLater(runnable, TaskStage.END_SERVER_TICK, TimeUnit.TICKS, 1);
	}

	public void start() {
		if (this.isDone()) {
			this.ticks = 0;
		}

		if (ticks != 0) {
			AmbleKit.LOGGER.error("Attempted to start a Bedrock Animation Tracker that is already running! {}", this);
			return;
		}

		this.tick();
	}

	private void tick() {
		ticks++;

		if (this.isDone()) return;

		this.ticker.accept(this::tick);
	}

	public boolean isDone() {
		if (animation.shouldLoop) {
			return false;
		}
		return ticks >= animation.animationLength * 20; // Convert seconds to ticks
	}

	public boolean isStarted() {
		return ticks > 0;
	}

	public int getTicks() {
		if (reversed) {
			return (int) (animation.animationLength * 20 - ticks);
		}

		return ticks;
	}
}
