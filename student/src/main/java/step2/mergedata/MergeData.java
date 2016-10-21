package step2.mergedata;

import java.util.HashMap;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import allen.base.common.AAI_IO;
import allen.base.common.Common;

/**
 * Step 2: merge student behaviour data.
 * 
 * @author Allen Lin, 10 Oct 2016
 */
public class MergeData {
	static String WORK_DIR = "C:/Users/allen/Desktop/2016_09_12 Student Behavior/";
	static String LIBGATE = WORK_DIR + "libgate.csv";
	static String LIBWEB = WORK_DIR + "libweb.csv";
	static String WORKSTATION = WORK_DIR + "workstation.csv";
	static String ROOMBOOKING = WORK_DIR + "roombooking.csv";
	static String EMOTION = WORK_DIR + "emotion.csv";

	static String[] behaviours = { "libgate", "libweb", "workstation", "roombooking" };
	static HashMap<String, WeekLog> weekLogs = new HashMap<String, WeekLog>();

	public static void main(String[] args) {
		// input data format: [stuid, label, datetime]
		// output format: [stuid+week+label, week_log]
		for (String behaviour : behaviours) {
			updateWeekLogs(behaviour);
		}
		// save weeklogs to file
		saveWeekLogs(WORK_DIR + "weeklogs.csv");
	}

	static void saveWeekLogs(String file) {
		String buf = "stuid, week, label";
		for (String behaviour : behaviours) {
			buf += ", " + behaviour;
		}
		for (String key : weekLogs.keySet()) {
			WeekLog weekLog = weekLogs.get(key);
			// key = stuid + "_" + week + "_" + label;
			String log = key.replace("_", ",");
			for (String behaviour : behaviours) {
				Integer freq = weekLog.behaviourFreq.get(behaviour);
				freq = (freq == null) ? 0 : freq;
				log += "," + freq;
			}
			buf += "\n" + log;
		}
		AAI_IO.saveFile(file, buf);
	}

	// update week logs with behavior data
	static void updateWeekLogs(String behaviour) {
		// String file = WORK_DIR + behaviour + ".csv";
		String buf = AAI_IO.readFile(WORK_DIR + behaviour + ".csv");
		buf = buf.replaceAll("\r", "");
		String[] logs = buf.split("\n");
		for (int i = 1; i < logs.length; i++) {
			String items[] = logs[i].split(",");
			Common.Assert(items.length >= 3);
			// input data format: items[] = {stuid, label, datetime}
			String stuid = items[0];
			String label = items[1];
			String dateTimeStr = items[2];
			// get week from dateTime
			dateTimeStr = dateTimeStr.replace("  ", " ");
			String[] parts = dateTimeStr.split(" ");
			Integer week = getWeek(parts[0]);
			// update weeklog[]
			String key = stuid + "_" + week + "_" + label;
			updateWeekLog(key, behaviour);
		}
	}

	static void updateWeekLog(String key, String behaviour) {
		WeekLog weeklog = weekLogs.get(key);
		if (weeklog == null) {
			weeklog = new WeekLog();
			weekLogs.put(key, weeklog);
		}
		Integer behaviourFreq = weeklog.behaviourFreq.get(behaviour);
		if (behaviourFreq == null) {
			behaviourFreq = 0;
		}
		weeklog.behaviourFreq.put(behaviour, behaviourFreq + 1);
	}

	// 2015-03-17 4:56:26 PM
	static Integer getWeek(String str) {
		String pattern = "yyyy-MM-dd";
		DateTime dateTime = DateTime.parse(str, DateTimeFormat.forPattern(pattern));
		return dateTime.getWeekOfWeekyear();
	}
}