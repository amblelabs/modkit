-- Test Script: Basic test script for GUI interactions
-- This script is designed for GUI element click handlers
--
-- Note: GUI scripts receive 'self' (LuaElement) and use self:minecraft() for data

function onClick(self, mouseX, mouseY, button)
    local text = self:getText()
    if (text == nil) then
        --- find first child which is not nil
        for i = 0, self:childCount() - 1 do
            local child = self:child(i)
            local childText = child:getText()
            if (childText ~= nil) then
                --- set to player username
                child:setText("Hello " .. self:minecraft():username() .. "!")
                break
            end
        end
    end

    -- search the inventory for an apple and select it if its in the hotbar
    local inventory = self:minecraft():player():inventory() -- a table of ItemStacks
    for slotIndex, itemStack in pairs(inventory) do
        if (itemStack ~= nil and itemStack:id() == "minecraft:apple") then
            -- drop apple
            self:minecraft():dropStack(slotIndex, true)
            break
        end
    end
    
    -- print all entities
    local entities = self:minecraft():entities()
    print(entities)
    for _, entity in pairs(entities) do
        print(entity)
        print("Entity: " .. entity:type() .. " at " .. entity:position():toString())
    end
end

function onExecute(mc)
    mc:log("Test script executed via command!")
    mc:sendMessage("§aTest script executed!", false)
end
