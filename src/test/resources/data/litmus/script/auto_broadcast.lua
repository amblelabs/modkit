-- Auto Broadcast Script: Periodically broadcasts messages to all players
-- Enable with: /serverscript enable litmus:auto_broadcast
-- Disable with: /serverscript disable litmus:auto_broadcast
--
-- This is a SERVER-SIDE script that broadcasts messages periodically.

local tickCount = 0
local messageIndex = 1
local BROADCAST_INTERVAL = 1200 -- Every 60 seconds (1200 ticks)

-- Messages to cycle through
local messages = {
    "§6§l[TIP] §r§7Use §e/serverscript list §7to see enabled scripts!",
    "§6§l[TIP] §r§7Server-side scripts run for all players automatically.",
    "§6§l[TIP] §r§7Scripts are loaded from the §edata §7folder.",
    "§6§l[INFO] §r§7This message was sent by a Lua script!",
    "§a§l[SERVER] §r§7Welcome to the server! Enjoy your stay.",
}

function onEnable(mc)
    tickCount = 0
    messageIndex = 1
    mc:log("Auto Broadcast enabled with " .. #messages .. " messages")
    mc:broadcast("§a§l[Server] §r§7Auto broadcast enabled!")
end

function onTick(mc)
    tickCount = tickCount + 1
    
    if tickCount % BROADCAST_INTERVAL ~= 0 then
        return
    end
    
    -- Broadcast current message
    local message = messages[messageIndex]
    mc:broadcast(message)
    mc:log("Broadcasted message " .. messageIndex .. ": " .. message)
    
    -- Move to next message
    messageIndex = messageIndex + 1
    if messageIndex > #messages then
        messageIndex = 1
    end
end

function onDisable(mc)
    mc:broadcast("§c§l[Server] §r§7Auto broadcast disabled")
    mc:log("Auto Broadcast disabled after " .. tickCount .. " ticks")
end
