package allen.sim.measure;

import allen.base.common.*;
import allen.sim.dataset.DataSet;
import eval.searching.PrecRecall;

public class Test {
	/** Experiment 1: testing algorithm on Precision, Recall, and F metrics */
	private static void TestPrecRecall(String dataFile, String simName) throws Exception {
		String m_simFile = WORK_DIR + simName + "_sim_objs.csv";
		String options = " -i " + dataFile + " -o " + m_simFile + " -s " + simName + " -debug ";
		SimMeasure module = new SimMeasure();
		module.addOptions(options.split(" "));
		module.start();
		module.join();
		System.out.println("\n" + module);
		// get precision, recall and F metrics of top k results, k=1,...
		options = " -i " + m_simFile + " -o " + WORK_DIR + simName + "_metrics.m" + " -debug ";
		PrecRecall metrics = new PrecRecall();
		metrics.addOptions(options.split(" "));
		metrics.start();
		metrics.join();
		System.out.println("\n" + metrics);
	}

	public static void TestPrecRecall(String dataFile) throws Exception {
		String simNames[] = { "COS", "COS_INTRA", "COS_INTER" };
		for (String simName : simNames) {
			TestPrecRecall(dataFile, simName);
		}
	}

	/** Experiment 2: output obj-obj similarity graph (matrix) */
	public static void TestSimMatrix(String dataFile, String simName) throws Exception {
		SimMeasure simMeasure = SimMeasure.getSimMeasure(simName);
		simMeasure.debug(true);
		System.out.println("\"" + simMeasure.name() + " " + simName + "\" on " + AAI_IO.getFileName(dataFile));
		DataSet dataSet = new DataSet();
		dataSet.debug(true);
		dataSet.loadArff(dataFile);
		dataSet.setClass(-1);
		dataSet.saveArff(dataFile + ".copy.arff");
		dataSet.dbgOutputSummary();
		simMeasure.dataSet(dataSet);
		// simMeasure.saveSimObjs(dataFile + "." + simName + ".sim_objs.txt");
		simMeasure.saveSimGraph(dataFile + "." + simName + ".sim_graph.txt");
		// simMeasure.saveSimMatrix(dataFile + "." + simName +
		// ".sim_matrix.txt");
	}

	///////////////////////////////////////////////////////////////////
	// data sets
	private static String DATA_NAME = "zoo"; // CMS same with Jian
	// private static String DATA_NAME = "balloons"; // CMS same with Jian
	// private static String DATA_NAME = "test"; // CMS same with Jian
	// private static String DATA_NAME = "shuttle"; // CMS same with Jian
	///////////////////////////////////////////////////////////////////
	private static String WORK_DIR = "../Datasets/";
	private static String m_dataFile = WORK_DIR + DATA_NAME + "/" + DATA_NAME + ".arff";

	public static void main(String[] args) throws Exception {
		TestSimMatrix(m_dataFile, "COS");
		// TestSimMatrix(m_dataFile, "CoS_intra");
		// TestSimMatrix(m_dataFile, "CoS_inter");
		TestSimMatrix(m_dataFile, "CMS");
		// TestSimMatrix(m_dataFile, "CMS_intra");
		// TestSimMatrix(m_dataFile, "CMS_inter");
	}
}