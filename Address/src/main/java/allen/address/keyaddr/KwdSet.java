package allen.address.keyaddr;

import java.util.Collection;
import java.util.HashMap;

/** Global Keyword Set: <kwdStr, kwdObj> */
public class KwdSet {
	public HashMap<String, Kwd> m_kwdSet = new HashMap<String, Kwd>();

	public int size() {
		return m_kwdSet.size();
	}

	public Kwd get(String kwdStr) {
		return m_kwdSet.get(kwdStr);
	}

	public Collection<String> getStrs() {
		return m_kwdSet.keySet();
	}

	public Collection<Kwd> getKwds() {
		return m_kwdSet.values();
	}

	public Kwd add(String kwdStr) throws Exception {
		kwdStr = kwdStr.intern();
		Kwd kwd = m_kwdSet.get(kwdStr);
		if (kwd == null) {
			kwd = new Kwd(kwdStr);
			m_kwdSet.put(kwdStr, kwd);
		}
		return kwd;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		for (String kwdStr : m_kwdSet.keySet()) {
			buf.append(get(kwdStr).toString() + "\n");
		}
		return buf.toString();
	}
}