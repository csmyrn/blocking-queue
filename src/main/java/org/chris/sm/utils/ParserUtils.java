package org.chris.sm.utils;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

public class ParserUtils {
	private static volatile Map<String, Date> dateCache1 = new HashMap<String, Date>();
	private static volatile Map<String,java.sql.Date> dateCache2 = new HashMap<String, java.sql.Date>();
	private static volatile Map<String, Timestamp> dateCache3 = new HashMap<String, Timestamp>();
	private static volatile Map<Object,String> objectToStringCache = new HashMap<Object, String>();
	
	public static String fromCharDescriptionToChar(String chardescr) {
		if ("space".equalsIgnoreCase(chardescr))
			return " ";
		else if ("period".equalsIgnoreCase(chardescr))
			return ".";
		else if ("comma".equalsIgnoreCase(chardescr))
			return ",";
		else return chardescr;
	}
	/**
	 * converts a value in string format to the proper object value
	 * @param parserfield the <code>ParserField</code> that contains the 
	 * field name, size and decimals
	 * @param type object class type. Using BeanUtils the string value is converted to an object of the specified type
	 * @param value string value to be converted to an object value
	 * @return the converted string value
	 * @throws GatewayException
	 */
	public static synchronized Object fromStringToObject(ParserField pfield, String value) throws ParseException 
	{
		value = fixDoubleQuotes(value);
		
		if (!StringUtils.isNotBlank(value))
			return null;
		if (java.sql.Date.class.isAssignableFrom(pfield.getClassType())) {
			if (!StringUtils.isNotBlank(value)|| (pfield.getDateEmptyChar() != null && value.matches(pfield.getDateEmptyChar() + "+")))
				return null;
			java.sql.Date dt = dateCache2.get(java.sql.Date.class.getName() + pfield.getDateFormat() + value);
			if (dt == null) {
				dt = new java.sql.Date(FormatterUtils.getDateFormat(pfield.getDateFormat(), Locale.getDefault()).parse(value).getTime());
				dateCache2.put(java.sql.Date.class.getName() + pfield.getDateFormat() + value, dt);
			}			
			return dt;					
		}
		if (Timestamp.class.isAssignableFrom(pfield.getClassType())) {
			if (!StringUtils.isNotBlank(value)|| (pfield.getDateEmptyChar() != null && value.matches(pfield.getDateEmptyChar() + "+")))
				return null;
			Timestamp dt = dateCache3.get(java.sql.Date.class.getName() + pfield.getDateFormat() + value);
			if (dt == null) {
				dt = new Timestamp(FormatterUtils.getDateFormat(pfield.getDateFormat(), Locale.getDefault()).parse(value).getTime());
				dateCache3.put(java.sql.Date.class.getName() + pfield.getDateFormat() + value, dt);
			}			
			return dt;					
		} 		
		if (Date.class.isAssignableFrom(pfield.getClassType())) {
			if (!StringUtils.isNotBlank(value)|| (pfield.getDateEmptyChar() != null && value.matches(pfield.getDateEmptyChar() + "+")))
				return null;
			Date dt = dateCache1.get(Date.class.getName() + pfield.getDateFormat() + value);
			if (dt == null) {
				dt = FormatterUtils.getDateFormat(pfield.getDateFormat(), Locale.getDefault()).parse(value);
				dateCache1.put(Date.class.getName() + pfield.getDateFormat() + value, dt);
			}			
			return dt;					
		} 

		if (Integer.class.isAssignableFrom(pfield.getClassType()) || pfield.getClassType().getName().equals("int")) {
			if (!StringUtils.isNotBlank(value))
				return null;		
			//if (pfield.getNumberEmptyChar() != null && value.matches(pfield.getNumberEmptyChar() + "+"))
				//return null;
			return Integer.valueOf(Integer.parseInt(value));
		}
		if (String.class.isAssignableFrom(pfield.getClassType())) {
			if (!StringUtils.isNotBlank(value))
				return null;		
			if (pfield.getTextEmptyChar() != null && value.matches(pfield.getTextEmptyChar() + "+"))
				return null;
			if (pfield.isRemoveLeadingZeros())
				value = value.replaceAll("^0+(.*)", "$1");
			return value.trim();
		}
		if (Boolean.class.isAssignableFrom(pfield.getClassType()) || pfield.getClassType().getName().equals("boolean")) {
			if (!StringUtils.isNotBlank(value))
				return null;
			if (FormatterUtils.StringToBoolean(value))
				return Boolean.TRUE;
			else
				return Boolean.FALSE;
		}		
		if (BigDecimal.class.isAssignableFrom(pfield.getClassType()) || pfield.getClassType().getName().equals("double")) {
			if (!StringUtils.isNotBlank(value))
				return null;
			return new BigDecimal(value);
		}
		if (Float.class.isAssignableFrom(pfield.getClassType()) || pfield.getClassType().getName().equals("double")) {
			if (!StringUtils.isNotBlank(value))
				return null;
			return new Float(value);
		}

		return null;
	}

	public static synchronized String fromObjectToString(ParserField pfield, Object value) {	
		if (value == null)
			return "";
		if (Date.class.isAssignableFrom(pfield.getClassType()) || java.sql.Date.class.isAssignableFrom(pfield.getClassType())) {
			String dt = objectToStringCache.get(value);
			if (dt == null) {
				dt = FormatterUtils.getDateFormat(pfield.getDateFormat(), Locale.getDefault()).format((Date) value);
				objectToStringCache.put(value, dt);
			}			
			return dt;					
		} 

		if (Integer.class.isAssignableFrom(pfield.getClassType()) || pfield.getClassType().getName().equals("int")) {
			return String.valueOf(((Integer) value).intValue());
		}
		if (String.class.isAssignableFrom(pfield.getClassType())) {
			return (String) value;
		}
		if (Boolean.class.isAssignableFrom(pfield.getClassType()) || pfield.getClassType().getName().equals("boolean")) {
			return FormatterUtils.BooleanToString((Boolean) value);
		}		
		if (BigDecimal.class.isAssignableFrom(pfield.getClassType()) || pfield.getClassType().getName().equals("double")) {
			return value.toString();
		}	
		throw new RuntimeException("value [" + value + "] is of type that is not supported in Object to String converter");

	}
	
	public static Map<String, Object> parseLine(String[] lineFields, Collection<ParserField> parserFields) throws ParseException 
	{
		HashMap<String, Object> row = new HashMap<String, Object>();
		int i = 0;
		for (ParserField f: parserFields) {
			if (f.getConstantValue() != null) {
				row.put(f.getName(), f.getConstantValue());
			} else if (f.getCopyFromField() != null) {
				row.put(f.getName(), row.get(f.getCopyFromField()));
			} else if (f.getConcatenateColumns() == null) {
				if (i >= lineFields.length)
					continue;				
				Object value = ParserUtils.fromStringToObject(f, lineFields[i++]);
				if (f.getLength() > 0 && value instanceof String) {
					String s = (String) value;
					if (s.length() > f.getLength()) {
						if (f.getAlign() != null && f.getAlign().equalsIgnoreCase("right"))
							value = s.substring(s.length() - f.getLength(), s.length());
						else
							value = s.substring(0, f.getLength());
					}
				}
				row.put(f.getName(), value);
			} else {
				StringBuffer sb = new StringBuffer();
				int k = 1;
				for (String col: f.getConcatenateColumns()) {
					if (row.get(col) != null)
						sb.append(row.get(col));
					if (k < f.getConcatenateColumns().length)
						sb.append(f.getConcatenateSeparator());
					k++;
				}
				row.put(f.getName(), sb.toString().trim());
			}
			if (row.get(f.getName()) != null) {
				if (row.get(f.getName()).equals(f.getIgnoreRowWhenValueIs()))			
					return null;
				else if (row.get(f.getName()) instanceof String && f.getMinLength() > 0) {
					String val = (String) row.get(f.getName());
					if (val.length() < f.getMinLength()) {
						if (f.getAlign().equalsIgnoreCase("right") || f.getAlign() == null)
							val = Utils.repeatString(f.getEmptyChar(), f.getMinLength() - val.length()) + val;
						else
							val = val + Utils.repeatString(f.getEmptyChar(), f.getMinLength() - val.length());
						row.put(f.getName(), val);
					}
				}
			} 
		}
		return row;
	}
	
	private static String fixDoubleQuotes(String value)
	{
		if (StringUtils.isNotBlank(value)){
			if (value.startsWith("\""))
				value = value.replaceFirst("\"", "");
			if (value.endsWith("\""))
				value = value.substring(0, value.length() - 1);
		}
		
		return value;
	}
}
