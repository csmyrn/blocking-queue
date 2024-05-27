package org.chris.sm.utils;

import java.beans.PropertyEditorSupport;

public class ParserFieldPropertyEditor extends PropertyEditorSupport {
	@Override
	public void setValue(Object value) {
		super.setValue(value);
	}
	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		if (text instanceof String) {
			ParserField f = ParserField.fromMetadata(text);
			this.setValue(f);
		}
	}
	@Override
	public String getAsText() {
		Object val = this.getValue();
		return val.toString();
	}
}
