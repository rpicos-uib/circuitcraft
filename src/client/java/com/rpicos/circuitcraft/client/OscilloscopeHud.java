package com.rpicos.circuitcraft.client;

import com.rpicos.circuitcraft.item.ProbeItem;
import com.rpicos.circuitcraft.network.ProbeDataPayload;
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
 * Renders like looking at a held map: while the probe is in either hand, up to
 * {@link com.rpicos.circuitcraft.network.ProbeWatchManager#MAX_CHANNELS} pinned channels are
 * stacked in the corner, each showing its own scrolling voltage (or, for an ammeter, current)
 * trace - so two or three signals can be watched side by side instead of one at a time. Once all
 * three slots are full, the oldest is outlined in yellow to show which channel a new pin would
 * evict.
 */
public class OscilloscopeHud implements HudElement {

	private static final int WIDTH = 118;
	private static final int HEIGHT = 74;
	private static final int MARGIN = 6;
	private static final int GAP = 4;
	private static final int HIGHLIGHT_COLOR = 0xFFFFD060;

	// One accent color per stacked channel, bottom-to-top; loops if somehow more channels ever show.
	private static final int[] CHANNEL_COLORS = {0xFF60E080, 0xFF60C0E0, 0xFFE0A060};

	@Override
	public void extractRenderState(GuiGraphicsExtractor extractor, DeltaTracker deltaTracker) {
		Minecraft client = Minecraft.getInstance();
		LocalPlayer player = client.player;
		if (player == null || !isHoldingProbe(player)) {
			return;
		}

		Font font = client.font;
		List<ProbeDataPayload> channels = ProbeClientState.currentChannels();

		if (channels.isEmpty()) {
			int y0 = extractor.guiHeight() - HEIGHT - MARGIN;
			drawFrame(extractor, MARGIN, y0);
			extractor.text(font, "no signal", MARGIN + 6, y0 + HEIGHT / 2 - 4, 0xFF808080);
			return;
		}

		for (int i = 0; i < channels.size(); i++) {
			int y0 = extractor.guiHeight() - MARGIN - (i + 1) * HEIGHT - i * GAP;
			drawChannel(extractor, font, MARGIN, y0, channels.get(i), CHANNEL_COLORS[i % CHANNEL_COLORS.length]);
		}
	}

	private static void drawFrame(GuiGraphicsExtractor extractor, int x0, int y0) {
		extractor.fill(x0, y0, x0 + WIDTH, y0 + HEIGHT, 0xC0101014);
		extractor.outline(x0, y0, WIDTH, HEIGHT, 0xFF3A3A40);
	}

	private static void drawChannel(GuiGraphicsExtractor extractor, Font font, int x0, int y0,
			ProbeDataPayload data, int traceColor) {
		drawFrame(extractor, x0, y0);
		if (data.willBeReplacedNext()) {
			// A second, larger outline just outside the normal frame - marks the channel a 4th
			// pin would evict, without needing to disturb the frame's own border color/thickness.
			extractor.outline(x0 - 2, y0 - 2, WIDTH + 4, HEIGHT + 4, HIGHLIGHT_COLOR);
		}

		int x1 = x0 + WIDTH;
		List<Float> history = data.history();
		int graphX0 = x0 + 4;
		int graphY0 = y0 + 4;
		int graphX1 = x1 - 4;
		int graphY1 = y0 + HEIGHT - 24;
		int graphHeight = graphY1 - graphY0;
		int midY = graphY0 + graphHeight / 2;

		extractor.horizontalLine(graphX0, graphX1, midY, 0xFF2E4A2E);

		float maxAbs = 0.001f;
		for (float v : history) {
			maxAbs = Math.max(maxAbs, Math.abs(v));
		}

		if (history.size() >= 2) {
			int n = history.size();
			int span = graphX1 - graphX0;
			int prevX = graphX0;
			int prevY = midY - Math.round(history.get(0) / maxAbs * (graphHeight / 2f));
			for (int i = 1; i < n; i++) {
				int x = graphX0 + span * i / (n - 1);
				int y = midY - Math.round(history.get(i) / maxAbs * (graphHeight / 2f));
				extractor.fill(prevX, Math.min(prevY, y), x + 1, Math.max(prevY, y) + 1, traceColor);
				prevX = x;
				prevY = y;
			}
		}

		String reading = String.format("%.2fV  %.3fA", data.voltage(), data.current());
		extractor.text(font, reading, x0 + 4, graphY1 + 2, traceColor, false);
		String summary = data.willBeReplacedNext() ? data.summary() + " (next)" : data.summary();
		extractor.text(font, summary, x0 + 4, graphY1 + 12, 0xFFDDDDDD, false);
	}

	private static boolean isHoldingProbe(Player player) {
		ItemStack main = player.getMainHandItem();
		ItemStack off = player.getOffhandItem();
		return main.getItem() instanceof ProbeItem || off.getItem() instanceof ProbeItem;
	}
}
