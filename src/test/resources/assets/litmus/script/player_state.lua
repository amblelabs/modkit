-- Player State Script: Shows detailed player state information
-- Run with: /amblescript execute litmus:player_state

function onExecute()
    local player = minecraft:player()
    
    -- Header
    minecraft:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
    minecraft:sendMessage("§e§l✦ Player State: §f" .. minecraft:username() .. " §e§l✦", false)
    minecraft:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
    
    -- Health & Hunger
    local health = player:health()
    local maxHealth = player:maxHealth()
    local food = player:foodLevel()
    local saturation = player:saturation()
    local armor = player:armorValue()
    
    minecraft:sendMessage("§c❤ Health: §f" .. string.format("%.1f", health) .. "§7/§f" .. string.format("%.0f", maxHealth), false)
    minecraft:sendMessage("§6🍖 Hunger: §f" .. food .. "§7/§f20 §8(Saturation: " .. string.format("%.1f", saturation) .. ")", false)
    minecraft:sendMessage("§9🛡 Armor: §f" .. armor, false)
    
    -- Experience
    local xpLevel = player:experienceLevel()
    local xpProgress = player:experienceProgress()
    local totalXp = player:totalExperience()
    
    -- XP bar visualization
    local barLength = 20
    local filledLength = math.floor(xpProgress * barLength)
    local xpBar = "§a"
    for i = 1, barLength do
        if i <= filledLength then
            xpBar = xpBar .. "|"
        else
            xpBar = xpBar .. "§8|"
        end
    end
    minecraft:sendMessage("§a✧ Level: §f" .. xpLevel .. " " .. xpBar .. " §7(" .. string.format("%.0f", xpProgress * 100) .. "%)", false)
    minecraft:sendMessage("§7  Total XP: §e" .. totalXp, false)
    
    minecraft:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
    minecraft:sendMessage("§e§l✦ Movement State ✦", false)
    minecraft:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
    
    -- Movement states
    local states = {}
    
    if player:isOnGround() then
        table.insert(states, "§a✓ On Ground")
    else
        table.insert(states, "§c✗ Airborne")
    end
    
    if player:isSprinting() then
        table.insert(states, "§a✓ Sprinting")
    end
    
    if player:isSneaking() then
        table.insert(states, "§a✓ Sneaking")
    end
    
    if player:isSwimming() then
        table.insert(states, "§b✓ Swimming")
    end
    
    if player:isTouchingWater() then
        table.insert(states, "§b✓ In Water")
    end
    
    if player:isFlying() then
        table.insert(states, "§d✓ Flying")
    end
    
    if player:isOnFire() then
        table.insert(states, "§c🔥 On Fire!")
    end
    
    if player:isInvisible() then
        table.insert(states, "§7✓ Invisible")
    end
    
    if player:isGlowing() then
        table.insert(states, "§e✓ Glowing")
    end
    
    for _, state in ipairs(states) do
        minecraft:sendMessage("  " .. state, false)
    end
    
    -- Velocity
    local vel = player:velocity()
    local speed = math.sqrt(vel.x * vel.x + vel.z * vel.z)
    minecraft:sendMessage("§7Speed: §f" .. string.format("%.2f", speed * 20) .. " §7blocks/sec", false)
    
    -- Game mode
    minecraft:sendMessage("§7Game Mode: §e" .. minecraft:gameMode(), false)
    
    minecraft:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
    minecraft:sendMessage("§e§l✦ Active Effects ✦", false)
    minecraft:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
    
    -- Status effects
    local effects = player:effects()
    if #effects > 0 then
        for _, effect in ipairs(effects) do
            local cleanEffect = effect:gsub("minecraft:", ""):gsub("_", " ")
            minecraft:sendMessage("  §d✦ §f" .. cleanEffect, false)
        end
    else
        minecraft:sendMessage("  §8No active effects", false)
    end
    
    minecraft:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
end
