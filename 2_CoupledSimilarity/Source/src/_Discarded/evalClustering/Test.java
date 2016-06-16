package _Discarded.evalClustering;

public class Test {

	public static void main(String[] args) throws Throwable {
		String WORK_DIR = "D:/GoogleDrive/UTS/SourceCode/1_CoupledSimilarity/1_kmodes/";
		// String m_clusterCSV = WORK_DIR + "example2.txt.COS.csv";
		String m_clusterCSV = WORK_DIR + "zoo.data.INTER.csv";
		EvalCluster evalCluster = new EvalCluster();
		evalCluster.Calc(m_clusterCSV);
		double acc = evalCluster.getAC();
		double nmi = evalCluster.getNMI();
		System.out.println("acc = " + acc);
		System.out.println("nmi = " + nmi);
	}
}