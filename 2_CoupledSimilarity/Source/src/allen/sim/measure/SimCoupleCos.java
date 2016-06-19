package allen.sim.measure;

import java.util.Collection;
import java.util.HashSet;

import allen.base.common.Common;
import allen.sim.dataset.Feature;
import allen.sim.dataset.FtrType;
import allen.sim.dataset.Value;

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
	protected double intraSim(Value val1, Value val2) throws Exception {
		int objNum1 = getOwnerObjs(val1).size();
		int objNum2 = getOwnerObjs(val2).size();
		double product = 1. * objNum1 * objNum2;
		return product / (objNum1 + objNum2 + product);
	}

	/** COS inter-coupled sim(val1, val2). */
	@Override
	protected double interSim(Value val1, Value val2) throws Exception {
		Common.Assert(val1.ftr() == val2.ftr());
		Common.Assert(val1.ftr().type() == FtrType.CATEGORICAL);
		Collection<Feature> cateFtrLst = this.getFtrs();
		cateFtrLst.remove(val1.ftr()); // remove current feature
		double wt = 1. / (cateFtrLst.size() - 1);
		double interObjSim = 0;
		for (Feature ftrK : cateFtrLst) {
			double interFtrSim = IRSI(val1, val2, ftrK);
			interObjSim += wt * interFtrSim;
		}
		cateFtrLst.add(val1.ftr()); // add back current feature
		return interObjSim;
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
	private double IRSI(Value val1, Value val2, Feature ftrK) throws Exception {
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

	@Override
	public String help() {
		return "COS (Coupled Object Similarity) algorithm for measuring object-object similarities based on value-value similarities.\n"
				+ "COS was proposed in TNNLS-2013 paper: \"Coupled Attribute Similarity Learning on Categorical Data\" authored by Can Wang et al.\n\n"
				+ ((SimMeasure) this).help();
	}
}