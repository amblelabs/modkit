package dev.amble.lib.api.sync;

import java.util.UUID;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;

import dev.amble.lib.AmbleKit;
import dev.amble.lib.api.sync.handler.ComponentManager;
import dev.amble.lib.api.sync.handler.SyncComponent;
import dev.amble.lib.api.sync.handler.TickingComponent;
import dev.amble.lib.api.sync.manager.SyncManager;

public abstract class RootComponent extends Initializable<SyncComponent.InitContext> implements Disposable, TickingComponent {
    private UUID uuid;
    protected ComponentManager manager;

    protected RootComponent(UUID uuid) {
        this.uuid = uuid;
        this.manager = new ComponentManager(getSyncManager().getManagerId(), getSyncManager().getRegistry());
    }

    @Override
    protected void onInit(SyncComponent.InitContext ctx) {
        super.onInit(ctx);

        SyncComponent.init(manager, this, ctx);

        SyncComponent.postInit(manager, ctx);
    }

    public UUID getUuid() {
        return uuid;
    }

    @Override
    public String toString() {
        return uuid.toString();
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    public <T extends SyncComponent> T handler(SyncComponent.IdLike type) {
        if (this.manager == null) {
            AmbleKit.LOGGER.error("Asked for a handler too early on {}", this);
            return null;
        }

        return this.manager.get(type);
    }

    public ComponentManager getHandlers() {
        return manager;
    }
    public abstract SyncManager<?, ?> getSyncManager();

    @Override
    public void dispose() {
        this.getHandlers().dispose();
    }

    @Override
    public void tick(MinecraftServer server) {
        this.getHandlers().tick(server);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void tick(MinecraftClient client) {
        this.getHandlers().tick(client) ;
    }
}
