# CircuitCraft: Experiment 5 - Memristor Pinched Hysteresis Loop
#
# A function generator driving a memristor in series with an ammeter, closed by a return path
# back to the same ground as the generator's back lead, with a frequency module against the
# generator's north face slowing its default 1Hz down to 0.5Hz. Traced on the X-Y oscilloscope
# probe: memristor voltage against ammeter current. See
# latex_mod/sections/07_results_experiments.tex for the full writeup. Run with:
#
#   /function circuitcraft:memristor_hysteresis
#
# Stand where you want the bench's southwest corner before running it. Clears its own space
# and pours its own foundation first.
#
# No right-clicks needed: the frequency module's default 0.5Hz, the generator's default 5V
# sine, and the memristor's default Ron=100 ohm/Roff=10,000 ohm/qmax=1e-3 C preset are exactly
# the values used. Flip the lever. Pin the given X-Y probe to the memristor FIRST, then the
# ammeter SECOND - the probe always assigns the most recently pinned position to the vertical
# (Y) axis, so this order puts memristor voltage on X and ammeter current on Y. Expected: a
# pinched loop crossing through the origin on every cycle, widest where the drive voltage is
# largest and narrowing toward the origin - the defining memristor signature, as opposed to an
# ordinary resistor's straight line.

fill ~-1 ~0 ~-2 ~5 ~3 ~3 minecraft:air
fill ~-1 ~-1 ~-2 ~5 ~-1 ~3 minecraft:stone

# The loop: ground - function generator - memristor - ammeter - wire, closed by a return path
# back to the same ground.
setblock ~0 ~0 ~0 circuitcraft:ground
setblock ~1 ~0 ~0 circuitcraft:function_generator[facing=east]
setblock ~2 ~0 ~0 circuitcraft:memristor[facing=east]
setblock ~3 ~0 ~0 circuitcraft:ammeter[facing=east]
setblock ~4 ~0 ~0 circuitcraft:wire

# Return path, closing the loop back to the same ground block.
fill ~0 ~0 ~1 ~0 ~0 ~2 circuitcraft:wire
fill ~4 ~0 ~1 ~4 ~0 ~2 circuitcraft:wire
fill ~0 ~0 ~2 ~4 ~0 ~2 circuitcraft:wire

# Frequency module against the generator's north face - an undirected utility cube, no facing.
setblock ~1 ~0 ~-1 circuitcraft:frequency_module

# Lever on top of the function generator, left off.
setblock ~1 ~1 ~0 minecraft:lever[face=floor,facing=north,powered=false]

give @s circuitcraft:xy_probe 1
