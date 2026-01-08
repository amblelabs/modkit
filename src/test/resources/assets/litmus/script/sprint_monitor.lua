-- Sprint Monitor Script: Shows sprint/movement info in action bar
-- Enable with: /amblescript enable litmus:sprint_monitor
-- Disable with: /amblescript disable litmus:sprint_monitor

local lastUpdate = 0
local UPDATE_INTERVAL = 2 -- Update every 2 ticks for smooth display

function onEnable()
    minecraft:sendMessage("§b🏃 Sprint Monitor enabled!", false)
end

function onTick()
    lastUpdate = lastUpdate + 1
    if lastUpdate < UPDATE_INTERVAL then
        return
    end
    lastUpdate = 0
    
    local player = minecraft:player()
    local vel = player:velocity()
    local speed = math.sqrt(vel.x * vel.x + vel.z * vel.z) * 20 -- blocks per second
    
    -- Build status string
    local status = ""
    
    -- Movement mode
    if player:isFlying() then
        status = status .. "§b✈ Flying "
    elseif player:isSwimming() then
        status = status .. "§3🏊 Swimming "
    elseif player:isSprinting() then
        status = status .. "§a🏃 Sprinting "
    elseif player:isSneaking() then
        status = status .. "§7🚶 Sneaking "
    elseif speed > 0.1 then
        status = status .. "§f🚶 Walking "
    else
        status = status .. "§8⏸ Still "
    end
    
    -- Speed indicator
    local speedColor = "§7"
    if speed > 10 then
        speedColor = "§c"
    elseif speed > 7 then
        speedColor = "§e"
    elseif speed > 4 then
        speedColor = "§a"
    end
    status = status .. speedColor .. string.format("%.1f", speed) .. " m/s"
    
    -- Ground state
    if not player:isOnGround() and not player:isFlying() and not player:isSwimming() then
        status = status .. " §d↑ Airborne"
    end
    
    -- Water state
    if player:isTouchingWater() and not player:isSwimming() then
        status = status .. " §9💧"
    end
    
    minecraft:sendMessage(status, true)
end

function onDisable()
    minecraft:sendMessage("§7🏃 Sprint Monitor disabled", false)
end
