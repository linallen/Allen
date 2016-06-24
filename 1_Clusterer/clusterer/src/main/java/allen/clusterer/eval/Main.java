package allen.clusterer.eval;

import java.util.ArrayList;

import allen.base.common.AAI_IO;
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
	static String outputDbg = "c:/tmp/debug.txt";

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

	static String DATA_DIR = "../Datasets/";

	static void evalClustering(String clustererName, String simName, String inputArff, int round) throws Exception {
		ArrayList<Metrics> metricsLst = new ArrayList<Metrics>();
		for (int i = 0; i < round; i++) {
			// Clusterer clusterer = Clusterer.getClusterer(clustererName);
			Clusterer clusterer = (Clusterer) Clusterer.getInstance(clustererName);
			String options = " -i " + inputArff + " -s " + simName + " -debug ";
			clusterer.addOptions(options.split(" "));
			clusterer.start();
			clusterer.join();
			Metrics metrics = Metrics.getMetrics(clusterer.labels(), clusterer.clusters());
			metricsLst.add(metrics);
		}
		Metrics sumMetrics = new Metrics();
		for (Metrics metrics : metricsLst) {
			System.out.println(metrics.toString());
			sumMetrics.addMetrics(metrics);
		}
		sumMetrics.divide(metricsLst.size());
		AAI_IO.saveFile(outputDbg, sumMetrics.toCSV() + "\n", true);
		System.out.println("All done\n");
	}

	// LEI GU: ������shuttle,balloon,zoo,soybean-small,
	// �ҵĽ�������ȣ����ձ鶼��50%һ�¡������Ǽ������ݼ��ϡ�

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
		// register clusterers[] and v[]
		register();
		// set Metrics matlab directory
		Metrics.setMatlabDir(AAI_IO.getAbsDir("../Matlab/CMS/functions/"));
		// evaluation
		String dataNames[] = { "shuttle" };
		// "COS", "COS_INTER", "COS_INTRA", "CMS", "CMS_INTER", "CMS_INTRA",
		// "SMD", "OFD"
		String simNames[] = { "CMS", "CMS_INTER", "CMS_INTRA", "COS", "COS_INTER", "COS_INTRA", "SMD", "OFD" };
		String clustererNames[] = { "KMODES" }; // "SC_JIAN"
		AAI_IO.saveFile(outputDbg, "cluster,sim_measure,dataset\n");
		for (String clustererName : clustererNames) {
			for (String simName : simNames) {
				for (String dataName : dataNames) {
					String inputArff = DATA_DIR + dataName + "/" + dataName + ".arff";
					AAI_IO.saveFile(outputDbg, clustererName + "," + simName + "," + dataName + ",", true);
					evalClustering(clustererName, simName, inputArff, 20);
				}
			}
		}
		System.out.println("\nAll finished.");
	}
}