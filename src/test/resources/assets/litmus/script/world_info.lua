-- World Info Script: Displays world environment information
-- Run with: /amblescript execute litmus:world_info
--
-- Note: minecraft data is passed as first argument to callbacks.

function onExecute(mc)
    local player = mc:player()
    local pos = player:blockPosition()
    
    -- Header
    mc:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
    mc:sendMessage("§e§l✦ World Information ✦", false)
    mc:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
    
    -- Dimension
    local dimension = mc:dimension()
    local dimColor = "§a"
    if dimension:find("nether") then
        dimColor = "§c"
    elseif dimension:find("end") then
        dimColor = "§d"
    end
    mc:sendMessage("§7Dimension: " .. dimColor .. dimension, false)
    
    -- Time of day
    local worldTime = mc:worldTime()
    local dayCount = mc:dayCount()
    local timeOfDay = worldTime % 24000
    
    local timeString = "Day"
    local timeIcon = "☀"
    if timeOfDay >= 13000 and timeOfDay < 23000 then
        timeString = "Night"
        timeIcon = "☾"
    elseif timeOfDay >= 23000 or timeOfDay < 1000 then
        timeString = "Dawn"
        timeIcon = "✧"
    elseif timeOfDay >= 11000 and timeOfDay < 13000 then
        timeString = "Dusk"
        timeIcon = "✧"
    end
    
    mc:sendMessage("§7Time: §e" .. timeIcon .. " " .. timeString .. " §7(Day §f" .. dayCount .. "§7)", false)
    
    -- Weather
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
    
    mc:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
    mc:sendMessage("§e§l✦ Location Details ✦", false)
    mc:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
    
    -- Biome
    local biome = mc:biomeAt(pos.x, pos.y, pos.z)
    local cleanBiome = biome:gsub("minecraft:", ""):gsub("_", " ")
    mc:sendMessage("§7Biome: §a" .. cleanBiome, false)
    
    -- Block below player
    local blockBelow = mc:blockAt(pos.x, pos.y - 1, pos.z)
    local cleanBlock = blockBelow:gsub("minecraft:", ""):gsub("_", " ")
    mc:sendMessage("§7Standing on: §b" .. cleanBlock, false)
    
    -- Light level
    local lightLevel = mc:lightLevelAt(pos.x, pos.y, pos.z)
    local lightColor = "§a"
    if lightLevel < 8 then
        lightColor = "§c" -- Mobs can spawn
    elseif lightLevel < 12 then
        lightColor = "§e"
    end
    mc:sendMessage("§7Light Level: " .. lightColor .. lightLevel .. " §8(mobs spawn below 8)", false)
    
    -- Looking at block (client only feature)
    if mc:isClientSide() then
        local lookingAt = mc:lookingAtBlock()
        if lookingAt then
            local targetBlock = mc:blockAt(lookingAt.x, lookingAt.y, lookingAt.z)
            local cleanTarget = targetBlock:gsub("minecraft:", ""):gsub("_", " ")
            mc:sendMessage("§7Looking at: §d" .. cleanTarget .. " §8(" .. lookingAt.x .. ", " .. lookingAt.y .. ", " .. lookingAt.z .. ")", false)
        end
    end
    
    mc:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
end
