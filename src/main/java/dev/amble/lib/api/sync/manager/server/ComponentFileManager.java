package dev.amble.lib.api.sync.manager.server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;

import dev.amble.lib.AmbleKit;
import dev.amble.lib.api.sync.RootComponent;
import dev.amble.lib.api.sync.manager.SyncManager;

public class ComponentFileManager<R extends RootComponent> {

    protected boolean locked = false;
    protected final String modid;
    protected final String name;
    protected final Class<? extends R> clazz;

    public ComponentFileManager(String modid, String name, Class<? extends R> clazz) {
        this.modid = modid;
        this.name = name;
        this.clazz = clazz;
    }

    public void delete(MinecraftServer server, UUID uuid) {
        try {
            Files.deleteIfExists(this.getSavePath(server, uuid, "json"));
        } catch (IOException e) {
            AmbleKit.LOGGER.error("Failed to delete Root Component {} {}", name, uuid, e);
        }
    }

    private Path getRootSavePath(Path root) {
        return root.resolve("." + modid + "/" + name);
    }

    public Path getRootSavePath(MinecraftServer server) {
        return this.getRootSavePath(server.getSavePath(WorldSavePath.ROOT));
    }

    private Path getSavePath(MinecraftServer server, UUID uuid, String suffix) throws IOException {
        Path result = this.getRootSavePath(server).resolve(uuid.toString() + "." + suffix);
        Files.createDirectories(result.getParent());

        return result;
    }

    public R load(MinecraftServer server, SyncManager<R, ?> manager, UUID uuid, ComponentLoader<R> function,
                        Consumer<R> consumer) {
        if (this.locked)
            return null;

        long start = System.currentTimeMillis();

        try {
            Path file = this.getSavePath(server, uuid, "json");
            String raw = Files.readString(file);

            JsonObject object = JsonParser.parseString(raw).getAsJsonObject();

            R root = function.apply(manager.getFileGson(), object);
            consumer.accept(root);

            AmbleKit.LOGGER.info("Deserialized {} {} in {}ms", name, root, System.currentTimeMillis() - start);
            return root;
        } catch (IOException e) {
            AmbleKit.LOGGER.warn("Failed to load {} {}!", name, uuid);
            AmbleKit.LOGGER.warn(e.getMessage());
        }

        return null;
    }

    public void save(MinecraftServer server, SyncManager<R, ?> manager, R root) {
        try {
            Path savePath = this.getSavePath(server, root.getUuid(), "json");
            Files.writeString(savePath, manager.getFileGson().toJson(root, clazz));
        } catch (IOException e) {
            AmbleKit.LOGGER.warn("Couldn't save Root Component {}", root.getUuid(), e);
        }
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public boolean isLocked() {
        return locked;
    }

    public List<UUID> getList(MinecraftServer server) {
        try {
            return Files.list(this.getRootSavePath(server)).map(path -> {
                String name = path.getFileName().toString();
                return UUID.fromString(name.substring(0, name.indexOf('.')));
            }).toList();
        } catch (IOException e) {
            AmbleKit.LOGGER.error("Failed to list {} files", name,e);
        }

        return List.of();
    }

    @FunctionalInterface
    public interface ComponentLoader<R> {
        R apply(Gson gson, JsonObject object);
    }
}
