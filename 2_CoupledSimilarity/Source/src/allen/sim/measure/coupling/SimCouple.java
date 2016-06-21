package allen.sim.measure.coupling;

import java.util.Collection;
import java.util.HashSet;

import allen.sim.dataset.Feature;
import allen.sim.dataset.Obj;
import allen.sim.dataset.Value;
import allen.sim.datastructure.ValueSim;
import allen.sim.measure.SimMeasure;

/**
 * Coupling-based similarity measures for calculating object-object similarities
 * based on value-value similarities.
 * 
 * @author Allen Lin, 20 Jan 2016
 */
public abstract class SimCouple extends SimMeasure {
	private static final long serialVersionUID = -1189132738106530336L;

	/** storing all Sim(val1, val2), val1 and val2 belong to same feature. */
	private ValueSim m_valSimPool = new ValueSim();

	/** define intra-coupled sim(val1, val2). */
	protected abstract double intraSim(Value val1, Value val2) throws Exception;

	/**
	 * define inter-coupled sim(val1, val2).
	 * 
	 * @param cateFtrs
	 *            collection of all categorical features.
	 */
	protected abstract double interSim(Value val1, Value val2) throws Exception;

	/** define sim(val1, val2). */
	protected abstract double calcValSim(Value val1, Value val2) throws Exception;

	/**
	 * For a given a value srcVal, get the objects containing it, and then
	 * return the values dstValues that the objects have on Feature[k].<br>
	 * See EQ(3.3) of COS paper.
	 */
	protected final HashSet<Value> IIF(Value srcVal, Feature ftrK) {
		HashSet<Value> valsK = new HashSet<Value>();
		for (Obj obj : getOwnerObjs(srcVal)) {
			valsK.add(obj.value(ftrK));
		}
		return valsK;
	}

	/**
	 * calculate ICP(set1, set2) = |set1 n set2|/|set2|.<br>
	 * See EQ(3.4) of COS paper.
	 */
	protected final double ICP(Collection<Obj> objs1, Collection<Obj> objs2) {
		Collection<Obj> inter = new HashSet<Obj>(objs1);
		inter.retainAll(objs2);
		return 1. * inter.size() / objs2.size();
	}

	/** @return Sim(val1, val2) */
	protected final double getValSim(Value val1, Value val2) throws Exception {
		// 1. sort val1 and val2 by id
		if (val1.id() > val2.id()) {
			Value temp = val1;
			val1 = val2;
			val2 = temp;
		}
		// 2. return sim(val1, val2)
		Double valSim = m_valSimPool.getSim(val1, val2);
		if (valSim == null) {
			valSim = calcValSim(val1, val2);
			m_valSimPool.addSim(val1, val2, valSim);
		}
		return valSim;
	}

	/** define weighted Sim(val1, val2) */
	protected abstract double getWeightedValSim(Value val1, Value val2) throws Exception;

	/** compute Sim(obj1, obj2) */
	@Override
	public final double sim(Obj objX, Obj objY) throws Exception {
		double sim = 0;
		for (Feature ftr : getFtrs()) {
			Value valX = objX.value(ftr);
			Value valY = objY.value(ftr);
			sim += getWeightedValSim(valX, valY);
		}
		return sim;
	}

	@Override
	public String help() {
		return "Coupling-based similarity measures for calculating object-object similarities based on value-value similarities.\n"
				+ super.help();
	}

	@Override
	public String version() {
		return "v1.0, No buffer version. Created on 20 Jan 2016, Allen Lin.\n"
				+ "v2.0, Buffered version. 3 Feb 2016, Allen Lin.\n"
				+ "v2.1, draw obj-obj similarity matrix in Matlab. 5 Feb 2016, Allen Lin.\n"
				+ "v2.2, added \"-t sim_type\" to support intra- and inter- similarity scores. 25 Feb 2016, Allen Lin.\n"
				+ "v2.3, extracted DataSet class from CoupleSim class, so that the DataSet class can be shared among similarity measures. 24 Mar 2016, Allen Lin\n"
				+ "v2.4, major revision. 19 June 2016, Allen Lin";
	}
}