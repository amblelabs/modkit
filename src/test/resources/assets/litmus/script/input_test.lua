-- Input Test Script: Shows which movement keys are currently pressed
-- Run with: /amblescript execute litmus:input_test
--
-- Note: Uses client-only input detection features

function onExecute(mc, args)
    -- Check if we're on the client side
    if not mc:isClientSide() then
        mc:sendMessage("§cThis script requires client-side features!", false)
        return
    end
    
    -- Header
    mc:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
    mc:sendMessage("§e§l✦ Input State ✦", false)
    mc:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
    
    -- Movement keys
    local keys = {
        {"forward", "W", "Forward"},
        {"back", "S", "Back"},
        {"left", "A", "Left"},
        {"right", "D", "Right"},
        {"jump", "Space", "Jump"},
        {"sneak", "Shift", "Sneak"},
        {"sprint", "Ctrl", "Sprint"},
        {"attack", "LMB", "Attack"},
        {"use", "RMB", "Use"}
    }
    
    -- Visual keyboard layout for WASD
    local w = mc:isKeyPressed("forward") and "§a[W]" or "§8[W]"
    local a = mc:isKeyPressed("left") and "§a[A]" or "§8[A]"
    local s = mc:isKeyPressed("back") and "§a[S]" or "§8[S]"
    local d = mc:isKeyPressed("right") and "§a[D]" or "§8[D]"
    
    mc:sendMessage("§7Movement Keys:", false)
    mc:sendMessage("      " .. w, false)
    mc:sendMessage("    " .. a .. " " .. s .. " " .. d, false)
    mc:sendMessage("", false)
    
    -- Other keys
    mc:sendMessage("§7Action Keys:", false)
    
    local pressedKeys = {}
    
    for _, keyData in ipairs(keys) do
        local keyName = keyData[1]
        local displayKey = keyData[2]
        local description = keyData[3]
        
        if mc:isKeyPressed(keyName) then
            table.insert(pressedKeys, "  §a✓ " .. displayKey .. " §7(" .. description .. ")")
        end
    end
    
    if #pressedKeys > 0 then
        for _, msg in ipairs(pressedKeys) do
            mc:sendMessage(msg, false)
        end
    else
        mc:sendMessage("  §8No action keys pressed", false)
    end
    
    mc:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
    mc:sendMessage("§7Tip: Hold keys while running this script!", false)
    mc:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
end
