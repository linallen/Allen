package allen.sim.eval.descriptor;

import allen.base.common.Common;
import allen.base.dataset.DataSet;
import allen.base.dataset.Obj;
import allen.sim.measure.SimMeasure;

/**
 * Used for evaluating similarity measures, Descriptor RD (Relative
 * Dissimilarity) is the ratio of average inter-cluster similarity upon average
 * intra-cluster similarity for all cluster labels. Smaller RD means better
 * similarity measure.
 * 
 * RD(simMeasure, dataSet) = average(inter-cluster similarity) /
 * average(intra-cluster similarity)
 * 
 * @author Allen Lin, 29 Mar 2016
 */
public class RD extends Descriptor {
	protected double m_interSum;
	protected double m_intraSum;

	protected double m_interNum;
	protected double m_intraNum;

	public String getSimName() {
		return getClass().getSimpleName();
	}

	/** calculate common statistics such as sum of inter/intra similarities */
	protected final void calcInterIntra(SimMeasure simMeasure, DataSet dataSet) throws Exception {
		m_interSum = m_intraSum = m_interNum = m_intraNum = 0;
		for (int i = 0; i < dataSet.objNum(); i++) {
			Obj obj1 = (Obj) dataSet.getObj(i);
			for (int j = i + 1; j < dataSet.objNum(); j++) {
				Obj obj2 = (Obj) dataSet.getObj(j);
				double score = simMeasure.sim(obj1, obj2);
				// System.out.println(obj1.toString() + "\n" + obj2.toString() +
				// "\n" + "score = " + score);
				if (obj1.getLabel() == obj2.getLabel()) {
					m_intraSum += score;
					m_intraNum++;
				} else {
					m_interSum += score;
					m_interNum++;
				}
			}
		}
	}

	/** return descriptor value */
	public double getDescriptor(SimMeasure simMeasure, DataSet dataSet) throws Exception {
		calcInterIntra(simMeasure, dataSet);
		double interAve = Descriptor.divide(m_interSum, m_interNum);
		double intraAve = Descriptor.divide(m_intraSum, m_intraNum);
		Common.Assert(m_intraSum > 0);
		return interAve / intraAve;
		// return interAve / intraAve + ", (" + Common.decimal(m_interSum, 2) +
		// "/" + Common.decimal(m_intraSum, 2) + ")";
	}
}