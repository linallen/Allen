package student;

/**
 * Generate synthetic student behavior data from pre-assumption of behavior
 * distribution of Good and Bad students.
 * 
 * @author Allen Lin, 20 Sep 2016
 */
public class DataGenerate {
	static String WORK_DIR = "C:/Users/allen/Desktop/2016_09_12 Student Behavior/";
	static String STUDENT = WORK_DIR + "student.txt";
	static String LIBGATE = WORK_DIR + "libgate.txt";
	static String LIBWEB = WORK_DIR + "libweb.txt";
	static String WORKSTATION = WORK_DIR + "workstation.txt";
	static String ROOMBOOKING = WORK_DIR + "roombooking.txt";
	static String EMOTION = WORK_DIR + "emotion.txt";

	public static void main(String[] args) {
		// 0. read in student IDs delimited with space
		String[] stuIds = readStuIds(STUDENT);
		// 1. "libgate": <stuId, gate, enter_time>
		// 2. "libweb": <stuId, ip, session, log_time?
		// 3. "workstation": <stuId, connect_time, disconnect_time, duration>
		// 4. "roombooking": <stuId, bid, start_time, end_time, create_time>
		// 5. "emotion": <stuId, date, emotion(happy/sad)>
	}

	private static String[] readStuIds(String file) {
		return null;
	}
}