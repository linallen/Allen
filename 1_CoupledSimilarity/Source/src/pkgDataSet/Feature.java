package pkgDataSet;

import java.util.ArrayList;
import java.util.HashMap;

import pkgCommon.Common;

public class Feature {
	/** feature name (optional) */
	private String m_ftrName = new String();

	/** value set of the feature */
	private HashMap<String, Value> m_values = new HashMap<String, Value>();

	public Feature(String ftrName) {
		m_ftrName = ftrName;
	}

	/** add value to value set, and return value object */
	public Value add(String valStr, Obj obj) {
		if (!Common.isValid(valStr)) {
			return null;
		}
		Value ftrValue = m_values.get(valStr);
		// build mapping<value, object>
		if (ftrValue == null) {
			ftrValue = new Value(this, valStr);
			m_values.put(valStr, ftrValue);
		}
		// update mapping <value, obj> for named obj
		if (!obj.name().isEmpty()) {
			ftrValue.addObj(obj);
		}
		return ftrValue;
	}

	/** add value strings to value set, and return value list */
	public ArrayList<Value> addValues(String valueStrs[]) {
		ArrayList<Value> values = new ArrayList<Value>();
		for (String valueStr : valueStrs) {
			values.add(addValue(valueStr));
		}
		return values;
	}

	/** add a value string to value set, and return value object */
	public Value addValue(String valueStr) {
		valueStr = valueStr.trim();
		// if it's a missing value
		if (Value.isMissingValue(valueStr)) {
			return null; // TODO VERIFY
		}
		//
		Value value = m_values.get(valueStr);
		if (value == null) {
			value = new Value(this, valueStr);
			m_values.put(valueStr, value);
		}
		return value;
	}

	public String getName() {
		return m_ftrName;
	}

	public Value getValue(String value) {
		return m_values.get(value);
	}

	public ArrayList<Value> valLst() {
		return new ArrayList<Value>(m_values.values());
	}

	public int getValueNum() {
		return m_values.size();
	}

	/** return mapping <value, object> */
	public String getValueObjMapping() {
		String buf = new String();
		for (Value value : m_values.values()) {
			buf += m_ftrName + ":" + value.toString() + "\n";
		}
		return buf;
	}

	public String toString() {
		String buf = new String();
		if (Common.notNullEmpty(m_ftrName)) {
			buf = m_ftrName;
		}
		return buf + "<" + strValues() + ">";
	}

	/** get representation of feature value set */
	private String strValues() {
		String buf = new String();
		for (String value : m_values.keySet()) {
			buf += value + ",";
		}
		return buf;
	}
}