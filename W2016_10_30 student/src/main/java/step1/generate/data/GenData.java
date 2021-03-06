package step1.generate.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.joda.time.DateTime;

import allen.base.common.AAI_IO;
import step1.distribution.DistExpStudent;
import step1.distribution.DistNorm;
import step1.distribution.Distribution;

/**
 * Generate synthetic student behavior data from pre-assumption of behavior
 * distribution of Good and Bad students.<br>
 * Autumn session: Monday 2 Mar - Friday 29 May 2016 (13 weeks)
 * 
 * @author Allen Lin, 20 Sep 2016
 */
public class GenData {
	static String WORK_DIR = "C:/Users/allen/Desktop/2016_09_12_Student_Behavior/";
	static String STUDENT = WORK_DIR + "_student_100.csv";
	static String LIBGATE = WORK_DIR + "libgate.csv";
	static String LIBWEB = WORK_DIR + "libweb.csv";
	static String WORKSTATION = WORK_DIR + "workstation.csv";
	static String ROOMBOOKING = WORK_DIR + "roombooking.csv";
	static String EMOTION = WORK_DIR + "emotion.csv";

	static String start = "2015-3-2";
	static String end = "2015-5-29";
	static List<DateTime> between = DTime.getBetween(start, end);
	static List<Object> paras = null;
	static double pertMean = 0.1;
	static double pertSd = 0.05;
	static double startWeek = 1;
	static double endWeek = (between.size() / 7 + 1) + 0.;
	static Random r = new Random();

	public static void main(String[] args) {
		// 0. read in student IDs delimited with space
		ArrayList<Student> students = new ArrayList<Student>();
		readStuIds(STUDENT, students);

		// 1. "libgate": [stuId, enter_time[, gate, record]]
		AAI_IO.saveFile(LIBGATE, "stuId, label, enter_time, week_of_year, day_of_week, dist_dbg, gate, record");
		// 2. "libweb": [stuId, log_time, ip, session, ...]
		AAI_IO.saveFile(LIBWEB, "stuId, label, log_time, week_of_year, day_of_week, dist_dbg, ip, log_browser");
		// 3. "workstation": [stuId, connect_time, [disconnect_time, duration]]
		AAI_IO.saveFile(WORKSTATION,
				"stuId, label, connect_time, disconnect_time, duration, week_of_year, day_of_week, dist_dbg, total_traffic (MB), vendor");
		// 4. "roombooking": [stuId, create_time, start_time, end_time, ...]
		AAI_IO.saveFile(ROOMBOOKING,
				"stuId, label, create_time, week_of_year, day_of_week, dist_dbg, start_time, end_time");
		// 5. "emotion": [stuId, date, emotion(happy/sad)]
		AAI_IO.saveFile(EMOTION, "stuId, label, date, emotion, week_of_year, day_of_week, dist_dbg");

		Distribution distNorm = new DistNorm();
		Distribution distExpStudent = new DistExpStudent();
		for (int i = 0; i < students.size(); i++) {
			Student student = students.get(i);
			if (student.label.equalsIgnoreCase("ONE")) {
				// 1. "libgate": [stuId, enter_time[, gate, record]]
				distNorm.setParas(0.7, 0.1);
				distExpStudent.setParas(0.2, pertSd, startWeek, endWeek);
				genLogs(LIBGATE, student, distNorm, distExpStudent, 0.6);

				// 2. "libweb": [stuId, log_time, ip, session, ...]
				distNorm.setParas(0.7, 0.1);
				genLogs(LIBWEB, student, distNorm, distExpStudent, 1.);

				// 3. "workstation": [stuId, connect_time, disconnect_time,
				// duration]
				distNorm.setParas(0.7, 0.1);
				genLogsWorkstation(WORKSTATION, student, distNorm, distExpStudent, 1.);

				// 4. "roombooking": [stuId, create_time, start_time, end_time]
				distNorm.setParas(0.7, 0.1);
				distExpStudent.setParas(0.1, pertSd, startWeek, endWeek);
				genLogs(ROOMBOOKING, student, distNorm, distExpStudent, 0.6);

				// 5. "emotion": [stuId, date, emotion(happy/sad)]
				distNorm.setParas(0.7, 0.1);
				genLogsEmotion(EMOTION, student, distNorm);
			} else { // ZERO
				distNorm.setParas(0.1, 0.05);
				genLogs(LIBGATE, student, distNorm, null, 1.);
				genLogs(LIBWEB, student, distNorm, null, 1.);
				genLogsWorkstation(WORKSTATION, student, distNorm, null, 1.);
				distExpStudent.setParas(0.1, pertSd, startWeek, endWeek);
				genLogs(ROOMBOOKING, student, distNorm, distExpStudent, 0.6);
				// 5. "emotion": [stuId, date, emotion(happy/sad)]
				distNorm.setParas(0.3, 0.1);
				genLogsEmotion(EMOTION, student, distNorm);
			}
		}
		System.out.println("All done.");
	}

	/**
	 * emotion logs: [stuId, date, emotion(happy/sad)]
	 */
	private static void genLogsEmotion(String logCSV, Student student, Distribution distNorm) {
		String bufCSV = new String(), log;
		for (int i = 0; i < between.size(); i++) {
			DateTime dateTime = DTime.randomTime(between.get(i));
			int dayOfWeek = dateTime.getDayOfWeek();
			if (dayOfWeek <= 5) { // skip Sat and Sun
				log = null;
				if (r.nextBoolean()) { // 50% sparse rate
					if (distNorm.hit()) {
						log = student.stuId + "," + student.label + "," + DTime.text(dateTime) + ",happy";
					} else {
						log = student.stuId + "," + student.label + "," + DTime.text(dateTime) + ",sad";
					}
				}
				if (log != null) {
					bufCSV += "\n" + log;
					bufCSV += "," + dateTime.getWeekOfWeekyear();
					bufCSV += "," + dateTime.getDayOfWeek();
					bufCSV += "," + "Norm";
				}
			}
		}
		AAI_IO.saveFile(logCSV, bufCSV, true);
	}

	/**
	 * workstation logs: [stuId, connect_time, disconnect_time, duration, ...]
	 */
	private static void genLogsWorkstation(String logCSV, Student student, Distribution distNorm,
			Distribution distExpStudent, double pNormal) {
		String bufCSV = new String(), log;
		boolean isNormDist = (distExpStudent == null) || Distribution.hitUniform(pNormal);
		for (int i = 0; i < between.size(); i++) {
			DateTime dateTime = DTime.randomTime(between.get(i));
			int dayOfWeek = dateTime.getDayOfWeek();
			if (dayOfWeek <= 5) { // skip Sat and Sun
				if (isNormDist) {
					log = genLog(student, dateTime, distNorm);
				} else {
					log = genLog(student, dateTime, distExpStudent, i / 7. + 1);
				}
				if (log != null) {
					bufCSV += "\n" + log;
					int durMinutes = 60 + r.nextInt(120); // seconds
					DateTime disconnect_time = dateTime.plusMinutes(durMinutes);
					bufCSV += "," + DTime.text(disconnect_time);
					bufCSV += "," + getDurationText(durMinutes);
					bufCSV += "," + dateTime.getWeekOfWeekyear();
					bufCSV += "," + dateTime.getDayOfWeek();
					bufCSV += "," + (isNormDist ? "Norm" : "Exp");
					// add [Total Traffic (MB), Vendor]
					Double totalTrafic = 0.1 + r.nextDouble() * 100;
					String vendors[] = { "Apple", "Intel", "Hon Hai", "Legend", "Unknown" };
					bufCSV += "," + totalTrafic + "," + vendors[r.nextInt(vendors.length)];
				}
			}
		}
		AAI_IO.saveFile(logCSV, bufCSV, true);
	}

	private static String getDurationText(int durMinutes) {
		String buf = new String();
		int days = durMinutes / 60 / 24;
		int remainMinutes = durMinutes - days * 60 * 24;
		int hours = remainMinutes / 60;
		int mintues = remainMinutes - hours * 60;
		buf += (days > 0) ? (days + " days ") : "";
		buf += (hours > 0) ? (hours + " hours ") : "";
		buf += (mintues > 0) ? (mintues + " mintues") : "";
		return buf.trim();
	}

	/**
	 * @param pNormal
	 *            P(student dist is Normal), other Exponential
	 */
	private static void genLogs(String logCSV, Student student, Distribution distNorm, Distribution distExpStudent,
			double pNormal) {
		logCSV = logCSV.toLowerCase();
		String bufCSV = new String(), log;
		boolean isNormDist = Distribution.hitUniform(pNormal);
		// Added by Allen, 20 Oct 2016, libgate and libweb may have multiple
		// logs in a day
		// TODO
		int dailyTimesMax = 1; // default {0, 1}
		if (logCSV.endsWith("libgate.csv")) {
			dailyTimesMax = r.nextInt(5 + 1); // {0, 1, ..., 5}
		} else if (logCSV.endsWith("libweb.csv")) {
			dailyTimesMax = r.nextInt(50 + 1);// {0, 1, ..., 50}
		}
		// Added by Allen, 20 Oct 2016
		for (int i = 0; i < between.size(); i++) {
			for (int dailyTimes = 0; dailyTimes < dailyTimesMax; dailyTimes++) {
				DateTime dateTime = DTime.randomTime(between.get(i));
				int dayOfWeek = dateTime.getDayOfWeek();
				if (dayOfWeek <= 5) { // skip Sat and Sun
					if (isNormDist) {
						log = genLog(student, dateTime, distNorm);
					} else {
						log = genLog(student, dateTime, distExpStudent, i / 7. + 1);
					}
					if (log != null) {
						bufCSV += "\n" + log;
						bufCSV += "," + dateTime.getWeekOfWeekyear();
						bufCSV += "," + dateTime.getDayOfWeek();
						bufCSV += "," + (isNormDist ? "Norm" : "Exp");
						// append additional columns
						if (logCSV.endsWith("libgate.csv")) {
							// gate, record
							int gate_no = (r.nextInt(5) + 1);
							String gate = "LIBRARY GATE " + gate_no;
							String record = "Student " + student.stuId + " Access in Gate " + gate_no;
							bufCSV += "," + gate + "," + record;
						} else if (logCSV.endsWith("libweb.csv")) {
							// ip, log_browser
							String ip = "203.166." + (r.nextInt(250) + 1) + "." + (r.nextInt(250) + 1);
							String browsers[] = { "Mozilla/5.0", "Chrome 53.0.2785 (64-bit)", "Internet Explorer",
									"Others" };
							String log_browser = browsers[r.nextInt(browsers.length)];
							bufCSV += "," + ip + "," + log_browser;
						} else if (logCSV.endsWith("roombooking.csv")) {
							// start_time, end_time
							DateTime start_time = dateTime.plusHours(12 + r.nextInt(200));
							DateTime end_time = start_time.plusHours(1 + r.nextInt(3));
							bufCSV += "," + DTime.text(start_time) + "," + DTime.text(end_time);
						}
					}
				}
			}
		}
		AAI_IO.saveFile(logCSV, bufCSV, true);
	}

	/** generate logs <stuId, time, ...> according to a given distribution */
	public static String genLog(Student student, DateTime dateTime, Distribution dist, Object... paras) {
		// dateTime = DTime.randomTime(dateTime); // randomize time
		if (dist.hit(paras)) {
			return student.stuId + "," + student.label + "," + DTime.text(dateTime);
		}
		return null;
	}

	/** read in Students <stuId, label(zero/one)> */
	private static void readStuIds(String fileName, ArrayList<Student> students) {
		String buf = AAI_IO.readFile(fileName);
		String records[] = buf.split("\r\n");
		for (String record : records) {
			String items[] = record.split(",");
			Student student = new Student();
			student.stuId = items[0];
			student.label = items[1];
			students.add(student);
		}
	}

	static class Student {
		String stuId;
		String label;

		public String toString() {
			return "stuId = " + stuId + ", label = " + label;
		}
	}
}