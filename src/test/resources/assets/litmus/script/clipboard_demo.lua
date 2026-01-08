-- Clipboard Demo Script: Demonstrates clipboard and UI functionality
-- Run with: /amblescript execute litmus:clipboard_demo
--
-- Note: This script uses client-only features (clipboard, window size)

function onExecute(mc)
    -- Check if we're on the client side
    if not mc:isClientSide() then
        mc:sendMessage("§cThis script requires client-side features!", false)
        return
    end
    
    local player = mc:player()
    local pos = player:position()
    
    -- Header
    mc:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
    mc:sendMessage("§e§l✦ Clipboard Demo ✦", false)
    mc:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
    
    -- Show current clipboard content
    local currentClipboard = mc:clipboard()
    if currentClipboard and currentClipboard ~= "" then
        local preview = currentClipboard
        if #preview > 50 then
            preview = preview:sub(1, 50) .. "..."
        end
        mc:sendMessage("§7Current clipboard: §f" .. preview, false)
    else
        mc:sendMessage("§7Current clipboard: §8(empty)", false)
    end
    
    mc:sendMessage("", false)
    
    -- Copy coordinates to clipboard
    local coords = string.format("%.0f %.0f %.0f", pos.x, pos.y, pos.z)
    mc:setClipboard(coords)
    mc:sendMessage("§a✓ Copied coordinates to clipboard!", false)
    mc:sendMessage("§7  " .. coords, false)
    
    mc:sendMessage("", false)
    
    -- Window info
    mc:sendMessage("§e§l✦ Window Info ✦", false)
    mc:sendMessage("§7Window size: §f" .. mc:windowWidth() .. "§7 x §f" .. mc:windowHeight(), false)
    
    -- Play a sound to indicate success
    mc:playSound("minecraft:entity.experience_orb.pickup", 1.0, 1.5)
    
    mc:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
    mc:sendMessage("§7Tip: Paste (Ctrl+V) to use the coordinates!", false)
    mc:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
end
