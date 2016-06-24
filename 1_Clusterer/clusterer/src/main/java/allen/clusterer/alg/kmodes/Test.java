package allen.clusterer.alg.kmodes;

public class Test {
	static String WORK_DIR = "C:/Allen/UTS/UTS_SourceCode/2_CoupledSimilarity/_experiments/datasets/";
	static String DATA_SET = "example1";
	static String m_inputFile = WORK_DIR + DATA_SET + "/" + DATA_SET + ".data";

	public static void main(String[] args) throws Exception {
		// -i input_file [-k k] -s sim_name [-o output_file]
		// SMD, OFD, ADD, COS, INTRA, INTER
		String m_simName = "SMD";
		String options = " -i " + m_inputFile + " -s " + m_simName + " -debug ";
		Kmodes module = new Kmodes();
		module.addOptions(options.split(" "));
		module.start();
		module.join();
		System.out.println("\n" + module);
	}

	// public static void test() throws Throwable {
	// // 1. load data
	// DataSet data = new DataSet();
	// // data.debug(true);
	// data.loadData(m_inputFile);
	// // 2. CoupleSim: pre-computation
	// CoupleSim coupleSim = new CoupleSim();
	// coupleSim.debug(true);
	// coupleSim.preComp(data);
	// // 2. run k-modes on data with CoupleSim
	// Kmodes kmodes = new Kmodes();
	// kmodes.debug(true);
	// kmodes.kModes(data.clsNum(), data, coupleSim);
	// kmodes.saveModes(m_modesFile);
	// }
}
