package allen.csv.converter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import allen.base.common.Common;

/**
 * Class for storing a feature.
 * <p>
 * 1. By default, a feature is NOMINAL and has a MAX_VAL_NUM-sized value set.
 * <br>
 * 2. Each time a non-missing value being added to its value set, the feature
 * will perform some checks (see below and addValue()).<br>
 * 3. When the value set reaches its capacity, if all items in the value set are
 * numerical, then the feature's type is changed to NUMERICAL, or STRING
 * otherwise. In both cases, the value set is cleared and disabled.
 */

public class Feature {
	/**
	 * nominal features with more than m_maxVals distinct values are STRINGs.
	 */
	private int m_maxVals;

	/** feature types (for ARFF) */
	public static enum ftrType {
		NUMERICAL, NOMINAL, STRING;
	}

	/** feature name */
	private String m_ftrName;
	/** feature type. NOMINAL by default */
	private ftrType m_ftrType = ftrType.NOMINAL;
	/** if all items in value set are numbers */
	private boolean m_allNumbers = true;
	private HashSet<String> m_valueSet;

	public Feature(String ftrName, int maxVals) {
		m_ftrName = ftrName;
		m_maxVals = maxVals;
		m_valueSet = new HashSet<String>(m_maxVals);
	}

	/** value set --> sorted string for output */
	private String valueSetString() {
		ArrayList<String> sortedValueSet = new ArrayList<String>(m_valueSet);
		Collections.sort(sortedValueSet);
		String buf = new String();
		for (String value : sortedValueSet) {
			buf += value + ",";
		}
		return (buf.length() > 0) ? buf.substring(0, buf.length() - 1) : "";
	}

	/** return "feature1 {a, b}, ..." */
	public String toString() {
		switch (m_ftrType) {
		case NUMERICAL:
			return m_ftrName + " numeric";
		case NOMINAL:
			if (m_allNumbers) {
				return m_ftrName + " numeric";
			}
			return m_ftrName + " {" + valueSetString() + "}";
		case STRING:
			return m_ftrName + " string";
		default:
			return m_ftrName + " unknown";
		}
	}

	/** set feature's type */
	public void setTypeString() {
		m_ftrType = ftrType.STRING;
	}

	/** add a new non-missing value to the feature. */
	public void addValue(String value) throws Exception {
		boolean valueIsNumeric = Common.isNumeric(value);
		// 1. if a NUMERICAL feature meets a string value, change it to STRING
		if ((!valueIsNumeric) && (m_ftrType == ftrType.NUMERICAL)) {
			m_ftrType = ftrType.STRING;
			m_allNumbers = false;
		}
		// 2. only NOMINAL features have value set, check if can add the value
		if ((m_ftrType == ftrType.NOMINAL) && !m_valueSet.contains(value)) {
			m_allNumbers = m_allNumbers && valueIsNumeric;
			if (m_valueSet.size() == m_maxVals) {
				m_ftrType = m_allNumbers ? ftrType.NUMERICAL : ftrType.STRING;
				m_valueSet.clear();
			} else {
				m_valueSet.add(value);
			}
		}
	}
}