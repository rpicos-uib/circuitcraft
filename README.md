# Mine Memristors

A Fabric mod that turns Minecraft into an analog electronics lab. Place resistors, capacitors,
inductors and memristors as blocks, wire them up, drive them with power supplies and function
generators, and probe the live voltage/current with a handheld oscilloscope — all backed by a
real modified-nodal-analysis (MNA) circuit solver, the same family of algorithm SPICE uses, not a
scripted approximation.

This is a teaching tool: the goal is for the in-game behavior to actually match what you'd see on
a real bench (RC charge curves, RL transients, a memristor's resistance drifting with accumulated
charge), just at Minecraft-tick timescales instead of real-world ones.

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
3. Grab `minememristors-<version>.jar` from this repo's [Releases](../../releases) page and drop
   it in the same `mods` folder.
4. Make sure the profile you launch with is on Java 25+ (recent launchers that auto-manage a JVM
   per Minecraft version will already do this once you select 26.2).
5. Launch with the Fabric profile.

### Option B — build from source

```bash
git clone https://github.com/rpicos-uib/mine-memristors.git
cd mine-memristors
./gradlew build
```

The mod jar comes out at `build/libs/minememristors-<version>.jar`. You need a JDK 25 available;
either make it your default `java`, or point Gradle at it explicitly:

```bash
JAVA_HOME=/path/to/jdk-25 ./gradlew build
```

Useful dev tasks: `./gradlew runClient` (launches a dev client with the mod loaded) and
`./gradlew runServer` (headless dedicated server — handy for checking the mod loads without a GUI).

## The components

| Block/Item | What it is |
|---|---|
| **Resistor** | Ohmic resistor. Right-click (empty hand) to cycle 10 / 100 / 1,000 / 10,000 Ω. |
| **Capacitor** | Ideal capacitor, trapezoidal-integration model. Cycles 1 / 10 / 100 / 1,000 µF. |
| **Inductor** | Ideal inductor, trapezoidal-integration model. Cycles 0.01 / 0.1 / 1 / 5 H (scaled for Minecraft's tick rate, not real-world component ratings). |
| **Memristor** | Charge-controlled linear-drift (HP) memristor model. Resistance drifts between 100 Ω and 10,000 Ω based on accumulated charge through it — the "memory" persists even when the circuit is rebuilt. Cycles a "switching speed" preset. |
| **Power Supply** | Ideal DC voltage source. Cycles 1.5 / 5 / 9 / 12 / 24 V. |
| **Function Generator** | Time-varying voltage source. Cycles sine/square/triangle presets at various frequencies. |
| **Wire** | Zero-resistance conductor block. Connects on all six faces. |
| **Probe** | Right-click a component to read its live V/I/state on the action bar. Hold it in either hand to bring up an oscilloscope HUD in the corner of the screen, showing a scrolling voltage trace of whatever component you're currently looking at. |

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

All vanilla ingredients, no dependency on any other mod:

| Item | Recipe |
|---|---|
| Resistor ×2 | Clay ball + 2 iron nuggets (shapeless) |
| Capacitor ×2 | 2 iron nuggets + paper (shapeless) — paper as the historical capacitor dielectric |
| Inductor ×2 | 2 copper ingots + iron nugget (shapeless) |
| Memristor ×1 | Redstone + amethyst shard + iron nugget (shapeless) |
| Power Supply ×1 | 3×3 iron/copper shell around a redstone block core |
| Function Generator ×1 | 3×3 iron/copper shell, quartz + redstone torch core |
| Wire ×6 | 1 copper ingot (shapeless, cheap/bulk) |
| Probe ×1 | Redstone / iron nugget / stick, vertically (shaped) |

None of these have recipe-book unlock advancements yet, so they won't show a "new recipe" toast —
but they're fully craftable by hand right now. See [Contributing](#contributing) if you want to add
those.

All items are also available in their own **Mine Memristors** creative-inventory tab.

## Architecture, for anyone extending this

```
src/main/java/com/rpicos/minememristors/
  sim/            Pure-Java circuit solver — zero Minecraft dependency, unit-testable standalone
  block/          Block classes (placement, orientation, right-click interactions)
  blockentity/    BlockEntity classes (the actual sim state + circuit wiring per component)
  network/        World-side wire connectivity graph + the client<->server probe protocol
  item/           The probe item
src/client/java/com/rpicos/minememristors/client/   HUD rendering, client-side networking
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
6. Optionally add a `data/minememristors/recipe/your_component.json`.

### Known limitations (v0.1)

- **Component state resets on circuit rebuild.** `CircuitNetworkManager` rebuilds the whole
  `Circuit` from scratch whenever wiring changes anywhere in that network, so a capacitor's charge
  or an inductor's current resets to zero at that point. The memristor is the exception — its
  state fraction is explicitly carried across rebuilds, since persistent state is the entire point
  of a memristor. Making the others persist too is a good first contribution.
- **The oscilloscope HUD was built and compiled against real decompiled sources for this game
  version, but never visually verified** — it was developed in a sandboxed environment with no
  display available to actually launch a game window. If it looks wrong, that's the likely reason;
  please open an issue with a screenshot.
- **No recipe-book unlock advancements** — recipes work but won't appear highlighted/toast when
  first available.
- **Values are fixed presets**, cycled by right-click, rather than a numeric-entry GUI.

## Contributing

Issues and PRs welcome. If you're adding a component, please include a note on the physical model
you used (a link to the equations is enough) — the goal of this mod is that in-game behavior is
actually correct, not just plausible-looking.

## Citation

If you use Mine Memristors — in a classroom, a paper, a demo, a derivative mod, anywhere — please
credit the original author, Rodrigo Picos. A link back to this repository is enough for informal
use; for academic work, please cite it as:

```
Rodrigo Picos. Mine Memristors: a Fabric mod for teaching analog electronics in Minecraft.
https://github.com/rpicos-uib/mine-memristors, 2026.
```

BibTeX:

```bibtex
@software{picos_mine_memristors,
  author = {Picos, Rodrigo},
  title  = {Mine Memristors: a Fabric mod for teaching analog electronics in Minecraft},
  url    = {https://github.com/rpicos-uib/mine-memristors},
  year   = {2026}
}
```

See also [`CITATION.cff`](CITATION.cff), which GitHub reads automatically for its "Cite this
repository" button.

## License

MIT — see [LICENSE](LICENSE). The license permits reuse without attribution; the citation request
above is a courtesy ask on top of that, not an added legal restriction.
