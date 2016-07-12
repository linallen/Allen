package allen.sim.eval.descriptor;

import allen.base.dataset.DataSet;
import allen.sim.measure.SimMeasure;

/**
 * Used for evaluating similarity measures, Descriptor SD (Sum-Dissimilarity) is
 * the sum of object dissimilarities within all the clusters. Smaller SD means
 * better similarity measure.
 * 
 * @author Allen Lin, 29 Mar 2016
 */
public class SD extends Descriptor {
	/** return descriptor value SD: the bigger, the better */
	public double getDescriptor(SimMeasure simMeasure, DataSet dataSet) throws Exception {
		// calcInterIntra(simMeasure, dataSet);
		// assert (m_intraSum > 0);
		// return m_intraSum;

		return 0;
	}
}