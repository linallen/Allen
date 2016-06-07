package pkgSimilarity;

import java.util.HashMap;

import pkgDataSet.Value;

/**
 * Similarity scores between feature values.
 * <p>
 * Data Structure: [Value1, SimValues[]]
 * 
 * @author Allen Lin, 1 Feb 2016
 */
public class ValueSim {
	/** top K similar feature values to keep */
	// private int m_topK = Integer.MAX_VALUE;

	/** similarity table of values: [ val1, Map[val2, sim_score] ] */
	private HashMap<Value, HashMap<Value, Double>> m_valSim = new HashMap<Value, HashMap<Value, Double>>();

	public boolean isEmpty() {
		return m_valSim.isEmpty();
	}

	public void clear() {
		m_valSim.clear();
	}

	/** add sim(val1, val2) = score */
	public void addSim(Value val1, Value val2, Double simScore) {
		HashMap<Value, Double> simValues = m_valSim.get(val1);
		if (simValues == null) {
			simValues = new HashMap<Value, Double>();
			m_valSim.put(val1, simValues);
		}
		simValues.put(val2, simScore);
	}

	/** TODO retain top K similar values */
	public void retainTopK() {
		// for (Value value : m_valSim.keySet()) {
		// }
	}

	/** return sim(val1, val2) */
	private double getSimScore(Value val1, Value val2) {
		HashMap<Value, Double> simValues = m_valSim.get(val1);
		Double sim = (simValues != null) ? simValues.get(val2) : null;
		return (sim != null) ? sim : 0;
	}

	/** return sim(val1, val2) */
	public double getSim(Value val1, Value val2) {
		if (val1 == val2) {
			// return 1;
		}
		if (Value.isMissing(val1) || Value.isMissing(val2)) {
			return 0; // TODO DEBUG missing value
		}
		return Math.max(getSimScore(val1, val2), getSimScore(val2, val1));
	}

	/** return (top-K) similar values to a given value */
	public HashMap<Value, Double> getSimValues(Value value) {
		return m_valSim.get(value);
	}
}