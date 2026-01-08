-- Player Tracker Script: Monitors players and logs activity
-- Enable with: /serverscript enable litmus:player_tracker
-- Disable with: /serverscript disable litmus:player_tracker
--
-- This is a SERVER-SIDE script that tracks player activity.

local lastPlayerCount = 0
local tickCount = 0
local CHECK_INTERVAL = 100 -- Check every 5 seconds (100 ticks)

function onEnable(mc)
    lastPlayerCount = mc:playerCount()
    tickCount = 0
    mc:log("Player Tracker enabled - tracking " .. lastPlayerCount .. " players")
    mc:broadcast("§e[Server] §7Player tracking enabled")
end

function onTick(mc)
    tickCount = tickCount + 1
    
    if tickCount % CHECK_INTERVAL ~= 0 then
        return
    end
    
    local currentCount = mc:playerCount()
    
    -- Check if player count changed
    if currentCount ~= lastPlayerCount then
        local diff = currentCount - lastPlayerCount
        if diff > 0 then
            mc:log("Player count increased: " .. lastPlayerCount .. " -> " .. currentCount)
        else
            mc:log("Player count decreased: " .. lastPlayerCount .. " -> " .. currentCount)
        end
        lastPlayerCount = currentCount
    end
    
    -- Log all player positions every 5 seconds
    local players = mc:allPlayers()
    for _, player in ipairs(players) do
        local pos = player:position()
        mc:log("Player " .. player:name() .. " at " .. 
               string.format("%.0f, %.0f, %.0f", pos.x, pos.y, pos.z) ..
               " (Health: " .. string.format("%.1f", player:health()) .. ")")
    end
end

function onDisable(mc)
    mc:log("Player Tracker disabled after " .. tickCount .. " ticks")
    mc:broadcast("§e[Server] §7Player tracking disabled")
end
