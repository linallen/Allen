package allen.sim.measure;

public class Test {
	private static String WORK_DIR = "C:/Allen/UTS/1_Work/2016_06_25_CoupleSimExp/Datasets/";
	private static String m_dataArff = WORK_DIR + "zoo/zoo.arff";
	private static String m_simName = "CMS"; // COS, COS-INTER, SMD, etc
	private static int m_topK = -1;
	private static int m_clsIdx = -1;

	public static void main(String[] args) throws Throwable {
		// -i input_arff -s sim_name [-k top_k]
		String options = "-i " + m_dataArff + " -s " + m_simName + (m_topK > 0 ? (" -k " + m_topK) : "")
				+ (m_clsIdx > 0 ? (" -c " + m_clsIdx) : "");
		SimMeasure module = new SimMeasure();
		module.debug(true);
		module.addOptions(options.split(" "));
		module.start();
		module.join();
		System.out.println("\n\n" + module.description());
	}
}