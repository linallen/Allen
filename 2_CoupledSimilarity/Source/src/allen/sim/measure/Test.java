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
		DataSet data = new DataSet();
		data.loadArff(dataFile, false);
		data.dbgOutputSummary();
		String simGraphFile = m_dataFile + "." + simName + ".simGraph.txt";
		System.out.println("\"" + simMeasure.name() + " " + simName + "\" on " + AAI_IO.getFileName(dataFile));
		simMeasure.saveSimGraph(simGraphFile);

	}

	///////////////////////////////////////////////////////////////////
	// data sets
	// private static String DATA_NAME = "shuttle";
	// private static String DATA_NAME = "TestExample1";
	private static String DATA_NAME = "zoo";
	// private static String DATA_NAME = "example1";
	///////////////////////////////////////////////////////////////////
	private static String WORK_DIR = "D:/GoogleDrive/UTS/SourceCode/1_CoupledSimilarity/1_coupling/_datasets/";
	private static String m_dataFile = WORK_DIR + DATA_NAME + "/" + DATA_NAME + ".data";

	public static void main(String[] args) throws Exception {
		// TODO DEBUG
		TestSimMatrix(m_dataFile, "CMS");
		TestSimMatrix(m_dataFile, "CMS_intra");
		TestSimMatrix(m_dataFile, "CMS_inter");
		TestSimMatrix(m_dataFile, "COS");
		TestSimMatrix(m_dataFile, "CoS_intra");
		TestSimMatrix(m_dataFile, "CoS_inter");
		// TODO DEBUG
		// TestSimMatrix(m_dataFile, "INTRA");
		// TestSimMatrix(m_dataFile, "INTER");
		System.out.println("\nAll done");
	}
}