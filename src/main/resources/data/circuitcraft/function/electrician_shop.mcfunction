# CircuitCraft: Electrician's Workshop
#
# Builds a small, self-contained shop with a Breadboard job site inside, ready for a nearby
# unemployed villager to claim as an Electrician. Run with:
#
#   /function circuitcraft:electrician_shop
#
# Stand on the spot you want as the workshop's floor center before running it - the whole
# structure is placed relative to that position (floor one block below your feet, walls rising
# around you). Builds its own solid foundation first, so it's safe to run over uneven terrain,
# water, or ungenerated chunks alike.

# Clear the build volume first so trees/terrain can't poke through the walls or roof.
fill ~-5 ~-3 ~-5 ~5 ~6 ~5 minecraft:air

# Solid footing, two blocks thick, regardless of what's underneath. Matches the clear
# volume's full x/z extent (not just the walls' footprint) so the outer ring cleared above
# doesn't get left as an open-air trench with nothing under it.
fill ~-5 ~-3 ~-5 ~5 ~-2 ~5 minecraft:stone

# Floor.
fill ~-3 ~-1 ~-3 ~3 ~-1 ~3 minecraft:stone_bricks

# Walls (oak plank ring, 3 tall).
fill ~-3 ~0 ~-3 ~3 ~2 ~-3 minecraft:oak_planks
fill ~-3 ~0 ~3 ~3 ~2 ~3 minecraft:oak_planks
fill ~-3 ~0 ~-3 ~-3 ~2 ~3 minecraft:oak_planks
fill ~3 ~0 ~-3 ~3 ~2 ~3 minecraft:oak_planks

# Copper corner posts, one block taller than the walls (a small parapet detail).
fill ~-3 ~0 ~-3 ~-3 ~3 ~-3 minecraft:cut_copper
fill ~-3 ~0 ~3 ~-3 ~3 ~3 minecraft:cut_copper
fill ~3 ~0 ~-3 ~3 ~3 ~-3 minecraft:cut_copper
fill ~3 ~0 ~3 ~3 ~3 ~3 minecraft:cut_copper

# Windows: two flanking the door on the south wall, one on each side wall.
setblock ~-2 ~1 ~3 minecraft:glass_pane
setblock ~2 ~1 ~3 minecraft:glass_pane
setblock ~-3 ~1 ~0 minecraft:glass_pane
setblock ~3 ~1 ~0 minecraft:glass_pane

# Door, centered on the south wall.
setblock ~0 ~0 ~3 minecraft:oak_door[facing=south,half=lower,hinge=left,open=false,powered=false]
setblock ~0 ~1 ~3 minecraft:oak_door[facing=south,half=upper,hinge=left,open=false,powered=false]

# Roof: a single overhanging slab cap.
fill ~-4 ~3 ~-4 ~4 ~3 ~4 minecraft:dark_oak_slab[type=top]

# Lightning rod finial - a real one, and the most literal "electrician" symbol available.
setblock ~0 ~4 ~0 minecraft:lightning_rod[facing=up,powered=false]

# Interior lighting.
setblock ~-2 ~0 ~-2 minecraft:torch
setblock ~2 ~0 ~-2 minecraft:torch

# The job site itself, against the back (north) wall.
setblock ~0 ~0 ~-2 circuitcraft:breadboard

# A small shelf of stock along the west interior wall.
setblock ~-2 ~0 ~-1 circuitcraft:wire
setblock ~-2 ~0 ~0 circuitcraft:resistor[facing=east]
setblock ~-2 ~0 ~1 circuitcraft:capacitor[facing=east]
