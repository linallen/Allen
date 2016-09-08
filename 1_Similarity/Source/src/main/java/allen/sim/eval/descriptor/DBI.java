package allen.sim.eval.descriptor;

import java.util.ArrayList;
import java.util.HashMap;

import allen.base.common.Common;
import allen.base.dataset.DataSet;
import allen.base.dataset.Obj;
import allen.sim.measure.SimMeasure;

/**
 * Davies-Bouldin index (DBI) is a metric for evaluating clustering/similarity
 * algorithms. See the definition in Wiki
 * https://en.wikipedia.org/wiki/Davies%E2%80%93Bouldin_index<br>
 * Higher better.
 * 
 * @author Allen Lin, 12 July 2016
 */
public class DBI extends Descriptor {
	private static final long serialVersionUID = -3870322560396203556L;

	/** intra-cluster similarities */
	private HashMap<String, Double> m_intraClusterSim;
	/** inter-cluster similarities */
	private HashMap<String, Double> m_interClusterSim;

	protected Double getIntraClusterSim(String label) {
		return m_intraClusterSim.get(label);
	}

	protected Double getInterClusterSim(String label1, String label2) {
		return m_interClusterSim.get(label1 + " " + label2);
	}

	/** return sim(C) = ave(sim(a1,a2)), for all a1, a2 in C */
	private static Double calcIntraClusterSim(SimMeasure simMeasure, ArrayList<Obj> objLst) throws Exception {
		Common.Assert(!objLst.isEmpty() && (objLst.size() > 1));
		double sum = 0;
		int n = objLst.size();
		for (int i = 0; i < n; i++) {
			Obj obj1 = objLst.get(i);
			for (int j = i + 1; j < n; j++) {
				Obj obj2 = objLst.get(j);
				sum += simMeasure.sim(obj1, obj2);
			}
		}
		return (n < 2) ? Double.NaN : (2 * sum / (n * (n - 1)));
	}

	/** return sim(C1, C2) = ave(sim(a1,a2)), for all a1 in C1, a2 in C2 */
	private static Double calcInterClusterSim(SimMeasure simMeasure, ArrayList<Obj> objLst1, ArrayList<Obj> objLst2)
			throws Exception {
		Common.Assert(!objLst1.isEmpty() && !objLst2.isEmpty());
		double sum = 0;
		for (Obj obj1 : objLst1) {
			for (Obj obj2 : objLst2) {
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

		// 0. only consider classes containing 2 or more objects
		ArrayList<String> labelLst = new ArrayList<String>();
		for (String label : dataSet.getCls().getValStrSet()) {
			if (dataSet.getClsObjs(label).size() > 1) {
				labelLst.add(label);
			} else {
				output("ignore label " + label + " of " + dataSet.dataName());
			}
		}

		// 1. calculate intra-cluster similarity sim(C)
		for (String label : labelLst) {
			Double simIntraCluster = calcIntraClusterSim(simMeasure, dataSet.getClsObjs(label));
			m_intraClusterSim.put(label, simIntraCluster);
		}
		// 2. calculate inter-cluster similarity sim(C1, C2)
		for (int i = 0; i < labelLst.size(); i++) {
			String label1 = labelLst.get(i);
			for (int j = i + 1; j < labelLst.size(); j++) {
				String label2 = labelLst.get(j);
				Double simIntraCluster = calcInterClusterSim(simMeasure, dataSet.getClsObjs(label1),
						dataSet.getClsObjs(label2));
				m_interClusterSim.put(label1 + " " + label2, simIntraCluster);
				m_interClusterSim.put(label2 + " " + label1, simIntraCluster);
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
		Double sumDBI = 0.;
		for (int i = 0; i < n; i++) {
			// Di = max(Rij) = max{(Si+Sj)/Mij}
			String labeli = labelLst.get(i);
			Double Si = getIntraClusterSim(labeli);
			if (Si == null || Si.isNaN()) {
				continue;
			}
			Double Di = 0.;
			for (int j = 0; j < n; j++) {
				if (j != i) {
					String labelj = labelLst.get(j);
					Double Sj = getIntraClusterSim(labelj);
					if (Sj == null || Sj.isNaN()) {
						continue;
					}
					Double Mij = getInterClusterSim(labeli, labelj);
					if (Mij == null || Mij.isNaN() || (Mij == 0.)) {
						continue;
					}
					Double Rij = (Si + Sj) / Mij;
					Di = Math.max(Di, Rij);
				}
			}
			sumDBI += Di;
		}
		return sumDBI / n;
	}
}