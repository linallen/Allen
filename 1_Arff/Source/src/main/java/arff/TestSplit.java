package arff;

public class TestSplit {
	private static String WORK_DIR = "C:/Dropbox/UTS/SourceCode/5_CodingArff/_data/mushroom/";
	private static String m_sourceArff = WORK_DIR + "mushroom.arff";

	public static void main(String[] args) throws Throwable {
		Arff module = new Arff();
		module.splitArff(m_sourceArff, "cap-shape");
		System.out.println("\n" + module);
	}
}
