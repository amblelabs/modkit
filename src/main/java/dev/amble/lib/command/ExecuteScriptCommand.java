package dev.amble.lib.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.amble.lib.AmbleKit;
import dev.amble.lib.script.AmbleScript;
import dev.amble.lib.script.ScriptManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class ExecuteScriptCommand {

	private static final SuggestionProvider<FabricClientCommandSource> SCRIPT_SUGGESTIONS = (context, builder) -> {
		return CommandSource.suggestIdentifiers(
				ScriptManager.getCache().keySet().stream()
						.map(id -> Identifier.of(id.getNamespace(), id.getPath().replace("script/", "").replace(".lua", ""))),
				builder
		);
	};

	public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
		dispatcher.register(literal("amblescript")
				.then(literal("execute")
						.then(argument("id", IdentifierArgumentType.identifier())
								.suggests(SCRIPT_SUGGESTIONS)
								.executes(ExecuteScriptCommand::execute))));
	}

	private static int execute(CommandContext<FabricClientCommandSource> context) {
		Identifier scriptId = context.getArgument("id", Identifier.class);
		Identifier fullScriptId = scriptId.withPrefixedPath("script/").withSuffixedPath(".lua");

		try {
			AmbleScript script = ScriptManager.load(
					fullScriptId,
					MinecraftClient.getInstance().getResourceManager()
			);

			if (script.onExecute() == null || script.onExecute().isnil()) {
				context.getSource().sendError(Text.literal("Script '" + scriptId + "' has no onExecute function"));
				return 0;
			}

			script.onExecute().call();
			context.getSource().sendFeedback(Text.literal("Executed script: " + scriptId));
			return 1;
		} catch (Exception e) {
			context.getSource().sendError(Text.literal("Failed to execute script '" + scriptId + "': " + e.getMessage()));
			AmbleKit.LOGGER.error("Failed to execute script {}", scriptId, e);
			return 0;
		}
	}
}
