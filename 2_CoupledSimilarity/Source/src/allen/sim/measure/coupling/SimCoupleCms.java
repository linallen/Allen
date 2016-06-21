package allen.sim.measure.coupling;

import java.util.HashSet;

import allen.base.common.Common;
import allen.sim.dataset.Feature;
import allen.sim.dataset.FtrType;
import allen.sim.dataset.Value;

/**
 * CMS (Coupled Metric Similarity) algorithm for measuring object-object
 * similarities based on value-value similarities. CMS was proposed in
 * "Coupled Metric Similarity Learning for Non-IID Categorical Data" authored by
 * Songlei Jian et al.
 * 
 * @author Allen Lin, 20 Jan 2016
 */
public class SimCoupleCms extends SimCouple {
	private static final long serialVersionUID = 562579004732926584L;

	/** intra-coupled sim(val1, val2). */
	@Override
	protected final double intraSim(Value val1, Value val2) throws Exception {
		if (val1 == val2) {
			return 1;
		}
		int p = getOwnerObjs(val1).size() + 1;
		int q = getOwnerObjs(val2).size() + 1;
		double logP = Math.log(p);
		double logQ = Math.log(q);
		double intraSim = (logP * logQ) / (logP + logQ + logP * logQ);
		return intraSim;
	}

	/** inter-coupled sim(val1, val2). */
	@Override
	protected final double interSim(Value val1, Value val2) throws Exception {
		Common.Assert(val1.ftr() == val2.ftr());
		Common.Assert(val1.ftr().type() == FtrType.CATEGORICAL);
		double interSim = 0;
		for (Feature ftrK : getFtrs()) {
			if (ftrK != val1.ftr()) {
				double interAttrSim = interFtrK(val1, val2, ftrK);
				interSim += interAttrSim;
			}
		}
		return interSim / (getFtrs().size() - 1);
	}

	/** CMS sim(val1, val2). */
	@Override
	protected double calcValSim(Value val1, Value val2) throws Exception {
		return intraSim(val1, val2) / 2 + interSim(val1, val2) / 2;
	}

	/** define weighted Sim(val1, val2) */
	@Override
	protected final double getWeightedValSim(Value val1, Value val2) throws Exception {
		return getValSim(val1, val2) / this.getFtrs().size();
	}

	/**
	 * calculate the Inter-attribute Similarity of Attribute Values w.r.t.
	 * Another Attribute.<br>
	 * See EQ(7) in the CMS paper - "Coupled Metric Similarity"
	 */
	private final double interFtrK(Value val1, Value val2, Feature ftrK) throws Exception {
		if (val1 == val2) {
			return 1;
		}
		// 1. get the intersection of values 1 and 2 on feature[k]
		HashSet<Value> interK = IIF(val1, ftrK);
		interK.retainAll(IIF(val2, ftrK));
		// 2. calculate minimal over the intersection I
		double maxSum = 0, minSum = 0;
		for (Value valK : interK) {
			if (!Value.isMissing(valK)) {
				double icp1 = ICP(getOwnerObjs(valK), getOwnerObjs(val1));
				double icp2 = ICP(getOwnerObjs(valK), getOwnerObjs(val2));
				maxSum += Math.max(icp1, icp2);
				minSum += Math.min(icp1, icp2);
			}
		}
		return (maxSum == 0) ? 0 : maxSum / (2 * maxSum - minSum);
	}

	public String help() {
		return "CMS (Coupled Metrics Similarity) algorithm for measuring object-object similarities based on value-value similarities.\n"
				+ "CMS was proposed in paper: \"Coupled Metric Similarity Learning for Non-IID Categorical Data\" authored by Songlei Jian et al.\n"
				+ super.help();
	}

	@Override
	public String version() {
		return "v2.4, major revision, re-organize source code, create SimCouple as an abstract class of coupling similarity measures. 19 June 2016, Allen Lin";
	}

	public static void main(String[] args) throws Exception {
		getModule(Thread.currentThread().getStackTrace()[1].getClassName()).Main(args);
	}
}