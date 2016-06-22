package allen.clusterer.eval.main;

import java.util.ArrayList;

import allen.base.common.AAI_IO;
import allen.clusterer.alg.Clusterer;
import allen.clusterer.alg.kmodes.Kmodes;
import allen.clusterer.alg.spectral.jian.SpecClusterJian;
import allen.clusterer.eval.metrics.Metrics;

/**
 * Evaluating a similarity measure's clustering performance for multiple rounds
 * to get the average performance.
 * <p>
 * Input: [sim_alg, cluster_alg, input_arff]<br>
 * Output: [ACC(accuracy/precision), NMI(Normalized Mutual Information)]
 */
public class Main {
	static String DATA_DIR = "../Datasets/";

	static void evalClustering(String clustererName, String simName, String inputArff, int round) throws Exception {
		ArrayList<Metrics> metricsLst = new ArrayList<Metrics>();
		for (int i = 0; i < round; i++) {
			// Clusterer clusterer = Clusterer.getClusterer(clustererName);
			Clusterer clusterer = Clusterer.getInstance(clustererName);
			String options = " -i " + inputArff + " -s " + simName + "" + " -debug ";
			clusterer.addOptions(options.split(" "));
			clusterer.start();
			clusterer.join();
			Metrics metrics = Metrics.getMetrics(clusterer.labels(), clusterer.clusters());
			metricsLst.add(metrics);
		}
		for (Metrics metrics : metricsLst) {
			System.out.println(metrics.toString());
		}
		System.out.println("All done\n");
	}

	// LEI GU: 就用了shuttle,balloon,zoo,soybean-small,
	// 我的结果（精度），普遍都是50%一下。就在那几个数据集上。

	// balloons Balloons20_objs_4_ftrs_2classes
	// Soybean_small47_objs_35_ftrs_4classes
	// Zoo101_objs_17_ftrs_7classes
	// ?? Lymphography (Empty)
	// ?? Audiology200_objs_70_ftrs_24classes, SC crashed, too many classes?
	// Soybean_large307_objs_35_ftrs_19classes
	// Dermatology366_objs_34_ftrs_6_classes done
	// BreastCancer699_objs_10_ftrs_2classes done
	//
	// ?? shuttle15_objs_6_ftrs_2classes, SC crashed

	// "balloons", "Soybean_small47_objs_35_ftrs_4classes",
	// "Voting_records435_objs_16_ftrs_2classes",
	// "Zoo101_objs_17_ftrs_7classes",
	// "BreastCancer699_objs_10_ftrs_2classes",
	// "Dermatology366_objs_34_ftrs_6_classes",
	// "Soybean_large307_objs_35_ftrs_19classes"
	public static void main(String[] args) throws Exception {
		String dataNames[] = { "shuttle" };
		// "COS", "COS_INTER", "COS_INTRA", "CMS", "CMS_INTER", "CMS_INTRA",
		// "SMD", "OFD"
		String simNames[] = { "COS", "COS_INTER", "COS_INTRA", "SMD", "OFD" };
		String clustererNames[] = { "SC_JIAN" };

		// 1. register clusterers
		Clusterer.register("SC_JIAN", SpecClusterJian.class);
		Clusterer.register("KMODES", Kmodes.class);

		// 3. set Metrics matlab directory
		Metrics.setMatlabDir(AAI_IO.getAbsDir("../Matlab/CMS/functions/"));

		// evaluation
		for (String clustererName : clustererNames) {
			for (String simName : simNames) {
				for (String dataName : dataNames) {
					String inputArff = DATA_DIR + dataName + "/" + dataName + ".arff";
					evalClustering(clustererName, simName, inputArff, 20);
				}
			}
		}

		System.out.println("\nAll finished.");
	}
}