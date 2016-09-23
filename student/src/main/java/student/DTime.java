package student;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.joda.time.DateTime;

public class DTime {
	/** return a list of data_times[] */
	public static List<DateTime> getBetween(DateTime start, DateTime end) {
		List<DateTime> ret = new ArrayList<DateTime>();
		DateTime tmp = start;
		while (tmp.isBefore(end) || tmp.equals(end)) {
			ret.add(tmp);
			tmp = tmp.plusDays(1);
		}
		return ret;
	}

	/** return a list of data_times[] */
	public static List<DateTime> getBetween(String start, String end) {
		return getBetween(DateTime.parse(start), DateTime.parse(end));
	}

	/** return date_time text, e.g., "2013-4-1 18:3:21" */
	public static String text(DateTime dateTime) {
		String buf = new String();
		buf += dateTime.getYear() + "-" + dateTime.getMonthOfYear() + "-" + dateTime.getDayOfMonth() + " ";
		buf += dateTime.getHourOfDay() + ":" + dateTime.getMinuteOfHour() + ":" + dateTime.getSecondOfMinute();
		return buf;
	}

	/** return a random time for a given date */
	public static DateTime randomTime(DateTime dateTime) {
		// dateTime's hour, minute, and second properties must be 0s.
		Random r = new Random();
		dateTime = dateTime.plusHours(7 + r.nextInt(13)); // 7:00 -20:00
		dateTime = dateTime.plusMinutes(r.nextInt(60));
		dateTime = dateTime.plusSeconds(r.nextInt(60));
		return dateTime;
	}

	/** Test */
	public static void main(String[] args) {
		List<DateTime> between = getBetween("2015-3-2", "2015-5-29");
		for (DateTime d : between) {
			System.out.println(text(randomTime(d)));
		}
	}
}