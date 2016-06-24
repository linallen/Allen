package allen.clusterer.alg.spectral.jian;

import allen.clusterer.Clusterer;
import allen.clusterer.alg.kmodes.Kmodes;
import allen.sim.measure.SimMeasure;
import allen.sim.measure.SimOFD;
import allen.sim.measure.SimSMD;
import allen.sim.measure.coupling.SimCoupleCms;
import allen.sim.measure.coupling.SimCoupleCmsInter;
import allen.sim.measure.coupling.SimCoupleCmsIntra;
import allen.sim.measure.coupling.SimCoupleCos;
import allen.sim.measure.coupling.SimCoupleCosInter;
import allen.sim.measure.coupling.SimCoupleCosIntra;

public class Test {
	private static void register() {
		// 0. register clusterers[]
		Clusterer.register("SC_JIAN", SpecClusterJian.class);
		Clusterer.register("KMODES", Kmodes.class);

		// 0. register sim_measures[]
		SimMeasure.register("CMS", SimCoupleCms.class);
		SimMeasure.register("CMS_INTRA", SimCoupleCmsIntra.class);
		SimMeasure.register("CMS_INTER", SimCoupleCmsInter.class);
		SimMeasure.register("COS", SimCoupleCos.class);
		SimMeasure.register("COS_INTRA", SimCoupleCosIntra.class);
		SimMeasure.register("COS_INTER", SimCoupleCosInter.class);
		SimMeasure.register("SMD", SimSMD.class);
		SimMeasure.register("OFD", SimOFD.class);
	}

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
		register();
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