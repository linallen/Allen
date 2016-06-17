package allen.sim.dataset;

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

	/** manipulation functions ***************************************/
	/** add value to the feature */
	public void addValue(Value value) {
		m_valSet.put(value.name(), value);
	}

	public Value getValue(String valueStr) {
		return m_valSet.get(valueStr);
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