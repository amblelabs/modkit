package dev.amble.lib.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.amble.lib.AmbleKit;
import dev.amble.lib.animation.AnimatedEntity;
import dev.amble.lib.client.bedrock.BedrockAnimationReference;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class PlayAnimationCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(literal(AmbleKit.MOD_ID)
				.requires(source -> source.hasPermissionLevel(2))
				.then(literal("animation").then(argument("target", EntityArgumentType.entity())
						.then(argument("id", IdentifierArgumentType.identifier()).executes(PlayAnimationCommand::execute)))));
	}

	private static int execute(CommandContext<ServerCommandSource> context) {
		Identifier animationId = IdentifierArgumentType.getIdentifier(context, "id");
		Entity target;

		try {
			target = EntityArgumentType.getEntity(context, "target");
		} catch (Exception e) {
			context.getSource().sendError(Text.literal("Invalid Target, using self."));
			target = context.getSource().getEntity();
		}

		if (!(target instanceof AnimatedEntity animated)) {
			context.getSource().sendError(Text.literal("Target is not an AnimatedEntity"));
			return 0;
		}

		animated.playAnimation(BedrockAnimationReference.parse(animationId));

		String name = target.getEntityName();
		context.getSource().sendFeedback(() -> Text.literal("Playing animation "+ animationId +" on "+ name), true);
		return 1;
	}
}
