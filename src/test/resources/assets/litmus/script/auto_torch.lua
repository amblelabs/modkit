-- Auto Torch Script: Warns when light level is low (mobs can spawn)
-- Enable with: /amblescript enable litmus:auto_torch
-- Disable with: /amblescript disable litmus:auto_torch

local lastWarning = 0
local WARNING_COOLDOWN = 100 -- ticks between warnings (5 seconds)
local ticksSinceWarning = WARNING_COOLDOWN

function onEnable()
    minecraft:sendMessage("§e🔦 Auto Torch Advisor enabled!", false)
    minecraft:sendMessage("§7  Will warn you when light level is dangerously low", false)
end

function onTick()
    ticksSinceWarning = ticksSinceWarning + 1
    
    -- Only check every 10 ticks for performance
    if ticksSinceWarning % 10 ~= 0 then
        return
    end
    
    local player = minecraft:player()
    local pos = player:blockPosition()
    local lightLevel = minecraft:lightLevelAt(pos.x, pos.y, pos.z)
    
    -- Check if we're on the ground and light is low
    if player:isOnGround() and lightLevel < 8 and ticksSinceWarning >= WARNING_COOLDOWN then
        ticksSinceWarning = 0
        
        -- Different warnings based on light level
        if lightLevel <= 0 then
            minecraft:sendMessage("§4⚠ DANGER! §cComplete darkness (Light: " .. lightLevel .. ") - Mobs WILL spawn!", true)
            minecraft:playSound("minecraft:block.note_block.bass", 0.8, 0.5)
        elseif lightLevel <= 3 then
            minecraft:sendMessage("§c⚠ Warning! §eVery dark (Light: " .. lightLevel .. ") - High spawn risk!", true)
            minecraft:playSound("minecraft:block.note_block.hat", 0.5, 0.8)
        else
            minecraft:sendMessage("§e⚠ Caution: §7Low light (Light: " .. lightLevel .. ") - Mobs can spawn", true)
        end
    end
end

function onDisable()
    minecraft:sendMessage("§7🔦 Auto Torch Advisor disabled", false)
end
