-- skin_set.lua
-- Set or clear a player's skin with command-line arguments
--
-- Usage:
--   /serverscript execute litmus:skin_set                              - Clear your skin
--   /serverscript execute litmus:skin_set <skin_username>              - Set skin (wide arms)
--   /serverscript execute litmus:skin_set <skin_username> <slim>       - Set skin with arm style
--   /serverscript execute litmus:skin_set <skin_username> <slim> <target_player>
--
-- Examples:
--   /serverscript execute litmus:skin_set                    - Clear your own skin
--   /serverscript execute litmus:skin_set Notch              - Set your skin to Notch (wide arms)
--   /serverscript execute litmus:skin_set Notch true         - Set your skin to Notch (slim arms)
--   /serverscript execute litmus:skin_set Notch false duzo   - Set duzo's skin to Notch (wide arms)
--
-- Arguments:
--   skin_username - The Minecraft username to copy the skin from (omit to clear skin)
--   slim          - (optional) "true" for slim (Alex) arms, defaults to false (Steve arms)
--   target_player - (optional) Player to apply the skin to; defaults to command executor

function onExecute(mc, args)
    -- Determine target player first
    local targetPlayer = args[3]
    if targetPlayer == nil or targetPlayer == "" then
        local player = mc:player()
        if player == nil then
            mc:sendMessage("§cNo player context and no target player specified!", false)
            return
        end
        targetPlayer = player:name()
    end

    -- No arguments = clear skin
    if args == nil or #args < 1 or args[1] == nil or args[1] == "" then
        if mc:clearSkin(targetPlayer) then
            mc:sendMessage("§aSkin cleared for §f" .. targetPlayer, false)
        else
            mc:sendMessage("§cFailed to clear skin! Player may not exist.", false)
        end
        return
    end

    local skinUsername = args[1]
    local slimArg = args[2]

    -- Parse slim boolean (defaults to false)
    local slim = slimArg == "true" or slimArg == "1" or slimArg == "yes"

    -- Apply the skin
    mc:sendMessage("§7Setting skin for §f" .. targetPlayer .. "§7 to §f" .. skinUsername .. "§7 (slim: §f" .. tostring(slim) .. "§7)...", false)

    if mc:setSkin(targetPlayer, skinUsername) then
        mc:sendMessage("§aSkin applied successfully!", false)
        -- Set the arm model
        mc:setSkinSlim(targetPlayer, slim)
    else
        mc:sendMessage("§cFailed to apply skin! Player may not exist.", false)
    end
end
