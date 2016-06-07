package pkgDataSet;

import java.util.HashMap;

/** Class set */
public class ClsSet {
	private HashMap<String, Cls> m_clsSet = new HashMap<String, Cls>();

	public int size() {
		return m_clsSet.keySet().size();
	}

	public Cls add(String clsName) {
		Cls cls = m_clsSet.get(clsName);
		if (cls == null) {
			cls = new Cls(clsName);
			m_clsSet.put(clsName, cls);
		}
		return cls;
	}

	public Cls get(String clsName) {
		return m_clsSet.get(clsName);
	}

	// public Collection<Cls> getClsSet() {
	// return m_clsLst.values();
	// }

	public String toSummary() {
		String buf = new String();
		for (Cls cls : m_clsSet.values()) {
			buf += cls.toSummary() + "\n";
		}
		return buf;
	}

	public String toString() {
		String buf = new String();
		for (Cls cls : m_clsSet.values()) {
			buf += cls.toString() + "\n";
		}
		return buf;
	}
}
