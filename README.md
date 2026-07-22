# CircuitCraft

A Fabric mod that turns Minecraft into an analog electronics lab. Place resistors, capacitors,
inductors and memristors as blocks, wire them up, drive them with power supplies and function
generators, and probe the live voltage/current with a handheld oscilloscope — all backed by a
real modified-nodal-analysis (MNA) circuit solver, the same family of algorithm SPICE uses, not a
scripted approximation.

This is a teaching tool: the goal is for the in-game behavior to actually match what you'd see on
a real bench (RC charge curves, RL transients, a memristor's resistance drifting with accumulated
charge), just at Minecraft-tick timescales instead of real-world ones.

## Screenshots

The oscilloscope pinning three channels at once — a Function Generator, a Capacitor, and a
Resistor, each with their own color-coded trace and live V/I readout:

| Square wave | Triangle wave |
|---|---|
| <img src="docs/screenshots/square_waveform.png"> | <img src="docs/screenshots/triangle_waveform.png"> |

## Requirements

- **Minecraft 26.2** — note this is Mojang's new `year.release` versioning, *not* the old `1.21.x`
  line. The mod targets this specific version because it's what "latest" means as of build time.
- **Fabric Loader** >= 0.19.3
- **Fabric API** 0.155.2+26.2 (must match the Minecraft version)
- **Java 25** — this is a Minecraft 26.2 requirement in general (it ships unobfuscated and needs a
  current JDK), not something specific to this mod.

## Installation

### Option A — prebuilt jar

1. Install [Fabric Loader](https://fabricmc.net/use/installer/) for Minecraft 26.2.
2. Download the **Fabric API** jar for 26.2 from [Modrinth](https://modrinth.com/mod/fabric-api) or
   [CurseForge](https://www.curseforge.com/minecraft/mc-mods/fabric-api) and drop it in your
   `mods` folder.
3. Grab `circuitcraft-<version>.jar` from this repo's [Releases](../../releases) page and drop
   it in the same `mods` folder.
4. Make sure the profile you launch with is on Java 25+ (recent launchers that auto-manage a JVM
   per Minecraft version will already do this once you select 26.2).
5. Launch with the Fabric profile.

### Option B — build from source

```bash
git clone https://github.com/rpicos-uib/circuitcraft.git
cd mine-memristors
./gradlew build
```

The mod jar comes out at `build/libs/circuitcraft-<version>.jar`. You need a JDK 25 available;
either make it your default `java`, or point Gradle at it explicitly:

```bash
JAVA_HOME=/path/to/jdk-25 ./gradlew build
```

Useful dev tasks: `./gradlew runClient` (launches a dev client with the mod loaded) and
`./gradlew runServer` (headless dedicated server — handy for checking the mod loads without a GUI).

## The components

For any component whose value is a single number rather than a category (resistance,
capacitance, inductance, voltage, frequency, the memristor's parameters), an empty-hand
**shift+right-click** opens a text-entry screen — the same interaction spirit as editing a
sign — pre-filled with the current value(s) and the same min/max range its preset cycle
already covers. A plain empty-hand right-click still cycles through the fixed presets exactly
as before; the two interactions coexist.

| | Block/Item | What it is |
|---|---|---|
| <img src="docs/icons/resistor.png" width="32"> | **Resistor** | Ohmic resistor. Right-click (empty hand) to cycle 10 / 100 / 1,000 / 10,000 Ω. |
| <img src="docs/icons/capacitor.png" width="32"> | **Capacitor** | Ideal capacitor, trapezoidal-integration model. Cycles 1 / 10 / 100 / 1,000 µF. |
| <img src="docs/icons/inductor.png" width="32"> | **Inductor** | Ideal inductor, trapezoidal-integration model. Cycles 0.01 / 0.1 / 1 / 5 H (scaled for Minecraft's tick rate, not real-world component ratings). |
| <img src="docs/icons/memristor.png" width="32"> | **Memristor** | Charge-controlled linear-drift (HP) memristor model. Resistance drifts between $R_{on}$ and $R_{off}$ based on accumulated charge through it — the "memory" persists even when the circuit is rebuilt. Empty-hand right-click cycles a "switching speed" ($q_{max}$) preset as before (defaults: $R_{on}=100\,\Omega$, $R_{off}=10{,}000\,\Omega$); shift+right-click opens the value editor for all three parameters at once — $R_{on}$ (10–1,000 Ω), $R_{off}$ (1,000–100,000 Ω), and $q_{max}$ (1e-4–1e-2 C). |
| <img src="docs/icons/power_supply.png" width="32"> | **Power Supply** | Ideal DC voltage source. Cycles 1.5 / 5 / 9 / 12 / 24 V. Inactive (open-circuit) until it receives a redstone signal - wire up the circuit first, then power it on. |
| <img src="docs/icons/function_generator.png" width="32"> | **Function Generator** | Time-varying voltage source. Cycles sine/square/triangle presets (waveform shape only - see Voltage/Frequency Module below for amplitude and frequency). Same redstone-activation behavior as the Power Supply. Defaults to 5V/1Hz with no modules attached. |
| <img src="docs/icons/wire.png" width="32"> | **Wire** | Zero-resistance conductor block. Connects on all six faces. Probeable in its own right (see Probe below) - gives the absolute voltage at that point in the circuit, not just a drop across two leads. |
| <img src="docs/icons/ground.png" width="32"> | **Ground** | Ties whatever network it's wired into to a real 0V reference point, the same way a real circuit needs a ground reference before "voltage at this node" means anything. Conductive on all six faces, wire it in like any other participant. Probing it always reads exactly 0V, confirming what's actually tied to reference. |
| <img src="docs/icons/ammeter.png" width="32"> | **Ammeter** | A 0V voltage source in series - electrically an ideal wire, so it doesn't disturb the circuit, but gives an exact current reading. Pin it with the Probe to see a live current trace on the oscilloscope, the same way you'd watch a voltage. |
| <img src="docs/icons/diode.png" width="32"> | **Diode** | The lead facing the direction you were looking when you placed it is the anode, the opposite lead is the cathode - current flows readily anode→cathode past the forward voltage, and is (almost) blocked in reverse. Right-click cycles silicon (~0.7V) / germanium (~0.3V) / red LED (~2V) presets. Modeled with a linearized Shockley diode equation re-fit every tick, not a lookup table. |
| <img src="docs/icons/op_amp.png" width="32"> | **Ideal Op-Amp** | Infinite gain, infinite input impedance, zero output impedance - the textbook ideal op-amp, enforcing a "virtual short" between its two inputs via its own dedicated branch-current unknown in the solver (the same MNA trick used for voltage sources, just referencing different nodes for the constraint than for the current injection). The only 3-terminal component: output and the inverting input (V−) are the front/back leads as usual; the non-inverting input (V+) is the block's top face (or its north face, if the block itself is oriented vertically). No adjustable preset - an ideal op-amp has no parameters to tune. |
| <img src="docs/icons/voltage_module.png" width="32"> | **Voltage Module** | Undirected utility cube (no facing, no leads) that touches a Function Generator on any face and sets its amplitude. Right-click cycles its own preset (1.5/5/9/12/24 V). Same-kind modules touching each other relay one shared value along the whole chain - whichever module was right-clicked most recently wins and propagates to every generator the chain reaches, so one control can drive several generators at once. |
| <img src="docs/icons/frequency_module.png" width="32"> | **Frequency Module** | Same idea as the Voltage Module, but sets a Function Generator's frequency (0.5/1/2/5/10 Hz presets). A mixed chain of Voltage and Frequency modules relays both values through, regardless of which order they're arranged in. |
| <img src="docs/icons/probe.png" width="32"> | **Probe** | Right-click a component, Wire, or Ground to pin it as one of up to 3 channels shown simultaneously on the oscilloscope HUD (pinning a 4th evicts the oldest); shift+right-click unpins it. Hold the probe in either hand to see the HUD - each pinned channel gets its own scrolling trace, color-coded, stacked in the corner. A component shows the voltage drop across its two leads; a Wire or Ground shows the absolute voltage at that single point. Each channel's trace is auto-scaled to its own history, with the full-scale value printed at the top and bottom of its graph in SI-prefixed form (k/m/u/n/p, whichever keeps the number compact). Once all 3 slots are full, the oldest channel is outlined in yellow and labeled "(next)" so you can see which one a new pin would evict before you commit to it. |
| <img src="docs/icons/xy_probe.png" width="32"> | **X-Y Oscilloscope Probe** | A second, independent probe: instead of plotting channels against time, it plots one pinned channel's voltage against another's - a real bench oscilloscope's X-Y mode, tracing Lissajous figures for phase/frequency comparisons. Right-click to pin - whichever block you just clicked always becomes (or stays) the **Y** channel, demoting the previous Y to X and evicting the old X if both slots were already full; shift+right-click unpins. Each axis is scaled independently to its own channel's peak magnitude, with the full-scale value printed at both ends of each axis directly on the plot in SI-prefixed form (k/m/u/n/p), so two very differently sized signals both use the plot's full range instead of one being squashed by a shared scale - a 90°-phase-shifted, equal-amplitude pair still traces an actual circle, since the two independent scales coincide whenever the amplitudes actually match. Independent of the regular Probe's own pins - hold both at once to see both HUDs side by side. |

### Wiring rules

Every component occupies one block, oriented by the direction you were looking when you placed
it. Its two electrical leads are its front and back faces (along that facing axis) — the other
four faces are insulated. Wire blocks conduct on all six faces. Two positions merge into the same
electrical node whenever each one presents a conductive face toward the other — so components
"connect" either through wire, or by placing two components lead-to-lead directly against each
other, no wire needed in between.

An unconnected lead doesn't crash anything — it's treated as a floating node, so you can build a
circuit incrementally and it'll simulate (uselessly, but safely) at every intermediate stage.

## Crafting recipes

All vanilla ingredients, no dependency on any other mod. Shapeless recipes are shown as a loose set
of ingredients; shaped ones show the actual 3×3 grid layout.

| Recipe | Notes |
|---|---|
| <img src="docs/recipes/resistor.png"> | **Resistor ×2** — clay ball as the resistive body, iron nuggets as leads. |
| <img src="docs/recipes/capacitor.png"> | **Capacitor ×2** — paper as the historical capacitor dielectric. |
| <img src="docs/recipes/inductor.png"> | **Inductor ×2** — copper for the coil. |
| <img src="docs/recipes/memristor.png"> | **Memristor ×1** — amethyst shard for the switching medium. |
| <img src="docs/recipes/power_supply.png"> | **Power Supply ×1** — 3×3 iron/copper shell around a redstone block core. |
| <img src="docs/recipes/function_generator.png"> | **Function Generator ×1** — 3×3 iron/copper shell, quartz + redstone torch core. |
| <img src="docs/recipes/wire.png"> | **Wire ×6** — one copper ingot, cheap/bulk. |
| <img src="docs/recipes/ground.png"> | **Ground ×4** — iron nugget + copper ingot. |
| <img src="docs/recipes/ammeter.png"> | **Ammeter ×2** — iron nugget, copper ingot, redstone. |
| <img src="docs/recipes/voltage_module.png"> | **Voltage Module ×1** — gold nugget, redstone, iron nugget. |
| <img src="docs/recipes/frequency_module.png"> | **Frequency Module ×1** — amethyst shard, redstone, iron nugget. |
| <img src="docs/recipes/diode.png"> | **Diode ×2** — iron nugget, redstone, quartz. |
| <img src="docs/recipes/op_amp.png"> | **Ideal Op-Amp ×1** — 2 gold nuggets, redstone, quartz. |
| <img src="docs/recipes/probe.png"> | **Probe ×1** — redstone, iron nugget, and a stick, stacked vertically. |
| <img src="docs/recipes/xy_probe.png"> | **X-Y Oscilloscope Probe ×1** — redstone, quartz, and a stick, stacked vertically. |

None of these have recipe-book unlock advancements yet, so they won't show a "new recipe" toast —
but they're fully craftable by hand right now. See [Contributing](#contributing) if you want to add
those.

All items are also available in their own **CircuitCraft** creative-inventory tab.

## Architecture, for anyone extending this

```
src/main/java/com/rpicos/circuitcraft/
  sim/            Pure-Java circuit solver — zero Minecraft dependency, unit-testable standalone
  block/          Block classes (placement, orientation, right-click interactions)
  blockentity/    BlockEntity classes (the actual sim state + circuit wiring per component)
  network/        World-side wire connectivity graph + the client<->server probe protocol
  item/           The probe item
src/client/java/com/rpicos/circuitcraft/client/   HUD rendering, client-side networking
```

### The solver (`sim` package)

`Circuit` is a general modified-nodal-analysis engine: node 0 is always ground, every other node
is an integer you allocate with `addNode()`. `Element` implementations (`Resistor`, `Capacitor`,
`Inductor`, `Memristor`) stamp themselves into the conductance matrix each step; `VoltageSource`
gets its own branch-current unknown, the standard MNA treatment for ideal sources. Reactive
elements use trapezoidal-integration companion models (the same technique SPICE uses) rather than
backward Euler, so LC-type behavior doesn't get artificially damped out.

Because this package has no Minecraft imports at all, you can write and run plain-Java tests
against it directly with `javac`/`java` — no Gradle, no decompiling Minecraft, fast iteration.
That's how the solver was validated during development: against closed-form RC/RL step responses
and the memristor's analytic charge-controlled ODE.

### Adding a new component type

1. Add the physics to `sim/` if it's a new kind of element (skip this if it's just a different
   preset of an existing one).
2. Create a `blockentity/YourComponentBlockEntity.java` extending `ComponentBlockEntity`,
   implementing `addToCircuit`, `probeCurrent`, `probeSummary`, and `cyclePreset`.
3. Create a `block/YourComponentBlock.java` extending `ComponentBlock` (copy an existing one, e.g.
   `ResistorBlock.java` — it's a ~25-line template).
4. Register it in `ModBlocks`, `ModBlockEntities`, `ModItems`, and add it to `ModCreativeTab`.
5. Add a texture (`textures/block/your_component.png`, 16×16 — front/back automatically get the
   shared `terminal.png` lead texture, so you only need the body texture for the other four
   faces), a `blockstates/your_component.json` and `models/block/your_component.json` (copy an
   existing pair — they're generic besides the texture path), and a lang entry.
6. Optionally add a `data/circuitcraft/recipe/your_component.json`.

### Known limitations (v0.2)

- **Component state resets on circuit rebuild.** `CircuitNetworkManager` rebuilds the whole
  `Circuit` from scratch whenever wiring changes anywhere in that network, so a capacitor's charge
  or an inductor's current resets to zero at that point. The memristor is the exception — its
  state fraction is explicitly carried across rebuilds, since persistent state is the entire point
  of a memristor. Making the others persist too is a good first contribution.
- **No recipe-book unlock advancements** — recipes work but won't appear highlighted/toast when
  first available.
- **Values are fixed presets**, cycled by right-click, rather than a numeric-entry GUI.

## Contributing

Issues and PRs welcome. If you're adding a component, please include a note on the physical model
you used (a link to the equations is enough) — the goal of this mod is that in-game behavior is
actually correct, not just plausible-looking.

## Citation

If you use CircuitCraft — in a classroom, a paper, a demo, a derivative mod, anywhere — please
credit the authors: Rodrigo Picos, Stavros G. Stavrinides, George Stavrinides, Ariadna Picos,
and Gerard Picos. A link back to this repository is enough for informal use; for academic
work, please cite it as:

```
Rodrigo Picos, Stavros G. Stavrinides, George Stavrinides, Ariadna Picos, and Gerard Picos.
CircuitCraft: a Fabric mod for teaching analog electronics in Minecraft.
https://github.com/rpicos-uib/circuitcraft, 2026.
```

BibTeX:

```bibtex
@software{picos_mine_memristors,
  author = {Picos, Rodrigo and Stavrinides, Stavros G. and Stavrinides, George and Picos, Ariadna and Picos, Gerard},
  title  = {CircuitCraft: a Fabric mod for teaching analog electronics in Minecraft},
  url    = {https://github.com/rpicos-uib/circuitcraft},
  year   = {2026}
}
```

See also [`CITATION.cff`](CITATION.cff), which GitHub reads automatically for its "Cite this
repository" button.

## License

MIT — see [LICENSE](LICENSE). The license permits reuse without attribution; the citation request
above is a courtesy ask on top of that, not an added legal restriction.
