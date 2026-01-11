package dev.amble.lib.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.amble.lib.AmbleKit;
import dev.amble.lib.script.LuaScript;
import dev.amble.lib.script.ServerScriptManager;
import dev.amble.lib.script.lua.LuaBinder;
import dev.amble.lib.script.lua.ServerMinecraftData;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.Set;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/**
 * Server-side command for managing server scripts.
 * Usage: /serverscript [enable|disable|execute|toggle|list|available] [script_id]
 */
public class ServerScriptCommand {

	private static final String SCRIPT_PREFIX = "script/";
	private static final String SCRIPT_SUFFIX = ".lua";

	/**
	 * Converts a full script identifier to a display-friendly format.
	 * Removes the "script/" prefix and ".lua" suffix.
	 */
	private static String getDisplayId(Identifier id) {
		return id.getPath().replace(SCRIPT_PREFIX, "").replace(SCRIPT_SUFFIX, "");
	}

	/**
	 * Converts a user-provided script ID to the full internal identifier.
	 */
	private static Identifier toFullScriptId(Identifier scriptId) {
		return scriptId.withPrefixedPath(SCRIPT_PREFIX).withSuffixedPath(SCRIPT_SUFFIX);
	}

	private static final SuggestionProvider<ServerCommandSource> TICKABLE_SCRIPT_SUGGESTIONS = (context, builder) -> {
		return CommandSource.suggestIdentifiers(
				ServerScriptManager.getInstance().getCache().entrySet().stream()
						.filter(entry -> entry.getValue().onTick() != null && !entry.getValue().onTick().isnil())
						.map(entry -> Identifier.of(entry.getKey().getNamespace(), getDisplayId(entry.getKey()))),
				builder
		);
	};

	private static final SuggestionProvider<ServerCommandSource> ENABLED_TICKABLE_SCRIPT_SUGGESTIONS = (context, builder) -> {
		return CommandSource.suggestIdentifiers(
				ServerScriptManager.getInstance().getEnabledScripts().stream()
						.filter(id -> {
							LuaScript script = ServerScriptManager.getInstance().getCache().get(id);
							return script != null && script.onTick() != null && !script.onTick().isnil();
						})
						.map(id -> Identifier.of(id.getNamespace(), getDisplayId(id))),
				builder
		);
	};

	private static final SuggestionProvider<ServerCommandSource> EXECUTABLE_SCRIPT_SUGGESTIONS = (context, builder) -> {
		return CommandSource.suggestIdentifiers(
				ServerScriptManager.getInstance().getCache().entrySet().stream()
						.filter(entry -> entry.getValue().onExecute() != null && !entry.getValue().onExecute().isnil())
						.map(entry -> Identifier.of(entry.getKey().getNamespace(), getDisplayId(entry.getKey()))),
				builder
		);
	};

	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(literal("serverscript")
				.requires(source -> source.hasPermissionLevel(2)) // Require operator permissions
				.then(literal("execute")
						.then(argument("id", IdentifierArgumentType.identifier())
								.suggests(EXECUTABLE_SCRIPT_SUGGESTIONS)
								.executes(context -> execute(context, ""))
								.then(argument("args", StringArgumentType.greedyString())
										.executes(context -> execute(context, StringArgumentType.getString(context, "args"))))))
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

	private static int execute(CommandContext<ServerCommandSource> context, String argsString) {
		Identifier scriptId = context.getArgument("id", Identifier.class);
		Identifier fullScriptId = toFullScriptId(scriptId);

		try {
			LuaScript script = ServerScriptManager.getInstance().getCache().get(fullScriptId);

			if (script == null) context.getSource().sendError(Text.translatable("command.amblekit.script.error.not_found", scriptId));
				context.getSource().sendError(Text.literal("Server script '" + scriptId + "' not found"));
			}

			if (script.onExecute() == null || script.onExecute().isnil()) {
				context.getSource().sendError(Text.translatable(translationKey("error.no_execute"), scriptId));
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

			// Parse arguments into a Lua table
			LuaTable argsTable = new LuaTable();
			if (!argsString.isEmpty()) {
				String[] args = argsString.split(" ");
				for (int i = 0; i < args.length; i++) {
					argsTable.set(i + 1, LuaValue.valueOf(args[i]));
				}
			}

			script.onExecute().call(boundData, argsTable);
			context.getSource().sendFeedback(() -> Text.translatable(translationKey("executed"), scriptId), true);
			return 1;
		} catch (Exception e) {
			context.getSource().sendError(Text.translatable(translationKey("error.execute_failed"), scriptId, e.getMessage()));
			AmbleKit.LOGGER.error("Failed to execute server script {}", scriptId, e);
			return 0;
		}
	}

	private static int enable(CommandContext<ServerCommandSource> context) {
		Identifier scriptId = context.getArgument("id", Identifier.class);
		Identifier fullScriptId = toFullScriptId(scriptId);

		if (!ServerScriptManager.getInstance().getCache().containsKey(fullScriptId)) {
			context.getSource().sendError(Text.translatable(translationKey("error.not_found"), scriptId));
			return 0;
		}

		if (ServerScriptManager.getInstance().isEnabled(fullScriptId)) {
			context.getSource().sendError(Text.translatable(translationKey("error.already_enabled"), scriptId));
			return 0;
		}

		if (ServerScriptManager.getInstance().enable(fullScriptId)) {
			context.getSource().sendFeedback(() -> Text.translatable(translationKey("enabled"), scriptId).formatted(Formatting.GREEN), true);
			return 1;
		} else {
			context.getSource().sendError(Text.translatable(translationKey("error.enable_failed"), scriptId));
			return 0;
		}
	}

	private static int disable(CommandContext<ServerCommandSource> context) {
		Identifier scriptId = context.getArgument("id", Identifier.class);
		Identifier fullScriptId = toFullScriptId(scriptId);

		if (!ServerScriptManager.getInstance().isEnabled(fullScriptId)) {
			context.getSource().sendError(Text.translatable(translationKey("error.not_enabled"), scriptId));
			return 0;
		}

		if (ServerScriptManager.getInstance().disable(fullScriptId)) {
			context.getSource().sendFeedback(() -> Text.translatable(translationKey("disabled"), scriptId).formatted(Formatting.RED), true);
			return 1;
		} else {
			context.getSource().sendError(Text.translatable(translationKey("error.disable_failed"), scriptId));
			return 0;
		}
	}

	private static int toggle(CommandContext<ServerCommandSource> context) {
		Identifier scriptId = context.getArgument("id", Identifier.class);
		Identifier fullScriptId = toFullScriptId(scriptId);

		if (!ServerScriptManager.getInstance().getCache().containsKey(fullScriptId)) {
			context.getSource().sendError(Text.translatable(translationKey("error.not_found"), scriptId));
			return 0;
		}

		boolean wasEnabled = ServerScriptManager.getInstance().isEnabled(fullScriptId);
		ServerScriptManager.getInstance().toggle(fullScriptId);

		if (wasEnabled) {
			context.getSource().sendFeedback(() -> Text.translatable(translationKey("disabled"), scriptId).formatted(Formatting.RED), true);
		} else {
			context.getSource().sendFeedback(() -> Text.translatable(translationKey("enabled"), scriptId).formatted(Formatting.GREEN), true);
		}
		return 1;
	}

	private static int listEnabled(CommandContext<ServerCommandSource> context) {
		Set<Identifier> enabled = ServerScriptManager.getInstance().getEnabledScripts();

		if (enabled.isEmpty()) {
			context.getSource().sendFeedback(() -> Text.translatable(translationKey("list.none_enabled")).formatted(Formatting.GRAY), false);
			return 1;
		}

		context.getSource().sendFeedback(() -> Text.translatable(translationKey("list.enabled_header"), enabled.size()).formatted(Formatting.GOLD, Formatting.BOLD), false);
		for (Identifier id : enabled) {
			String displayId = getDisplayId(id);
			context.getSource().sendFeedback(() ->
					Text.literal("✓ ").formatted(Formatting.GREEN)
							.append(Text.literal(id.getNamespace() + ":" + displayId).formatted(Formatting.WHITE)), false);
		}
		return 1;
	}

	private static int listAvailable(CommandContext<ServerCommandSource> context) {
		Set<Identifier> available = ServerScriptManager.getInstance().getCache().keySet();
		Set<Identifier> enabled = ServerScriptManager.getInstance().getEnabledScripts();

		if (available.isEmpty()) {
			context.getSource().sendFeedback(() -> Text.translatable(translationKey("list.none_available")).formatted(Formatting.GRAY), false);
			return 1;
		}

		context.getSource().sendFeedback(() -> Text.translatable(translationKey("list.available_header"), available.size()).formatted(Formatting.GOLD, Formatting.BOLD), false);
		for (Identifier id : available) {
			String displayId = getDisplayId(id);
			Text statusIcon = enabled.contains(id)
					? Text.literal("✓ ").formatted(Formatting.GREEN)
					: Text.literal("○ ").formatted(Formatting.GRAY);
			context.getSource().sendFeedback(() ->
					statusIcon.copy().append(Text.literal(id.getNamespace() + ":" + displayId).formatted(Formatting.WHITE)), false);
		}
		return Command.SINGLE_SUCCESS;
	}
}
