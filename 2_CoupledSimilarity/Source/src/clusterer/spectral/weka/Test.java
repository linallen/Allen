package clusterer.spectral.weka;

public class Test {

	private static String WORK_DIR = "D:/GoogleDrive/UTS/SourceCode/10_SpectralClustering_Weka/_data/";
	private static String m_simFile = WORK_DIR + "sim_test.txt";

	public static void main(String[] args) throws Throwable {
		SpectralWeka sc = new SpectralWeka();
		sc.clustering(m_simFile, 0);
	}
}