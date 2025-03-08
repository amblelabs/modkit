package dev.amble.lib.api.sync.link;

import dev.amble.lib.api.sync.Disposable;
import dev.amble.lib.api.sync.RootComponent;
import dev.amble.lib.api.sync.manager.SyncManager;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public class RootRef<R extends RootComponent> implements Disposable {
	private final LoadFunc load;
	private UUID id;

	private R cached;

	public RootRef(UUID id, LoadFunc load) {
		this.id = id;
		this.load = load;
	}

	public RootRef(R tardis, LoadFunc load) {
		if (tardis != null)
			this.id = tardis.getUuid();

		this.load = load;
		this.cached = tardis;
	}

	public R get() {
		if (this.cached != null && !this.shouldInvalidate())
			return this.cached;

		this.cached = (R) this.load.apply(this.id);
		return this.cached;
	}

	private boolean shouldInvalidate() {
		return this.cached.isAged();
	}

	public UUID getId() {
		return id;
	}

	public boolean isPresent() {
		return this.get() != null;
	}

	public boolean isEmpty() {
		return this.get() == null;
	}

	/**
	 * @return the result of the function, {@literal null} otherwise.
	 */
	public <T> Optional<T> apply(Function<R, T> consumer) {
		if (this.isPresent())
			return Optional.of(consumer.apply(this.cached));

		return Optional.empty();
	}

	public void ifPresent(Consumer<R> consumer) {
		if (this.isPresent())
			consumer.accept(this.get());
	}

	@Override
	public void dispose() {
		this.cached = null;
	}

	public boolean contains(R tardis) {
		return this.get() == tardis;
	}

	public static <R extends RootComponent> RootRef<R> createAs(Entity entity, R tardis, SyncManager source) {
		return new RootRef<>(tardis, real -> (RootComponent) source.with(entity, (o, manager) -> manager.demand(o, real)));
	}

	public static <R extends RootComponent> RootRef<R> createAs(Entity entity, UUID uuid, SyncManager source) {
		return new RootRef<>(uuid, real -> (RootComponent) source.with(entity, (o, manager) -> manager.demand(o, real)));
	}

	public static <R extends RootComponent> RootRef<R> createAs(BlockEntity blockEntity, R tardis, SyncManager source) {
		return new RootRef<>(tardis, real -> (RootComponent) source.with(blockEntity, (o, manager) -> manager.demand(o, real)));
	}

	public static <R extends RootComponent> RootRef<R> createAs(BlockEntity blockEntity, UUID uuid, SyncManager source) {
		return new RootRef<>(uuid, real -> (RootComponent) source.with(blockEntity, (o, manager) -> manager.demand(o, real)));
	}

	public static <R extends RootComponent> RootRef<R> createAs(World world, R tardis, SyncManager source) {
		return new RootRef<>(tardis, real -> (RootComponent) source.with(world, (o, manager) -> manager.demand(o, real)));
	}

	public static <R extends RootComponent> RootRef<R> createAs(World world, UUID uuid, SyncManager source) {
		return new RootRef<>(uuid, real -> (RootComponent) source.with(world, (o, manager) -> manager.demand(o, real)));
	}


	public interface LoadFunc extends Function<UUID, RootComponent> {
	}
}
