package eval.descriptor;

import pkgDataSet.DataSet;
import pkgDataSet.Obj;
import similarity.measure.SimMeasure;

public abstract class Descriptor implements DescriptorIF {
	public double m_interSum;
	public double m_intraSum;

	public double m_interNum;
	public double m_intraNum;

	public double getInter() {
		return m_interSum;
	}

	public double getIntra() {
		return m_intraSum;
	}

	/** define it yourself */
	protected abstract String getDescriptor() throws Exception;

	public String getDescriptor(SimMeasure simMeasure, DataSet data, boolean useSim) throws Exception {
		m_interSum = m_intraSum = m_interNum = m_intraNum = 0;
		for (int i = 0; i < data.objNum(); i++) {
			Obj obj1 = (Obj) data.getObj(i);
			for (int j = i + 1; j < data.objNum(); j++) {
				Obj obj2 = (Obj) data.getObj(j);
				double score = useSim ? simMeasure.sim(obj1, obj2, data) : simMeasure.distance(obj1, obj2, data);
				// System.out.println(obj1.toString() + "\n" + obj2.toString() +
				// "\n" + "score = " + score);
				if (obj1.cls() == obj2.cls()) {
					m_intraSum += score;
					m_intraNum++;
				} else {
					m_interSum += score;
					m_interNum++;
				}
			}
		}
		return getDescriptor();
	}
}