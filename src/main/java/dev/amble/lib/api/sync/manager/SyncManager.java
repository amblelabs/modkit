package dev.amble.lib.api.sync.manager;

import java.util.Collection;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jetbrains.annotations.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;

import dev.amble.lib.AmbleKit;
import dev.amble.lib.api.sync.Exclude;
import dev.amble.lib.api.sync.RootComponent;
import dev.amble.lib.api.sync.handler.ComponentManager;
import dev.amble.lib.api.sync.handler.ComponentRegistry;
import dev.amble.lib.api.sync.handler.SyncComponent;
import dev.amble.lib.data.DirectedBlockPos;
import dev.amble.lib.data.DirectedGlobalPos;
import dev.amble.lib.data.gson.*;

public abstract class SyncManager<P extends RootComponent, C> {
    protected final RootMap<P> lookup = new RootMap<>();

    protected final Gson networkGson;
    protected final Gson fileGson;

    protected SyncManager() {
        this.networkGson = this.getNetworkGson(this.createGsonBuilder(Exclude.Strategy.NETWORK)).create();
        this.fileGson = this.getFileGson(this.createGsonBuilder(Exclude.Strategy.FILE)).create();
    }

    protected GsonBuilder createGsonBuilder(Exclude.Strategy strategy) {
        return new GsonBuilder().setExclusionStrategies(new ExclusionStrategy() {
                    @Override
                    public boolean shouldSkipField(FieldAttributes field) {
                        Exclude exclude = field.getAnnotation(Exclude.class);

                        if (exclude == null)
                            return false;

                        Exclude.Strategy excluded = exclude.strategy();
                        return excluded == Exclude.Strategy.ALL || excluded == strategy;
                    }

                    @Override
                    public boolean shouldSkipClass(Class<?> clazz) {
                        return false;
                    }
                })
                .registerTypeAdapter(DirectedGlobalPos.class, DirectedGlobalPos.serializer())
                .registerTypeAdapter(DirectedBlockPos.class, DirectedBlockPos.serializer())
                .registerTypeAdapter(NbtCompound.class, new NbtSerializer())
                .registerTypeAdapter(ItemStack.class, new ItemStackSerializer())
                .registerTypeAdapter(Identifier.class, new IdentifierSerializer())
                .registerTypeAdapter(GlobalPos.class, new GlobalPosSerializer())
                .registerTypeAdapter(BlockPos.class, new BlockPosSerializer())
                .registerTypeAdapter(RegistryKey.class, new RegistryKeySerializer())
                .registerTypeAdapter(ComponentManager.class, ComponentManager.serializer(this.getRegistry(), this.getManagerId()))
                .registerTypeAdapter(SyncComponent.IdLike.class, getRegistry().idSerializer());
    }

    protected GsonBuilder getNetworkGson(GsonBuilder builder) {
        return builder;
    }

    protected GsonBuilder getFileGson(GsonBuilder builder) {
//		if (!AmbleKit.CONFIG.SERVER.MINIFY_JSON)
//			builder.setPrettyPrinting();

        // /\
        builder.setPrettyPrinting();

        return builder;
//		return builder.registerTypeAdapter(Value.class, Value.serializer())
//				.registerTypeAdapter(BoolValue.class, BoolValue.serializer())
//				.registerTypeAdapter(IntValue.class, IntValue.serializer())
//				.registerTypeAdapter(RangedIntValue.class, RangedIntValue.serializer())
//				.registerTypeAdapter(DoubleValue.class, DoubleValue.serializer());
    }

    public void get(C c, UUID uuid, Consumer<P> consumer) {
        if (uuid == null)
            return; // ugh

        P result = this.lookup.get(uuid);

        if (result == null) {
            this.load(c, uuid, consumer);
            return;
        }

        consumer.accept(result);
    }

    /**
     * By all means a bad practice. Use {@link #get(Object, UUID, Consumer)}
     * instead. Ensures to return a {@link RootComponent} instance as fast as possible.
     * <p>
     * By using this method you accept the risk of the object not being on the
     * client.
     *
     * @deprecated Have you read the comment?
     */
    @Nullable @Deprecated
    public abstract P demand(C c, UUID uuid);

    public abstract void load(C c, UUID uuid, @Nullable Consumer<P> consumer);

    public void reset() {
        this.lookup.clear();
    }

    public Collection<UUID> ids() {
        return this.lookup.keySet();
    }

    public void forEach(Consumer<P> consumer) {
        this.lookup.forEach((uuid, t) -> consumer.accept(t));
    }

    public P find(Predicate<P> predicate) {
        for (P t : this.lookup.values()) {
            if (predicate.test(t))
                return t;
        }

        return null;
    }


    public Gson getNetworkGson() {
        return this.networkGson;
    }

    public Gson getFileGson() {
        return fileGson;
    }

    @FunctionalInterface
    public interface ContextManager<C, R> {
        R run(C c, SyncManager<?, C> manager);
    }

    public Identifier askPacket() {
        return createPacket("ask");
    }
    public Identifier sendPacket() {
        return createPacket("send");
    }
    public Identifier sendBulkPacket() {
        return createPacket("send_bulk");
    }
    public Identifier sendComponentPacket() {
        return createPacket("send_component");
    }
    public Identifier removePacket() {
        return createPacket("remove");
    }

    public Identifier createPacket(String id) {
        return AmbleKit.id(this.modId() + "/" + name() + "/" + id);
    }

    public abstract ComponentRegistry getRegistry();
    public abstract SyncComponent.IdLike getManagerId();
    public abstract String modId();
    public abstract String name();
}
