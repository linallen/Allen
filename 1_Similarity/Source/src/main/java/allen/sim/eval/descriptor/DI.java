package allen.sim.eval.descriptor;

import java.util.ArrayList;

import allen.base.common.Common;
import allen.base.dataset.DataSet;
import allen.sim.measure.SimMeasure;

/**
 * Dunn index (DI) is a metric for evaluating clustering/similarity algorithms.
 * See the definition in Wiki https://en.wikipedia.org/wiki/Dunn_index<br>
 * Higher better.
 * 
 * @author Allen Lin, 12 July 2016
 */
public class DI extends DBI {
	private static final long serialVersionUID = 615749356203429050L;

	@Override
	public double getMetric(SimMeasure simMeasure, DataSet dataSet) throws Exception {
		// 1. pre-compute sim(C) and sim(C1, C2)
		buildClusterSim(simMeasure, dataSet);
		// 2. compute DI = min(intra-cluster sim) / max(inter-cluster sim)
		ArrayList<String> labelLst = new ArrayList<String>(dataSet.getCls().getValStrSet());
		int n = labelLst.size();
		Common.Assert(n > 0);
		// 2.1 calculate min(inter-cluster) and max(intra-cluster)
		double maxInterCluster = 0;
		double minIntraCluster = Double.MAX_VALUE;
		for (int i = 0; i < n; i++) {
			String labeli = labelLst.get(i);
			Double intraCluster = getIntraClusterSim(labeli);
			if (intraCluster.isNaN()) {
				continue;
			}
			minIntraCluster = Math.min(minIntraCluster, intraCluster);
			for (int j = i + 1; j < n; j++) {
				String labelj = labelLst.get(j);
				Double interCluster = getInterClusterSim(labeli, labelj);
				maxInterCluster = Math.max(maxInterCluster, interCluster);
			}
		}
		return minIntraCluster / maxInterCluster;
	}
}