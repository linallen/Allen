package allen.clusterer.eval.main;

import java.util.ArrayList;
import java.util.HashSet;

import allen.base.common.AAI_IO;
import allen.base.common.Timer;
import allen.clusterer.Clusterer;
import allen.clusterer.alg.kmodes.Kmodes;
import allen.clusterer.alg.spectral.jian.SpecClusterJian;
import allen.sim.measure.SimMeasure;
import allen.sim.measure.SimOFD;
import allen.sim.measure.SimSMD;
import allen.sim.measure.coupling.SimCoupleCms;
import allen.sim.measure.coupling.SimCoupleCmsInter;
import allen.sim.measure.coupling.SimCoupleCmsIntra;
import allen.sim.measure.coupling.SimCoupleCos;
import allen.sim.measure.coupling.SimCoupleCosInter;
import allen.sim.measure.coupling.SimCoupleCosIntra;

/**
 * Evaluating a similarity measure's clustering performance for multiple rounds
 * to get the average performance.
 * <p>
 * Input: [sim_alg, cluster_alg, input_arff]<br>
 * Output: [ACC(accuracy/precision), NMI(Normalized Mutual Information)]
 */
public class Main {
	static String outputDbg = "C:/Allen/UTS/UTS_SourceCode/2016_06_25_CoupleSimExp/Experiments/2016_06_22_Evaluation_Clusters_TODO/Evaluation_new.csv";

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

	static void evalClustering(String clustererName, String simName, String inputArff, int round) throws Exception {
		ArrayList<Metrics> metricsLst = new ArrayList<Metrics>();
		for (int i = 0; i < round; i++) {
			Clusterer clusterer = (Clusterer) Clusterer.getInstance(clustererName);
			clusterer.name(clustererName + "_" + simName + "_" + AAI_IO.getFileNamePre(inputArff) + "_[" + (i + 1) + "/"
					+ round + "]");
			String options = " -i " + inputArff + " -s " + simName + " -r " + " -debug ";
			clusterer.addOptions(options.split(" "));
			clusterer.start();
			clusterer.join();
			Metrics metrics = Metrics.getMetrics(clusterer.labels(), clusterer.clusters());
			// TODO debug
			boolean isDebug = false;
			if (isDebug) {
				// sim_measure,clusterer,labels[],flags[],NMI
				String debug = clusterer.simMeasure().moduleName() + "," + clusterer.moduleName() + ",";
				// Double nmi = metrics.getMetric("NMI_spec");
				// if (nmi == null || nmi.isNaN()) {
				// System.out.println("nmi == null");
				// }
				debug += metrics.getMetric("NMI_spec") + ",";
				debug += Metrics.getMatlabArray(clusterer.clusters()) + ",";
				debug += Metrics.getMatlabArray(clusterer.labels()) + "\n";
				AAI_IO.saveFile("c:/temp/debug.csv", debug, true);
			}
			// TODO debug
			metricsLst.add(metrics);
		}
		// output results for debug
		String buf = new String();
		Metrics sumMetrics = new Metrics();
		for (Metrics metrics : metricsLst) {
			buf += metrics.toString() + "\n";
			sumMetrics.addMetrics(metrics);
		}
		String debugFile = inputArff + "." + clustererName + "_" + simName + "_" + round + ".txt";
		AAI_IO.saveFile(debugFile, buf);
		// output results for debug

		// output results
		sumMetrics.divide(metricsLst.size());
		AAI_IO.saveFile(outputDbg, sumMetrics.toCSV() + "\n", true);
		AAI_IO.saveFile(debugFile, sumMetrics.toCSV(), true);
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
		int ROUND = 20;
		String dataNames[] = { "balloons", "soybean-s", "zoo", "lymphography", "Audiology200_objs_70_ftrs_24classes",
				"soybean-l", "Dermatology366_objs_34_ftrs_6_classes", "wisconsin",
				"BreastCancer699_objs_10_ftrs_2classes" };// , "shuttle"
		String simNames[] = { "COS", "COS_INTER", "COS_INTRA", "CMS", "CMS_INTER", "CMS_INTRA", "SMD", "OFD" };
		// String simNames[] = { "CMS" };
		String clustererNames[] = { "KMODES", "SC_JIAN" }; // "KMODES","SC_JIAN"
		// AAI_IO.saveFile(outputDbg,
		// "data_set,sim_measure-clusterer,Prec,Recall,NMI,Fscore\n");
		String finishedFile = "c:/temp/finished.txt";
		String finishedExps = AAI_IO.readFile(finishedFile);
		HashSet<String> finishedSet = new HashSet<String>();
		for (String finishedExp : finishedExps.split("\n")) {
			if (finishedExp.trim().isEmpty() == false) {
				finishedSet.add(finishedExp);
			}
		}

		for (String dataName : dataNames) {
			for (String clustererName : clustererNames) {
				for (String simName : simNames) {
					String inputArff = DATA_DIR + dataName + "/" + dataName + ".arff";
					//////////////////
					// output data summary to file as data names
					// DataSet data = new DataSet();
					// data.loadArff(inputArff);
					// data.setClass(-1);
					// System.out.println(data.dataSummary());
					//////////////////

					String expIdString = dataName + "," + clustererName + "-" + simName;
					if (finishedSet.contains(expIdString)) {
						continue;
					}
					AAI_IO.saveFile(outputDbg, expIdString, true);
					try {
						evalClustering(clustererName, simName, inputArff, ROUND);
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