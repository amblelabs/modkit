package dev.amble.litmus.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import dev.amble.lib.client.gui.*;
import dev.amble.lib.client.gui.registry.AmbleGuiRegistry;
import dev.amble.litmus.LitmusMod;
import dev.drtheo.scheduler.api.TimeUnit;
import dev.drtheo.scheduler.api.client.ClientScheduler;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.awt.Color;
import java.awt.Rectangle;


public class TestScreenCommand {
	public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
		dispatcher.register(ClientCommandManager.literal("ambleScreen").executes(source -> {
			source.getSource().sendFeedback(Text.literal("Available screens: "));

			for (AmbleContainer container : AmbleGuiRegistry.getInstance().toList()) {
				source.getSource().sendFeedback(Text.literal(" - " + container.id().toString()));
			}

			MinecraftClient.getInstance().execute(() -> {
				ClientScheduler.get().runTaskLater(() -> {
					AmbleContainer container = AmbleContainer.builder().layout(new Rectangle(0,0, 216, 138)).background(AmbleDisplayType.texture(new AmbleDisplayType.TextureData(new Identifier(LitmusMod.MOD_ID, "textures/gui/test_screen.png"), 0, 0, 216, 138, 256, 256))).build();
					AmbleContainer child1 = AmbleContainer.builder().layout(new Rectangle(0,0, 50, 50)).background(AmbleDisplayType.color(Color.BLUE)).build();
					AmbleContainer child2 = AmbleContainer.builder().layout(new Rectangle(0,0, 25, 25)).background(AmbleDisplayType.color(Color.ORANGE)).build();
					AmbleButton child3 = AmbleButton.buttonBuilder().layout(new Rectangle(0,0, 75, 40)).horizontalAlign(UIAlign.CENTRE).background(Color.GREEN).hoverDisplay(Color.YELLOW).pressDisplay(Color.RED).onClick(() -> {
						System.out.println("Button Clicked!");
					}).build();
					AmbleText child4 = AmbleText.textBuilder().background(AmbleDisplayType.color(new Color(0,0,0,0))).text(Text.literal("press me")).build();
					child4.setPreferredLayout(child3.getPreferredLayout());
					child3.addChild(child4);
					container.setPadding(10);
					container.setSpacing(1);
					container.addChild(child1);
					container.addChild(child2);
					container.addChild(child3);
					container.setHorizontalAlign(UIAlign.CENTRE);
					container.setVerticalAlign(UIAlign.CENTRE);
					container.recalcuateLayout();

					container.display();
				}, TimeUnit.SECONDS, 1);
			});
			return Command.SINGLE_SUCCESS;
		}).then(ClientCommandManager.argument("id", IdentifierArgumentType.identifier()).executes(source -> {
			Identifier id = source.getArgument("id", Identifier.class);
			AmbleContainer container = AmbleGuiRegistry.getInstance().get(id);
			if (container == null) {
				source.getSource().sendError(Text.literal("No screen found with id: " + id.toString()));
				return 0;
			}

			ClientScheduler.get().runTaskLater(container::display, TimeUnit.SECONDS, 1);

			return Command.SINGLE_SUCCESS;
		})));
	}
}
