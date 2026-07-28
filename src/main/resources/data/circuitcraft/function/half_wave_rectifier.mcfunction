# CircuitCraft: Experiment 4 - Half-Wave Rectifier
#
# A function generator driving a diode and a load resistor in series, with the resistor's far
# lead tied back to the same ground as the generator's back lead. The diode faces toward the
# generator (its anode west, cathode east), so current flows readily generator-to-load on the
# forward half of each cycle and is blocked on the reverse half. See
# latex_mod/sections/07_results_experiments.tex for the full writeup. Run with:
#
#   /function circuitcraft:half_wave_rectifier
#
# Stand where you want the bench's southwest corner before running it. Clears its own space
# and pours its own foundation first.
#
# No right-clicks needed: the function generator's default 5V/1Hz sine, the diode's default
# silicon preset (~0.7V forward drop), and the resistor's default 100 ohm load are exactly the
# values used. Flip the lever. For the clearest comparison, pin the given oscilloscope probe
# to the function generator itself (undistorted input) as one channel and the load resistor
# (rectified output) as a second. Expected: the output follows the input minus ~0.7V on each
# positive half-cycle (peaking around 4.3V) and reads ~0V on each negative half-cycle - ten
# rectified pulses across the oscilloscope's 10s history window at 1Hz.

fill ~-1 ~0 ~-1 ~5 ~3 ~3 minecraft:air
fill ~-1 ~-1 ~-1 ~5 ~-1 ~3 minecraft:stone

# The loop: ground - function generator - diode (anode toward generator) - R - wire, closed
# by a return path back to the same ground.
setblock ~0 ~0 ~0 circuitcraft:ground
setblock ~1 ~0 ~0 circuitcraft:function_generator[facing=east]
setblock ~2 ~0 ~0 circuitcraft:diode[facing=west]
setblock ~3 ~0 ~0 circuitcraft:resistor[facing=east]
setblock ~4 ~0 ~0 circuitcraft:wire

# Return path, closing the loop back to the same ground block.
fill ~0 ~0 ~1 ~0 ~0 ~2 circuitcraft:wire
fill ~4 ~0 ~1 ~4 ~0 ~2 circuitcraft:wire
fill ~0 ~0 ~2 ~4 ~0 ~2 circuitcraft:wire

# Lever on top of the function generator, left off.
setblock ~1 ~1 ~0 minecraft:lever[face=floor,facing=north,powered=false]

give @s circuitcraft:probe 1
