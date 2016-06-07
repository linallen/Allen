package clusterer.kmodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import pkgCommon.Common;
import pkgDataSet.DataSet;
import pkgDataSet.Obj;
import pkgDataSet.Value;

/** The mode object in k-modes algorithm. */
public class Mode extends Obj {
	private static int m_modeNum = 0;

	private DataSet m_data;

	/** objects contained in this mode (for debug only) */
	private HashSet<Obj> m_objs = new HashSet<Obj>();

	/** feature values[] of this object */
	private ArrayList<ValueFreq> m_valFreq;

	public Mode(DataSet data, int ftrNum) {
		m_data = data;
		//name("Mode_" + (m_modeNum++));
		name("" + (m_modeNum++));
		m_valFreq = new ArrayList<ValueFreq>();
		for (int i = 0; i < ftrNum; i++) {
			ValueFreq valFreq = new ValueFreq();
			m_valFreq.add(valFreq);
		}
	}

	public int size() {
		return m_objs.size();
	}

	public boolean isEmpty() {
		return m_objs.size() == 0;
	}

	/** add/remove an object to/from the mode (cluster) */
	public void objAddRemove(Obj obj, boolean add) throws Exception {
		// 1. add/remove object from the mode (cluster)
		if (add) {
			m_objs.add(obj);
		} else {
			m_objs.remove(obj);
		}
		// 2. update value frequencies
		Common.Assert(obj.valueNum() <= m_valFreq.size());
		for (int i = 0; i < obj.valueNum(); i++) {
			Value value = obj.getValue(i);
			if (!Value.isMissing(value)) {
				String valueStr = value.getValue();
				ValueFreq valFreq = m_valFreq.get(i);
				Common.Assert(valFreq != null);
				valFreq.updateFreq(valueStr, add);
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