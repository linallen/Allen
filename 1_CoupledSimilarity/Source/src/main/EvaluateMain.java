package main;
import matlabcontrol.MatlabProxy;
import matlabcontrol.MatlabProxyFactory;
import pkgCommon.AAI_IO;
import pkgDataSet.DataSet;
import pkgSimMeasure.SimMeasure;

public class EvaluateMain {
	static String WORK_DIR = "D:/GoogleDrive/UTS/SourceCode/1_CoupledSimilarity/_experiments/";
	static String MATLAB_DIR = "D:/GoogleDrive/UTS/SourceCode/1_CoupledSimilarity/_experiments/_matlab/";
	static int ROUND = 100;

	public static void main(String[] args) throws Exception {
		MatlabProxy proxy = new MatlabProxyFactory().getProxy();
		EvaluationClustering eva = new EvaluationClustering();
		eva.useMatlab(MATLAB_DIR, proxy);

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
		String dataNames[] = { "BreastCancer699_objs_10_ftrs_2classes", "Dermatology366_objs_34_ftrs_6_classes",
				"Soybean_large307_objs_35_ftrs_19classes" };
		// "COS", "COS_INTER", "COS_INTRA", "CMS", "CMS_INTER", "CMS_INTRA",
		// "SMD", "OFD"
		String simNames[] = { "COS", "COS_INTER", "COS_INTRA", "CMS", "CMS_INTER", "CMS_INTRA", "SMD", "OFD" };
		String clusterNames[] = { "KM", "SC" };// , "SC"

		// 1. data set
		for (String dataName : dataNames) {
			String dataDir = WORK_DIR + dataName + "/";
			String dataFile = dataDir + dataName + ".data";
			DataSet data = new DataSet();
			data.loadData(dataFile);
			data.dbgSummary();
			// 2. similarity measure
			for (String simName : simNames) {
				SimMeasure simMeasure = SimMeasure.getSimMeasure(simName);
				// 3. clustering algorithm
				for (String clusterName : clusterNames) {
					// Run clustering(simMeasure, dataSet)
					// Clustering are KM (k-modes) and SC (spectral clustering)
					// 1) input for KM: similarity measure
					// 2) input for SC: similarity matrix/graph file
					// 3) output: [data_set, sim_alg, cluster_alg, AC, NMI]
					double sumACC = 0, sumNMI = 0;
					for (int round = 0; round < ROUND; round++) {
						eva.evaluate(simMeasure, clusterName, data, round);
						sumACC += eva.m_ACC;
						sumNMI += eva.m_NMI;
						System.out.print(simName + ", " + clusterName + ", " + AAI_IO.getFileNamePre(dataFile) + ", "
								+ round + ": " + eva.m_ACC + eva.m_NMI);
					}
					AAI_IO.saveFile("d:/_evaluate.csv", dataName + "," + simName + "," + clusterName + ","
							+ sumACC / ROUND + "," + sumNMI / ROUND + "\n", true);
				}
			}
		}
		System.out.println("\nAll finished.");
	}
}