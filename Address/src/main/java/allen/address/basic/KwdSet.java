package allen.address.basic;

import java.util.Collection;
import java.util.HashMap;

import aai.base.common.AAI_IO;
import aai.base.common.Timer;
import aai.base.module.AAI_Module;

/** Global Keyword Set: <kwdStr, kwdObj> */
public class KwdSet extends AAI_Module {
	private static final long serialVersionUID = -8763440357696765066L;

	/** global kwds[] */
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

	public Kwd add(String kwdStr) {
		try {
			kwdStr = kwdStr.intern();
			Kwd kwd = m_kwdSet.get(kwdStr);
			if (kwd == null) {
				kwd = new Kwd(kwdStr);
				m_kwdSet.put(kwdStr, kwd);
			}
			return kwd;
		} catch (Exception e) {
			return null;
		}
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		for (String kwdStr : m_kwdSet.keySet()) {
			buf.append(Kwd.toString(this.get(kwdStr)) + "\n");
		}
		return buf.toString();
	}

	// TODO REVISE read indexed [kwd, addrs[]]
	public void loadKwds(String kwdFile) {
		output("Started loading kwds[] from file " + kwdFile);
		Timer timer = new Timer();
		String buf = AAI_IO.readFile(kwdFile);
		String lines[] = buf.split("\n");
		for (int i = 0; i < lines.length; i++) {
			progress(i + 1, lines.length);
			String items[] = lines[i].split(" ");
			Kwd kwd = add(items[0]);
			for (int j = 1; j < items.length; j++) {
				kwd.addAddr(Integer.parseInt(items[j]));
			}
		}
		output("Finished loading kwds[] from file. " + m_kwdSet.size() + " loaded. " + timer);
	}
}