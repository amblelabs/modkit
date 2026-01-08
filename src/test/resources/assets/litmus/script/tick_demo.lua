-- Tick Demo Script: Demonstrates onEnable, onTick, onDisable lifecycle
-- Enable with: /amblescript enable litmus:tick_demo
-- Disable with: /amblescript disable litmus:tick_demo
--
-- Note: minecraft data is passed as first argument to callbacks.
-- Use mc:isClientSide() to check if running on client or server.

local tickCount = 0
local lastSecond = 0

function onEnable(mc)
    tickCount = 0
    lastSecond = 0
    mc:sendMessage("§a✓ Tick Demo enabled! Counting ticks...", false)
    mc:log("Tick Demo enabled - isClientSide: " .. tostring(mc:isClientSide()))
    
    -- playSound only available on client
    if mc:isClientSide() then
        mc:playSound("minecraft:block.note_block.pling", 1.0, 2.0)
    end
end

function onTick(mc)
    tickCount = tickCount + 1
    
    -- Every 20 ticks (1 second), show a message
    local currentSecond = math.floor(tickCount / 20)
    if currentSecond > lastSecond then
        lastSecond = currentSecond
        
        -- Show in action bar every second
        mc:sendMessage("§7Tick Demo: §e" .. tickCount .. " ticks §7(§f" .. currentSecond .. "s§7)", true)
        
        -- Play a subtle sound every 5 seconds (client only)
        if currentSecond % 5 == 0 and mc:isClientSide() then
            mc:playSound("minecraft:block.note_block.hat", 0.5, 1.0)
        end
    end
end

function onDisable(mc)
    local totalSeconds = math.floor(tickCount / 20)
    mc:sendMessage("§c✗ Tick Demo disabled!", false)
    mc:sendMessage("§7  Ran for §e" .. tickCount .. " ticks §7(§f" .. totalSeconds .. " seconds§7)", false)
    
    if mc:isClientSide() then
        mc:playSound("minecraft:block.note_block.bass", 1.0, 0.5)
    end
end
