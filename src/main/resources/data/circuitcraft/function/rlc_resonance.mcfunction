# CircuitCraft: Experiment 3 - RLC Resonance Bode Plot
#
# The same idea as the RC low-pass bench, but with an inductor inserted between the resistor
# and capacitor, turning a first-order rolloff into a second-order response capable of
# resonant peaking. See latex_mod/sections/07_results_experiments.tex for the full writeup.
# Run with:
#
#   /function circuitcraft:rlc_resonance
#
# Stand where you want the bench's southwest corner before running it. Clears its own space
# and pours its own foundation first.
#
# Right-click the resistor three times, cycling 100 -> 1000 -> 10,000 -> 10 ohm (the fourth
# and smallest preset, reached by wrapping back around past the top of the cycle). Right-click
# the inductor twice (0.1 -> 1 -> 5H) and the capacitor twice (10 -> 100 -> 1000uF) - both to
# their largest preset. Shift-right-click the AC Source and set its frequency range to
# 0.2Hz-20Hz. Flip the lever, right-click the given AC probe against the AC Source to pin it,
# then right-click the capacitor to run the sweep. Expected: a resonant peak around 2.24Hz at
# roughly +17.0dB above the passband, then a fall-off at the second-order asymptotic rate of
# -40dB/decade, ~-38dB below the peak by the sweep's 20Hz upper bound.

fill ~-1 ~0 ~-1 ~6 ~3 ~3 minecraft:air
fill ~-1 ~-1 ~-1 ~6 ~-1 ~3 minecraft:stone

# The loop: ground - AC Source - R - L - C - wire, closed by a return path to the same ground.
setblock ~0 ~0 ~0 circuitcraft:ground
setblock ~1 ~0 ~0 circuitcraft:ac_source[facing=east]
setblock ~2 ~0 ~0 circuitcraft:resistor[facing=east]
setblock ~3 ~0 ~0 circuitcraft:inductor[facing=east]
setblock ~4 ~0 ~0 circuitcraft:capacitor[facing=east]
setblock ~5 ~0 ~0 circuitcraft:wire

# Return path, closing the loop back to the same ground block.
fill ~0 ~0 ~1 ~0 ~0 ~2 circuitcraft:wire
fill ~5 ~0 ~1 ~5 ~0 ~2 circuitcraft:wire
fill ~0 ~0 ~2 ~5 ~0 ~2 circuitcraft:wire

# Lever on top of the AC Source, left off.
setblock ~1 ~1 ~0 minecraft:lever[face=floor,facing=north,powered=false]

give @s circuitcraft:ac_probe 1
