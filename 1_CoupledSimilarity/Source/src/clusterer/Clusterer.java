package clusterer;

import java.io.BufferedWriter;
import java.io.FileWriter;

import sim.dataset.DataSet;
import sim.dataset.Obj;
import sim.measure.SimMeasure;

public interface Clusterer {
	public int numberOfClusters();

	public int[] clustering(String simGraphFile, int k) throws Exception;

	/**
	 * main function of clustering with given similarity measure and clustering
	 * number.
	 * 
	 * @param simMeasure
	 *            similarity measure object
	 * @param k
	 *            cluster number
	 * @return cluster id array, starting from 0
	 */
	public int[] clustering(SimMeasure simMeasure, DataSet data, int k) throws Exception;

	public default void saveClusters(int[] cluster, String clusterCSV, DataSet data) throws Exception {
		if (cluster.length != data.objNum()) {
			throw new Exception("ERROR!");
		}
		BufferedWriter bw = new BufferedWriter(new FileWriter(clusterCSV));
		for (int i = 0; i < data.objNum(); i++) {
			Obj obj = data.getObj(i);
			bw.write(obj.name() + "," + obj.cls().getName() + "," + cluster[i] + "," + obj.strValues() + "\n");
		}
		bw.close();
	}
}
