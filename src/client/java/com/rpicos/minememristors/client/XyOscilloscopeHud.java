package com.rpicos.minememristors.client;

import com.rpicos.minememristors.item.XyProbeItem;
import com.rpicos.minememristors.network.XyProbeDataPayload;
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
 * Renders in the bottom-right corner (mirroring the time-domain oscilloscope's bottom-left
 * position, so both can be held and shown at once without overlapping): a square plot of the Y
 * channel's voltage against the X channel's voltage, the same Lissajous-figure display mode a
 * real bench oscilloscope's X-Y mode produces, instead of either channel plotted against time.
 * Both axes share one scale (the larger of the two channels' peak magnitude) so the plotted
 * shape's actual aspect ratio is preserved - a 90-degree phase-shifted, equal-amplitude pair
 * traces a circle, not a stretched ellipse.
 */
public class XyOscilloscopeHud implements HudElement {

	private static final int WIDTH = 118;
	private static final int PLOT_SIZE = WIDTH - 8;
	private static final int HEIGHT = PLOT_SIZE + 4 + 24;
	private static final int MARGIN = 6;
	private static final int TRACE_COLOR = 0xFFE080E0;

	@Override
	public void extractRenderState(GuiGraphicsExtractor extractor, DeltaTracker deltaTracker) {
		Minecraft client = Minecraft.getInstance();
		LocalPlayer player = client.player;
		if (player == null || !isHoldingXyProbe(player)) {
			return;
		}

		Font font = client.font;
		int x0 = extractor.guiWidth() - WIDTH - MARGIN;
		int y0 = extractor.guiHeight() - HEIGHT - MARGIN;

		extractor.fill(x0, y0, x0 + WIDTH, y0 + HEIGHT, 0xC0101014);
		extractor.outline(x0, y0, WIDTH, HEIGHT, 0xFF3A3A40);

		XyProbeDataPayload data = XyProbeClientState.current();
		if (data == null) {
			extractor.text(font, "no X-Y signal", x0 + 6, y0 + HEIGHT / 2 - 4, 0xFF808080);
			return;
		}

		int plotX0 = x0 + 4;
		int plotY0 = y0 + 4;
		int plotCenterX = plotX0 + PLOT_SIZE / 2;
		int plotCenterY = plotY0 + PLOT_SIZE / 2;

		extractor.horizontalLine(plotX0, plotX0 + PLOT_SIZE, plotCenterY, 0xFF2E2E4A);
		extractor.verticalLine(plotCenterX, plotY0, plotY0 + PLOT_SIZE, 0xFF2E2E4A);

		List<Float> xHistory = data.xHistory();
		List<Float> yHistory = data.yHistory();
		float maxAbs = 0.001f;
		for (float v : xHistory) {
			maxAbs = Math.max(maxAbs, Math.abs(v));
		}
		for (float v : yHistory) {
			maxAbs = Math.max(maxAbs, Math.abs(v));
		}

		int n = Math.min(xHistory.size(), yHistory.size());
		float half = PLOT_SIZE / 2f;
		for (int i = 0; i < n; i++) {
			int px = plotCenterX + Math.round(xHistory.get(i) / maxAbs * half);
			int py = plotCenterY - Math.round(yHistory.get(i) / maxAbs * half);
			extractor.fill(px, py, px + 1, py + 1, TRACE_COLOR);
		}

		int textY = plotY0 + PLOT_SIZE + 4;
		extractor.text(font, String.format("X: %.2fV  %s", data.xVoltage(), data.xSummary()), x0 + 4, textY, 0xFFDDDDDD, false);
		extractor.text(font, String.format("Y: %.2fV  %s", data.yVoltage(), data.ySummary()), x0 + 4, textY + 10, 0xFFDDDDDD, false);
	}

	private static boolean isHoldingXyProbe(Player player) {
		ItemStack main = player.getMainHandItem();
		ItemStack off = player.getOffhandItem();
		return main.getItem() instanceof XyProbeItem || off.getItem() instanceof XyProbeItem;
	}
}
