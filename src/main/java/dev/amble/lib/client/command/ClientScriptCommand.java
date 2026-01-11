package dev.amble.lib.client.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.amble.lib.AmbleKit;
import dev.amble.lib.script.LuaScript;
import dev.amble.lib.script.ScriptManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

import java.util.Set;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

/**
 * Client-side command for managing client scripts.
 * Usage: /amblescript [execute|enable|disable|toggle|list|available] [script_id]
 */
public class ClientScriptCommand {

	private static final String SCRIPT_PREFIX = "script/";
	private static final String SCRIPT_SUFFIX = ".lua";

	private static String translationKey(String key) {
		return "command." + AmbleKit.MOD_ID + ".client_script." + key;
	}

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

	private static final SuggestionProvider<FabricClientCommandSource> TICKABLE_SCRIPT_SUGGESTIONS = (context, builder) -> {
		return CommandSource.suggestIdentifiers(
				ScriptManager.getInstance().getCache().entrySet().stream()
						.filter(entry -> entry.getValue().onTick() != null && !entry.getValue().onTick().isnil())
						.map(entry -> Identifier.of(entry.getKey().getNamespace(), getDisplayId(entry.getKey()))),
				builder
		);
	};

	private static final SuggestionProvider<FabricClientCommandSource> ENABLED_TICKABLE_SCRIPT_SUGGESTIONS = (context, builder) -> {
		return CommandSource.suggestIdentifiers(
				ScriptManager.getInstance().getEnabledScripts().stream()
						.filter(id -> {
							LuaScript script = ScriptManager.getInstance().getCache().get(id);
							return script != null && script.onTick() != null && !script.onTick().isnil();
						})
						.map(id -> Identifier.of(id.getNamespace(), getDisplayId(id))),
				builder
		);
	};

	private static final SuggestionProvider<FabricClientCommandSource> EXECUTABLE_SCRIPT_SUGGESTIONS = (context, builder) -> {
		return CommandSource.suggestIdentifiers(
				ScriptManager.getInstance().getCache().entrySet().stream()
						.filter(entry -> entry.getValue().onExecute() != null && !entry.getValue().onExecute().isnil())
						.map(entry -> Identifier.of(entry.getKey().getNamespace(), getDisplayId(entry.getKey()))),
				builder
		);
	};

	public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
		dispatcher.register(literal("amblescript")
				.then(literal("execute")
						.then(argument("id", IdentifierArgumentType.identifier())
								.suggests(EXECUTABLE_SCRIPT_SUGGESTIONS)
								.executes(context -> execute(context, ""))
								.then(argument("args", StringArgumentType.greedyString())
										.executes(context -> execute(context, StringArgumentType.getString(context, "args"))))))
				.then(literal("enable")
						.then(argument("id", IdentifierArgumentType.identifier())
								.suggests(TICKABLE_SCRIPT_SUGGESTIONS)
								.executes(ClientScriptCommand::enable)))
				.then(literal("disable")
						.then(argument("id", IdentifierArgumentType.identifier())
								.suggests(ENABLED_TICKABLE_SCRIPT_SUGGESTIONS)
								.executes(ClientScriptCommand::disable)))
				.then(literal("toggle")
						.then(argument("id", IdentifierArgumentType.identifier())
								.suggests(TICKABLE_SCRIPT_SUGGESTIONS)
								.executes(ClientScriptCommand::toggle)))
				.then(literal("list")
						.executes(ClientScriptCommand::listEnabled))
				.then(literal("available")
						.executes(ClientScriptCommand::listAvailable)));
	}

	private static int execute(CommandContext<FabricClientCommandSource> context, String argsString) {
		Identifier scriptId = context.getArgument("id", Identifier.class);
		Identifier fullScriptId = toFullScriptId(scriptId);

		try {
			LuaScript script = ScriptManager.getInstance().load(
					fullScriptId,
					MinecraftClient.getInstance().getResourceManager()
			);

			if (script.onExecute() == null || script.onExecute().isnil()) {
				context.getSource().sendError(Text.translatable(translationKey("error.no_execute"), scriptId));
				return 0;
			}

			LuaValue data = ScriptManager.getInstance().getScriptData(fullScriptId);

			// Parse arguments into a Lua table
			LuaTable argsTable = new LuaTable();
			if (!argsString.isEmpty()) {
				String[] args = argsString.split(" ");
				for (int i = 0; i < args.length; i++) {
					argsTable.set(i + 1, LuaValue.valueOf(args[i]));
				}
			}

			script.onExecute().call(data, argsTable);
			context.getSource().sendFeedback(Text.translatable(translationKey("executed"), scriptId));
			return 1;
		} catch (Exception e) {
			context.getSource().sendError(Text.translatable(translationKey("error.execute_failed"), scriptId, e.getMessage()));
			AmbleKit.LOGGER.error("Failed to execute script {}", scriptId, e);
			return 0;
		}
	}

	private static int enable(CommandContext<FabricClientCommandSource> context) {
		Identifier scriptId = context.getArgument("id", Identifier.class);
		Identifier fullScriptId = toFullScriptId(scriptId);

		// Ensure script is loaded
		try {
			ScriptManager.getInstance().load(fullScriptId, MinecraftClient.getInstance().getResourceManager());
		} catch (Exception e) {
			context.getSource().sendError(Text.translatable(translationKey("error.not_found"), scriptId));
			return 0;
		}

		if (ScriptManager.getInstance().isEnabled(fullScriptId)) {
			context.getSource().sendError(Text.translatable(translationKey("error.already_enabled"), scriptId));
			return 0;
		}

		if (ScriptManager.getInstance().enable(fullScriptId)) {
			context.getSource().sendFeedback(Text.translatable(translationKey("enabled"), scriptId).formatted(Formatting.GREEN));
			return 1;
		} else {
			context.getSource().sendError(Text.translatable(translationKey("error.enable_failed"), scriptId));
			return 0;
		}
	}

	private static int disable(CommandContext<FabricClientCommandSource> context) {
		Identifier scriptId = context.getArgument("id", Identifier.class);
		Identifier fullScriptId = toFullScriptId(scriptId);

		if (!ScriptManager.getInstance().isEnabled(fullScriptId)) {
			context.getSource().sendError(Text.translatable(translationKey("error.not_enabled"), scriptId));
			return 0;
		}

		if (ScriptManager.getInstance().disable(fullScriptId)) {
			context.getSource().sendFeedback(Text.translatable(translationKey("disabled"), scriptId).formatted(Formatting.RED));
			return 1;
		} else {
			context.getSource().sendError(Text.translatable(translationKey("error.disable_failed"), scriptId));
			return 0;
		}
	}

	private static int toggle(CommandContext<FabricClientCommandSource> context) {
		Identifier scriptId = context.getArgument("id", Identifier.class);
		Identifier fullScriptId = toFullScriptId(scriptId);

		// Ensure script is loaded
		try {
			ScriptManager.getInstance().load(fullScriptId, MinecraftClient.getInstance().getResourceManager());
		} catch (Exception e) {
			context.getSource().sendError(Text.translatable(translationKey("error.not_found"), scriptId));
			return 0;
		}

		boolean wasEnabled = ScriptManager.getInstance().isEnabled(fullScriptId);
		ScriptManager.getInstance().toggle(fullScriptId);

		if (wasEnabled) {
			context.getSource().sendFeedback(Text.translatable(translationKey("disabled"), scriptId).formatted(Formatting.RED));
		} else {
			context.getSource().sendFeedback(Text.translatable(translationKey("enabled"), scriptId).formatted(Formatting.GREEN));
		}
		return 1;
	}

	private static int listEnabled(CommandContext<FabricClientCommandSource> context) {
		Set<Identifier> enabled = ScriptManager.getInstance().getEnabledScripts();

		if (enabled.isEmpty()) {
			context.getSource().sendFeedback(Text.translatable(translationKey("list.none_enabled")).formatted(Formatting.GRAY));
			return 1;
		}

		context.getSource().sendFeedback(Text.translatable(translationKey("list.enabled_header"), enabled.size()).formatted(Formatting.GOLD, Formatting.BOLD));
		for (Identifier id : enabled) {
			String displayId = getDisplayId(id);
			context.getSource().sendFeedback(
					Text.literal("✓ ").formatted(Formatting.GREEN)
							.append(Text.literal(id.getNamespace() + ":" + displayId).formatted(Formatting.WHITE))
			);
		}
		return 1;
	}

	private static int listAvailable(CommandContext<FabricClientCommandSource> context) {
		Set<Identifier> available = ScriptManager.getInstance().getCache().keySet();
		Set<Identifier> enabled = ScriptManager.getInstance().getEnabledScripts();

		if (available.isEmpty()) {
			context.getSource().sendFeedback(Text.translatable(translationKey("list.none_available")).formatted(Formatting.GRAY));
			return 1;
		}

		context.getSource().sendFeedback(Text.translatable(translationKey("list.available_header"), available.size()).formatted(Formatting.GOLD, Formatting.BOLD));
		for (Identifier id : available) {
			String displayId = getDisplayId(id);
			Text statusIcon = enabled.contains(id)
					? Text.literal("✓ ").formatted(Formatting.GREEN)
					: Text.literal("○ ").formatted(Formatting.GRAY);
			context.getSource().sendFeedback(
					statusIcon.copy().append(Text.literal(id.getNamespace() + ":" + displayId).formatted(Formatting.WHITE))
			);
		}
		return 1;
	}
}
