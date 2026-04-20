-- Weather Announcer Script: Announces weather changes to all players
-- Enable with: /serverscript enable litmus:weather_announcer
-- Disable with: /serverscript disable litmus:weather_announcer
--
-- This is a SERVER-SIDE script that monitors and announces weather changes.

local lastRaining = false
local lastThundering = false
local CHECK_INTERVAL = 20 -- Check every second

local tickCount = 0

function onEnable(mc)
    lastRaining = mc:isRaining()
    lastThundering = mc:isThundering()
    tickCount = 0
    
    -- Announce current weather
    local weather = getWeatherName(lastRaining, lastThundering)
    mc:broadcast("§e☁ Weather Announcer enabled! §7Current weather: " .. weather)
    mc:log("Weather Announcer enabled - current weather: " .. weather)
end

function getWeatherName(raining, thundering)
    if thundering then
        return "§5⚡ Thunderstorm"
    elseif raining then
        return "§9🌧 Rain"
    else
        return "§e☀ Clear"
    end
end

function onTick(mc)
    tickCount = tickCount + 1
    
    if tickCount % CHECK_INTERVAL ~= 0 then
        return
    end
    
    local currentRaining = mc:isRaining()
    local currentThundering = mc:isThundering()
    
    -- Check for weather changes
    if currentRaining ~= lastRaining or currentThundering ~= lastThundering then
        local oldWeather = getWeatherName(lastRaining, lastThundering)
        local newWeather = getWeatherName(currentRaining, currentThundering)
        
        mc:broadcast("§6§l[Weather] §r" .. oldWeather .. " §7→ " .. newWeather)
        mc:log("Weather changed: " .. oldWeather .. " -> " .. newWeather)
        
        -- Play ambient sounds based on weather
        if currentThundering and not lastThundering then
            -- Could play thunder sound at all player locations
            mc:broadcast("§5⚡ A thunderstorm is approaching!")
        elseif currentRaining and not lastRaining then
            mc:broadcast("§9🌧 It's starting to rain...")
        elseif not currentRaining and lastRaining then
            mc:broadcast("§e☀ The weather is clearing up!")
        end
        
        lastRaining = currentRaining
        lastThundering = currentThundering
    end
end

function onDisable(mc)
    mc:broadcast("§7☁ Weather Announcer disabled")
    mc:log("Weather Announcer disabled")
end
