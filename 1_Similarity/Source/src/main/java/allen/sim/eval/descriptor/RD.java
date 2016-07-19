package allen.sim.eval.descriptor;

import java.util.ArrayList;

import allen.base.common.Common;
import allen.base.dataset.DataSet;
import allen.sim.measure.SimMeasure;

/**
 * Relative Dissimilarity (RD) is the ratio of average intra-cluster similarity
 * upon average inter-cluster similarity for all cluster labels. <br>
 * See the definition in TNNLS'13: Coupled Attribute Similarity Learning on
 * Categorical Data, on page 11<br>
 * Higher better.
 * 
 * @author Allen Lin, 14 July 2016
 */
public class RD extends DBI {
	private static final long serialVersionUID = -2602212493222353731L;

	@Override
	public double getMetric(SimMeasure simMeasure, DataSet dataSet) throws Exception {
		// 1. pre-compute sim(C) and sim(C1, C2)
		buildClusterSim(simMeasure, dataSet);
		// 2. compute DI = min(intra-cluster sim) / max(inter-cluster sim)
		ArrayList<String> labelLst = new ArrayList<String>(dataSet.getCls().getValStrSet());
		int n = labelLst.size();
		Common.Assert(n > 0);
		// 2.1 calculate min(inter-cluster) and max(intra-cluster)
		double sumIntra = 0;
		double sumInter = 0;
		int numIntra = 0;
		int numInter = 0;
		for (int i = 0; i < n; i++) {
			String labeli = labelLst.get(i);
			Double simIntraCluster = getIntraClusterSim(labeli);
			if (simIntraCluster == null || simIntraCluster.isNaN()) {
				continue;
			}
			sumIntra += simIntraCluster;
			numIntra++;
			for (int j = i + 1; j < n; j++) {
				String labelj = labelLst.get(j);
				Double simInterCluster = getInterClusterSim(labeli, labelj);
				if ((simInterCluster == null) || simInterCluster.isNaN()) {
					continue;
				}
				sumInter += simInterCluster;
				numInter++;
			}
		}
		double aveIntra = (numIntra == 0) ? 0 : (sumIntra / numIntra);
		double aveInter = (numInter == 0) ? 0 : (sumInter / numInter);
		return aveIntra / aveInter;
	}
}