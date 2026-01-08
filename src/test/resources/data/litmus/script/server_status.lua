-- Server Status Script: Shows server information and statistics
-- Run with: /serverscript execute litmus:server_status
--
-- This is a SERVER-SIDE script. It runs on the server and has access to
-- all players, server TPS, and other server-specific information.

function onExecute(mc)
    -- Confirm we're on the server
    if mc:isClientSide() then
        mc:sendMessage("§cThis script should only run on the server!", false)
        return
    end
    
    mc:log("Server status script executed")
    
    -- Header
    mc:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
    mc:sendMessage("§e§l✦ Server Status ✦", false)
    mc:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
    
    -- Server info
    mc:sendMessage("§7Server: §f" .. mc:serverName(), false)
    mc:sendMessage("§7Type: §f" .. (mc:isDedicatedServer() and "Dedicated" or "Integrated"), false)
    
    -- Performance
    local tps = mc:serverTps()
    local tpsColor = "§a"
    if tps < 15 then
        tpsColor = "§c"
    elseif tps < 18 then
        tpsColor = "§e"
    end
    mc:sendMessage("§7TPS: " .. tpsColor .. string.format("%.1f", tps) .. "§7/20", false)
    mc:sendMessage("§7Tick Count: §f" .. mc:tickCount(), false)
    
    -- World info
    mc:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
    mc:sendMessage("§e§l✦ World Info ✦", false)
    mc:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
    
    mc:sendMessage("§7Dimension: §f" .. mc:dimension(), false)
    mc:sendMessage("§7Day: §f" .. mc:dayCount(), false)
    
    local weatherIcon = "☀"
    local weatherText = "Clear"
    local weatherColor = "§e"
    if mc:isThundering() then
        weatherIcon = "⚡"
        weatherText = "Thunderstorm"
        weatherColor = "§5"
    elseif mc:isRaining() then
        weatherIcon = "🌧"
        weatherText = "Raining"
        weatherColor = "§9"
    end
    mc:sendMessage("§7Weather: " .. weatherColor .. weatherIcon .. " " .. weatherText, false)
    
    -- Players
    mc:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
    mc:sendMessage("§e§l✦ Players (" .. mc:playerCount() .. "/" .. mc:maxPlayers() .. ") ✦", false)
    mc:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
    
    local players = mc:allPlayerNames()
    if #players > 0 then
        for _, name in ipairs(players) do
            mc:sendMessage("  §a• §f" .. name, false)
        end
    else
        mc:sendMessage("  §8No players online", false)
    end
    
    -- Worlds
    local worlds = mc:worldNames()
    if #worlds > 0 then
        mc:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
        mc:sendMessage("§e§l✦ Loaded Worlds ✦", false)
        mc:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
        for _, world in ipairs(worlds) do
            mc:sendMessage("  §b• §f" .. world, false)
        end
    end
    
    mc:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
end
