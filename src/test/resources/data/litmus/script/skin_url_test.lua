-- Skin URL Test Script: Test applying skins from URLs
-- Run with: /serverscript execute litmus:skin_url_test
--
-- This script tests URL-based skin application and slim arm toggling.

-- Example skin URLs (these are placeholder URLs - replace with real ones)
local TEST_SKINS = {
    {
        name = "Classic Steve",
        url = "https://assets.mojang.com/SkinTemplates/steve.png",
        slim = false
    },
    {
        name = "Classic Alex",
        url = "https://assets.mojang.com/SkinTemplates/alex.png",
        slim = true
    }
}

-- Current test index per player
local playerTestIndex = {}

function onExecute(mc, args)
    local player = mc:player()
    if player == nil then
        mc:log("No player context!")
        return
    end
    
    local playerName = player:name()
    local uuid = player:uuid()
    
    -- Get or initialize test index for this player
    if not playerTestIndex[uuid] then
        playerTestIndex[uuid] = 0
    end
    
    -- Cycle through tests
    playerTestIndex[uuid] = playerTestIndex[uuid] + 1
    local testNum = playerTestIndex[uuid]
    
    mc:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
    mc:sendMessage("§e§l✦ Skin URL Test #" .. testNum .. " ✦", false)
    mc:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
    
    if testNum == 1 then
        -- Test: Set skin by username
        mc:sendMessage("§7Test: Setting skin by username (Notch)...", false)
        if mc:setSkin(playerName, "Notch") then
            mc:sendMessage("§a✓ Success! Skin set to Notch", false)
        else
            mc:sendMessage("§c✗ Failed to set skin by username", false)
        end
        
    elseif testNum == 2 then
        -- Test: Check hasSkin
        mc:sendMessage("§7Test: Checking hasSkin()...", false)
        local hasSkin = mc:hasSkin(playerName)
        local hasSkinUuid = mc:hasSkinByUuid(uuid)
        mc:sendMessage("§7hasSkin(name): " .. (hasSkin and "§atrue" or "§cfalse"), false)
        mc:sendMessage("§7hasSkinByUuid(uuid): " .. (hasSkinUuid and "§atrue" or "§cfalse"), false)
        
    elseif testNum == 3 then
        -- Test: Toggle slim arms
        mc:sendMessage("§7Test: Toggling slim arms (true)...", false)
        if mc:hasSkin(playerName) then
            if mc:setSkinSlim(playerName, true) then
                mc:sendMessage("§a✓ Success! Slim arms enabled", false)
            else
                mc:sendMessage("§c✗ Failed to set slim arms", false)
            end
        else
            mc:sendMessage("§c✗ No custom skin to modify!", false)
        end
        
    elseif testNum == 4 then
        -- Test: Toggle slim arms back
        mc:sendMessage("§7Test: Toggling slim arms (false)...", false)
        if mc:hasSkin(playerName) then
            if mc:setSkinSlim(playerName, false) then
                mc:sendMessage("§a✓ Success! Wide arms enabled", false)
            else
                mc:sendMessage("§c✗ Failed to set wide arms", false)
            end
        else
            mc:sendMessage("§c✗ No custom skin to modify!", false)
        end
        
    elseif testNum == 5 then
        -- Test: Set by UUID
        mc:sendMessage("§7Test: Setting skin by UUID...", false)
        mc:sendMessage("§8UUID: " .. uuid, false)
        if mc:setSkinByUuid(uuid, "jeb_") then
            mc:sendMessage("§a✓ Success! Skin set to jeb_ via UUID", false)
        else
            mc:sendMessage("§c✗ Failed to set skin by UUID", false)
        end
        
    elseif testNum == 6 then
        -- Test: Clear skin
        mc:sendMessage("§7Test: Clearing skin...", false)
        if mc:clearSkin(playerName) then
            mc:sendMessage("§a✓ Success! Skin cleared", false)
        else
            mc:sendMessage("§c✗ Failed to clear skin", false)
        end
        
        -- Verify it's cleared
        if not mc:hasSkin(playerName) then
            mc:sendMessage("§a✓ Verified: hasSkin() returns false", false)
        else
            mc:sendMessage("§c✗ Warning: hasSkin() still returns true!", false)
        end
        
        -- Reset test counter
        playerTestIndex[uuid] = 0
        mc:sendMessage("§7Tests complete! Run again to restart.", false)
    end
    
    mc:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
    mc:sendMessage("§7Run again for next test (" .. (testNum % 6 + 1) .. "/6)", false)
end
