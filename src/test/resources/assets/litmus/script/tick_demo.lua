-- Tick Demo Script: Demonstrates onEnable, onTick, onDisable lifecycle
-- Enable with: /amblescript enable litmus:tick_demo
-- Disable with: /amblescript disable litmus:tick_demo

local tickCount = 0
local lastSecond = 0

function onEnable()
    tickCount = 0
    lastSecond = 0
    minecraft:sendMessage("§a✓ Tick Demo enabled! Counting ticks...", false)
    minecraft:playSound("minecraft:block.note_block.pling", 1.0, 2.0)
end

function onTick()
    tickCount = tickCount + 1
    
    -- Every 20 ticks (1 second), show a message
    local currentSecond = math.floor(tickCount / 20)
    if currentSecond > lastSecond then
        lastSecond = currentSecond
        
        -- Show in action bar every second
        minecraft:sendMessage("§7Tick Demo: §e" .. tickCount .. " ticks §7(§f" .. currentSecond .. "s§7)", true)
        
        -- Play a subtle sound every 5 seconds
        if currentSecond % 5 == 0 then
            minecraft:playSound("minecraft:block.note_block.hat", 0.5, 1.0)
        end
    end
end

function onDisable()
    local totalSeconds = math.floor(tickCount / 20)
    minecraft:sendMessage("§c✗ Tick Demo disabled!", false)
    minecraft:sendMessage("§7  Ran for §e" .. tickCount .. " ticks §7(§f" .. totalSeconds .. " seconds§7)", false)
    minecraft:playSound("minecraft:block.note_block.bass", 1.0, 0.5)
end
