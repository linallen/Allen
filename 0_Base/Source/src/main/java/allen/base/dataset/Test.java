package allen.base.dataset;

public class Test {
	/** Test */
	public static void main(String[] args) throws Exception {
		DataSet module = new DataSet();
		module.debug(true);
		module.loadArff("c:/temp/test.arff");
		module.saveArff("c:/temp/test-copy.arff");
	}
}