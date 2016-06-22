package allen.clusterer.alg.spectral.jian;

public class Test {
	///////////////////////////////////////////////////////////////////
	// data sets
	// private static String DATA_NAME = "balloons"; // CMS same with Jian
	// private static String DATA_NAME = "zoo"; // CMS same with Jian
	// private static String DATA_NAME = "test"; // CMS same with Jian
	private static String DATA_NAME = "shuttle"; // CMS same with Jian
	// private static String DATA_NAME = "soybean-s"; // CMS same with Jian
	///////////////////////////////////////////////////////////////////
	private static String WORK_DIR = "../Datasets/";
	private static String m_inputArff = WORK_DIR + DATA_NAME + "/" + DATA_NAME + ".arff";
	private static String m_simName = "CMS_INTRA";

	private static void TestRound(String simName, int round) throws Exception {
		for (int i = 0; i < round; i++) {
			SpecClusterJian module = new SpecClusterJian();
			String options = " -i " + m_inputArff + " -s " + simName + "" + " -debug ";
			module.addOptions(options.split(" "));
			module.start();
			module.join();
		}
		System.out.println("All done\n");
	}

	// -i input_arff -s sim_name [-k top_k] [-r] [-o output_file]
	public static void main(String[] args) throws Exception {
		TestRound(m_simName, 50);
		// SpecClusterJian module = new SpecClusterJian();
		// String options = " -i " + m_inputArff + " -s " + m_simName + "" + "
		// -debug ";
		// module.addOptions(options.split(" "));
		// module.start();
		// module.join();
		// System.out.println("\n" + module.description());
	}
}