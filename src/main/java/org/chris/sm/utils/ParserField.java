package org.chris.sm.utils;

import java.util.ArrayList;
import java.util.List;

public class ParserField {
	private String name;
	private Object constantValue;
	private Class classType = String.class;
	private String dateFormat;
	private String dateEmptyChar;	
	private String numberEmptyChar = "0";
	private String textEmptyChar;
	private String[] concatenateColumns;
	private String concatenateSeparator = " ";
	private String copyFromField;
	private Object ignoreRowWhenValueIs;
	private int length;
	private int minLength;
	private String emptyChar = " ";
	private boolean removeLeadingZeros;
	private String align;
	public String getAlign() {
		return align;
	}
	public void setAlign(String align) {
		this.align = align;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Class getClassType() {
		return classType;
	}
	public void setClassType(Class classType) {
		this.classType = classType;
	}
	public int getLength() {
		return length;
	}
	public void setLength(int length) {
		this.length = length;
	}
	public String getDateFormat() {
		return dateFormat;
	}
	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}
	public String getDateEmptyChar() {
		return dateEmptyChar;
	}
	public void setDateEmptyChar(String dateEmptyChar) {
		this.dateEmptyChar = dateEmptyChar;
	}
	public String getNumberEmptyChar() {
		return numberEmptyChar;
	}
	public void setNumberEmptyChar(String numberEmptyChar) {
		this.numberEmptyChar = numberEmptyChar;
	}
	public String getTextEmptyChar() {
		return textEmptyChar;
	}
	public void setTextEmptyChar(String textEmptyChar) {
		this.textEmptyChar = textEmptyChar;
	}
	public String[] getConcatenateColumns() {
		return concatenateColumns;
	}
	public void setConcatenateColumns(String[] concatenateColumns) {
		this.concatenateColumns = concatenateColumns;
	}
	public String getConcatenateSeparator() {
		return concatenateSeparator;
	}
	public void setConcatenateSeparator(String concatenateSeparator) {
		this.concatenateSeparator = concatenateSeparator;
	}
	public Object getConstantValue() {
		return constantValue;
	}
	public void setConstantValue(Object constantValue) {
		this.constantValue = constantValue;
	}
	public String getCopyFromField() {
		return copyFromField;
	}
	public void setCopyFromField(String copyFromField) {
		this.copyFromField = copyFromField;
	}
	public Object getIgnoreRowWhenValueIs() {
		return ignoreRowWhenValueIs;
	}
	public void setIgnoreRowWhenValueIs(Object ignoreRowWhenValueIs) {
		this.ignoreRowWhenValueIs = ignoreRowWhenValueIs;
	}
	public int getMinLength() {
		return minLength;
	}
	public void setMinLength(int minLength) {
		this.minLength = minLength;
	}
	public String getEmptyChar() {
		return emptyChar;
	}
	public void setEmptyChar(String emptyChar) {
		this.emptyChar = emptyChar;
	}
	public boolean isRemoveLeadingZeros() {
		return removeLeadingZeros;
	}
	public void setRemoveLeadingZeros(boolean removeLeadingZeros) {
		this.removeLeadingZeros = removeLeadingZeros;
	}

	public static List<ParserField> fromMetadataList(String fields) {
		List<ParserField> result = new ArrayList<ParserField>();
		for (String field: fields.split(";|,")) {
			ParserField f = new ParserField();
			f.setName(field);		
			result.add(f);
		}
		return result;
	}

	public static ParserField fromMetadata(String parserFieldText) {
		String[] parts = parserFieldText.split(":");
		String name = null, classType = null;
		if (parts.length >= 1)
			name = parts[0];

		if (parts.length >= 2) {
			classType = parts[1];
		}
		ParserField f = new ParserField();
		f.setName(name);
		if (classType != null)
		try {
			f.setClassType(Class.forName(classType));
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		return f;
	}
}
