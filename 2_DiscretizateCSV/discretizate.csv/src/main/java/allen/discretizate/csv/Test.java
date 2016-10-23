package allen.discretizate.csv;

public class Test {
	/**
	 * parameters: unsupervised[0] or supervised[bin number>0] + inCSV + outCSV
	 */
	public static void main(String[] argv) throws Exception {
		String DATA_DIR = "C:/Allen/UTS/UTS_SourceCode/2_DiscretizateCSV/_data/";
		int discardNum = 1;
		int binNum = 3;
		String orgFtrsCSV = DATA_DIR + "1_input.csv";
		String disFtrsCSV = orgFtrsCSV + ".discretized.B=" + binNum + ".csv";
		String pointsCSV = orgFtrsCSV + ".points.B=" + binNum + ".csv";

		String options = "-i " + orgFtrsCSV + " -o " + disFtrsCSV + " -p " + pointsCSV + " -D " + discardNum + " -B "
				+ binNum;
		// Discretize.main(options.split(" "));

		Discretize module = new Discretize();
		module.debug(true);
		module.addOptions(options.split(" "));
		module.start();
		module.join();
		System.out.println("\n\n" + module.description());
	}
}