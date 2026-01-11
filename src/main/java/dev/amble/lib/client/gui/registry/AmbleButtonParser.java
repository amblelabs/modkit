package dev.amble.lib.client.gui.registry;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.amble.lib.AmbleKit;
import dev.amble.lib.client.gui.*;
import dev.amble.lib.script.LuaScript;
import dev.amble.lib.script.ScriptManager;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

/**
 * Parser for AmbleButton elements.
 * <p>
 * This parser handles JSON objects that have button-specific properties:
 * on_click, script, hover_background, or press_background.
 */
public class AmbleButtonParser implements AmbleElementParser {

    @Override
    public @Nullable AmbleContainer parse(JsonObject json, @Nullable Identifier resourceId, AmbleContainer base) {
        // Check if this is a button (has button-specific properties)
        boolean isButton = json.has("on_click") || json.has("script") || json.has("hover_background") || json.has("press_background");

        if (!isButton) {
            return null;
        }

        String context = resourceId != null ? " (resource: " + resourceId + ")" : "";

        // Handle text for button - add as child
        if (json.has("text")) {
            String text = json.get("text").getAsString();

            // Parse text alignment
            UIAlign textHorizAlign = UIAlign.CENTRE;
            UIAlign textVertAlign = UIAlign.CENTRE;
            if (json.has("text_alignment")) {
                if (!json.get("text_alignment").isJsonArray()) {
                    throw new IllegalStateException("UI text Alignment must be array [horizontal, vertical]" + context);
                }

                JsonArray alignmentArray = json.get("text_alignment").getAsJsonArray();
                if (alignmentArray.size() < 2) {
                    throw new IllegalStateException("UI text Alignment array must have at least 2 elements" + context);
                }
                String horizAlignKey = alignmentArray.get(0).getAsString();
                String vertAlignKey = alignmentArray.get(1).getAsString();

                textHorizAlign = UIAlign.valueOf(horizAlignKey.toUpperCase());
                textVertAlign = UIAlign.valueOf(vertAlignKey.toUpperCase());
            }

            // For buttons with text, create a child AmbleText element with transparent background
            AmbleText textChild = AmbleText.textBuilder()
                    .text(Text.translatable(text))
                    .textHorizontalAlign(textHorizAlign)
                    .textVerticalAlign(textVertAlign)
                    .layout(new Rectangle(base.getLayout()))
                    .background(new Color(0, 0, 0, 0))
                    .build();
            base.addChild(textChild);
        }

        AmbleButton button = AmbleButton.buttonBuilder().build();
        button.copyFrom(base);

        if (json.has("on_click")) {
            // todo run actual java methods via reflection
            String clickCommand = json.get("on_click").getAsString();
            button.setOnClick(() -> {
                try {
                    String string2 = SharedConstants.stripInvalidChars(clickCommand);
                    if (string2.startsWith("/")) {
                        if (!MinecraftClient.getInstance().player.networkHandler.sendCommand(string2.substring(1))) {
                            AmbleKit.LOGGER.error("Not allowed to run command with signed argument from click event: '{}'", string2);
                        }
                    } else {
                        AmbleKit.LOGGER.error("Failed to run command without '/' prefix from click event: '{}'", string2);
                    }
                } catch (Exception e) {
                    AmbleKit.LOGGER.error("Error occurred while running command from click event: '{}'", clickCommand, e);
                }
            });
        } else {
            button.setOnClick(() -> {
            });
        }

        if (json.has("script")) {
            Identifier scriptId = new Identifier(json.get("script").getAsString()).withPrefixedPath("script/").withSuffixedPath(".lua");
            LuaScript script = ScriptManager.getInstance().load(
                    scriptId,
                    MinecraftClient.getInstance().getResourceManager()
            );

            button.setScript(script);
        }

        if (json.has("hover_background")) {
            AmbleDisplayType hoverBg = AmbleDisplayType.parse(json.get("hover_background"));
            button.setHoverDisplay(hoverBg);
        } else {
            button.setHoverDisplay(button.getBackground());
        }

        if (json.has("press_background")) {
            AmbleDisplayType pressBg = AmbleDisplayType.parse(json.get("press_background"));
            button.setPressDisplay(pressBg);
        } else {
            button.setPressDisplay(button.getBackground());
        }

        return button;
    }

    @Override
    public int priority() {
        // Button has higher priority than text since buttons can have text
        return 100;
    }
}

