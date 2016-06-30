package allen.clusterer.eval.main;

import allen.base.dataset.DataSet;
import allen.base.dataset.MissingValue;

public class MainMissingValue {
	private static String DATA_DIR = "C:/Allen/UTS/UTS_SourceCode/2016_06_25_CoupleSimExp/Datasets/";

	public static void main(String[] args) throws Exception {
		String dataNames[] = { "balloons", "soybean-s", "zoo", "lymphography", "Audiology200_objs_70_ftrs_24classes",
				"soybean-l", "Dermatology366_objs_34_ftrs_6_classes", "wisconsin",
				"BreastCancer699_objs_10_ftrs_2classes", "shuttle" };
		for (String dataName : dataNames) {
			String inputArff = DATA_DIR + dataName + "/" + dataName + ".arff";
			DataSet dataSet = new DataSet();
			dataSet.loadArff(inputArff);
			MissingValue.replaceWithFreq(dataSet);
			dataSet.saveArff(inputArff + "._new.arff");
		}
	}
}