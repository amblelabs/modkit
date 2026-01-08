-- Hotbar Cycle Script: Cycles through hotbar slots with a fun animation
-- Run with: /amblekit execute litmus:hotbar_cycle

local cycleIndex = 1
local cycleDirection = 1

function onExecute()
	local currentSlot = minecraft:selectedSlot()
	
	-- Calculate next slot (1-9)
	local nextSlot = currentSlot + cycleDirection
	
	if nextSlot > 9 then
		nextSlot = 1
		cycleIndex = cycleIndex + 1
	elseif nextSlot < 1 then
		nextSlot = 9
		cycleIndex = cycleIndex + 1
	end
	
	-- Select the next slot
	minecraft:selectSlot(nextSlot)
	
	-- Create a visual indicator
	local indicator = ""
	for i = 1, 9 do
		if i == nextSlot then
			indicator = indicator .. "§e[" .. i .. "]"
		else
			indicator = indicator .. "§7 " .. i .. " "
		end
	end
	
	minecraft:sendMessage("§6Hotbar: " .. indicator, true)
	
	-- Change direction every full cycle
	if cycleIndex > 2 then
		cycleDirection = -cycleDirection
		cycleIndex = 1
		minecraft:sendMessage("§d✦ Direction reversed! ✦", true)
	end
end
