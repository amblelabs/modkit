package dev.amble.lib.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.amble.lib.AmbleKit;
import dev.amble.lib.skin.SkinData;
import dev.amble.lib.skin.SkinTracker;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class SetSkinCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(literal(AmbleKit.MOD_ID)
				.requires(source -> source.hasPermissionLevel(2))
				.then(literal("skin").then(argument("name", StringArgumentType.string()).executes(SetSkinCommand::execute))));
	}

	private static int execute(CommandContext<ServerCommandSource> context) {
		String name = StringArgumentType.getString(context, "name");

		SkinData data = SkinData.username(name);

		SkinTracker.getInstance().add(context.getSource().getPlayer().getUuid(), data);

		context.getSource().sendFeedback(() -> Text.literal("Set skin to " + name), true);

		return 1;
	}
}
