package allen.clusterer.spectral.matlab;

public class Test {
	public static void main(String[] args) {
		try {
			SpecClusterMatlab scMatlab = new SpecClusterMatlab();
			// scMatlab.set
			scMatlab.clustering();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// System.out.println(scMatlab.toString());
	}
}