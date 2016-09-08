package allen.sim.eval.descriptor;

import java.util.ArrayList;

import allen.base.common.Common;
import allen.base.dataset.DataSet;
import allen.sim.measure.SimMeasure;

/**
 * Dunn' index (DI') = max(Si) / min(Mij), which is suggested by Longbing.
 * 
 * @author Allen Lin, 12 July 2016
 */
public class DI_prime extends DBI {
	private static final long serialVersionUID = -7938179547419103103L;

	@Override
	public double getMetric(SimMeasure simMeasure, DataSet dataSet) throws Exception {
		// 1. pre-compute sim(C) and sim(C1, C2)
		buildClusterSim(simMeasure, dataSet);
		// 2. compute DI = min(intra-cluster sim) / max(inter-cluster sim)
		ArrayList<String> labelLst = new ArrayList<String>(dataSet.getCls().getValStrSet());
		int n = labelLst.size();
		Common.Assert(n > 0);
		// 2.1 calculate min(inter-cluster) and max(intra-cluster)
		double minInter = Double.MAX_VALUE;
		double maxIntra = 0;
		for (int i = 0; i < n; i++) {
			String labeli = labelLst.get(i);
			Double simIntraCluster = getIntraClusterSim(labeli);
			if (simIntraCluster == null || simIntraCluster.isNaN()) {
				continue;
			}
			maxIntra = Math.max(maxIntra, simIntraCluster);
			for (int j = i + 1; j < n; j++) {
				String labelj = labelLst.get(j);
				Double simInterCluster = getInterClusterSim(labeli, labelj);
				if ((simInterCluster == null) || simInterCluster.isNaN()) {
					continue;
				}
				minInter = Math.min(minInter, simInterCluster);
			}
		}
		return maxIntra / minInter;
	}
}