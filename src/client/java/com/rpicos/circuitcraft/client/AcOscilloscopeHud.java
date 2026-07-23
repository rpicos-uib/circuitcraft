package com.rpicos.circuitcraft.client;

import com.rpicos.circuitcraft.item.AcProbeItem;
import com.rpicos.circuitcraft.network.AcBodePayload;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Renders a Bode plot - magnitude in dB on top, phase in degrees below, both against
 * log-frequency - of the AC probe's last completed sweep, or a hint prompting the player toward
 * the second click if a source has been pinned but no sweep has run yet. Placed in the top-left
 * corner, deliberately apart from the other two oscilloscopes' bottom corners, since all three
 * probes can in principle be held (and their HUDs shown) at once.
 */
public class AcOscilloscopeHud implements HudElement {

	private static final int WIDTH = 150;
	private static final int GRAPH_HEIGHT = 46;
	private static final int GRAPH_GAP = 14;
	private static final int HEIGHT = GRAPH_HEIGHT * 2 + GRAPH_GAP + 20;
	private static final int MARGIN = 6;
	private static final int MAG_COLOR = 0xFF60C0E0;
	private static final int PHASE_COLOR = 0xFFE0A060;

	@Override
	public void extractRenderState(GuiGraphicsExtractor extractor, DeltaTracker deltaTracker) {
		Minecraft client = Minecraft.getInstance();
		LocalPlayer player = client.player;
		if (player == null || !isHoldingAcProbe(player)) {
			return;
		}

		Font font = client.font;
		int x0 = MARGIN;
		int y0 = MARGIN;

		extractor.fill(x0, y0, x0 + WIDTH, y0 + HEIGHT, 0xC0101014);
		extractor.outline(x0, y0, WIDTH, HEIGHT, 0xFF3A3A40);

		AcProbeClientState.Display display = AcProbeClientState.current();
		if (display == null) {
			extractor.text(font, "AC probe: no source pinned", x0 + 4, y0 + HEIGHT / 2 - 4, 0xFF808080, false);
			return;
		}
		if (display instanceof AcProbeClientState.Hint hint) {
			extractor.text(font, "AC source pinned:", x0 + 4, y0 + 4, 0xFFDDDDDD, false);
			extractor.text(font, hint.sourcePos().toShortString(), x0 + 4, y0 + 16, MAG_COLOR, false);
			extractor.text(font, "Right-click a signal", x0 + 4, y0 + 30, 0xFFAAAAAA, false);
			extractor.text(font, "point to run the sweep", x0 + 4, y0 + 40, 0xFFAAAAAA, false);
			return;
		}

		AcBodePayload payload = ((AcProbeClientState.Result) display).payload();
		List<Float> freqs = payload.freqsHz();
		if (freqs.isEmpty()) {
			extractor.text(font, "AC probe: empty sweep", x0 + 4, y0 + HEIGHT / 2 - 4, 0xFF808080, false);
			return;
		}

		int graphX0 = x0 + 4;
		int graphX1 = x0 + WIDTH - 4;

		int magY0 = y0 + 2;
		drawTrace(extractor, font, graphX0, magY0, graphX1, magY0 + GRAPH_HEIGHT, payload.magnitudesDb(), MAG_COLOR, "dB");

		int phaseY0 = magY0 + GRAPH_HEIGHT + GRAPH_GAP;
		drawTrace(extractor, font, graphX0, phaseY0, graphX1, phaseY0 + GRAPH_HEIGHT, payload.phasesDeg(), PHASE_COLOR, "deg");

		String freqRange = SiFormat.magnitude(freqs.get(0)) + "Hz - " + SiFormat.magnitude(freqs.get(freqs.size() - 1)) + "Hz";
		extractor.text(font, freqRange, graphX0, phaseY0 + GRAPH_HEIGHT + 3, 0xFF9090A8, false);
	}

	private static void drawTrace(GuiGraphicsExtractor extractor, Font font, int x0, int y0, int x1, int y1,
			List<Float> values, int color, String unit) {
		float min = Float.MAX_VALUE;
		float max = -Float.MAX_VALUE;
		for (float v : values) {
			min = Math.min(min, v);
			max = Math.max(max, v);
		}
		if (max - min < 1e-6f) {
			max = min + 1f;
		}
		int height = y1 - y0;

		ScopeGrid.draw(extractor, x0, y0, x1, y1, 6, 4);
		extractor.text(font, String.format("%.0f%s", max, unit), x0, y0, 0xFF9090A8, false);
		extractor.text(font, String.format("%.0f%s", min, unit), x0, y1 - 8, 0xFF9090A8, false);

		int n = values.size();
		if (n < 2) {
			return;
		}
		int span = x1 - x0;
		int prevX = x0;
		int prevY = y1 - Math.round((values.get(0) - min) / (max - min) * height);
		for (int i = 1; i < n; i++) {
			int x = x0 + span * i / (n - 1);
			int y = y1 - Math.round((values.get(i) - min) / (max - min) * height);
			extractor.fill(prevX, Math.min(prevY, y), x + 1, Math.max(prevY, y) + 1, color);
			prevX = x;
			prevY = y;
		}
	}

	private static boolean isHoldingAcProbe(Player player) {
		ItemStack main = player.getMainHandItem();
		ItemStack off = player.getOffhandItem();
		return main.getItem() instanceof AcProbeItem || off.getItem() instanceof AcProbeItem;
	}
}
