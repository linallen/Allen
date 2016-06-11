package feature;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import common.Common;

public class Feature implements Serializable {
	private static final long serialVersionUID = -3112397935229769909L;

	/** feature name */
	private String m_name;

	/** feature type */
	private FtrType m_type;

	/** distinct feature values (for Categorical features only) */
	private ArrayList<String> m_ftrValues = new ArrayList<String>();

	/**
	 * instance values of this feature (inst_idx starting from 0, inst_value)
	 */
	private HashMap<Integer, Object> m_instValues = new HashMap<Integer, Object>();

	/** constructors *********************************************/
	public Feature() {
	}

	/** property functions ***************************************/
	/** set feature name */
	public void name(String name) {
		m_name = name;
	}

	/** get feature name */
	public String name() {
		return m_name;
	}

	/** set feature type */
	public void type(FtrType type) {
		m_type = type;
	}

	/** set feature type */
	public void type(String typeStr) {
		m_type = FtrType.getFtrType(typeStr);
	}

	/** get feature type */
	public FtrType type() {
		return m_type;
	}

	/** feature values[] **************************************/
	/** # of distinct feature values */
	public int ftrValueNum() {
		return m_ftrValues.size();
	}

	/** add a distinct feature value */
	public void addFtrValue(String value) {
		value = value.intern();
		if (!m_ftrValues.contains(value)) {
			m_ftrValues.add(value);
		}
	}

	/** get feature value[i] */
	public String getFtrValue(int i) {
		return m_ftrValues.get(i);
	}

	/** set feature value[i] */
	public void setFtrValue(int i, String value) {
		m_ftrValues.set(i, value);
	}

	/** get index of a feature value (for Categorical feature only) */
	public int getFtrValueIdx(String ftrValue) {
		return m_ftrValues.indexOf(ftrValue);
	}

	/** remove feature value[i] */
	public void removeFtrValue(int i) {
		m_ftrValues.remove(i);
	}

	/** remove feature values */
	public void removeFtrValues() {
		m_ftrValues.clear();
	}

	/** clear all object properties */
	public void clear() {
		m_name = null;
		m_type = null;
		m_ftrValues.clear();
		m_instValues.clear();
	}

	/** instances[] ********************************************/
	/** get # of non-null instances */
	public int instValueNum() {
		return m_instValues.size();
	}

	private static boolean isMissingValue(String value) {
		return (value == null) || value.isEmpty() || value.trim().equals("?");
	}

	/** set a new value at instance[i] */
	public void setInstValue(int i, String value) throws Exception {
		if (isMissingValue(value)) {
			return;
		}
		Object valueObj = value;
		if (type() == FtrType.CATEGORICAL) {
			valueObj = value.intern();
			if (!m_ftrValues.contains((String) valueObj)) {
				// update feature values[]. Sth wrong might have happened.
				addFtrValue((String) valueObj);
			}
		} else if (type() == FtrType.NUMERIC) {
			if (!Common.isNumeric(value)) {
				throw new Exception(value + ": non-numeric value in feature " + name());
			}
			if (Common.isInteger(value)) {
				valueObj = Integer.valueOf(value);
			} else {
				valueObj = Double.valueOf(value);
			}
		}
		// add value to instance values[]
		m_instValues.put(i, valueObj);
	}

	/** get instance value at instance[instIdx] */
	public Object getInstValue(int i) {
		return m_instValues.get(i);
	}

	/** get instance value at instance[instIdx] */
	public String getInstValueStr(int i) {
		Object valueObj = m_instValues.get(i);
		return (valueObj == null) ? "?" : valueObj.toString();
	}

	/** operation functions **************************************/
	/** replace feature value, added on 12 Feb 2015 */
	public boolean replaceFtrValue(String oldFtrValue, String newFtrValue) {
		// 1. replace feature value
		int ftrIdx = getFtrValueIdx(oldFtrValue);
		if (ftrIdx < 0) {
			return false;
		}
		setFtrValue(ftrIdx, newFtrValue);
		// 2. replace instance value
		for (int i : m_instValues.keySet()) {
			String instValue = (String) m_instValues.get(i);
			if (instValue.equals(oldFtrValue)) {
				m_instValues.put(i, newFtrValue);
			}
		}
		return true;
	}

	public void removeInstances() {
		m_instValues.clear();
	}

	/** alter feature type */
	public void alterType(String typeStr) throws Exception {
		FtrType newType = FtrType.getFtrType(typeStr);
		if (newType == null) {
			throw new Exception(Common.quote(typeStr) + " is not a valid feature type.");
		}
		if (m_type != newType) {
			// switch to NUMERIC
			if (newType == FtrType.NUMERIC) {
				for (String value : m_ftrValues) {
					if (!Common.isNumeric(value)) {
						throw new Exception("Feature" + Common.quote(name()) + " cannot be transfered to Numeric."
								+ " Non-numeric value found: " + value);
					}
				}
				m_ftrValues.clear();
			}
			// switch to CATEGORICAL
			if (newType == FtrType.CATEGORICAL) {
				m_ftrValues.clear();
				for (Object obj : m_instValues.values()) {
					addFtrValue(obj.toString());
				}
			}
			type(typeStr);
		}
	}

	/** output functions *****************************************/
	private String ftrValues() {
		String buf = new String();
		for (Object value : m_ftrValues) {
			buf += (buf.isEmpty() ? "" : ",") + value;
		}
		return "{" + buf + "}";
	}

	public String toString() {
		if (type() == FtrType.CATEGORICAL) {
			return name() + " " + ftrValues();
		}
		return name() + " " + type();
	}

	public String toArff() {
		if (type() == FtrType.CATEGORICAL) {
			return name() + " " + ftrValues();
		}
		return name() + " " + type();
	}
}