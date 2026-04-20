-- Admin Commands Script: Utility commands for server administration
-- Run with: /serverscript execute litmus:admin_commands
--
-- This is a SERVER-SIDE script with various admin utilities.

function onExecute(mc, args)
    mc:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
    mc:sendMessage("§e§l✦ Admin Commands Executed ✦", false)
    mc:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
    
    -- Log current server state
    mc:log("=== Admin Commands Executed ===")
    mc:log("Server: " .. mc:serverName())
    mc:log("TPS: " .. string.format("%.1f", mc:serverTps()))
    mc:log("Players: " .. mc:playerCount() .. "/" .. mc:maxPlayers())
    
    -- List all players with their locations
    local players = mc:allPlayers()
    mc:sendMessage("§e§l✦ Player Locations ✦", false)
    
    for _, player in ipairs(players) do
        local pos = player:position()
        local health = player:health()
        local maxHealth = player:maxHealth()
        
        local healthColor = "§a"
        if health / maxHealth < 0.25 then
            healthColor = "§c"
        elseif health / maxHealth < 0.5 then
            healthColor = "§e"
        end
        
        local locationStr = string.format("§f%.0f§7, §f%.0f§7, §f%.0f", pos.x, pos.y, pos.z)
        local healthStr = healthColor .. string.format("%.0f", health) .. "§7/" .. string.format("%.0f", maxHealth)
        
        mc:sendMessage("  §a" .. player:name() .. " §7→ " .. locationStr .. " §7(" .. healthStr .. "§7)", false)
        mc:log("Player: " .. player:name() .. " at " .. locationStr .. " health: " .. health)
    end
    
    if #players == 0 then
        mc:sendMessage("  §8No players online", false)
    end
    
    -- World info
    mc:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
    mc:sendMessage("§e§l✦ World State ✦", false)
    mc:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
    
    mc:sendMessage("§7Dimension: §f" .. mc:dimension(), false)
    mc:sendMessage("§7World Time: §f" .. mc:worldTime() .. " §7(Day " .. mc:dayCount() .. ")", false)
    
    local weather = "Clear"
    if mc:isThundering() then
        weather = "Thunderstorm"
    elseif mc:isRaining() then
        weather = "Raining"
    end
    mc:sendMessage("§7Weather: §f" .. weather, false)
    
    mc:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
    mc:sendMessage("§7Admin report logged to console.", false)
    mc:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
    
    mc:log("=== End Admin Report ===")
end
