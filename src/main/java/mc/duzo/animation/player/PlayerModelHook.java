package mc.duzo.animation.player;

import java.util.Optional;

import net.minecraft.client.model.ModelPart;

/**
 * For creating custom methods in player model class
 * Not very good, and should be changed
 *
 * @author duzo
 */
public interface PlayerModelHook {
    Optional<ModelPart> animation$getChild(String name);
    ModelPart animation$getPart();
}