package allen.clusterer.eval.main;

import java.util.ArrayList;
import java.util.HashSet;

import allen.base.common.AAI_IO;
import allen.base.common.Common;
import allen.base.common.Timer;
import allen.base.dataset.DataSet;
import allen.clusterer.Clusterer;
import allen.clusterer.alg.kmodes.Kmodes;
import allen.clusterer.alg.spectral.jian.SpecClusterJian;
import allen.sim.measure.SimMeasure;
import allen.sim.measure.coupling.SimCoupleCms;

/**
 * Evaluating a similarity measure's clustering performance for multiple rounds
 * to get the average performance.
 * <p>
 * Input: [sim_alg, cluster_alg, input_arff]<br>
 * Output: [ACC(accuracy/precision), NMI(Normalized Mutual Information)]
 */
public class MainCmsAlphaTest {
	static final String outputDbg = "C:/Allen/UTS/1_Work/2016_06_25_CoupleSimExp/Experiments/2016_07_01_Evaluation_Clusters_CMS/Evaluation_ClustersCMS.csv";
	static final int ROUND = 100;
	static final boolean DEBUG = false;

	private static void register() {
		// 0. register clusterers[]
		Clusterer.register("SC_JIAN", SpecClusterJian.class);
		Clusterer.register("KMODES", Kmodes.class);
		// 0. register sim_measures[]
		SimMeasure.register("CMS", SimCoupleCms.class);
	}

	static void evalClusteringCMS(String clustererName, Double alpha, String inputArff, int round) throws Exception {
		// 1. prepare objects
		DataSet dataSet = new DataSet();
		dataSet.loadArff(inputArff);
		dataSet.setClass(-1);
		SimCoupleCms simMeasure = new SimCoupleCms();
		simMeasure.dataSet(dataSet);
		simMeasure.m_alpha = alpha;
		// TODO: DEL setUniqeName()?
		simMeasure.setUniqeName("CMS_alpha=" + Common.decimal(alpha, 1));
		simMeasure.name("CMS_alpha=" + Common.decimal(alpha, 1));

		// 2. run experimetns
		ArrayList<Metrics> metricsLst = new ArrayList<Metrics>();
		for (int i = 0; i < round; i++) {
			Clusterer clusterer = (Clusterer) Clusterer.getInstance(clustererName);
			clusterer.debug(DEBUG);
			int k = dataSet.clsNum();
			clusterer.clustering(dataSet, simMeasure, k);
			Metrics metrics = Metrics.getMetrics(clusterer.labels(), clusterer.clusters());
			metricsLst.add(metrics);
		}
		// output results for debug
		String buf = new String();
		Metrics sumMetrics = new Metrics();
		for (Metrics metrics : metricsLst) {
			buf += metrics.toString() + "\n";
			sumMetrics.addMetrics(metrics);
		}
		// output results
		sumMetrics.divide(metricsLst.size());
		AAI_IO.saveFile(outputDbg, sumMetrics.toCSV() + "\n", true);
		// if (DEBUG)
		{
			// output results for debug
			String debugFile = inputArff + "." + clustererName + "_a=" + Common.decimal(alpha, 1) + "_" + round
					+ ".txt";
			AAI_IO.saveFile(debugFile, buf);
			AAI_IO.saveFile(debugFile, sumMetrics.toCSV(), true);
		}
	}

	private static String DATA_DIR = "C:/Allen/UTS/1_Work/2016_06_25_CoupleSimExp/Datasets/";
	private static String MATLAB_DIR = "C:/Allen/UTS/1_Work/2016_06_25_CoupleSimExp/Matlab/CMS/functions/";

	public static void main(String[] args) throws Exception {
		Timer timer = new Timer();
		// register clusterers[] and sim_measures[]
		register();
		// set Metrics matlab directory
		Metrics.setMatlabDir(MATLAB_DIR);
		// evaluation
		// "balloons", "soybean-s", "zoo", "lymphography",
		// "Audiology200_objs_70_ftrs_24classes", "soybean-l",
		// "Dermatology366_objs_34_ftrs_6_classes", "wisconsin",
		// "BreastCancer699_objs_10_ftrs_2classes"
		String dataNames[] = { "balloons", "soybean-s", "zoo", "lymphography", "Audiology200_objs_70_ftrs_24classes",
				"soybean-l", "Dermatology366_objs_34_ftrs_6_classes", "wisconsin",
				"BreastCancer699_objs_10_ftrs_2classes" };
		String clustererNames[] = { "KMODES", "SC_JIAN" }; // , "SC_JIAN"
		String finishedFile = "c:/temp/finished_CMS.txt";
		// AAI_IO.saveFile(finishedFile, ""); // TODO comment it
		String finishedExps = AAI_IO.readFile(finishedFile);
		HashSet<String> finishedSet = new HashSet<String>();
		for (String finishedExp : finishedExps.split("\n")) {
			if (finishedExp.trim().isEmpty() == false) {
				finishedSet.add(finishedExp);
			}
		}

		for (String dataName : dataNames) {
			for (String clustererName : clustererNames) {
				for (double alpha = 0.0; alpha <= 1.0; alpha += 0.1) {
					String inputArff = DATA_DIR + dataName + "/" + dataName + ".arff";
					String expIdString = dataName + "," + clustererName + "-CMS_alpha=" + Common.decimal(alpha, 1);
					if (finishedSet.contains(expIdString)) {
						continue;
					}
					AAI_IO.saveFile(outputDbg, expIdString, true);
					try {
						evalClusteringCMS(clustererName, alpha, inputArff, ROUND);
						finishedSet.add(inputArff);
						AAI_IO.saveFile(finishedFile, expIdString + "\n", true);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			System.out.println("Finished data " + dataName);
		}
		System.out.println("\nAll finished. Totoal time: " + timer);
	}
}