package pkgArff;

public class Test {
	private static String WORK_DIR = "C:/Dropbox/UTS/SourceCode/5_CodingArff/_data/";
	private static String m_inputArff = WORK_DIR + "input.arff";
	private static String m_outputARFF = WORK_DIR + "output.arff";
	private static String m_outputCSV = WORK_DIR + "output.CSV";

	public static void main(String[] args) throws Throwable {
		Arff arff = new Arff(null);
		arff.load(m_inputArff);
		arff.saveARFF(m_outputARFF);
		arff.saveCSV(m_outputCSV);
		System.out.println("All done.");
	}
}