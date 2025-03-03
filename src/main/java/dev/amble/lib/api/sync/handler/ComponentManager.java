package dev.amble.lib.api.sync.handler;

import java.lang.reflect.Array;
import java.util.Map;
import java.util.function.Consumer;

import com.google.gson.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;

import dev.amble.lib.AmbleKit;
import dev.amble.lib.api.sync.Exclude;
import dev.amble.lib.api.sync.RootComponent;
import dev.amble.lib.data.enummap.EnumMap;

public class ComponentManager extends SyncComponent implements TickingComponent {
    @Exclude
    private final EnumMap<SyncComponent.IdLike, SyncComponent> handlers;
    protected final ComponentRegistry registry;

    public ComponentManager(SyncComponent.IdLike id, ComponentRegistry registry) {
        super(id);

        this.registry = registry;
        handlers = new EnumMap<>(registry::lookup,
                size -> (SyncComponent[]) Array.newInstance(SyncComponent.class, size));
    }

    @Override
    public void onCreate() {
        this.registry.fill(this::createHandler);
    }

    @Override
    protected void onInit(InitContext ctx) {
        this.forEach(component -> SyncComponent.init(component, this.parent, ctx));
    }


    @Override
    public void postInit(InitContext ctx) {
        this.forEach(component -> component.postInit(ctx));
    }

    private void forEach(Consumer<SyncComponent> consumer) {
        for (SyncComponent component : this.handlers.getValues()) {
            if (component == null)
                continue;

            consumer.accept(component);
        }
    }

    private void createHandler(SyncComponent component) {
        this.handlers.put(component.getId(), component);
    }

    /**
     * Called on the END of a servers tick
     *
     * @param server
     *            the current server
     */
    public void tick(MinecraftServer server) {
        this.forEach(component -> {
            if (!(component instanceof TickingComponent tickable))
                return;

            try {
                tickable.tick(server);
            } catch (Exception e) {
                AmbleKit.LOGGER.error("Ticking failed for {} | {}", component.getId().name(), component.parent().getUuid().toString(), e);
            }
        });
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void tick(MinecraftClient client) {
        this.forEach(component -> {
            if (!(component instanceof TickingComponent tickable))
                return;

            try {
                tickable.tick(client);
            } catch (Exception e) {
                AmbleKit.LOGGER.error("Ticking failed for {} | {}", component.getId().name(), component.parent().getUuid().toString(), e);
            }
        });
    }

    /**
     * @deprecated Use {@link RootComponent#handler(IdLike)}
     */
    @Deprecated
    @ApiStatus.Internal
    @SuppressWarnings("unchecked")
    public <C extends SyncComponent> C get(IdLike id) {
        return (C) this.handlers.get(id);
    }

    @Override
    public void dispose() {
        super.dispose();

        this.forEach(SyncComponent::dispose);
        this.handlers.clear();
    }

    @ApiStatus.Internal
    public void set(SyncComponent t) {
        this.handlers.put(t.getId(), t);
    }

    public Object serializer() {
        return new Serializer(this.registry, this.getId());
    }
    public static Object serializer(ComponentRegistry r, IdLike id) {
        return new Serializer(r, id);
    }

    static class Serializer implements JsonSerializer<ComponentManager>, JsonDeserializer<ComponentManager> {
        private final IdLike id;
        private final ComponentRegistry registry;

        public Serializer(ComponentRegistry registry, IdLike id) {
            this.id = id;
            this.registry = registry;
        }

        @Override
        public ComponentManager deserialize(JsonElement json, java.lang.reflect.Type type,
                                                 JsonDeserializationContext context) throws JsonParseException {
            ComponentManager manager = new ComponentManager(id, registry);
            Map<String, JsonElement> map = json.getAsJsonObject().asMap();

            ComponentRegistry registry = manager.registry;

            for (Map.Entry<String, JsonElement> entry : map.entrySet()) {
                String key = entry.getKey();
                JsonElement element = entry.getValue();

                IdLike id = registry.get(key);

                if (id == null) {
                    AmbleKit.LOGGER.error("Can't find a component id with name '{}'!", key);
                    continue;
                }

                manager.set(context.deserialize(element, id.clazz()));
            }

            for (int i = 0; i < manager.handlers.size(); i++) {
                if (manager.handlers.get(i) != null)
                    continue;

                IdLike id = registry.get(i);
                AmbleKit.LOGGER.debug("Appending new component {}", id);

                manager.set(id.create());
            }

            return manager;
        }

        @Override
        public JsonElement serialize(ComponentManager manager, java.lang.reflect.Type type,
                                     JsonSerializationContext context) {
            JsonObject result = new JsonObject();

            manager.forEach(component -> {
                IdLike idLike = component.getId();

                if (idLike == null) {
                    AmbleKit.LOGGER.error("Id was null for {}", component.getClass());
                    return;
                }

                result.add(idLike.name(), context.serialize(component));
            });

            return result;
        }
    }
}
