package allen.sim.dataset;

import java.util.Collection;
import java.util.HashMap;

import allen.base.module.AAI_Module;

/**
 * Feature class.
 * 
 * @author Allen Lin, 17 June 2016
 */
public class Feature extends AAI_Module {
	private static final long serialVersionUID = 6148083896195395634L;

	/** feature type: NUMERIC, CATEGORICAL, STRING, DATE */
	private FtrType m_type;

	/** value set (for CATEGORICAL feature ONLY). [ftr_name, value_obj] */
	private HashMap<String, Value> m_valSet = new HashMap<String, Value>();

	/** property functions ***************************************/
	/** set feature type */
	public void type(FtrType type) {
		m_type = type;
	}

	/** get feature type */
	public FtrType type() {
		return m_type;
	}

	public Feature deepCopy() throws Exception {
		Feature ftrCopy = new Feature();
		ftrCopy.name(name());
		ftrCopy.owner(owner());
		ftrCopy.type(type());
		ftrCopy.m_valSet = new HashMap<String, Value>();
		for (String valName : m_valSet.keySet()) {
			Value value = m_valSet.get(valName).deepCopy();
			addValue(value);
		}
		return ftrCopy;
	}

	/** manipulation functions ***************************************/
	/** add value to the feature */
	public void addValue(Value value) {
		m_valSet.put(value.name(), value);
	}

	public Value getValue(String valueStr) {
		return m_valSet.get(valueStr);
	}

	public Collection<Value> values() {
		return m_valSet.values();
	}

	/** add value strings to value set, and return value list */
	// public ArrayList<Value> addValues(String valueStrs[]) {
	// ArrayList<Value> values = new ArrayList<Value>();
	// for (String valueStr : valueStrs) {
	// values.add(addValue(valueStr));
	// }
	// return values;
	// }

	// public ArrayList<Value> valLst() {
	// return new ArrayList<Value>(m_valSet.values());
	// }

	// public int getValueNum() {
	// return m_valSet.size();
	// }

	/** return mapping <value, object> */
	// public String getValueObjMapping() {
	// String buf = new String();
	// for (Value value : m_valSet.values()) {
	// buf += m_name + ":" + value.toString() + "\n";
	// }
	// return buf;
	// }

	/** return "ftr_name[value1(2)value2(4)...]" */
	public String getValueCounts() {
		StringBuffer sb = new StringBuffer();
		sb.append(name() + "[");
		for (Value value : this.values()) {
			if (value.count() > 0) {
				sb.append(value.name() + "(" + value.count() + ")");
			}
		}
		return sb.append("]").toString();
	}

	public String toString() {
		String buf = name() + " (" + type() + ")";
		return buf + "<" + strValues() + ">";
	}

	/** get representation of feature value set */
	private String strValues() {
		String buf = new String();
		for (String value : m_valSet.keySet()) {
			buf += value + ",";
		}
		return buf;
	}
}