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
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.Set;

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

	private static final SuggestionProvider<FabricClientCommandSource> ENABLED_SCRIPT_SUGGESTIONS = (context, builder) -> {
		return CommandSource.suggestIdentifiers(
				ScriptManager.getEnabledScripts().stream()
						.map(id -> Identifier.of(id.getNamespace(), id.getPath().replace("script/", "").replace(".lua", ""))),
				builder
		);
	};

	public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
		dispatcher.register(literal("amblescript")
				.then(literal("execute")
						.then(argument("id", IdentifierArgumentType.identifier())
								.suggests(SCRIPT_SUGGESTIONS)
								.executes(ExecuteScriptCommand::execute)))
				.then(literal("enable")
						.then(argument("id", IdentifierArgumentType.identifier())
								.suggests(SCRIPT_SUGGESTIONS)
								.executes(ExecuteScriptCommand::enable)))
				.then(literal("disable")
						.then(argument("id", IdentifierArgumentType.identifier())
								.suggests(ENABLED_SCRIPT_SUGGESTIONS)
								.executes(ExecuteScriptCommand::disable)))
				.then(literal("toggle")
						.then(argument("id", IdentifierArgumentType.identifier())
								.suggests(SCRIPT_SUGGESTIONS)
								.executes(ExecuteScriptCommand::toggle)))
				.then(literal("list")
						.executes(ExecuteScriptCommand::listEnabled)));
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

	private static int enable(CommandContext<FabricClientCommandSource> context) {
		Identifier scriptId = context.getArgument("id", Identifier.class);
		Identifier fullScriptId = scriptId.withPrefixedPath("script/").withSuffixedPath(".lua");

		// Ensure script is loaded
		try {
			ScriptManager.load(fullScriptId, MinecraftClient.getInstance().getResourceManager());
		} catch (Exception e) {
			context.getSource().sendError(Text.literal("Script '" + scriptId + "' not found"));
			return 0;
		}

		if (ScriptManager.isEnabled(fullScriptId)) {
			context.getSource().sendError(Text.literal("Script '" + scriptId + "' is already enabled"));
			return 0;
		}

		if (ScriptManager.enable(fullScriptId)) {
			context.getSource().sendFeedback(Text.literal("§aEnabled script: " + scriptId));
			return 1;
		} else {
			context.getSource().sendError(Text.literal("Failed to enable script '" + scriptId + "'"));
			return 0;
		}
	}

	private static int disable(CommandContext<FabricClientCommandSource> context) {
		Identifier scriptId = context.getArgument("id", Identifier.class);
		Identifier fullScriptId = scriptId.withPrefixedPath("script/").withSuffixedPath(".lua");

		if (!ScriptManager.isEnabled(fullScriptId)) {
			context.getSource().sendError(Text.literal("Script '" + scriptId + "' is not enabled"));
			return 0;
		}

		if (ScriptManager.disable(fullScriptId)) {
			context.getSource().sendFeedback(Text.literal("§cDisabled script: " + scriptId));
			return 1;
		} else {
			context.getSource().sendError(Text.literal("Failed to disable script '" + scriptId + "'"));
			return 0;
		}
	}

	private static int toggle(CommandContext<FabricClientCommandSource> context) {
		Identifier scriptId = context.getArgument("id", Identifier.class);
		Identifier fullScriptId = scriptId.withPrefixedPath("script/").withSuffixedPath(".lua");

		// Ensure script is loaded
		try {
			ScriptManager.load(fullScriptId, MinecraftClient.getInstance().getResourceManager());
		} catch (Exception e) {
			context.getSource().sendError(Text.literal("Script '" + scriptId + "' not found"));
			return 0;
		}

		boolean wasEnabled = ScriptManager.isEnabled(fullScriptId);
		ScriptManager.toggle(fullScriptId);

		if (wasEnabled) {
			context.getSource().sendFeedback(Text.literal("§cDisabled script: " + scriptId));
		} else {
			context.getSource().sendFeedback(Text.literal("§aEnabled script: " + scriptId));
		}
		return 1;
	}

	private static int listEnabled(CommandContext<FabricClientCommandSource> context) {
		Set<Identifier> enabled = ScriptManager.getEnabledScripts();

		if (enabled.isEmpty()) {
			context.getSource().sendFeedback(Text.literal("§7No scripts are currently enabled"));
			return 1;
		}

		context.getSource().sendFeedback(Text.literal("§6§l━━━ Enabled Scripts (" + enabled.size() + ") ━━━"));
		for (Identifier id : enabled) {
			String displayId = id.getPath().replace("script/", "").replace(".lua", "");
			context.getSource().sendFeedback(Text.literal("§a✓ §f" + id.getNamespace() + ":" + displayId));
		}
		return 1;
	}
}
