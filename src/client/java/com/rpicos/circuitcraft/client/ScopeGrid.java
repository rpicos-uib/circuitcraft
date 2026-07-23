package com.rpicos.circuitcraft.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Shared graticule renderer for all three oscilloscope HUDs ({@link OscilloscopeHud},
 *  {@link XyOscilloscopeHud}, {@link AcOscilloscopeHud}) - draws evenly spaced division lines
 *  across a graph area, the way a real bench scope's screen is etched, so amplitude/time (or
 *  frequency) can be judged by eye against a fixed reference instead of only the auto-scaled
 *  extremes printed at the edges. Drawn before any trace/axis line so those remain the visually
 *  dominant features on top of the graticule. */
final class ScopeGrid {

	static final int COLOR = 0xFF23262B;

	private ScopeGrid() {
	}

	static void draw(GuiGraphicsExtractor extractor, int x0, int y0, int x1, int y1, int cols, int rows) {
		for (int c = 1; c < cols; c++) {
			int x = x0 + (x1 - x0) * c / cols;
			extractor.verticalLine(x, y0, y1, COLOR);
		}
		for (int r = 1; r < rows; r++) {
			int y = y0 + (y1 - y0) * r / rows;
			extractor.horizontalLine(x0, x1, y, COLOR);
		}
	}
}
