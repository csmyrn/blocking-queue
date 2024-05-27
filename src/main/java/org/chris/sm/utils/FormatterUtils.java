/* 
   Copyright 2003 Business Exchanges, Inc. All rights reserved.

   $Id: FormatterUtils.java,v 1.6 2007/12/17 11:56:38 lprotopapas Exp $ 
 */

package org.chris.sm.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.text.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class FormatterUtils {
	private static volatile HashMap<String, DateFormat> dfMap = new HashMap();
	private static volatile Object mutex = new Object();
	/**
	 * The <code>Log</code> instance for this application.
	 */
	private static final Logger log = LogManager.getLogger(FormatterUtils.class);

	private FormatterUtils() { } // prevent instatiation


	/**
	 *	formats a String up to a predefined size and justification by filling any leading or trailing spaces with 
	 *	a specific character
	 *       NOTE: only left and right justification are supported so far 
	 */
	public static String formatString(String value, int size, String justify, String leadingChar)
	{	
		if (size == 0)
			return value;
		StringBuffer s = new StringBuffer();

		if (value==null)
		{
			for (int i=0; i <size; i++)
				s.append(leadingChar);

		}
		else
		{
			if (size==0)
				return value;

			int len = value.length();
			if (len >=size)
				s.append(value.substring(0, size));
			else
			{
				int noEmptyChars= size - len;
				for (int i=0; i<noEmptyChars;i++)
					s.append(leadingChar);
				if (justify.equalsIgnoreCase("left"))
					s.insert(0, value);
				else if (justify.equalsIgnoreCase("right"))
					s.insert(noEmptyChars,value);
			}
		}	
		return s.toString();			
	}

	/**
	 *	parses a String of fixed length by removing any leading characters of the string and justification
	 *
	 */
	public static String parseString(String value, int size, String justify, String leadingChar)
	{
		log.trace("parseString: inputValue=" + value);

		if (value==null) 
			return null;
		String output;
		// since parsing outside this function e.g Digester may remove any leading or trailing spaces, we have to compensate for this
		if (value.length()<=size)
		{	
			size= value.length();
			if (justify.equalsIgnoreCase("right")) {
				int idx=0;
				boolean foundDiff=false;
				for (int i=0; i<size; i++)
					if (value.charAt(i)!=leadingChar.charAt(0))
					{	
						idx=i;foundDiff=true;
						break;
					}

				if (foundDiff)
					output = value.substring(idx);
				else
					output=leadingChar;
			}
			else if (justify.equalsIgnoreCase("left")) {

				int idx=size-1;

				boolean foundDiff=false;
				for (int i=size-1; i>=0; i--)
				{ 

					if (value.charAt(i)!=leadingChar.charAt(0))
					{	
						idx=i;foundDiff=true;
						break;
					}
				}

				if (foundDiff)
					output = value.substring(0,idx+1);
				else
					output=leadingChar;
			}
			else 
				output=value; // no justification, or justification that is not supported; return as it is
		}
		else // may never happen
			output=value.substring(0, size); 

		return output;			
	}
	
	public static DateFormat getDateFormat(String dateFormat, Locale locale) {
		DateFormat df;
		synchronized (mutex) {
			df = dfMap.get(dateFormat + locale.getCountry() + locale.getLanguage());
			if (df == null) {
				df = new SimpleDateFormat(dateFormat);
				dfMap.put(dateFormat + locale.getCountry() + locale.getLanguage(), df);
			}
		}
		return df;				
	}

	/**
	 *	Formats a date according to the specified dateFormat. 
	 *	If the input is null or an error occurs, it returns null 
	 *
	 */
	public static String formatDate(Date date, String dateFormat) {
		// construct a date formatter using the default user locale.
		DateFormat df = getDateFormat(dateFormat, Locale.getDefault());
		
		try {
			if(date==null){
				return null;
			}
			else 
				return df.format(date);
		} 
		catch(Exception e) { 
			if(log.isErrorEnabled()) {
				log.error("Error formatting date '" + date + "'.", e);
			}
			return null;
		}
	}


	/**
	 *	Formats a string representing a date according to the specified dateFormat. 
	 *	If the input is null or an error occurs, it returns null 
	 *
	 */
	public static Date parseDate(String dateString, String dateFormat, Locale locale) throws FileParserException {
		
		// construct a date formatter using the default user locale.
		DateFormat df = getDateFormat(dateFormat, locale);



		df.setLenient(false);

		try {
			if(dateString==null ){
				return null;
			}
			else {
				return df.parse(dateString);
			}
		} 
		catch(Exception e) {
			if(log.isErrorEnabled()) {
				log.error("Error parsing date '" + dateString + "'.", e);
			}
			throw new FileParserException("Error parsing date '" + dateString + "'.");
		}

	}

	/**
	 *	Formats an amount according to the specified format. 
	 *
	 */
	public static String formatAmount(double amount, 
			int size,  // does not include the sign character
			int decimals, 
			boolean dispayDecimalPoint, 
			String amountLeadingChar,
			Locale locale,
			boolean displaySign) {
		if (locale == null)
			locale = Locale.getDefault();
		NumberFormat form= NumberFormat.getInstance(locale);
		char decimalSeparator='.'; // default decimal separator

		if (form instanceof DecimalFormat) { 
			((DecimalFormat) form).setMaximumFractionDigits(decimals);
			((DecimalFormat) form).setMinimumFractionDigits(decimals);
			if (size!=0) 
				((DecimalFormat) form).setMaximumIntegerDigits(size-decimals);

			//do not use groupping.May be modified later in order to config
			((DecimalFormat) form).setGroupingUsed(false);



			DecimalFormatSymbols dfSymbols= ((DecimalFormat)form).getDecimalFormatSymbols();
			decimalSeparator= dfSymbols.getDecimalSeparator();	
		}

		// format the absolute value of amount.Sign will be placed later if needed
		String tempOutput=form.format(Math.abs(amount));
		String output;
		// remove the decimal separator if dispayDecimalPoint is false (NO)
		if (!dispayDecimalPoint){
			int idx = tempOutput.indexOf(decimalSeparator);
			if (idx!=-1)
				output = tempOutput.substring(0,idx) + tempOutput.substring(idx+1);
			else 
				output=tempOutput;	 	
		}
		else
			output=tempOutput;
		// prepend any leading characters to the output if such a string is specified and the length of the output is less than the required size.
		// if size is not specified, then do not prepend any characters and return the string as it is
		StringBuffer sb= new StringBuffer();

		if (amountLeadingChar!=null && size!=0 && output.length() < size)
		{
			if (!displaySign) {
				for (int i=0;i<size-output.length(); i++)
					sb.append(amountLeadingChar);
			}
			else 
			{	
				if (amount >=0)
					sb.append("+");
				else
					sb.append("-");
				for (int i=1;i<size-output.length(); i++)
					sb.append(amountLeadingChar);
			}
		} else if (displaySign) {
			if (amount >=0)
				sb.append("+");
			else
				sb.append("-");
		}
		sb.append(output);
		return sb.toString();
	}


	/**
	 *	Parses an amount according to the specified format in the configuration file 
	 *       Amount is considered always positive. We don't use any java NumberFormat class
	 */
	public static double parseAmount(String amountString, 
			int size,  // does not include the sign character
			int decimals, 
			boolean displayDecimalPoint, 
			String amountLeadingChar) 
	throws FileParserException{
		try {
			if ( amountString == null) 
				return 0.0;

			char decimalSeparator='.'; // default decimal separator

			// remove any leading characters that are equal to amountLeadingChar and place the appropriate decimal point
			String tempAmountString;
			int maxLeadingChars;
			// at least one integer digit should exist  
			if (!displayDecimalPoint)
				maxLeadingChars= size-decimals -1; 
			else
				maxLeadingChars= size-decimals -2;

			int idx=0;
			for (int i=0;i<maxLeadingChars; i++)
				if (amountString.charAt(i) != amountLeadingChar.charAt(0))
				{
					idx=i;
					break;
				}
			tempAmountString= amountString.substring(idx);
			// place the decimal point (as the default decimal separator)
			if (!displayDecimalPoint){ // decimal point does not exist, place it
				String integerPart= tempAmountString.substring( 0, tempAmountString.length()-decimals );
				String fractionPart=tempAmountString.substring( tempAmountString.length()-decimals);
				tempAmountString= integerPart + decimalSeparator + fractionPart;
			}
			else {  // decimal point exists, replace it with the decimal point of the number format 
				String integerPart= tempAmountString.substring( 0, tempAmountString.length()-decimals -1);
				String fractionPart=tempAmountString.substring( tempAmountString.length()-decimals);
				tempAmountString= integerPart + decimalSeparator + fractionPart;
			}

			// parse the amount string and get a double.
			double v = (new Double(tempAmountString)).doubleValue();
			return v;
		}
		catch(Exception e) {
			if(log.isErrorEnabled()) {
				log.error("Error parsing amount string '" + amountString + "'.", e);
			}
			throw new FileParserException("Error parsing amount string '" + amountString + "'.");
		}	

	}


	public static double roundDouble(double number) {

		return FormatterUtils.roundDouble(number, 2);

	}

	public static double roundDouble(double number, int scale) {

		return (new BigDecimal(number)
		).setScale(scale, BigDecimal.ROUND_HALF_EVEN).doubleValue();

	}

	public static String repeatMaker(char token, int len)
	{	if (len == 0) 
		return "";
	StringBuffer sb1 = new StringBuffer("");
	sb1.setLength(len);
	return sb1.toString().replace('\u0000', token);
	}

	public static String repeatMaker(String token, int len) 
	{
		if (len == 0 || token == null) 
			return "";
		if (token.length() >= len)
			return token.substring(0, len);
		else {
			StringBuffer sb = new StringBuffer();
			for (int k = 1; k <= len; k += token.length())
				sb.append(token);						
			return sb.toString().substring(0, len);
		}
	}

	public static boolean StringToBoolean(String value){
		if (value == null)
			return false;
		return value.trim().matches("(?i)(yes)|(true)|1");
	}

	public static String BooleanToString(Boolean value) {
		if (value == null)
			return "";
		if (value.booleanValue())
			return "1";
		else
			return "0";
	}
} // end of FormatterUtils