package org.chris.sm.utils;

import java.sql.Timestamp;

/**
 * A class for getting current system date. It is obvious that we don't really need this
 * class. The purpose of this class is to simulate various system dates when running unit tests.
 * @author lprotopapas
 *
 */
public class SystemDate {
	private static Timestamp curDate;
	public static Timestamp get() {
		if (curDate == null)
			return DateUtils.truncToDay(new Timestamp(new java.util.Date().getTime()));
		else
			return DateUtils.truncToDay(curDate);
	}
	public static Timestamp getWithDetail() {
		if (curDate == null)
			return new Timestamp(new java.util.Date().getTime());
		else
			return curDate;
	}
	
	public static void setSystemDate(Timestamp dt) {
		curDate = dt;
	}
}
