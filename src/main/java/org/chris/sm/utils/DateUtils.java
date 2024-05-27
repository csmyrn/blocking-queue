package org.chris.sm.utils;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class DateUtils {
	private static final long ONE_HOUR = 60 * 60 * 1000L;	
	private static final long ONE_MINUTE = 60 * 1000L;
		
	public static Timestamp truncToDay(Timestamp dt) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(dt);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return new Timestamp(cal.getTime().getTime());
	}
	public static Timestamp truncToMonth(Timestamp timestamp) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(timestamp);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		return new Timestamp(cal.getTime().getTime());
	}	
	public static Timestamp addDays(Timestamp dt, int days, boolean truncTimestamp) {
		Calendar cal = Calendar.getInstance();
		if (truncTimestamp)
			dt = truncToDay(dt);
		cal.setTime(dt);		
		cal.add(Calendar.DAY_OF_MONTH, days);
		return new Timestamp(cal.getTime().getTime());		
	}
	/**
	 * One hour (known as the "fudge" factor) is added to the 2 Timestamps passed as parameters to take in account the possible DLS (Day Light Saving) one hour missing. 
	 * The "right" way would be to compute the julian day number of both Timestamps and then do the substraction.
	 * Timestamps are truncated to midnight. 
	 */	
	public static long daysBetween(Timestamp smallerd1, Timestamp biggerd2){
		Timestamp d1 = truncToDay(smallerd1);
		Timestamp d2 = truncToDay(biggerd2);
		if (d1.compareTo(d2) > 0)
			throw new RuntimeException("smaller Timestamp [" + d1 + "] is larger than bigger Timestamp [" + d2 + "]");		
		return Math.abs(( (d2.getTime() - d1.getTime() + ONE_HOUR) / 
				(ONE_HOUR * 24)));
	}   

	public static long minutesBetween(Timestamp smallerd1, Timestamp biggerd2) {
		Timestamp d1 = smallerd1, d2 = biggerd2;
		if (d1.compareTo(d2) > 0)
			throw new RuntimeException("smaller Timestamp [" + d1 + "] is larger than bigger Timestamp [" + d2 + "]");		
		return  (d2.getTime() - d1.getTime()) / ONE_MINUTE; 		
	}

	public static Timestamp addSeconds(Timestamp Timestamp, int secondsToAdd) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(Timestamp);
		cal.set(Calendar.SECOND, cal.get(Calendar.SECOND) + secondsToAdd);
		return new Timestamp(cal.getTime().getTime());
	}

	public static String DateToString(Timestamp dt) {
		SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
		return df.format(dt);
	}


	public static Timestamp addMonths(Timestamp dt, int months, boolean truncMonth) {
		if (truncMonth)
			dt = DateUtils.firstDayOfMonth(dt);
		Calendar cal = Calendar.getInstance();
		cal.setTime(dt);
		cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) + months);
		return new Timestamp(cal.getTime().getTime());
	}

	public static Timestamp firstDayOfMonth(Timestamp dt) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(dt);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		return truncToDay(new Timestamp(cal.getTime().getTime()));
	}

	public static Timestamp lastDayOfMonth(Timestamp dt) {          
		return truncToDay(addDays(firstDayOfMonth(addMonths(dt,1, true)), -1, false));
	}

	public static Timestamp StringToDate(String dt) {
		SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
		try {
			return new Timestamp(df.parse(dt).getTime());
		} catch (ParseException e) {
			throw new RuntimeException(e.getMessage());
		}
	}  

	public static boolean isSameMonth(Timestamp dt1, Timestamp dt2) {
		return truncToMonth(dt1).equals(truncToMonth(dt2));
	}

	public static boolean isSameDay(Timestamp dt1, Timestamp dt2) {
		return truncToDay(dt1).equals(truncToDay(dt2));
	}

	public static Timestamp toTimestamp(Object dt) {
		SimpleDateFormat df1 = new SimpleDateFormat("yyyyMMdd");
		if (dt instanceof Timestamp)
			return (Timestamp) dt;
		else if (dt instanceof java.util.Date)
			return new Timestamp(((java.util.Date) dt).getTime());
		else if (dt instanceof java.sql.Date)
			return new Timestamp(((java.sql.Date) dt).getTime());
		else {
			String s = dt.toString();
			if (s.length() != 8)
				throw new RuntimeException("could not convert [" + dt.toString() + "] to Timestamp");
			try {
				return new Timestamp(df1.parse(s).getTime());
			} catch (ParseException e) {
				throw new RuntimeException(e);
			}	  
		}
	}

}
