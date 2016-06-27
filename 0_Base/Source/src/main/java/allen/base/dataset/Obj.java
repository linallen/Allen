package allen.base.dataset;

import java.util.Collection;
import java.util.HashMap;

import allen.base.module.AAI_Module;

/**
 * Object (or Instance) class. Obj = values[] + label
 * 
 * @author Allen Lin, 17 June 2016
 */
public class Obj extends AAI_Module {
	private static final long serialVersionUID = -2548855417351450132L;

	/** 1. values[] -- valid (non-missing, non-null) values ONLY */
	private HashMap<Feature, Value> m_values = new HashMap<Feature, Value>();

	/** 2. label */
	private Value m_label;

	/** property functions ***************************************/
	/** size of (non-null) value set */
	public int size() {
		return m_values.size();
	}

	/** set a feature value */
	public void setValue(Feature ftr, Value val) {
		m_values.put(ftr, val);
	}

	/** get a feature value */
	public Value getValue(Feature ftr) {
		return m_values.get(ftr);
	}

	public Collection<Value> getValues() {
		return m_values.values();
	}

	/** specify a feature as class */
	public void setClass(Feature cls) {
		m_label = m_values.get(cls);
		m_values.remove(cls);
	}

	public Value getLabel() {
		return m_label;
	}

	/** output functions ***************************************/
	public String valuesStr() {
		String buf = new String();
		// TODO need to sort values by feature index
		for (Value value : m_values.values()) {
			String valStr = Value.isMissing(value) ? "?" : value.name();
			buf += valStr + "|";
		}
		return buf;
	}

	public String toString() {
		return name() + ": [" + valuesStr() + "]";
	}
}