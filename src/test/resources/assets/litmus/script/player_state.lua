-- Player State Script: Shows detailed player state information
-- Run with: /amblescript execute litmus:player_state
--
-- Note: minecraft data is passed as first argument to callbacks

function onExecute(mc, args)
    local player = mc:player()
    
    -- Header - username is client-only, so we use player name instead
    local playerName = player:name()
    if mc:isClientSide() then
        playerName = mc:username()
    end
    
    mc:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
    mc:sendMessage("§e§l✦ Player State: §f" .. playerName .. " §e§l✦", false)
    mc:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
    
    -- Health & Hunger
    local health = player:health()
    local maxHealth = player:maxHealth()
    local food = player:foodLevel()
    local saturation = player:saturation()
    local armor = player:armorValue()
    
    mc:sendMessage("§c❤ Health: §f" .. string.format("%.1f", health) .. "§7/§f" .. string.format("%.0f", maxHealth), false)
    mc:sendMessage("§6🍖 Hunger: §f" .. food .. "§7/§f20 §8(Saturation: " .. string.format("%.1f", saturation) .. ")", false)
    mc:sendMessage("§9🛡 Armor: §f" .. armor, false)
    
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
    mc:sendMessage("§a✧ Level: §f" .. xpLevel .. " " .. xpBar .. " §7(" .. string.format("%.0f", xpProgress * 100) .. "%)", false)
    mc:sendMessage("§7  Total XP: §e" .. totalXp, false)
    
    mc:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
    mc:sendMessage("§e§l✦ Movement State ✦", false)
    mc:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
    
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
        mc:sendMessage("  " .. state, false)
    end
    
    -- Velocity
    local vel = player:velocity()
    local speed = math.sqrt(vel.x * vel.x + vel.z * vel.z)
    mc:sendMessage("§7Speed: §f" .. string.format("%.2f", speed * 20) .. " §7blocks/sec", false)
    
    -- Game mode (client only)
    if mc:isClientSide() then
        mc:sendMessage("§7Game Mode: §e" .. mc:gameMode(), false)
    end
    
    mc:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
    mc:sendMessage("§e§l✦ Active Effects ✦", false)
    mc:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
    
    -- Status effects
    local effects = player:effects()
    if #effects > 0 then
        for _, effect in ipairs(effects) do
            local cleanEffect = effect:gsub("minecraft:", ""):gsub("_", " ")
            mc:sendMessage("  §d✦ §f" .. cleanEffect, false)
        end
    else
        mc:sendMessage("  §8No active effects", false)
    end
    
    mc:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
end
