-- Clipboard Demo Script: Demonstrates clipboard and UI functionality
-- Run with: /amblescript execute litmus:clipboard_demo

function onExecute()
    local player = minecraft:player()
    local pos = player:position()
    
    -- Header
    minecraft:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
    minecraft:sendMessage("§e§l✦ Clipboard Demo ✦", false)
    minecraft:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
    
    -- Show current clipboard content
    local currentClipboard = minecraft:clipboard()
    if currentClipboard and currentClipboard ~= "" then
        local preview = currentClipboard
        if #preview > 50 then
            preview = preview:sub(1, 50) .. "..."
        end
        minecraft:sendMessage("§7Current clipboard: §f" .. preview, false)
    else
        minecraft:sendMessage("§7Current clipboard: §8(empty)", false)
    end
    
    minecraft:sendMessage("", false)
    
    -- Copy coordinates to clipboard
    local coords = string.format("%.0f %.0f %.0f", pos.x, pos.y, pos.z)
    minecraft:setClipboard(coords)
    minecraft:sendMessage("§a✓ Copied coordinates to clipboard!", false)
    minecraft:sendMessage("§7  " .. coords, false)
    
    minecraft:sendMessage("", false)
    
    -- Window info
    minecraft:sendMessage("§e§l✦ Window Info ✦", false)
    minecraft:sendMessage("§7Window size: §f" .. minecraft:windowWidth() .. "§7 x §f" .. minecraft:windowHeight(), false)
    
    -- Play a sound to indicate success
    minecraft:playSound("minecraft:entity.experience_orb.pickup", 1.0, 1.5)
    
    minecraft:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
    minecraft:sendMessage("§7Tip: Paste (Ctrl+V) to use the coordinates!", false)
    minecraft:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
end
