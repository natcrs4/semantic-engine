package com.crs4.sem.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


public enum Resolution {
	/** Limit a date's resolution to year granularity. */
    YEAR(4), 
    /** Limit a date's resolution to month granularity. */
    MONTH(6), 
    /** Limit a date's resolution to day granularity. */
    DAY(8), 
    /** Limit a date's resolution to hour granularity. */
    HOUR(10);

    final int formatLen;
    public SimpleDateFormat format;//should be cloned before use, since it's not threadsafe

    Resolution(int formatLen) {
      this.formatLen = formatLen;
      // formatLen 10's place:                     11111111
      // formatLen  1's place:            12345678901234567
      this.format = new SimpleDateFormat("yyyyMMddHHmmssSSS".substring(0,formatLen),Locale.ROOT);
      this.format.setTimeZone(TimeZone.getDefault());
    }

    public String format(Date date) {
    	return format.format(date);
    }
    /** this method returns the name of the resolution
     * in lowercase (for backwards compatibility) */
    @Override
    public String toString() {
      return super.toString().toLowerCase(Locale.ROOT);
    }

  

}
