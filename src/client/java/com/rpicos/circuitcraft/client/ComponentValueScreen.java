package com.rpicos.circuitcraft.client;

import com.rpicos.circuitcraft.blockentity.EditableField;
import com.rpicos.circuitcraft.network.ComponentValueUpdatePayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Lets a player type a value directly for each of a component's {@link EditableField}s, instead
 * of only cycling through its fixed presets one right-click at a time - the same interaction
 * spirit as editing a sign: type text into a box, then confirm. Each field's own min/max (the
 * same range its existing preset cycle already covers) is printed under its box so a player
 * knows the valid range before typing, and the value sent back to the server is clamped to it
 * client-side too, purely for display - the server re-clamps independently and is the actual
 * authority.
 */
public class ComponentValueScreen extends Screen {

	private static final int FIELD_WIDTH = 120;
	private static final int FIELD_HEIGHT = 20;
	private static final int ROW_SPACING = 46;
	private static final int HINT_COLOR = 0xFFA0A0A0;

	private final BlockPos pos;
	private final List<EditableField> fields;
	private final List<EditBox> boxes = new ArrayList<>();

	public ComponentValueScreen(BlockPos pos, List<EditableField> fields) {
		super(Component.literal("Edit values"));
		this.pos = pos;
		this.fields = fields;
	}

	@Override
	protected void init() {
		int startY = height / 2 - (fields.size() * ROW_SPACING) / 2;
		int x = width / 2 - FIELD_WIDTH / 2;

		boxes.clear();
		for (int i = 0; i < fields.size(); i++) {
			EditableField field = fields.get(i);
			int y = startY + i * ROW_SPACING + 12;
			EditBox box = new EditBox(font, x, y, FIELD_WIDTH, FIELD_HEIGHT, Component.literal(field.label()));
			box.setMaxLength(32);
			box.setValue(formatValue(field.current()));
			boxes.add(addRenderableWidget(box));
		}

		int buttonY = startY + fields.size() * ROW_SPACING + 12;
		addRenderableWidget(Button.builder(Component.literal("Done"), button -> submit())
				.bounds(width / 2 - 104, buttonY, 100, 20)
				.build());
		addRenderableWidget(Button.builder(Component.literal("Cancel"), button -> onClose())
				.bounds(width / 2 + 4, buttonY, 100, 20)
				.build());

		setInitialFocus(boxes.isEmpty() ? null : boxes.get(0));
	}

	private void submit() {
		List<Double> values = new ArrayList<>();
		for (int i = 0; i < fields.size(); i++) {
			EditableField field = fields.get(i);
			double parsed;
			try {
				parsed = Double.parseDouble(boxes.get(i).getValue().trim());
			} catch (NumberFormatException e) {
				parsed = field.current();
			}
			values.add(Math.clamp(parsed, field.min(), field.max()));
		}
		ClientPlayNetworking.send(new ComponentValueUpdatePayload(pos, values));
		onClose();
	}

	private static String formatValue(double value) {
		if (value == Math.rint(value) && Math.abs(value) < 1e15) {
			return Long.toString((long) value);
		}
		return Double.toString(value);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
		extractor.fill(0, 0, width, height, 0xC0101014);
		super.extractRenderState(extractor, mouseX, mouseY, partialTick);

		extractor.text(font, "Edit values", width / 2 - font.width("Edit values") / 2,
				(height / 2 - (fields.size() * ROW_SPACING) / 2) - 16, 0xFFFFFFFF, true);

		int startY = height / 2 - (fields.size() * ROW_SPACING) / 2;
		int x = width / 2 - FIELD_WIDTH / 2;
		for (int i = 0; i < fields.size(); i++) {
			EditableField field = fields.get(i);
			int y = startY + i * ROW_SPACING;
			String label = field.label() + " (" + field.unit() + ")";
			extractor.text(font, label, x, y, 0xFFDDDDDD, false);
			String hint = "Range: " + formatValue(field.min()) + " - " + formatValue(field.max()) + " " + field.unit();
			extractor.text(font, hint, x, y + 12 + FIELD_HEIGHT + 2, HINT_COLOR, false);
		}
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
