package step4.discretize;

import allen.discretizate.csv.Discretize;

// input: weekly log file: [stuid, week, label, libgate, libweb,
// workstation, roombooking, DUMMY_LABEL]
public class DiscretizeLog {
	static String WORK_DIR = "C:/Users/allen/Desktop/2016_09_12_Student_Behavior/";
	static String WEEKLOG = WORK_DIR + "1_weeklogs.csv";

	public static void main(String[] args) throws Exception {
		int discardNum = 3;
		int binNum = 3;
		// String orgFtrsCSV = WEEKLOG;
		String disFtrsCSV = WEEKLOG + ".disc.csv";
		String pointsCSV = WEEKLOG + ".points.B=" + binNum + ".csv";

		String options = "-i " + WEEKLOG + " -o " + disFtrsCSV + " -p " + pointsCSV + " -D " + discardNum + " -B "
				+ binNum;
		Discretize module = new Discretize();
		module.debug(true);
		module.addOptions(options.split(" "));
		module.start();
		module.join();
		System.out.println("\n\n" + module.description());
	}
}
