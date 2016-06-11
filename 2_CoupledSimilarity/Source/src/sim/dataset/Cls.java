package sim.dataset;

import java.util.ArrayList;

import pkgCommon.Common;

public class Cls {
	/** class name (optional, for debug only) */
	private String m_clsName = new String();

	/** objects belonging to this class */
	private ArrayList<Obj> m_objs = new ArrayList<Obj>();

	public Cls(String clsName) {
		m_clsName = clsName;
	}

	public int size() {
		return m_objs.size();
	}

	/** add object */
	public void add(Obj obj) {
		m_objs.add(obj);
	}

	public String getName() {
		return m_clsName;
	}

	public ArrayList<Obj> getObjLst() {
		return m_objs;
	}

	public int getObjNum() {
		return m_objs.size();
	}

	public String toSummary() {
		return getName() + ", " + size();
	}

	public String toString() {
		String buf = new String();
		if (Common.notNullEmpty(m_clsName)) {
			buf = m_clsName;
		}
		return buf + "<" + strObjs() + ">";
	}

	/** get representation of objects */
	private String strObjs() {
		String buf = new String();
		for (Obj obj : m_objs) {
			buf += obj.toString() + ",";
		}
		return buf;
	}
}