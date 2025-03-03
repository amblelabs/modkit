package dev.amble.lib.api.sync.handler;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.google.gson.*;

import dev.amble.lib.AmbleKit;
import dev.amble.lib.register.Registry;

public abstract class ComponentRegistry implements Registry {
    private final Map<String, SyncComponent.IdLike> REGISTRY = new HashMap<>();
    private SyncComponent.IdLike[] LOOKUP;

    private boolean frozen = false;

    public void register(SyncComponent.IdLike id) {
        if (!id.creatable())
            return;

        id.index(REGISTRY.size());
        REGISTRY.put(id.name(), id);

        if (frozen)
            AmbleKit.LOGGER.error("Tried to init a component id after the registry got frozen: {}", id);
    }

    public void register(SyncComponent.IdLike[] idLikes) {
        for (SyncComponent.IdLike idLike : idLikes) {
            register(idLike);
        }
    }

    @Override
    public void onCommonInit() {
        register(ids());

        LOOKUP = (SyncComponent.IdLike[]) Array.newInstance(SyncComponent.IdLike.class, REGISTRY.size());
        REGISTRY.forEach((name, idLike) -> LOOKUP[idLike.index()] = idLike);

        this.frozen = true;
    }

    protected abstract SyncComponent.IdLike[] ids();

    public void fill(Consumer<SyncComponent> consumer) {
        for (SyncComponent.IdLike id : LOOKUP) {
            consumer.accept(id.create());
        }
    }

    public SyncComponent.IdLike get(String name) {
        return switch (name) {
            default -> REGISTRY.get(name);
        };
    }

    public String get(SyncComponent component) {
        return component.getId().name();
    }

    public SyncComponent.IdLike get(int index) {
        return LOOKUP[index];
    }

    public Collection<SyncComponent.IdLike> getValues() {
        return REGISTRY.values();
    }
    public SyncComponent.IdLike[] lookup() {
        return LOOKUP;
    }

    public Object idSerializer() {
        return new Serializer(this);
    }

    private record Serializer(ComponentRegistry registry)
            implements
            JsonSerializer<SyncComponent.IdLike>,
            JsonDeserializer<SyncComponent.IdLike> {

        @Override
        public SyncComponent.IdLike deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            return registry.get(json.getAsString());
        }

        @Override
        public JsonElement serialize(SyncComponent.IdLike src, Type typeOfSrc, JsonSerializationContext context) {
            return context.serialize(src.name());
        }
    }
}
