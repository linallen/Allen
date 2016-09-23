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
public class DataGenerate {
	static String WORK_DIR = "C:/Users/allen/Desktop/2016_09_12 Student Behavior/";
	static String STUDENT = WORK_DIR + "student_test.csv";
	static String LIBGATE = WORK_DIR + "libgate.txt";
	static String LIBWEB = WORK_DIR + "libweb.txt";
	static String WORKSTATION = WORK_DIR + "workstation.txt";
	static String ROOMBOOKING = WORK_DIR + "roombooking.txt";
	static String EMOTION = WORK_DIR + "emotion.txt";

	static double pertMean = 0.1;
	static double pertSd = 0.05;
	static double startWeek = 1;
	static double endWeek = 13;

	public static void main(String[] args) {
		String start = "2015-3-2";
		String end = "2015-5-29";
		List<DateTime> between = DTime.getBetween(start, end);
		List<Object> paras = null;

		// 0. read in student IDs delimited with space
		ArrayList<Student> students = new ArrayList<Student>();
		readStuIds(STUDENT, students);
		// 1. "libgate": <stuId, enter_time, gate, record>
		Distribution distNorm = new DistNorm(0.7, 0.1);
		Distribution distExpStudent = new DistExpStudent(pertMean, pertSd, startWeek, endWeek);
		for (Student student : students) {
			if (Distribution.hitUniform(0.6)) {
				GenLibgate.genCSV(between, student.stuId, distNorm, paras);
			} else {
				GenLibgate.genCSV(between, student.stuId, distExpStudent, paras);
			}
		}
		// 2. "libweb": <stuId, ip, session, log_time?
		// 3. "workstation": <stuId, connect_time, disconnect_time, duration>
		// 4. "roombooking": <stuId, bid, start_time, end_time, create_time>
		// 5. "emotion": <stuId, date, emotion(happy/sad)>
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
	}
}