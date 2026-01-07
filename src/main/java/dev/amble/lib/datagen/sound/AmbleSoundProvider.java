package dev.amble.lib.datagen.sound;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import org.jetbrains.annotations.Nullable;

import net.minecraft.data.DataOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvent;

import dev.amble.lib.AmbleKit;
import dev.amble.lib.util.StringCursor;

/**
 * Datagen Provider for sounds, this class is used to generate the sounds.json file for the mod
 */
public class AmbleSoundProvider implements DataProvider {

    protected final FabricDataOutput dataOutput;
    private final Map<String, Set<SoundEventWrapper>> sounds = new HashMap<>();
    private final boolean extractVariants;

    public AmbleSoundProvider(FabricDataOutput dataOutput) {
        this(dataOutput, true);
    }

    public AmbleSoundProvider(FabricDataOutput dataOutput, boolean extractVariants) {
        this.dataOutput = dataOutput;
        this.extractVariants = extractVariants;
    }

    private boolean canAdd(String name, boolean check) {
        // you can't have duplicates in registries, nor bad ids.
        return check && !checkName(name);
    }

    public void addSound(String name, SoundEvent event) {
        this.addSound(name, true, event);
    }

    public void addSound(String name, boolean check, SoundEvent event) {
        if (canAdd(name, check))
            return;

        sounds.computeIfAbsent(name, s -> new HashSet<>()).add(new SoundEventWrapper(event));
    }

    public void addSound(String name, SoundEvent... events) {
        this.addSound(name, true, events);
    }

    public void addSound(String name, boolean check, SoundEvent... events) {
        if (canAdd(name, check))
            return;

        Set<SoundEventWrapper> set = sounds.computeIfAbsent(name, s -> new HashSet<>());

        for (SoundEvent event : events) {
            set.add(new SoundEventWrapper(event));
        }
    }

    @Override
    public CompletableFuture<?> run(DataWriter writer) {
        getSoundsFromMod(dataOutput.getModId()).forEach(sound -> {
            String path = sound.getId().getPath();

            addSound(path, false, sound);

            if (extractVariants) {
                String newPath = extractPath(path);

                // event is not a variant!
                if (newPath == null)
                    return;

                addSound(newPath, false, sound);
            }
        });

        JsonObject soundsJson = new JsonObject();
        sounds.forEach((soundName, soundEvents) ->
                soundsJson.add(soundName, serializeSounds(soundEvents)));

        return DataProvider.writeToPath(writer, soundsJson, getOutputPath());
    }

    public Path getOutputPath() {
        return dataOutput.resolvePath(DataOutput.OutputType.RESOURCE_PACK).resolve(dataOutput.getModId())
                .resolve("sounds.json");
    }

    @Override
    public String getName() {
        return "Sound Definitions";
    }

    private static JsonObject serializeSounds(Iterable<SoundEventWrapper> wrappers) {
        JsonObject obj = new JsonObject();
        JsonArray sounds = new JsonArray();

        //tardis/moody/moody,tardis/moody/moody1,tardis/moody/moody2 =>
        //tardis/moody/moody:[tardis/moody/moody, tardis/moody/moody1, tardis/moody/moody2]
        for (SoundEventWrapper wrapper : wrappers) {
            sounds.add(wrapper.event.getId().toString());
        }

        obj.add("sounds", sounds);
        return obj;
    }

    @Nullable private static String extractPath(String path) {
        StringCursor cursor = new StringCursor(path, path.length() - 1, -1);

        while (true) {
            if (!Character.isDigit(cursor.peek()))
                return cursor.substring();

            cursor.next();
        }
    }

    private static boolean checkName(String name) {
        if (name.contains(" ")) {
            AmbleKit.LOGGER.error("Sound event name cannot contain spaces: {}", name);
            return false;
        }

        for (Character character : name.toCharArray()) {
            if (Character.isUpperCase(character)) {
                AmbleKit.LOGGER.error("Sound event name cannot contain capital letters: {}", name);
                return false;
            }
        }

        return true;
    }

    public static Stream<SoundEvent> getSoundsFromMod(String namespace) {
        return Registries.SOUND_EVENT.stream().filter(sound -> sound.getId().getNamespace().equals(namespace));
    }

    static class SoundEventWrapper {

        private final SoundEvent event;

        public SoundEventWrapper(SoundEvent event) {
            this.event = event;
        }

        @Override
        public int hashCode() {
            return event.getId().hashCode();
        }
    }
}
