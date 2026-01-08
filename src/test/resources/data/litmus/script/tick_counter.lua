-- Tick Counter Script: Simple server-side tick counter demonstration
-- Enable with: /serverscript enable litmus:tick_counter
-- Disable with: /serverscript disable litmus:tick_counter
--
-- This is a SERVER-SIDE script demonstrating the tick lifecycle.

local tickCount = 0
local lastSecond = 0
local LOG_INTERVAL = 200 -- Log every 10 seconds (200 ticks)

function onEnable(mc)
    tickCount = 0
    lastSecond = 0
    
    mc:log("Server Tick Counter enabled")
    mc:log("  - TPS: " .. string.format("%.1f", mc:serverTps()))
    mc:log("  - Players online: " .. mc:playerCount())
    
    -- Notify all players
    mc:broadcast("§a[Server] §7Tick counter script enabled!")
end

function onTick(mc)
    tickCount = tickCount + 1
    
    -- Log to console every LOG_INTERVAL ticks
    if tickCount % LOG_INTERVAL == 0 then
        local seconds = tickCount / 20
        mc:log("Tick Counter: " .. tickCount .. " ticks (" .. seconds .. "s) - TPS: " .. string.format("%.1f", mc:serverTps()))
    end
    
    -- Broadcast to players every 60 seconds
    local currentSecond = math.floor(tickCount / 20)
    if currentSecond > lastSecond and currentSecond % 60 == 0 then
        lastSecond = currentSecond
        local minutes = math.floor(currentSecond / 60)
        mc:broadcast("§7[Server] Script running for §e" .. minutes .. " minute" .. (minutes > 1 and "s" or ""))
    end
end

function onDisable(mc)
    local totalSeconds = math.floor(tickCount / 20)
    local minutes = math.floor(totalSeconds / 60)
    local seconds = totalSeconds % 60
    
    mc:log("Server Tick Counter disabled")
    mc:log("  - Total ticks: " .. tickCount)
    mc:log("  - Runtime: " .. minutes .. "m " .. seconds .. "s")
    
    mc:broadcast("§c[Server] §7Tick counter disabled after §e" .. minutes .. "m " .. seconds .. "s")
end
