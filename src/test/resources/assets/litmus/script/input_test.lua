-- Input Test Script: Shows which movement keys are currently pressed
-- Run with: /amblescript execute litmus:input_test

function onExecute()
    -- Header
    minecraft:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
    minecraft:sendMessage("§e§l✦ Input State ✦", false)
    minecraft:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
    
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
    local w = minecraft:isKeyPressed("forward") and "§a[W]" or "§8[W]"
    local a = minecraft:isKeyPressed("left") and "§a[A]" or "§8[A]"
    local s = minecraft:isKeyPressed("back") and "§a[S]" or "§8[S]"
    local d = minecraft:isKeyPressed("right") and "§a[D]" or "§8[D]"
    
    minecraft:sendMessage("§7Movement Keys:", false)
    minecraft:sendMessage("      " .. w, false)
    minecraft:sendMessage("    " .. a .. " " .. s .. " " .. d, false)
    minecraft:sendMessage("", false)
    
    -- Other keys
    minecraft:sendMessage("§7Action Keys:", false)
    
    local pressedKeys = {}
    local unpressedKeys = {}
    
    for _, keyData in ipairs(keys) do
        local keyName = keyData[1]
        local displayKey = keyData[2]
        local description = keyData[3]
        
        if minecraft:isKeyPressed(keyName) then
            table.insert(pressedKeys, "  §a✓ " .. displayKey .. " §7(" .. description .. ")")
        end
    end
    
    if #pressedKeys > 0 then
        for _, msg in ipairs(pressedKeys) do
            minecraft:sendMessage(msg, false)
        end
    else
        minecraft:sendMessage("  §8No action keys pressed", false)
    end
    
    minecraft:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
    minecraft:sendMessage("§7Tip: Hold keys while running this script!", false)
    minecraft:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
end
