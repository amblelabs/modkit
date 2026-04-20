-- Skin Disguise Script: Disguise yourself as famous Minecraft players
-- Run with: /serverscript execute litmus:skin_disguise
--
-- This script demonstrates the basic skin API methods.

-- List of famous/notable Minecraft usernames
local DISGUISES = {
    "Notch",
    "jeb_",
    "Dinnerbone",
    "Herobrine",
    "Dream",
    "Technoblade",
    "Ph1LzA",
    "TommyInnit"
}

function onExecute(mc, args)
    local player = mc:player()
    if player == nil then
        mc:log("No player context - run this as a player!")
        return
    end
    
    local playerName = player:name()
    
    -- Check if already disguised
    if mc:hasSkin(playerName) then
        -- Clear the disguise
        if mc:clearSkin(playerName) then
            mc:sendMessage("§7Disguise removed! You look like yourself again.", false)
            mc:log("Player " .. playerName .. " removed their disguise")
        else
            mc:sendMessage("§cFailed to remove disguise!", false)
        end
    else
        -- Pick a random disguise
        math.randomseed(os.time())
        local disguise = DISGUISES[math.random(#DISGUISES)]
        
        if mc:setSkin(playerName, disguise) then
            mc:sendMessage("§aYou are now disguised as §e" .. disguise .. "§a!", false)
            mc:log("Player " .. playerName .. " disguised as " .. disguise)
        else
            mc:sendMessage("§cFailed to apply disguise!", false)
        end
    end
end
