# CircuitCraft: Electronics Engineer's Workshop
#
# Builds a small, self-contained lab with a Workbench job site inside, ready for a nearby
# unemployed villager to claim as an Electronics Engineer. Run with:
#
#   /function circuitcraft:engineer_workshop
#
# Stand on the spot you want as the workshop's floor center before running it - the whole
# structure is placed relative to that position (floor one block below your feet, walls rising
# around you). Builds its own solid foundation first, so it's safe to run over uneven terrain,
# water, or ungenerated chunks alike. Deliberately a different material palette from the
# Electrician's Workshop (stone brick/andesite/iron "lab" look instead of oak/copper "cabin"
# look) so the two are visually distinct at a glance, not just functionally.

# Clear the build volume first so trees/terrain can't poke through the walls or roof.
fill ~-5 ~-3 ~-5 ~5 ~6 ~5 minecraft:air

# Solid footing, two blocks thick, regardless of what's underneath. Matches the clear
# volume's full x/z extent (not just the walls' footprint) so the outer ring cleared above
# doesn't get left as an open-air trench with nothing under it.
fill ~-5 ~-3 ~-5 ~5 ~-2 ~5 minecraft:stone

# Floor.
fill ~-3 ~-1 ~-3 ~3 ~-1 ~3 minecraft:polished_andesite

# Walls (stone brick ring, 3 tall).
fill ~-3 ~0 ~-3 ~3 ~2 ~-3 minecraft:stone_bricks
fill ~-3 ~0 ~3 ~3 ~2 ~3 minecraft:stone_bricks
fill ~-3 ~0 ~-3 ~-3 ~2 ~3 minecraft:stone_bricks
fill ~3 ~0 ~-3 ~3 ~2 ~3 minecraft:stone_bricks

# Iron block corner posts, one block taller than the walls (a small parapet detail).
fill ~-3 ~0 ~-3 ~-3 ~3 ~-3 minecraft:iron_block
fill ~-3 ~0 ~3 ~-3 ~3 ~3 minecraft:iron_block
fill ~3 ~0 ~-3 ~3 ~3 ~-3 minecraft:iron_block
fill ~3 ~0 ~3 ~3 ~3 ~3 minecraft:iron_block

# Windows: iron bars instead of glass, a more "lab" than "cabin" look. Two flanking the door
# on the south wall, one on each side wall.
setblock ~-2 ~1 ~3 minecraft:iron_bars
setblock ~2 ~1 ~3 minecraft:iron_bars
setblock ~-3 ~1 ~0 minecraft:iron_bars
setblock ~3 ~1 ~0 minecraft:iron_bars

# Door, centered on the south wall.
setblock ~0 ~0 ~3 minecraft:iron_door[facing=south,half=lower,hinge=left,open=false,powered=false]
setblock ~0 ~1 ~3 minecraft:iron_door[facing=south,half=upper,hinge=left,open=false,powered=false]

# Roof: a single overhanging slab cap.
fill ~-4 ~3 ~-4 ~4 ~3 ~4 minecraft:smooth_stone_slab[type=top]

# A quartz-block finial - a "clean room" accent distinct from the Electrician's lightning rod.
setblock ~0 ~4 ~0 minecraft:quartz_block

# Interior lighting - plain torches, like the Electrician's Workshop, rather than a
# redstone-lamp contraption that would need real wiring to actually light up.
setblock ~-2 ~0 ~-2 minecraft:torch
setblock ~2 ~0 ~-2 minecraft:torch

# The job site itself, against the back (north) wall.
setblock ~0 ~0 ~-2 circuitcraft:workbench

# A small shelf of stock along the west interior wall.
setblock ~-2 ~0 ~1 circuitcraft:diode[facing=east]
setblock ~-2 ~0 ~0 circuitcraft:npn[facing=east]
