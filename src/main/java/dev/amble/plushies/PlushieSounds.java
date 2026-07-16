package dev.amble.plushies;

import dev.amble.lib.AmbleKit;
import dev.amble.lib.container.impl.SoundContainer;
import net.minecraft.sound.SoundEvent;

public class PlushieSounds implements SoundContainer {
    public static final SoundEvent BOOP = SoundEvent.of(AmbleKit.id("secret/boop"));
}
