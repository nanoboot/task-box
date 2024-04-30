/*
 *    Copyright (C) 2008 Igor Kriznar Copyright (C) 2024 Robert Vokac
 *    
 *    This file is part of Task-Box.
 *    
 *    Task-Box is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *    
 *    Task-Box is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *    
 *    You should have received a copy of the GNU General Public License
 *    along with Task-Box.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.nanoboot.forks.org.taskbox.journal;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * @author ikesan
 *
 */
public final class JournalTools {
	
	public static final long MILLIS_IN_DAY= 1000*60*60*24;
	public static final long MILLIS_IN_HOUR= 1000*60*60;
	
	private JournalTools() {
	}
	
	public static long today() {
		long today= System.currentTimeMillis();
		today/=MILLIS_IN_DAY;
		return today;
	}
	
	public static long toDay(long time) {
		return time/MILLIS_IN_DAY;
	}
	
	public static Date toDate(long days) {
		return new Date(days*MILLIS_IN_DAY);
	}

	public static int minutesOfDay() {
		GregorianCalendar g= new GregorianCalendar();
		return g.get(Calendar.HOUR_OF_DAY)*60+g.get(Calendar.MINUTE);
	}
	
}
