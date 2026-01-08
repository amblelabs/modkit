package dev.amble.lib.client.gui.lua;

import org.luaj.vm2.LuaValue;

public record GuiScript(
        LuaValue onInit,
        LuaValue onClick,
        LuaValue onRelease,
        LuaValue onHover
) {}
