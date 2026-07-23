# CircuitCraft — short version

An 8-page-target companion to the full paper in `../latex/`, both using the IEEEtran journal
class (target: IEEE Transactions on Education). This directory is a separate set of files,
not a single `\ifshort`-toggled source shared with the long version — the cuts below are too
extensive (roughly 60% of the long version's body word count) for conditional inclusion to
stay readable. **If the long version changes after this split, port relevant changes here by
hand** — nothing keeps the two in sync automatically.

Reuses `../latex/figures/` via `\graphicspath` rather than duplicating them.
`references.bib` was originally shared the same way, but Overleaf moved it into this
directory directly (rather than copying it) while editing there, which briefly broke the long
version's bibliography until it was restored — both versions now keep an independent copy of
`references.bib`, and it needs manual syncing if either version's citations change.

## What's cut relative to the long version

- **Appendix (crafting recipes)**: dropped entirely. Availability section points to the
  extended version/repo instead.
- **Worked experiments**: 2 of 6 kept in full (RC low-pass Bode plot — the AC-solver
  showcase; memristor pinched hysteresis loop — the paper's namesake feature), each with the
  same exact preset values and closed-form/qualitative expected results as the long version.
  The other four (voltage divider, RLC resonance, half-wave rectifier, op-amp open-loop Bode)
  are named in one sentence, pointing to the extended version.
- **Component gallery figures**: all four (`elements_gallery.png`, `wire_ground_gallery.png`,
  `generators_gallery.png`, `probes_gallery.png`) dropped; the seventeen blocks/items are
  described in compact prose within a single combined Architecture section instead. Only two
  figures remain in the whole short version: the package-architecture `tikzpicture` and one
  oscilloscope screenshot (`square_waveform.png` only, not the square+triangle pair).
- **The Function Generator's Control Network** (05b, ~968 words in the long version):
  dropped as a standalone section; folded into one clause in Architecture (modules override a
  generator's amplitude/frequency).
- **Related Work**: the four subsections (circuit simulators; Minecraft in education; the
  three related mods; memristor theory) condensed into continuous prose, same citations,
  substantially shorter per-citation discussion.
- **Circuit solver / AC analysis**: all core equations kept (MNA formulation, reactive
  companion models, memristor ODE, diode linearization, op-amp nullor, AC per-element
  admittances), but with much shorter surrounding justification prose. The two-pole op-amp AC
  model is mentioned without its display equation, since no kept experiment exercises it.
- **Pedagogy / Limitations**: same points, each cut to roughly a third of its long-version
  length.
- **Section count**: consolidated from 11 numbered top-level sections down to 7, merging
  pairs that were already logically adjacent into section+subsection pairs rather than
  separate top-level sections: Minecraft Mechanics + System Architecture; the transient and AC
  circuit solvers (now "The Circuit Solver" with two subsections); Pedagogical Design +
  Worked Experiments; and Verification + Limitations/Future Work. No prose was cut for this —
  only section-level headers were merged/demoted (\section to \subsection, and the two worked
  experiments from \subsection to \subsubsection), so this didn't change length materially.
  The long version was left alone; ask if it should get the same treatment.

## Page-count status

**Not verified by compiling** — the standing preference for this repo is no local
`pdflatex`/`bibtex` (see `../CLAUDE.md`); check the actual page count in Overleaf. Section
word counts total roughly 5,000 words (plus a ~230-word abstract), against a density-based
estimate of the long version's ~13,000 words fitting ~17 pages (~765 words/page including its
figure/table/equation load) — comfortably under an 8-page budget by that estimate, especially
given the short version also carries far fewer figures/tables (2 vs. ~11) and equations (~9
vs. ~15+). If Overleaf shows meaningful slack under 8 pages, there's room to restore a third
experiment or a figure before finalizing; if it runs over, the AC-analysis and circuit-solver
equation-heavy sections are the best remaining places to trim.
