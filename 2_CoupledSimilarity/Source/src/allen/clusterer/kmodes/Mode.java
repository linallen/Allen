package allen.clusterer.kmodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import allen.base.common.*;
import allen.sim.dataset.DataSet;
import allen.sim.dataset.Feature;
import allen.sim.dataset.FtrSet;
import allen.sim.dataset.FtrType;
import allen.sim.dataset.Obj;
import allen.sim.dataset.Value;

/**
 * The mode object in k-modes algorithm.
 * 
 * @author Allen Lin, 17 June 2016
 */
public class Mode extends FtrSet {
	/** the objs belonging to this mode (for DEBUG only) */
	private HashSet<Obj> m_objs = new HashSet<Obj>();

	/** property functions ***************************************/
	/** @return objects[] in this mode */
	public HashSet<Obj> getObjs() {
		return m_objs;
	}

	/** manipulation functions ***************************************/
	/** add an object to the mode (cluster) */
	public void addObj(Obj obj) throws Exception {
		m_objs.add(obj);
		// update value counts of the mode
		for (Value value : obj.values()) {
			Feature ftr = value.ftr();
			if (ftr.type() == FtrType.CATEGORICAL) {
				Feature modeFtr = getFtr(ftr.name());
			}
		}

		for (int i = 0; i < obj.size(); i++) {
			Value value = obj.getValue(i);
			if (!Value.isMissing(value)) {
				String valueStr = value.getValue();
				ValueFreq valFreq = m_valFreq.get(i);
				Common.Assert(valFreq != null);
				valFreq.updateFreq(valueStr, in);
			}
		}
		// 3. update values[]
		if (size() == 0) {
			// if no object left in this mode, need to move in a new object
			removeAllValues();
			return;
		}
		StringBuffer ftrValues = new StringBuffer();
		for (int i = 0; i < m_valFreq.size(); i++) {
			ValueFreq valueFreq = m_valFreq.get(i);
			String freqValue = valueFreq.getFreqValue();
			ftrValues.append(i == 0 ? "" : ",").append(freqValue);
		}
		m_data.setObjValues(this, ftrValues.toString().split(","));
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		// 1. objs[]
		sb.append(name() + " has " + m_objs.size() + " objs: ");
		for (Obj obj : m_objs) {
			sb.append(obj.name() + " ");
		}
		// 2. valFreq[]
		sb.append("| value_freq: ");
		for (ValueFreq valFreq : m_valFreq) {
			sb.append(valFreq.toString() + ", ");
		}
		return sb.toString();
	}

	/** [obj_name, obj_class, mode_name, mode_values] */
	public String toCSV() {
		StringBuffer sb = new StringBuffer();
		for (Obj obj : m_objs) {
			sb.append(obj.toCSV() + "," + name() + "," + obj.strValues() + "\n");
		}
		return sb.toString();
	}

	/** For feature[i] in this mode, its values and according frequencies */
	private class ValueFreq {
		/** mapping <ftrValue, frequency> */
		private HashMap<String, Integer> m_valueFreq = new HashMap<String, Integer>();

		/** increase or decrease by 1 the frequency of a feature value */
		public void updateFreq(String valueStr, boolean inc) {
			Integer freq = m_valueFreq.get(valueStr);
			if (inc) {
				m_valueFreq.put(valueStr, (freq == null ? 0 : freq) + 1);
			} else {
				Common.Assert(freq != null);
				m_valueFreq.put(valueStr, --freq);
				if (freq == 0) {
					m_valueFreq.remove(valueStr);
				}
			}
		}

		/** TODO return the most frequent value of this mode on feature */
		public String getFreqValue() {
			String valueMax = null;
			Integer freqMax = -1;
			for (String value : m_valueFreq.keySet()) {
				Integer freq = m_valueFreq.get(value);
				if (freq > freqMax) {
					freqMax = freq;
					valueMax = value;
				}
			}
			return valueMax;
		}

		public String toString() {
			StringBuffer sb = new StringBuffer();
			String valueMax = "";
			Integer freqMax = -1;
			for (String value : m_valueFreq.keySet()) {
				Integer freq = m_valueFreq.get(value);
				sb.append(value + "_" + freq + " ");
				if (freq > freqMax) {
					freqMax = freq;
					valueMax = value;
				}
			}
			return "(" + valueMax + "_" + freqMax + "): " + sb.toString();
		}
	}
}