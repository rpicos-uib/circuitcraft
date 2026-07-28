# CircuitCraft: Experiment 6 - Op-Amp Open-Loop Bode Plot
#
# The only bench using the ideal op-amp, run open-loop (no feedback network at all) so the
# entire point is to observe the op-amp's own raw two-pole frequency response directly. A
# ground block, then an AC Source (facing east) with its back lead against that ground; two
# wire blocks continuing east from the source's front lead, the second of which turns a corner
# upward and then east again to arrive directly above the op-amp, which sits facing NORTH (not
# east) so its non-inverting input - always the top face for a horizontally-facing op-amp -
# lands exactly where that overhead wire run terminates. The op-amp's output (north face) is
# left completely unconnected, probed directly rather than routed anywhere; its inverting
# input (south face, opposite its facing) is tied to a second, separate ground block placed
# immediately south of it. See latex_mod/sections/07_results_experiments.tex for the full
# writeup. Run with:
#
#   /function circuitcraft:opamp_bode
#
# Stand where you want the bench's southwest corner before running it. Clears its own space
# and pours its own foundation first.
#
# No right-clicks needed: the AC Source's default 1V amplitude is used unchanged.
# Shift-right-click it and set its frequency range to 1Hz-10MHz. Flip the lever, right-click
# the given AC probe against the AC Source to pin it, then right-click the op-amp block itself
# (probing a three-terminal component directly reads its output node's voltage) to run the
# sweep. Expected: close to the full open-loop DC gain, 20*log10(1e5) = 100dB, at 1Hz with
# negligible phase shift; exactly 97dB at the first pole (fp1 = 20Hz, the defining -3dB-from-DC
# property of a pole); a -20dB/decade rolloff until approaching the second pole
# (fp2 = 3MHz), where it steepens toward -40dB/decade with the phase continuing toward -180deg
# in total.

fill ~-1 ~0 ~-1 ~5 ~3 ~2 minecraft:air
fill ~-1 ~-1 ~-1 ~5 ~-1 ~2 minecraft:stone

# Ground - AC Source (back lead against it) - two wires east - corner up - one wire east again,
# arriving directly above the op-amp's position.
setblock ~0 ~0 ~0 circuitcraft:ground
setblock ~1 ~0 ~0 circuitcraft:ac_source[facing=east]
setblock ~2 ~0 ~0 circuitcraft:wire
setblock ~3 ~0 ~0 circuitcraft:wire
setblock ~3 ~1 ~0 circuitcraft:wire
setblock ~4 ~1 ~0 circuitcraft:wire

# The op-amp itself: facing north, so its up face (non-inverting input) meets the overhead
# wire above, its north face (output) is left unconnected, and its south face (inverting
# input) meets the second ground block placed immediately south of it.
setblock ~4 ~0 ~0 circuitcraft:op_amp[facing=north]
setblock ~4 ~0 ~1 circuitcraft:ground

# Lever on top of the AC Source, left off.
setblock ~1 ~1 ~0 minecraft:lever[face=floor,facing=north,powered=false]

give @s circuitcraft:ac_probe 1
