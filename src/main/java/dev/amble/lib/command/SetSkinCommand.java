package dev.amble.lib.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.amble.lib.AmbleKit;
import dev.amble.lib.skin.SkinData;
import dev.amble.lib.skin.SkinTracker;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class SetSkinCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(literal(AmbleKit.MOD_ID)
				.requires(source -> source.hasPermissionLevel(2))
				.then(literal("skin").then(argument("target", EntityArgumentType.player())
								.then(literal("clear").executes(SetSkinCommand::executeClear))
								.then(literal("slim").then(argument("slim", BoolArgumentType.bool())
										.executes(SetSkinCommand::executeSlim)))
								.then(literal("set").then(argument("value", StringArgumentType.greedyString())
												.executes(SetSkinCommand::execute))))));
	}

	private static int executeClear(CommandContext<ServerCommandSource> context) {
		ServerPlayerEntity player;

		try {
			player = EntityArgumentType.getPlayer(context, "target");
		} catch (CommandSyntaxException e) {
			context.getSource().sendError(Text.literal("Invalid Target, using self."));
			player = context.getSource().getPlayer();
		}

		if (player == null) {
			context.getSource().sendError(Text.literal("Invalid Target"));
			return 0;
		}

		SkinTracker.getInstance().removeSynced(player.getUuid());
		String username = player.getEntityName();
		context.getSource().sendFeedback(() -> Text.literal("Cleared skin of "+ username), true);

		return 1;
	}

	private static int executeSlim(CommandContext<ServerCommandSource> context) {
		boolean slim = BoolArgumentType.getBool(context, "slim");
		ServerPlayerEntity player;

		try {
			player = EntityArgumentType.getPlayer(context, "target");
		} catch (CommandSyntaxException e) {
			context.getSource().sendError(Text.literal("Invalid Target, using self."));
			player = context.getSource().getPlayer();
		}

		if (player == null) {
			context.getSource().sendError(Text.literal("Invalid Target"));
			return 0;
		}

		SkinData data = SkinTracker.getInstance().get(player.getUuid());
		if (data == null) {
			context.getSource().sendError(Text.literal("Player is not disguised."));
			return 0;
		}

		data = data.withSlim(slim);

		SkinTracker.getInstance().putSynced(context.getSource().getPlayer().getUuid(), data);

		String username = player.getEntityName();
		context.getSource().sendFeedback(() -> Text.literal("Set slimness of "+ username +" to " + slim), true);

		return 1;
	}

	private static int execute(CommandContext<ServerCommandSource> context) {
		String value = StringArgumentType.getString(context, "value");
		ServerPlayerEntity player;

		try {
			player = EntityArgumentType.getPlayer(context, "target");
		} catch (CommandSyntaxException e) {
			context.getSource().sendError(Text.literal("Invalid Target, using self."));
			player = context.getSource().getPlayer();
		}

		boolean isUrl = value.startsWith("http://") || value.startsWith("https://");

		SkinData data = isUrl ? SkinData.url(value, false) : SkinData.username(value, false);

		if (player == null) {
			context.getSource().sendError(Text.literal("Invalid Target"));
			return 0;
		}
		SkinTracker.getInstance().putSynced(context.getSource().getPlayer().getUuid(), data);

		String username = player.getEntityName();
		context.getSource().sendFeedback(() -> Text.literal("Set skin of "+ username +" to " + value), true);

		return 1;
	}
}
