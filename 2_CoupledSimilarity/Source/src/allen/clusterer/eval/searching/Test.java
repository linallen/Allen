package eval.searching;

public class Test {
	private static String WORK_DIR = "F:/3_GoogleDrive/UTS/SourceCode/1_CoupledSimilarity/_datasets/_test/";
	private static String m_inputFile = WORK_DIR + "_sim_objs.csv";

	public static void main(String[] args) throws Throwable {
		PrecRecall metrics = new PrecRecall();
		metrics.main(m_inputFile, 100, WORK_DIR + "metrics.m");
	}
}
