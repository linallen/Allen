package allen.sim.dataset;

import allen.base.common.Common;
import allen.base.module.AAI_Module;

public class Value extends AAI_Module {
	private static final long serialVersionUID = -1869885830125983799L;

	/** owner feature (for debug only) */
	private Feature m_feature;

	/** actual value which can be compared with "==" or "!=" */
	private Object m_value;

	/** count of this CATEGORICAL value (for frequency statistics) */
	private int m_count = 1;

	public Value(String valueStr, Feature ftr) throws Exception {
		valueStr = valueStr.trim().intern();
		m_feature = ftr;
		if (ftr.type() == FtrType.NUMERIC) {
			m_value = Common.toNumber(valueStr);
		} else {
			m_value = valueStr;
		}
		name(valueStr);
	}

	public Value(Feature ftr) throws Exception {
		m_feature = ftr;
	}

	public Value deepCopy() throws Exception {
		Value valueCopy = new Value(name(), this.ftr());
		// TODO return deep copy of Value
		valueCopy.ftr();
		return valueCopy;
	}

	/** property functions ***************************************/
	public void value(String valueStr) throws Exception {
		if (ftr().type() == FtrType.NUMERIC) {
			m_value = Common.toNumber(valueStr);
		} else {
			m_value = valueStr;
		}
	}

	public Feature ftr() {
		return m_feature;
	}

	/** increase value count */
	public void countInc() {
		m_count++;
	}

	/** decrease value count */
	public void countDec() {
		m_count--;
	}

	/** set value count */
	public void count(int count) {
		m_count = count;
	}

	/** get value count */
	public int count() {
		return m_count;
	}

	/** manipulation functions ***************************************/
	public boolean equal(Value value) {
		return m_value == value.m_value;
	}

	public static boolean isMissingValue(String valueStr) {
		return (valueStr == null) || valueStr.isEmpty() || valueStr.equals("*") || valueStr.equals("?");
	}

	public static boolean isMissing(Value value) {
		return (value == null) || value.isMissing();
	}

	/** TODO no use, delete it */
	public boolean isMissing() {
		return m_value == null;
	}

	/** output functions ***************************************/
	public String toString() {
		return name();
	}

	/** get representation of object set */
	// private String strObjs() {
	// String buf = new String();
	// for (Obj obj : m_objs) {
	// buf += obj.name() + ",";
	// }
	// return buf;
	// }
}