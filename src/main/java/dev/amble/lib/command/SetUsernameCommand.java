package dev.amble.lib.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.amble.lib.AmbleKit;
import dev.amble.lib.username.UsernameTracker;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class SetUsernameCommand {
	private static String translationKey(String key) {
		return "command." + AmbleKit.MOD_ID + ".username." + key;
	}

	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(literal(AmbleKit.MOD_ID)
				.requires(source -> source.hasPermissionLevel(2))
				.then(literal("username").then(argument("target", EntityArgumentType.entity())
						.then(literal("clear").executes(SetUsernameCommand::executeClear))
						.then(literal("set").then(argument("value", StringArgumentType.greedyString())
								.executes(SetUsernameCommand::execute))))));
	}

	private static int executeClear(CommandContext<ServerCommandSource> context) {
		Entity entity;

		try {
			entity = EntityArgumentType.getEntity(context, "target");
		} catch (CommandSyntaxException e) {
			context.getSource().sendError(Text.translatable(translationKey("error.invalid_target")));
			return 0;
		}

		UsernameTracker.getInstance().removeSynced(entity.getUuid());
		String entityName = entity.getEntityName();
		context.getSource().sendFeedback(() -> Text.translatable(translationKey("cleared"), entityName), true);

		return Command.SINGLE_SUCCESS;
	}

	private static int execute(CommandContext<ServerCommandSource> context) {
		String value = StringArgumentType.getString(context, "value");
		Entity entity;

		try {
			entity = EntityArgumentType.getEntity(context, "target");
		} catch (CommandSyntaxException e) {
			context.getSource().sendError(Text.translatable(translationKey("error.invalid_target")));
			return 0;
		}

		// Parse the input as Text (supports JSON format or plain string with formatting codes)
		Text displayName;
		if (value.startsWith("{") || value.startsWith("[")) {
			// Try to parse as JSON Text
			try {
				displayName = Text.Serializer.fromJson(value);
				if (displayName == null) {
					displayName = Text.of(value);
				}
			} catch (Exception e) {
				displayName = Text.of(value);
			}
		} else {
			// Plain text (supports § formatting codes)
			displayName = Text.of(value);
		}

		UsernameTracker.getInstance().putSynced(entity.getUuid(), displayName);

		String entityName = entity.getEntityName();
		context.getSource().sendFeedback(() -> Text.translatable(translationKey("set"), entityName, value), true);

		return Command.SINGLE_SUCCESS;
	}
}

