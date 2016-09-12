package allen.clusterer;

public class Test {
	static String WORK_DIR = "C:/Allen/UTS/1_Work/2016_06_25_CoupleSimExp/Datasets/";
	static String m_inputARFF = WORK_DIR + "zoo/zoo.arff";
	static String m_outputCSV = m_inputARFF + ".output.csv";
	static String m_simName = "CMS";
	static String m_clustererName = "KMODES";

	public static void main(String[] args) throws Exception {
		// -i input_arff [-s sim_name] [-n clusterer_name] [-k top_k] [-c
		// cls_idx] [-r] -o output_csv
		String options = " -i " + m_inputARFF + " -s " + m_simName + " -n " + m_clustererName + " -o " + m_outputCSV
				+ " -debug ";
		Clusterer module = new Clusterer();
		module.addOptions(options.split(" "));
		module.start();
		module.join();
		System.out.println("\n" + module);
	}
}