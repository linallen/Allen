package allen.sim.measure.coupling;

import java.util.HashSet;

import allen.base.common.Common;
import allen.base.dataset.Feature;
import allen.base.dataset.FtrType;
import allen.base.dataset.Value;

/**
 * COS (Coupled Object Similarity) algorithm for measuring object-object
 * similarities based on value-value similarities. COS was proposed in
 * TNNLS-2013 paper: "Coupled Attribute Similarity Learning on Categorical Data"
 * authored by Can Wang et al.
 * 
 * @author Allen Lin, 20 Jan 2016
 */
public class SimCoupleCos extends SimCouple {
	private static final long serialVersionUID = 6850842259902887554L;

	/** COS intra-coupled sim(val1, val2). */
	@Override
	protected final double intraSim(Value val1, Value val2) throws Exception {
		int objNum1 = getOwnerObjs(val1).size();
		int objNum2 = getOwnerObjs(val2).size();
		double product = 1. * objNum1 * objNum2;
		return product / (objNum1 + objNum2 + product);
	}

	/** COS inter-coupled sim(val1, val2). */
	@Override
	protected final double interSim(Value val1, Value val2) throws Exception {
		Common.Assert(val1.getFtr() == val2.getFtr());
		Common.Assert(val1.getFtr().type() == FtrType.CATEGORICAL);
		double wt = 1. / (getFtrs().size() - 1);
		double interSim = 0;
		for (Feature ftrK : getFtrs()) {
			if (ftrK != val1.getFtr()) {
				try {
					double interFtrSim = IRSI(val1, val2, ftrK);
					interSim += wt * interFtrSim;
				} catch (Exception e) {
					double interFtrSim = IRSI(val1, val2, ftrK);
					outputDbg("DEBUG: " + interFtrSim);
				}
			}
		}
		return interSim;
	}

	/** COS sim(val1, val2). */
	@Override
	protected double calcValSim(Value val1, Value val2) throws Exception {
		return intraSim(val1, val2) * interSim(val1, val2);
	}

	/** define weighted Sim(val1, val2) */
	@Override
	protected double getWeightedValSim(Value val1, Value val2) throws Exception {
		return getValSim(val1, val2);
	}

	/**
	 * calculate the IRSI similarity between values A and B (of the same
	 * feature) based on Feature[k]. EQ(5.8)
	 */
	private final double IRSI(Value val1, Value val2, Feature ftrK) throws Exception {
		// 1. get the intersection of values 1 and 2 on feature[k]
		HashSet<Value> interValK = IIF(val1, ftrK);
		interValK.retainAll(IIF(val2, ftrK));
		// 2. calculate minimal over the intersection I
		double delta = 0;
		for (Value valK : interValK) {
			double icp1 = ICP(getOwnerObjs(valK), getOwnerObjs(val1));
			double icp2 = ICP(getOwnerObjs(valK), getOwnerObjs(val2));
			delta += Math.min(icp1, icp2);
		}
		return delta;
	}

	public static String help() {
		return "COS (Coupled Object Similarity) algorithm for measuring object-object similarities based on value-value similarities.\n"
				+ "COS was proposed in TNNLS-2013 paper: \"Coupled Attribute Similarity Learning on Categorical Data\" authored by Can Wang et al.\n\n"
				+ SimCouple.help();
	}

	public static String version() {
		return "v1.0, No buffer version. Created on 20 Jan 2016, Allen Lin.\n"
				+ "v2.0, Buffered version. 3 Feb 2016, Allen Lin.\n"
				+ "v2.1, draw obj-obj similarity matrix in Matlab. 5 Feb 2016, Allen Lin.\n"
				+ "v2.2, added \"-t sim_type\" to support intra- and inter- similarity scores. 25 Feb 2016, Allen Lin.\n"
				+ "v2.3, extracted DataSet class from CoupleSim class, so that the DataSet class can be shared among similarity measures. 24 Mar 2016, Allen Lin\n"
				+ "v2.4, major revision, re-organize source code, create SimCouple as an abstract class of coupling similarity measures. 19 June 2016, Allen Lin";
	}

	public static void main(String[] args) throws Exception {
		exec(Thread.currentThread().getStackTrace()[1].getClassName(), args);
	}
}