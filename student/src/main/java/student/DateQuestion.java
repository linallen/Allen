package student;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.joda.time.DateTime;

public class DateQuestion {

	public static List<DateTime> getDateRange(DateTime start, DateTime end) {
		List<DateTime> ret = new ArrayList<DateTime>();
		DateTime tmp = start;
		while (tmp.isBefore(end) || tmp.equals(end)) {
			ret.add(tmp);
			tmp = tmp.plusDays(1);
		}
		return ret;
	}

	public static void main(String[] args) {
		DateTime start = DateTime.parse("2015-3-2");
		DateTime end = DateTime.parse("2015-5-29");

		Random r = new Random();

		List<DateTime> between = getDateRange(start, end);
		for (DateTime d : between) {
			d = d.plusMinutes(r.nextInt(1000));
			d = d.plusSeconds(r.nextInt(1000));
			System.out.println(dataTimeStr(d));
		}
	}

	/** 2013-04-01 6:38:20 AM */
	public static String dataTimeStr(DateTime dateTime) {
		String buf = new String();
		buf += dateTime.getYear() + "-" + dateTime.getMonthOfYear() + "-" + dateTime.getDayOfMonth() + " ";
		buf += dateTime.getHourOfDay() + ":" + dateTime.getMinuteOfHour() + ":" + dateTime.getSecondOfMinute();
		return buf;
	}
}