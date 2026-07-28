# CircuitCraft: Experiment 1 - Basic Voltage Divider
#
# The simplest circuit in the mod's worked-experiments set (see
# latex_mod/sections/07_results_experiments.tex for the full writeup): a DC source and two
# resistors in series, tapped at their midpoint, closed into a loop by a wire return path and
# a ground reference. Run with:
#
#   /function circuitcraft:voltage_divider
#
# Stand where you want the bench's southwest corner before running it - everything is placed
# relative to that position (floor one block below your feet). Clears its own space and pours
# its own foundation first, so it's safe to run on uneven terrain.
#
# No right-clicks needed: both resistors are left at their default 100 ohm preset and the
# supply at its default 5V preset. Flip the lever, then pin the given oscilloscope probe to
# the wire block between the two resistors (the divider's midpoint). Expected reading:
# 5V * 100/(100+100) = 2.5V, steady, with no transient (every element here is resistive).
# Follow-up left to the reader: right-click the second resistor twice (100 -> 1000 ->
# 10,000 ohm) and confirm the tap voltage rises toward 5 * 10000/10100 ~= 4.95V.

fill ~-1 ~0 ~-1 ~6 ~3 ~3 minecraft:air
fill ~-1 ~-1 ~-1 ~6 ~-1 ~3 minecraft:stone

# The loop: ground - power supply - R1 - wire tap - R2 - wire, closed by a return path.
setblock ~0 ~0 ~0 circuitcraft:ground
setblock ~1 ~0 ~0 circuitcraft:power_supply[facing=east]
setblock ~2 ~0 ~0 circuitcraft:resistor[facing=east]
setblock ~3 ~0 ~0 circuitcraft:wire
setblock ~4 ~0 ~0 circuitcraft:resistor[facing=east]
setblock ~5 ~0 ~0 circuitcraft:wire

# Return path, closing the loop back to the same ground block.
fill ~0 ~0 ~1 ~0 ~0 ~2 circuitcraft:wire
fill ~5 ~0 ~1 ~5 ~0 ~2 circuitcraft:wire
fill ~0 ~0 ~2 ~5 ~0 ~2 circuitcraft:wire

# Lever on top of the power supply, left off - marks a clean t=0 once flipped.
setblock ~1 ~1 ~0 minecraft:lever[face=floor,facing=north,powered=false]

give @s circuitcraft:probe 1
