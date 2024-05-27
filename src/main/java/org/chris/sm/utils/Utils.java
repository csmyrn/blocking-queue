package org.chris.sm.utils;

import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.net.URL;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;



public class Utils {
	public static String defaultDir = "";
	public static String[] String2Array(String input) {
		if (input == null)
			return null;
		return input.split("\\\\n|;");
	}

	public static String subString(String s, int beginIndex, int endIndex, String onEmptyString) {
		String result;
		if (beginIndex + 1 >= s.length())
			return onEmptyString;
		if (endIndex > s.length() || endIndex == -1)
			result = s.substring(beginIndex);
		else
			result = s.substring(beginIndex, endIndex);
		result = result.trim();
		if (result.equals(""))
			return onEmptyString;
		else 
			return result;
	}

	public static String right(String s, int len) {
		String ss = s.trim();
		if (ss.length() <= len)
			return s;
		return (ss.substring(ss.length() - len));
	}
	
	public static String left(String s, int len) {
		String ss = s.trim();
		if (ss.length() <= len)
			return s;
		return (ss.substring(0,len));
	}	

	public static String Array2String(String[] input, String delimiter) {
		if (input == null)
			return null;
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < input.length - 1; i++)
			result.append(input[i]).append(delimiter);
		result.append(input[input.length - 1]);
		return result.toString();
	}

	public static String ArrayObject2String(Object[] input, String delimiter) {
		if (input == null)
			return null;
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < input.length - 1; i++)
			result.append(input[i]).append(delimiter);
		result.append(input[input.length - 1]);
		return result.toString();
	}

	public static Set Array2Set(Object[] input) {
		HashSet result = new HashSet();
		if (input == null || input.length == 0)
			return result;
		for (int i = 0; i < input.length; i++)
			result.add(input[i]);
		return result;
	}

	public static String ListToString(List input, String delimiter) {
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < input.size() - 2; i++)
			result.append(input.get(i)).append(delimiter);
		result.append(input.get(input.size() - 1));
		return result.toString();
	}

	public static String extractPath(String filename) {
		String[] parts = filename.split("/|\\\\");
		StringBuffer sb = new StringBuffer();
		for (int i=0; i<parts.length - 1; i++)
			sb.append(parts[i] + "/");
		return sb.toString();
	}

	public static String includeTrailingSlash(String path) {
		if(StringUtils.contains(path, "/") && !StringUtils.endsWith(path, "/"))
			return path + "/";
		else if(StringUtils.contains(path, "\\") && !StringUtils.endsWith(path, "\\"))
			return path + "\\";

		return path;
	}		

	public static String removeTrailingString(String source, String  trail) {
		if (source == null)
			return null;
		return source.replaceAll(trail + "$", "");
	}

	public static String removeTrailingSlash(String path) {
		if (path == null)
			return null;
		if (path.equals(""))
			return "";
		if (path.endsWith("/") || path.endsWith("\\"))
			return path.replaceAll("(/|\\\\)+$", "");
		else 
			return path + "/";
	}		

	/**
	 * deserializes a Map from a string. 
	 * expected format of parameters is: param1=value1;param2=value2
	 * @return a HashMap
	 * @throws ParseException 
	 */
	public static Map StringParamsToMap(String params) throws ParseException {
		HashMap map = new HashMap();
		if (params == null || params.trim().equals(""))
			return map;
		String[] paramsWithValues = params.split(";");
		for (int i = 0; i < paramsWithValues.length; i++) {
			String[] paramWithValue = paramsWithValues[i].split("=");
			Object value = paramWithValue[1];
			if (paramWithValue[1].equalsIgnoreCase("true"))
				value = Boolean.TRUE;
			if (paramWithValue[1].equalsIgnoreCase("false"))
				value = Boolean.FALSE;	
			if (paramWithValue[1].matches("\\d+"))
				value = Integer.valueOf(paramWithValue[1]);
			if (paramWithValue[1].matches("\\d+\\.\\d+"))
				value = new BigDecimal(paramWithValue[1]);
			if (paramWithValue[1].matches("\\d{1,2}/\\d{1,2}/\\d{4}"))
				value = new SimpleDateFormat("dd/MM/yyyy").parse(paramWithValue[1]);
			map.put(paramWithValue[0], value);
		}
		return map;
	}


	public static Date truncDate(Date date, String truncType) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		if (truncType.equalsIgnoreCase("dd")) {
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
		}
		return cal.getTime(); 
	}

	public static String getDirectory(String filename) {
		String[] parts = filename.split("/|\\\\");
		return filename.replaceAll(parts[parts.length - 1], "");
	}
	public static String getFilenameWithoutDir(String filename) {
		String[] parts = filename.split("/|\\\\");
		return parts[parts.length - 1];
	}	
	public static String getFilenameWithoutSuffix(String filename) {
		String[] parts = filename.split("\\.");
		if (parts.length == 0)
			return filename;		
		return filename.replaceAll("\\." + parts[parts.length - 1], "");		
	}
	public static String getFilenameSuffix(String filename) {
		String[] parts = filename.split("\\.");
		if (parts.length == 0)
			return "";
		return parts[parts.length - 1];		
	}
	public static String findUniqueFilename(String filename, boolean getNextId) {
		String dir = getDirectory(filename);
		filename = getFilenameWithoutDir(filename);
		String filenameWithoutSuffix = getFilenameWithoutSuffix(filename);
		String[] files = (new File(dir)).list();
		int i = 0, maxi = 0;
		for (String f: files) {
			String f1 = getFilenameWithoutSuffix(f); 
			if (f1.toLowerCase().startsWith(filenameWithoutSuffix.toLowerCase())) {
				if (f1.equalsIgnoreCase(filenameWithoutSuffix))
					i = 1;
				else {
					try {
						i = Integer.parseInt(f1.replaceAll(filenameWithoutSuffix, "").replaceAll("\\-", ""));
					} catch (Exception e) {
						continue;
					}
				}
				if (i > maxi)
					maxi = i;

			}
		}
		if (maxi == 0)
			return dir + filename;
		else {
			String si = Integer.toString(maxi + (getNextId ? 1 : 0));
			int l = si.length();
			for (i = 0; i < 4 - l; i++)
				si = "0" + si;
			return dir + filenameWithoutSuffix + "-" + si + "." + getFilenameSuffix(filename);
		}
	}

	public static long getLastModified(URL resource) {
		if (resource.getPath().indexOf("!")>0)
			return new File(resource.getPath().replaceAll("file:/(.*)!.*", "$1")).lastModified();
		else
			return new File(resource.getPath()).lastModified();
	}
	public static String getEncodingFromLocale(Locale locale) {
		return "ISO-8859-7";
	}

	public static Object[] toArray(Map map, Object[] result) {
		Iterator it = map.keySet().iterator();
		int i = 0;
		while (it.hasNext()) {
			result[i++] = map.get(it.next());
		}
		return result;
	}

	public static String BigDecimalToString(BigDecimal number, Locale locale) {
		MessageFormat fmt = new MessageFormat("{0,number,currency}", locale);
		return fmt.format(new Object[] {number});
	}
	
	public static Boolean ObjectToBoolean(Object obj) {
		if (obj == null)
			return null;
		else if (obj instanceof Boolean)
			return (Boolean) obj;
		else if (obj instanceof Integer) {
			if (Integer.valueOf(1).equals(obj))
				return Boolean.TRUE;
			else
				return Boolean.FALSE;
		} else if (obj instanceof BigDecimal) {
			if (BigDecimal.valueOf(1).equals(obj))
				return Boolean.TRUE;
			else
				return Boolean.FALSE;
		} else
			throw new RuntimeException("could not convert object [" + obj + "] to boolean");
		
	}

	public static String getStackTrace(Exception e) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		PrintStream print = new PrintStream(out);
		e.printStackTrace(print);
		return new String(out.toByteArray());
	}

	public static List addObjectToList(Object obj) {
		if (obj == null)
			return new ArrayList();
		ArrayList result = new ArrayList();
		result.add(obj);
		return result;
	}

	/**
	 * Wrap text at a certain line length. Default line break character is HTML <BR>
	 *
	 * @param string The String
	 * @param lineLength The position to create a line break
	 *
	 * @return String
	 */
	public static String wrap(String string, int lineLength) {
		return wrap(string, "<br>", lineLength);
	}

	public static String wrap(String string, String lineSeparator, int lineLength) {
		String[] words = string.split("\\s");
		String line = "";
		StringBuffer result = new StringBuffer();
		for (String word: words) {
			if (word.length() > lineLength) {
				result.append(word + lineSeparator);
				line = "";
			} else if ((line + word).length() > lineLength ) {
				result.append(line + " " + word + lineSeparator);
				line = "";    			
			} else
				line += " " + word;
		}
		result.append(line);
		return result.toString();
	}

	/**
	 * Wrap text at a certain line length.
	 *
	 * @param string The String
	 * @param lineSeparator Line break
	 * @param lineLength The position to create a line break
	 *
	 * @return String
	 */
	public static String wrap1(String string, String lineSeparator, int lineLength) {
		if (string == null || string.trim().equals("")) {
			return "";
		}        
		if (lineLength < 2) {
			return string;
		}

		final String DELIM = String.valueOf((char) Character.CONTROL);

		if (string.startsWith(DELIM) && string.endsWith(DELIM)) {
			return string;
		}

		StringBuffer sb = new StringBuffer();

		if (string.replaceAll(DELIM, "").length() > lineLength) {
			StringTokenizer st = new StringTokenizer(string, DELIM);
			int totalTokens = st.countTokens();
			int count = 0;
			boolean isDone = true;

			while (st.hasMoreTokens()) {
				String s = st.nextToken();
				count++;

				if (s.length() > lineLength) {
					sb.append(s.substring(0, lineLength));
					sb.append(DELIM);
					sb.append(s.substring(lineLength, s.length()));
					isDone = false;
				} else {
					sb.append(s);
				}

				if (count != totalTokens) {
					sb.append(DELIM);
				}
			}

			return (isDone) ? sb.toString().replaceAll(DELIM, lineSeparator)
					: wrap(sb.toString(), lineSeparator, lineLength);
		} else {
			return string.replaceAll(DELIM, lineSeparator);
		}
	}

	public static Date getFirstNotNull(Date date1, Date date2, Date date3) {
		if (date1 != null)
			return date1;
		else if (date2 != null)
			return date2;
		else if (date3 != null)
			return date3;
		else
			throw new RuntimeException("both dates are null");
	}
	public static String getFirstNotEmpty(String s1, String s2, String s3) {
		if (StringUtils.isNotBlank(s1))
			return s1;
		else if (StringUtils.isNotBlank(s2))
			return s2;
		else if (StringUtils.isNotBlank(s3))
			return s3;
		else
			throw new RuntimeException("both strings are empty");

	}    
	public static Object toDouble(Object o) {
		// this is a fix for the ugly problem with Oracle driver, Java 5, and BigDecimals 
		// (see http://www.javalobby.org/java/forums/t88158.html)
		if (o == null)
			return null;		
		else if (o instanceof BigDecimal)
			return Double.valueOf(((BigDecimal) o).doubleValue()); 
		else if (o instanceof Double)
			return o;
		else if (o instanceof Integer)
			return Double.valueOf(((Integer)o).intValue());
		else if (o instanceof Long)
			return Double.valueOf(((Long)o).longValue());		
		else if (o instanceof Float)
			return Double.valueOf(((Float) o).doubleValue());
		else
			return o;
	}
	
	public static Object toBigDecimal(Object o) {
		if (o == null)
			return null;
		else if (o instanceof BigDecimal)
			return o; 
		else if (o instanceof Double)
			return BigDecimal.valueOf(((Double) o).doubleValue());
		else if (o instanceof Integer)
			return BigDecimal.valueOf(((Integer) o).intValue());
		else if (o instanceof Long)
			return BigDecimal.valueOf(((Long) o).longValue());		
		else if (o instanceof Float)
			return BigDecimal.valueOf(((Float) o).floatValue());
		else
			return o;
	}
	
	public static boolean equals(Object o1, Object o2) {
		if (o1 == null && o2 != null)
			return false;
		if (o2 == null && o1 != null)
			return false;
		if (o1 == null && o2 == null)
			return true;
		if (java.sql.Timestamp.class.isAssignableFrom(o1.getClass()) || 
				java.sql.Timestamp.class.isAssignableFrom(o2.getClass())) {
			return ((Date) o1).getTime() == ((Date) o2).getTime();
		}
		o1 = toDouble(o1);
		o2 = toDouble(o2);
		return o1.equals(o2);
	}

	public static Object add(Object o1, Object o2) {	
		if (o1 == null && o2 == null)
			return  Double.valueOf(0);
		if (o1 == null)
			o1 = Double.valueOf(0);
		if (o2 == null)
			o2 = Double.valueOf(0);
		o1 = toDouble(o1);
		o2 = toDouble(o2);
		if (!(o1 instanceof Double) || !(o2 instanceof Double))
			throw new RuntimeException("objects could not be converted to Double");
		return Double.valueOf(((Double) o1).doubleValue() + ((Double) o2).doubleValue());
	}
	
	public static String repeatString(String s, int count) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < count; i++)
			sb.append(s);
		return sb.toString();
	}

	public static Map copyMap(Map map) {
		HashMap result = new HashMap();
		for (Object key: map.keySet()) {
			result.put(key, map.get(key));
		}
		return result;
	}	
	
	public static String replaceParamsInString(String value, Map params) {
		if (value == null)
			return null;
		if (params == null)
			return replaceVariablesInString(value);
		Iterator keys = params.keySet().iterator();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			Object replaceValue = params.get(key);
			if (replaceValue == null)
				replaceValue = "";
			value = value.replaceAll("(?i)\\#\\{" + key + "\\}",replaceValue.toString());
		}
		return replaceVariablesInString(value);
	}

	public static String replaceVariablesInString(String valueWithVariables) {
		if (valueWithVariables == null)
			return null;
		// find sysdate variable		
		String dateFormat = valueWithVariables.replaceAll(".*\\#\\{sysdate\\((.*)\\)\\}.*","$1");
		if (!dateFormat.equals("") && !dateFormat.equals(valueWithVariables))
			valueWithVariables = valueWithVariables.replaceAll("(.*)(\\$\\{sysdate\\(.*\\)\\}(.*))",
					"$1" + new SimpleDateFormat(dateFormat).format(new Date()) + "$3");
		valueWithVariables = valueWithVariables.replaceAll("\\#\\{defaultdir\\}", defaultDir.replaceAll("\\\\", "/"));		
		return valueWithVariables;		
	}

}
