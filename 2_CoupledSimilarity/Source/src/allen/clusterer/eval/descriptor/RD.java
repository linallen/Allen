package allen.clusterer.eval.descriptor;

import allen.base.common.Common;
import allen.sim.dataset.DataSet;
import allen.sim.dataset.Obj;
import allen.sim.measure.SimMeasure;

/**
 * Used for evaluating similarity measures, Descriptor RD (Relative
 * Dissimilarity) is the ratio of average inter-cluster dissimilarity upon
 * average intra-cluster dissimilarity for all cluster labels. Greater RD means
 * better similarity measure.
 * 
 * @author Allen Lin, 29 Mar 2016
 */
public class RD extends Descriptor {
	/** return descriptor value */
	public String getDescriptor(SimMeasure simMeasure, DataSet data, boolean useSim) throws Exception {
		double interSum = 0, interNum = 0, intraSum = 0, intraNum = 0;
		for (int i = 0; i < data.objNum(); i++) {
			Obj obj1 = (Obj) data.getObj(i);
			for (int j = i + 1; j < data.objNum(); j++) {
				Obj obj2 = (Obj) data.getObj(j);
				double score = useSim ? simMeasure.sim(obj1, obj2, data) : simMeasure.distance(obj1, obj2, data);
				// System.out.println(obj1.toString() + "\n" + obj2.toString() +
				// "\n" + "score = " + score);
				if (obj1.cls() == obj2.cls()) {
					intraSum += score;
					intraNum++;
				} else {
					interSum += score;
					interNum++;
				}
			}
		}
		double interAve = DescriptorIF.divide(interSum, interNum);
		double intraAve = DescriptorIF.divide(intraSum, intraNum);
		assert (intraSum > 0);
		m_intraSum = intraSum;
		m_interSum = interSum;
		return interAve / intraAve + ", (" + Common.decimal(interSum, 2) + "/" + Common.decimal(intraSum, 2) + ")";
	}

	@Override
	protected String getDescriptor() throws Exception {
		double interAve = DescriptorIF.divide(m_interSum, m_interNum);
		double intraAve = DescriptorIF.divide(m_intraSum, m_intraNum);
		assert (m_intraSum > 0);
		return interAve / intraAve + ", (" + Common.decimal(m_interSum, 2) + "/" + Common.decimal(m_intraSum, 2) + ")";	}
}