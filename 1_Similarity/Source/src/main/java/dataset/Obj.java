package dataset;

import java.util.ArrayList;

/** Object (or Instance) class. An Object = FtrVals[] + [class] */
public class Obj {
	/** object name (for debug only) */
	private String m_name = new String();

	/** feature values[] of this object */
	private ArrayList<Value> m_valLst = new ArrayList<Value>();

	/** class, default = null */
	private Cls m_cls;

	/** get class */
	public Cls cls() {
		return m_cls;
	}

	/** set class */
	public void cls(Cls cls) {
		m_cls = cls;
	}

	public ArrayList<Value> values() {
		return m_valLst;
	}

	public void removeAllValues() {
		m_valLst.clear();
	}

	public int valueNum() {
		return m_valLst.size();
	}

	public void setValueNum(int valueNum) {
		if (m_valLst.size() >= valueNum) {
			// shrink values[]
			for (int i = m_valLst.size() - 1; i >= valueNum; i--) {
				m_valLst.remove(i);
			}
		} else if (m_valLst.size() < valueNum) {
			// extend values[]
			for (int i = m_valLst.size(); i < valueNum; i++) {
				m_valLst.add(null);
			}
		}
	}

	public String name() {
		return m_name;
	}

	public void name(String objName) {
		m_name = objName;
	}

	public String label() {
		return m_cls == null ? "" : m_cls.getName();
	}

	/** return obj's full name = cls_name + delimitor + obj_name */
	public String fullName() {
		return label() + " " + name();
	}

	/** set value[i], i = 0, ..., n-1 */
	protected void setValue(int i, Value val) {
		if (i >= m_valLst.size()) {
			setValueNum(i + 1);
		}
		m_valLst.set(i, val);
	}

	/** get obj's value on feature[i], i = 0, ..., n-1 */
	public Value getValue(int i) {
		if (i >= 0 && i < m_valLst.size()) {
			return m_valLst.get(i);
		}
		return null; // missing value
	}

	public String toString() {
		return m_name + "<" + strValues() + ":" + m_cls.getName() + ">";
	}

	/** [obj_name, obj_class] */
	public String toCSV() {
		return m_name + "," + m_cls.getName();
	}

	/** get representation of object set */
	public String strValues() {
		String buf = new String();
		for (Value value : m_valLst) {
			String valStr = Value.isMissing(value) ? "?" : value.getValue();
			buf += valStr + "|";
		}
		return buf;
	}

	public static void main(String[] args) throws Exception {
		Obj obj = new Obj();
		obj.setValueNum(5);
		obj.setValueNum(2);
	}
}