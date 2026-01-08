-- World Info Script: Displays world environment information
-- Run with: /amblescript execute litmus:world_info

function onExecute()
    local player = minecraft:player()
    local pos = player:blockPosition()
    
    -- Header
    minecraft:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
    minecraft:sendMessage("§e§l✦ World Information ✦", false)
    minecraft:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
    
    -- Dimension
    local dimension = minecraft:dimension()
    local dimColor = "§a"
    if dimension:find("nether") then
        dimColor = "§c"
    elseif dimension:find("end") then
        dimColor = "§d"
    end
    minecraft:sendMessage("§7Dimension: " .. dimColor .. dimension, false)
    
    -- Time of day
    local worldTime = minecraft:worldTime()
    local dayCount = minecraft:dayCount()
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
    
    minecraft:sendMessage("§7Time: §e" .. timeIcon .. " " .. timeString .. " §7(Day §f" .. dayCount .. "§7)", false)
    
    -- Weather
    local weatherIcon = "☀"
    local weatherText = "Clear"
    local weatherColor = "§e"
    if minecraft:isThundering() then
        weatherIcon = "⚡"
        weatherText = "Thunderstorm"
        weatherColor = "§5"
    elseif minecraft:isRaining() then
        weatherIcon = "🌧"
        weatherText = "Raining"
        weatherColor = "§9"
    end
    minecraft:sendMessage("§7Weather: " .. weatherColor .. weatherIcon .. " " .. weatherText, false)
    
    minecraft:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
    minecraft:sendMessage("§e§l✦ Location Details ✦", false)
    minecraft:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
    
    -- Biome
    local biome = minecraft:biomeAt(pos.x, pos.y, pos.z)
    local cleanBiome = biome:gsub("minecraft:", ""):gsub("_", " ")
    minecraft:sendMessage("§7Biome: §a" .. cleanBiome, false)
    
    -- Block below player
    local blockBelow = minecraft:blockAt(pos.x, pos.y - 1, pos.z)
    local cleanBlock = blockBelow:gsub("minecraft:", ""):gsub("_", " ")
    minecraft:sendMessage("§7Standing on: §b" .. cleanBlock, false)
    
    -- Light level
    local lightLevel = minecraft:lightLevelAt(pos.x, pos.y, pos.z)
    local lightColor = "§a"
    if lightLevel < 8 then
        lightColor = "§c" -- Mobs can spawn
    elseif lightLevel < 12 then
        lightColor = "§e"
    end
    minecraft:sendMessage("§7Light Level: " .. lightColor .. lightLevel .. " §8(mobs spawn below 8)", false)
    
    -- Looking at block
    local lookingAt = minecraft:lookingAtBlock()
    if lookingAt then
        local targetBlock = minecraft:blockAt(lookingAt.x, lookingAt.y, lookingAt.z)
        local cleanTarget = targetBlock:gsub("minecraft:", ""):gsub("_", " ")
        minecraft:sendMessage("§7Looking at: §d" .. cleanTarget .. " §8(" .. lookingAt.x .. ", " .. lookingAt.y .. ", " .. lookingAt.z .. ")", false)
    end
    
    minecraft:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
end
