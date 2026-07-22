package com.rpicos.circuitcraft.blockentity;

import java.util.List;

/** Implemented by any block entity whose numeric preset(s) can also be set directly by typing a
 *  value, rather than only by cycling through the fixed preset list with an empty-hand
 *  right-click - shift-right-click opens a text-entry screen (in the same spirit as editing a
 *  sign) pre-filled with the values {@link #editableFields()} reports. */
public interface ValueEditable {

	/** Current fields, in the fixed order {@link #applyEditedValues} expects them back. */
	List<EditableField> editableFields();

	/** Applies newly typed values, in the same order as {@link #editableFields()}. Implementations
	 *  must clamp to each field's own [min, max] themselves - the client clamps too, for display,
	 *  but the server must never trust that and re-validate independently. */
	void applyEditedValues(List<Double> values);
}
