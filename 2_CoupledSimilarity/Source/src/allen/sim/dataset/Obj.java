package allen.sim.dataset;

import java.util.Collection;
import java.util.HashMap;

import allen.base.module.AAI_Module;

/**
 * Object (or Instance) class.
 * 
 * @author Allen Lin, 17 June 2016
 */
public class Obj extends AAI_Module {
	private static final long serialVersionUID = -2548855417351450132L;

	/** value set -- valid (non-missing, non-null) values ONLY */
	private HashMap<Feature, Value> m_values = new HashMap<Feature, Value>();

	/** property functions ***************************************/
	/** size of (non-null) value set */
	public int size() {
		return m_values.size();
	}

	/** set a feature value */
	public void value(Feature ftr, Value val) {
		m_values.put(ftr, val);
	}

	/** get a feature value */
	public Value value(Feature ftr) {
		return m_values.get(ftr);
	}

	public Collection<Value> values() {
		return m_values.values();
	}

	// public void clear() {
	// m_values.clear();
	// }

	/** manipulation functions ***************************************/

	/** output functions ***************************************/
	private String valuesStr() {
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