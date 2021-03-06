package allen.sim.measure.coupling;

import java.util.Collection;
import java.util.HashSet;

import allen.base.dataset.Feature;
import allen.base.dataset.Obj;
import allen.base.dataset.Value;
import allen.base.table.Matrix;
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
	private Matrix m_matValSim;

	public SimCouple() {
		// so far all SimCouple algorithms are symmetric
		symmetric(true);
		m_matValSim = new Matrix(symmetric());
	}

	/** define intra-coupled sim(val1, val2). */
	protected abstract double intraSim(Value val1, Value val2) throws Exception;

	/** define inter-coupled sim(val1, val2). */
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
			// TODO DEBUG
			Value valK = obj.getValue(ftrK);
			if (valK == null) {
				// outputDbg("value_k is null");
				continue;
			}
			valsK.add(valK);
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
		if (Value.isMissing(val1) || Value.isMissing(val2)) {
			return 0.; // TODO DEBUG
		}
		Double valSim = (Double) m_matValSim.get(val1, val2);
		if (valSim == null) {
			valSim = calcValSim(val1, val2);
			m_matValSim.put(val1, val2, valSim);
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
			Value valX = objX.getValue(ftr);
			Value valY = objY.getValue(ftr);
			// if (!Value.isMissing(valX) && !Value.isMissing(valY))
			{
				sim += getWeightedValSim(valX, valY);
			}
		}
		return sim;
	}

	public static String help() {
		return "A coupling-based similarity measure for calculating object-object similarities based on value-value similarities.\n\n"
				+ SimMeasure.help();
	}

	public static String version() {
		return "v1.0, No buffer version. Created on 20 Jan 2016, Allen Lin.\n"
				+ "v2.0, Buffered version. 3 Feb 2016, Allen Lin.\n"
				+ "v2.1, draw obj-obj similarity matrix in Matlab. 5 Feb 2016, Allen Lin.\n"
				+ "v2.2, added \"-t sim_type\" to support intra- and inter- similarity scores. 25 Feb 2016, Allen Lin.\n"
				+ "v2.3, extracted DataSet class from CoupleSim class, so that the DataSet class can be shared among similarity measures. 24 Mar 2016, Allen Lin\n"
				+ "v2.4, major revision. 19 June 2016, Allen Lin";
	}

	public static void main(String[] args) throws Exception {
		exec(Thread.currentThread().getStackTrace()[1].getClassName(), args);
	}
}