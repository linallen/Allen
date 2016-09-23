package student;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import allen.base.common.AAI_IO;
import distribution.DistExpStudent;
import distribution.DistNorm;
import distribution.Distribution;

/**
 * Generate synthetic student behavior data from pre-assumption of behavior
 * distribution of Good and Bad students.<br>
 * Autumn session: Monday 2 Mar - Friday 29 May 2016 (13 weeks)
 * 
 * @author Allen Lin, 20 Sep 2016
 */
public class GenData_old {
	static String WORK_DIR = "C:/Users/allen/Desktop/2016_09_12 Student Behavior/";
	static String STUDENT = WORK_DIR + "student_test.csv";
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

	public static void main(String[] args) {
		// 0. read in student IDs delimited with space
		String bufCSV = new String(), log;
		Distribution distNorm = new DistNorm(0.7, 0.1);
		Distribution distExpStudent = new DistExpStudent();
		ArrayList<Student> students = new ArrayList<Student>();
		readStuIds(STUDENT, students);
		// 1. "libgate": <stuId, enter_time[, gate, record]>
		distExpStudent.setParas(0.2, pertSd, startWeek, endWeek);
		bufCSV = "stuId, enter_time, dist, week_of_year, day_of_week";
		for (Student student : students) {
			boolean isNormDist = Distribution.hitUniform(0.6);
			for (int i = 0; i < between.size(); i++) {
				DateTime dateTime = DTime.randomTime(between.get(i));
				int dayOfWeek = dateTime.getDayOfWeek();
				if (dayOfWeek <= 5) { // skip Sat and Sun
					if (isNormDist) {
						log = genLog(student.stuId, dateTime, distNorm);
					} else {
						log = genLog(student.stuId, dateTime, distExpStudent, i / 7. + 1);
					}
					if (log != null) {
						bufCSV += "\n" + log + "," + (isNormDist ? "Norm" : "Exp");
						bufCSV += "," + dateTime.getWeekOfWeekyear();
						bufCSV += "," + dateTime.getDayOfWeek();
					}
				}
			}
		}
		AAI_IO.saveFile(LIBGATE, bufCSV);
		// 2. "libweb": <stuId, ip, session, log_time?
		// 3. "workstation": <stuId, connect_time, disconnect_time, duration>
		// 4. "roombooking": <stuId, bid, start_time, end_time, create_time>
		// 5. "emotion": <stuId, date, emotion(happy/sad)>
		System.out.println("All done.");
	}

	private static void genLogs(String title, ArrayList<Student> students, Distribution distNorm,
			Distribution distExpStudent) {
		distExpStudent.setParas(0.2, pertSd, startWeek, endWeek);
		String bufCSV = title, log;
		for (Student student : students) {
			boolean isNormDist = Distribution.hitUniform(0.6);
			for (int i = 0; i < between.size(); i++) {
				DateTime dateTime = DTime.randomTime(between.get(i));
				int dayOfWeek = dateTime.getDayOfWeek();
				if (dayOfWeek <= 5) { // skip Sat and Sun
					if (isNormDist) {
						log = genLog(student.stuId, dateTime, distNorm);
					} else {
						log = genLog(student.stuId, dateTime, distExpStudent, i / 7. + 1);
					}
					if (log != null) {
						bufCSV += "\n" + log + "," + (isNormDist ? "Norm" : "Exp");
						bufCSV += "," + dateTime.getWeekOfWeekyear();
						bufCSV += "," + dateTime.getDayOfWeek();
					}
				}
			}
		}
		AAI_IO.saveFile(LIBGATE, bufCSV);
	}

	/** generate logs <stuId, time, ...> according to a given distribution */
	public static String genLog(String stuId, DateTime dateTime, Distribution dist, Object... paras) {
		// dateTime = DTime.randomTime(dateTime); // randomize time
		if (dist.hit(paras)) {
			return stuId + "," + DTime.text(dateTime);
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