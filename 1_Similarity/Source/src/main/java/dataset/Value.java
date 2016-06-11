package dataset;

import java.util.HashSet;

public class Value {
	/** owner feature (for debug only) */
	private Feature m_feature;

	/** feature value */
	private String m_value = new String();

	/** objects containing this feature value */
	private HashSet<Obj> m_objs = new HashSet<Obj>();

	public Value(Feature feature, String value) {
		m_feature = feature;
		m_value = value;
	}

	public static boolean isMissingValue(String valueStr) {
		return (valueStr == null) || valueStr.isEmpty() || valueStr.equals("*") || valueStr.equals("?");
	}

	public static boolean isMissing(Value value) {
		return (value == null) || value.isMissing();
	}

	public boolean isMissing() {
		return m_value.isEmpty();
	}

	public String getValue() {
		return m_value;
	}

	/** add an object that contains this feature value */
	public void addObj(Obj obj) {
		m_objs.add(obj);
	}

	public String getFtrName() {
		return m_feature.getName();
	}

	public HashSet<Obj> getObjs() {
		return m_objs;
	}

	public int getObjNum() {
		return m_objs.size();
	}

	public String toString() {
		return m_value + "<" + strObjs() + ">";
	}

	/** get representation of object set */
	private String strObjs() {
		String buf = new String();
		for (Obj obj : m_objs) {
			buf += obj.name() + ",";
		}
		return buf;
	}
}