package com.rpicos.circuitcraft.client;

import com.rpicos.circuitcraft.item.XyProbeItem;
import com.rpicos.circuitcraft.network.XyProbeDataPayload;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Renders in the bottom-right corner (mirroring the time-domain oscilloscope's bottom-left
 * position, so both can be held and shown at once without overlapping): a square plot of the Y
 * channel's voltage against the X channel's voltage, the same Lissajous-figure display mode a
 * real bench oscilloscope's X-Y mode produces, instead of either channel plotted against time.
 * Each axis is scaled independently to its own channel's peak magnitude, so two very
 * differently sized signals (a few volts on one channel, millivolts on the other) each still
 * use the plot's full range rather than one being squashed flat by a shared scale; the
 * trade-off is that a shape only has its true aspect ratio (an equal-amplitude, 90-degree
 * phase-shifted pair tracing an actual circle) when the two channels' peaks happen to match -
 * which they still do whenever amplitudes are in fact equal, since the two independent scales
 * then coincide. The Y label is always rendered bold: {@link com.rpicos.circuitcraft.network.XyProbeManager}
 * always appends the just-pinned position to the end of its 2-slot list, so the block a player
 * is about to right-click - whether it is brand new, currently X, or currently Y - always
 * becomes (or remains) the Y channel once pinned, and the label reflects that unconditionally.
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
		float maxAbsX = 0.001f;
		for (float v : xHistory) {
			maxAbsX = Math.max(maxAbsX, Math.abs(v));
		}
		float maxAbsY = 0.001f;
		for (float v : yHistory) {
			maxAbsY = Math.max(maxAbsY, Math.abs(v));
		}

		int n = Math.min(xHistory.size(), yHistory.size());
		float half = PLOT_SIZE / 2f;
		for (int i = 0; i < n; i++) {
			int px = plotCenterX + Math.round(xHistory.get(i) / maxAbsX * half);
			int py = plotCenterY - Math.round(yHistory.get(i) / maxAbsY * half);
			extractor.fill(px, py, px + 1, py + 1, TRACE_COLOR);
		}

		int textY = plotY0 + PLOT_SIZE + 4;
		// The next right-click always pins (or re-pins) the Y channel - see the class doc - so Y
		// is bolded unconditionally to show the player which axis their next click will set.
		Component xLabel = Component.literal(
				String.format("X: %.2fV  %s  (±%.2fV)", data.xVoltage(), data.xSummary(), maxAbsX));
		Component yLabel = Component.literal(
				String.format("Y: %.2fV  %s  (±%.2fV)", data.yVoltage(), data.ySummary(), maxAbsY))
				.withStyle(Style.EMPTY.withBold(true));
		extractor.text(font, xLabel, x0 + 4, textY, 0xFFDDDDDD, false);
		extractor.text(font, yLabel, x0 + 4, textY + 10, 0xFFDDDDDD, false);
	}

	private static boolean isHoldingXyProbe(Player player) {
		ItemStack main = player.getMainHandItem();
		ItemStack off = player.getOffhandItem();
		return main.getItem() instanceof XyProbeItem || off.getItem() instanceof XyProbeItem;
	}
}
