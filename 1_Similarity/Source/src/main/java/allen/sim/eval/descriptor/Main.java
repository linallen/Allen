package allen.sim.eval.descriptor;

import allen.base.common.AAI_IO;
import allen.base.common.Metrics;
import allen.base.common.Timer;
import allen.base.dataset.DataSet;
import allen.sim.measure.SimMeasure;
import allen.sim.measure.SimMeasureRegister;

/**
 * Evaluating a similarity measure's clustering performance for multiple rounds
 * to get the average performance.
 * <p>
 * Input: [sim_alg, input_arff]<br>
 * Output: [data_set, sim_measure, metrics[](such as DBI, DI, Prec, NMI, etc) ]
 */
public class Main {
	static String DATA_DIR = "C:/Allen/UTS/1_Work/2016_06_25_CoupleSimExp/Datasets/";
	static final String outputDbg = "C:/Allen/UTS/1_Work/2016_06_25_CoupleSimExp/Experiments/2016_07_01_Evaluation_Clusters_CMS/DI_prime.csv";
	static {
		// 0. register sim_measures[]
		SimMeasureRegister.register();
		// 1. register descriptors[]
		Descriptor.register("RD", RD.class);
		Descriptor.register("DI", DI.class);
		Descriptor.register("DI'", DI_prime.class);
		Descriptor.register("DBI", DBI.class);
	}

	static void evalSimMeasure(String simName, String inputArff, String[] metricNames, String outputFile)
			throws Exception {
		// 1. load data set
		DataSet dataSet = new DataSet();
		dataSet.loadArff(inputArff);
		dataSet.setClass(-1);
		// 2. similarity measure
		SimMeasure simMeasure = (SimMeasure) SimMeasure.getInstance(simName);
		simMeasure.dataSet(dataSet);
		// 3. metrics
		Metrics metrics = new Metrics();
		for (String metricName : metricNames) {
			Descriptor descriptor = (Descriptor) Descriptor.getInstance(metricName);
			double metric = descriptor.getMetric(simMeasure, dataSet);
			metrics.put(metricName, metric);
		}
		// output metrics
		String buf = new String();
		for (String metricName : metricNames) {
			buf += "," + metrics.getAverage(metricName);
		}
		AAI_IO.saveFile(outputFile, buf + "\n", true);
	}

	public static void main(String[] args) throws Exception {
		Timer timer = new Timer();
		// evaluation
		// "balloons", "soybean-s", "zoo", "lymphography",
		// "Audiology200_objs_70_ftrs_24classes", "soybean-l",
		// "Dermatology366_objs_34_ftrs_6_classes", "wisconsin",
		// "BreastCancer699_objs_10_ftrs_2classes"
		String dataNames[] = { "balloons", "soybean-s", "zoo", "lymphography", "Audiology200_objs_70_ftrs_24classes",
				"soybean-l", "Dermatology366_objs_34_ftrs_6_classes", "wisconsin",
				"BreastCancer699_objs_10_ftrs_2classes" };
		String simNames[] = { "COS", "COS_INTER", "COS_INTRA", "CMS", "CMS_INTER", "CMS_INTRA", "SMD", "OFD" };
		// "RD", "DI", "DBI"
		String metricNames[] = { "DI'" };
		// 1. write CSV title row
		if (!AAI_IO.fileExist(outputDbg)) {
			String buf = "data_set,sim_measure";
			for (String metricName : metricNames) {
				buf += "," + metricName;
			}
			AAI_IO.saveFile(outputDbg, buf + "\n");
		}
		// 2. append experiment results
		for (String dataName : dataNames) {
			for (String simName : simNames) {
				String inputArff = DATA_DIR + dataName + "/" + dataName + ".arff";
				String expIdString = dataName + "," + simName;
				AAI_IO.saveFile(outputDbg, expIdString, true);
				try {
					evalSimMeasure(simName, inputArff, metricNames, outputDbg);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println("\nAll finished. Totoal time: " + timer);
	}
}