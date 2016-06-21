package allen.sim.dataset;

import java.util.HashMap;

/** Class set */
public class ClsSet_DEL {
	private HashMap<String, Cls_DEL> m_clsSet = new HashMap<String, Cls_DEL>();

	public int size() {
		return m_clsSet.keySet().size();
	}

	public Cls_DEL add(String clsName) {
		Cls_DEL cls = m_clsSet.get(clsName);
		if (cls == null) {
			cls = new Cls_DEL(clsName);
			m_clsSet.put(clsName, cls);
		}
		return cls;
	}

	public Cls_DEL get(String clsName) {
		return m_clsSet.get(clsName);
	}

	// public Collection<Cls> getClsSet() {
	// return m_clsLst.values();
	// }

	public String toSummary() {
		String buf = new String();
		for (Cls_DEL cls : m_clsSet.values()) {
			buf += cls.toSummary() + "\n";
		}
		return buf;
	}

	public String toString() {
		String buf = new String();
		for (Cls_DEL cls : m_clsSet.values()) {
			buf += cls.toString() + "\n";
		}
		return buf;
	}
}
