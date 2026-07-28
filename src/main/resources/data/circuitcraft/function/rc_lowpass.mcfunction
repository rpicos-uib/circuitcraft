# CircuitCraft: Experiment 2 - RC Low-Pass Bode Plot
#
# An AC Source, a resistor, and a capacitor in series, with the capacitor's far lead tied
# back to the same ground as the source's back lead, driven and probed as a frequency sweep.
# See latex_mod/sections/07_results_experiments.tex for the full writeup. Run with:
#
#   /function circuitcraft:rc_lowpass
#
# Stand where you want the bench's southwest corner before running it. Clears its own space
# and pours its own foundation first.
#
# Right-click the resistor twice (100 -> 1000 -> 10,000 ohm); right-click the capacitor once
# (10 -> 100 uF). This gives tau = RC = 10000 * 100e-6 = 1s, a cutoff frequency
# fc = 1/(2*pi*tau) ~= 0.159Hz. Shift-right-click the AC Source and set its frequency range to
# 0.1Hz-100Hz (its 1V default amplitude is fine - a Bode plot's magnitude/phase don't depend
# on drive amplitude). Flip the lever, right-click the given AC probe against the AC Source to
# pin it, then right-click the capacitor to run the sweep. Expected: close to flat (~-1.4dB)
# at 0.1Hz, an exact -3dB/-45deg at fc ~= 0.159Hz, and a steep rolloff to ~-56dB by 100Hz
# (~20dB/decade past cutoff, the standard first-order asymptotic rate).

fill ~-1 ~0 ~-1 ~5 ~3 ~3 minecraft:air
fill ~-1 ~-1 ~-1 ~5 ~-1 ~3 minecraft:stone

# The loop: ground - AC Source - R - C - wire, closed by a return path back to the same ground.
setblock ~0 ~0 ~0 circuitcraft:ground
setblock ~1 ~0 ~0 circuitcraft:ac_source[facing=east]
setblock ~2 ~0 ~0 circuitcraft:resistor[facing=east]
setblock ~3 ~0 ~0 circuitcraft:capacitor[facing=east]
setblock ~4 ~0 ~0 circuitcraft:wire

# Return path, closing the loop back to the same ground block.
fill ~0 ~0 ~1 ~0 ~0 ~2 circuitcraft:wire
fill ~4 ~0 ~1 ~4 ~0 ~2 circuitcraft:wire
fill ~0 ~0 ~2 ~4 ~0 ~2 circuitcraft:wire

# Lever on top of the AC Source, left off - gates whether the sweep shows anything at all.
setblock ~1 ~1 ~0 minecraft:lever[face=floor,facing=north,powered=false]

give @s circuitcraft:ac_probe 1
