package dev.amble.lib.api.sync.link.block;

import dev.amble.lib.api.sync.RootComponent;
import dev.amble.lib.api.sync.link.Linkable;
import dev.amble.lib.api.sync.link.RootRef;
import dev.amble.lib.api.sync.manager.SyncManager;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import java.util.UUID;

public abstract class AbstractLinkableBlockEntity<R extends RootComponent> extends BlockEntity implements Linkable<R> {

    protected RootRef<R> ref;

    public AbstractLinkableBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public RootRef<R> parent() {
        return ref;
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);

        if (this.ref != null && this.ref.getId() != null)
            nbt.putUuid(getNbtPath(), this.ref.getId());
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);

        NbtElement id = nbt.get(getNbtPath());

        if (id == null)
            return;

        this.ref = RootRef.createAs(this, NbtHelper.toUuid(id), this.getSyncManager());

        if (this.world == null)
            return;

        this.onLinked();
    }

    @Override
    public void markRemoved() {
        super.markRemoved();

        if (this.ref == null || this.ref.isEmpty())
            return;

        if (!(this.world instanceof ServerWorld serverWorld))
            return;

        // ServerTardisManager.getInstance().unmark(serverWorld, (ServerTardis) this.ref.get(), new ChunkPos(this.pos));
    }

    @Override
    public void link(R tardis) {
        this.ref = RootRef.createAs(this, tardis, this.getSyncManager());
        this.handleLink();
    }

    @Override
    public void link(UUID id) {
        this.ref = RootRef.createAs(this, id, this.getSyncManager());
        this.handleLink();
    }

    private void mark() {
        /*if (this.world instanceof ServerWorld serverWorld)
            ServerTardisManager.getInstance().mark(serverWorld, (ServerTardis) this.tardis().get(),
                    new ChunkPos(this.pos));*/
    }

    private void handleLink() {
        this.mark();
        this.onLinked();

        this.sync();
        this.markDirty();
    }

    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        if (this.isLinked())
            this.mark();

        return createNbt();
    }

    protected void sync() {
        if (this.world != null && this.world.getChunkManager() instanceof ServerChunkManager chunkManager)
            chunkManager.markForUpdate(this.pos);
    }

    public abstract SyncManager getSyncManager();

    protected String getNbtPath() {
        return this.getSyncManager().createPacket("root").getPath();
    }
}
