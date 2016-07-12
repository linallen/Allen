package allen.sim.eval.descriptor;

import java.util.ArrayList;
import java.util.HashMap;

import allen.base.common.Common;
import allen.base.dataset.DataSet;
import allen.base.dataset.Obj;
import allen.sim.measure.SimMeasure;

/**
 * Davies¨CBouldin index (DBI) is a metric for evaluating clustering/similarity
 * algorithms. See the definition in Wiki
 * https://en.wikipedia.org/wiki/Davies%E2%80%93Bouldin_index
 * 
 * @author Allen Lin, 12 July 2016
 */
public class DBI extends Descriptor {
	private static final long serialVersionUID = -3870322560396203556L;

	/** intra-cluster similarities */
	private HashMap<String, Double> m_intraClusterSim;
	/** inter-cluster similarities */
	private HashMap<String, Double> m_interClusterSim;

	protected double getIntraClusterSim(String label) {
		Double sim = m_intraClusterSim.get(label);
		return sim == null ? 0 : sim;
	}

	protected double getInterClusterSim(String label1, String label2) {
		Double sim = m_interClusterSim.get(label1 + " " + label2);
		return sim == null ? 0 : sim;
	}

	/** return sim(C) = ave(sim(a1,a2)), for all a1, a2 in C */
	private static double calcIntraClusterSim(SimMeasure simMeasure, ArrayList<Obj> objLst) throws Exception {
		Common.Assert(!objLst.isEmpty());
		double sum = 0;
		int n = objLst.size();
		for (int i = 0; i < n; i++) {
			Obj obj1 = objLst.get(i);
			for (int j = i + 1; j < n; j++) {
				Obj obj2 = objLst.get(j);
				sum += simMeasure.sim(obj1, obj2);
			}
		}
		return (n < 2) ? 0 : (2 * sum / (n * (n - 1)));
	}

	/** return sim(C1, C2) = ave(sim(a1,a2)), for all a1 in C1, a2 in C2 */
	private static double calcInterClusterSim(SimMeasure simMeasure, ArrayList<Obj> objLst1, ArrayList<Obj> objLst2)
			throws Exception {
		Common.Assert(!objLst1.isEmpty() && !objLst2.isEmpty());
		double sum = 0;
		for (Obj obj1 : objLst1) {
			for (Obj obj2 : objLst1) {
				sum += simMeasure.sim(obj1, obj2);
			}
		}
		return sum / (objLst1.size() * objLst2.size());
	}

	/** pre-compute intra-cluster and inter-cluster similarities */
	protected void buildClusterSim(SimMeasure simMeasure, DataSet dataSet) throws Exception {
		output("Started building cluster similarities...");
		m_intraClusterSim = new HashMap<String, Double>();
		m_interClusterSim = new HashMap<String, Double>();
		ArrayList<String> labelLst = new ArrayList<String>(dataSet.getCls().getValStrSet());
		// 1. calculate intra-cluster similarity sim(C)
		for (String label : labelLst) {
			Double intraClusterSim = calcIntraClusterSim(simMeasure, dataSet.getClsObjs(label));
			if (intraClusterSim.isNaN()) {
				throw new Exception("NaN");
			}
			m_intraClusterSim.put(label, intraClusterSim);
		}
		// 2. calculate inter-cluster similarity sim(C1, C2)
		for (int i = 0; i < labelLst.size(); i++) {
			String label1 = labelLst.get(i);
			for (int j = i + 1; j < labelLst.size(); j++) {
				String label2 = labelLst.get(j);
				Double intraClusterSim = calcInterClusterSim(simMeasure, dataSet.getClsObjs(label1),
						dataSet.getClsObjs(label2));
				m_interClusterSim.put(label1 + " " + label2, intraClusterSim);
				m_interClusterSim.put(label2 + " " + label1, intraClusterSim);
			}
		}
		output("Finished building cluster similarities...");
	}

	@Override
	public double getMetric(SimMeasure simMeasure, DataSet dataSet) throws Exception {
		// 1. pre-compute sim(C) and sim(C1, C2)
		buildClusterSim(simMeasure, dataSet);
		// 2. compute DBI
		ArrayList<String> labelLst = new ArrayList<String>(dataSet.getCls().getValStrSet());
		int n = labelLst.size();
		Common.Assert(n > 0);
		double sumDBI = 0;
		for (int i = 0; i < n; i++) {
			// Di = max(Rij) = max{(Si+Sj)/Mij}
			String labeli = labelLst.get(i);
			double Si = getIntraClusterSim(labeli);
			double Di = 0;
			for (int j = 0; j < n; j++) {
				if (j != i) {
					String labelj = labelLst.get(j);
					double Sj = getIntraClusterSim(labelj);
					double Mij = getInterClusterSim(labeli, labelj);
					double Rij = (Si + Sj) / Mij;
					Di = Math.max(Di, Rij);
				}
			}
			sumDBI += Di;
		}
		return sumDBI / n;
	}
}