package dev.amble.lib.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.amble.lib.AmbleKit;
import dev.amble.lib.script.AmbleScript;
import dev.amble.lib.script.ServerScriptManager;
import dev.amble.lib.script.lua.LuaBinder;
import dev.amble.lib.script.lua.ServerMinecraftData;
import org.luaj.vm2.LuaValue;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Set;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/**
 * Server-side command for managing server scripts.
 * Usage: /serverscript [enable|disable|execute|toggle|list|available] [script_id]
 */
public class ServerScriptCommand {

	private static final SuggestionProvider<ServerCommandSource> TICKABLE_SCRIPT_SUGGESTIONS = (context, builder) -> {
		return CommandSource.suggestIdentifiers(
				ServerScriptManager.getCache().entrySet().stream()
						.filter(entry -> entry.getValue().onTick() != null && !entry.getValue().onTick().isnil())
						.map(entry -> Identifier.of(entry.getKey().getNamespace(), entry.getKey().getPath().replace("script/", "").replace(".lua", ""))),
				builder
		);
	};

	private static final SuggestionProvider<ServerCommandSource> ENABLED_TICKABLE_SCRIPT_SUGGESTIONS = (context, builder) -> {
		return CommandSource.suggestIdentifiers(
				ServerScriptManager.getEnabledScripts().stream()
						.filter(id -> {
							AmbleScript script = ServerScriptManager.getCache().get(id);
							return script != null && script.onTick() != null && !script.onTick().isnil();
						})
						.map(id -> Identifier.of(id.getNamespace(), id.getPath().replace("script/", "").replace(".lua", ""))),
				builder
		);
	};

	private static final SuggestionProvider<ServerCommandSource> EXECUTABLE_SCRIPT_SUGGESTIONS = (context, builder) -> {
		return CommandSource.suggestIdentifiers(
				ServerScriptManager.getCache().entrySet().stream()
						.filter(entry -> entry.getValue().onExecute() != null && !entry.getValue().onExecute().isnil())
						.map(entry -> Identifier.of(entry.getKey().getNamespace(), entry.getKey().getPath().replace("script/", "").replace(".lua", ""))),
				builder
		);
	};

	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(literal("serverscript")
				.requires(source -> source.hasPermissionLevel(2)) // Require operator permissions
				.then(literal("execute")
						.then(argument("id", IdentifierArgumentType.identifier())
								.suggests(EXECUTABLE_SCRIPT_SUGGESTIONS)
								.executes(ServerScriptCommand::execute)))
				.then(literal("enable")
						.then(argument("id", IdentifierArgumentType.identifier())
								.suggests(TICKABLE_SCRIPT_SUGGESTIONS)
								.executes(ServerScriptCommand::enable)))
				.then(literal("disable")
						.then(argument("id", IdentifierArgumentType.identifier())
								.suggests(ENABLED_TICKABLE_SCRIPT_SUGGESTIONS)
								.executes(ServerScriptCommand::disable)))
				.then(literal("toggle")
						.then(argument("id", IdentifierArgumentType.identifier())
								.suggests(TICKABLE_SCRIPT_SUGGESTIONS)
								.executes(ServerScriptCommand::toggle)))
				.then(literal("list")
						.executes(ServerScriptCommand::listEnabled))
				.then(literal("available")
						.executes(ServerScriptCommand::listAvailable)));
	}

	private static int execute(CommandContext<ServerCommandSource> context) {
		Identifier scriptId = context.getArgument("id", Identifier.class);
		Identifier fullScriptId = scriptId.withPrefixedPath("script/").withSuffixedPath(".lua");

		try {
			AmbleScript script = ServerScriptManager.getCache().get(fullScriptId);

			if (script == null) {
				context.getSource().sendError(Text.literal("Server script '" + scriptId + "' not found"));
				return 0;
			}

			if (script.onExecute() == null || script.onExecute().isnil()) {
				context.getSource().sendError(Text.literal("Server script '" + scriptId + "' has no onExecute function"));
				return 0;
			}

			// Create a new ServerMinecraftData with the executing player
			ServerCommandSource source = context.getSource();
			ServerPlayerEntity player = source.getPlayer();
			ServerMinecraftData data = new ServerMinecraftData(
					source.getServer(),
					source.getWorld(),
					player
			);
			LuaValue boundData = LuaBinder.bind(data);
			
			script.onExecute().call(boundData);
			context.getSource().sendFeedback(() -> Text.literal("Executed server script: " + scriptId), true);
			return 1;
		} catch (Exception e) {
			context.getSource().sendError(Text.literal("Failed to execute server script '" + scriptId + "': " + e.getMessage()));
			AmbleKit.LOGGER.error("Failed to execute server script {}", scriptId, e);
			return 0;
		}
	}

	private static int enable(CommandContext<ServerCommandSource> context) {
		Identifier scriptId = context.getArgument("id", Identifier.class);
		Identifier fullScriptId = scriptId.withPrefixedPath("script/").withSuffixedPath(".lua");

		if (!ServerScriptManager.getCache().containsKey(fullScriptId)) {
			context.getSource().sendError(Text.literal("Server script '" + scriptId + "' not found"));
			return 0;
		}

		if (ServerScriptManager.isEnabled(fullScriptId)) {
			context.getSource().sendError(Text.literal("Server script '" + scriptId + "' is already enabled"));
			return 0;
		}

		if (ServerScriptManager.enable(fullScriptId)) {
			context.getSource().sendFeedback(() -> Text.literal("§aEnabled server script: " + scriptId), true);
			return 1;
		} else {
			context.getSource().sendError(Text.literal("Failed to enable server script '" + scriptId + "'"));
			return 0;
		}
	}

	private static int disable(CommandContext<ServerCommandSource> context) {
		Identifier scriptId = context.getArgument("id", Identifier.class);
		Identifier fullScriptId = scriptId.withPrefixedPath("script/").withSuffixedPath(".lua");

		if (!ServerScriptManager.isEnabled(fullScriptId)) {
			context.getSource().sendError(Text.literal("Server script '" + scriptId + "' is not enabled"));
			return 0;
		}

		if (ServerScriptManager.disable(fullScriptId)) {
			context.getSource().sendFeedback(() -> Text.literal("§cDisabled server script: " + scriptId), true);
			return 1;
		} else {
			context.getSource().sendError(Text.literal("Failed to disable server script '" + scriptId + "'"));
			return 0;
		}
	}

	private static int toggle(CommandContext<ServerCommandSource> context) {
		Identifier scriptId = context.getArgument("id", Identifier.class);
		Identifier fullScriptId = scriptId.withPrefixedPath("script/").withSuffixedPath(".lua");

		if (!ServerScriptManager.getCache().containsKey(fullScriptId)) {
			context.getSource().sendError(Text.literal("Server script '" + scriptId + "' not found"));
			return 0;
		}

		boolean wasEnabled = ServerScriptManager.isEnabled(fullScriptId);
		ServerScriptManager.toggle(fullScriptId);

		if (wasEnabled) {
			context.getSource().sendFeedback(() -> Text.literal("§cDisabled server script: " + scriptId), true);
		} else {
			context.getSource().sendFeedback(() -> Text.literal("§aEnabled server script: " + scriptId), true);
		}
		return 1;
	}

	private static int listEnabled(CommandContext<ServerCommandSource> context) {
		Set<Identifier> enabled = ServerScriptManager.getEnabledScripts();

		if (enabled.isEmpty()) {
			context.getSource().sendFeedback(() -> Text.literal("§7No server scripts are currently enabled"), false);
			return 1;
		}

		context.getSource().sendFeedback(() -> Text.literal("§6§l━━━ Enabled Server Scripts (" + enabled.size() + ") ━━━"), false);
		for (Identifier id : enabled) {
			String displayId = id.getPath().replace("script/", "").replace(".lua", "");
			context.getSource().sendFeedback(() -> Text.literal("§a✓ §f" + id.getNamespace() + ":" + displayId), false);
		}
		return 1;
	}

	private static int listAvailable(CommandContext<ServerCommandSource> context) {
		Set<Identifier> available = ServerScriptManager.getCache().keySet();
		Set<Identifier> enabled = ServerScriptManager.getEnabledScripts();

		if (available.isEmpty()) {
			context.getSource().sendFeedback(() -> Text.literal("§7No server scripts available"), false);
			return 1;
		}

		context.getSource().sendFeedback(() -> Text.literal("§6§l━━━ Available Server Scripts (" + available.size() + ") ━━━"), false);
		for (Identifier id : available) {
			String displayId = id.getPath().replace("script/", "").replace(".lua", "");
			String status = enabled.contains(id) ? "§a✓" : "§7○";
			context.getSource().sendFeedback(() -> Text.literal(status + " §f" + id.getNamespace() + ":" + displayId), false);
		}
		return 1;
	}
}
