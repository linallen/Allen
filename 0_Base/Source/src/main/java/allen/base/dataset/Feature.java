package allen.base.dataset;

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
	public int size() {
		return m_valSet.size();
	}

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

	/** output functions ***************************************/
	/** return "ftr_name[value1(2)value2(4)...]" TODO DEL */
	public String getValueCounts() {
		StringBuffer sb = new StringBuffer();
		sb.append(name() + "[");
		for (Value value : this.values()) {
			if (value.count() > 0) {
				sb.append(value.getValueStr() + "(" + value.count() + ")");
			}
		}
		return sb.append("]").toString();
	}

	/** get representation of Categorical features' value set */
	private String valsStr() {
		String buf = new String();
		for (String value : m_valSet.keySet()) {
			buf += (buf.isEmpty() ? "" : ",") + value;
		}
		return buf;
	}

	public String toArff() {
		String buf = name() + " ";
		if (type() == FtrType.CATEGORICAL) {
			buf += "{" + valsStr() + "}";
		} else {
			buf += type().toString();
		}
		return buf;
	}

	public String toString() {
		String buf = name() + " (" + type() + ")";
		return buf + "<" + valsStr() + ">";
	}
}