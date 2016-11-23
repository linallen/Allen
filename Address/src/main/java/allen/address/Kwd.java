package allen.address;

import java.util.HashSet;

/** keyword object */
public class Kwd {
	/** keyword */
	private String m_kwd;

	/** addrs[] containing this kwd */
	private HashSet<Addr> m_addrs = new HashSet<Addr>();

	public Kwd(String kwd) {
		m_kwd = kwd.intern();
	}

	public String getName() {
		return m_kwd;
	}

	public void addAddr(Addr addr) {
		m_addrs.add(addr);
	}

	public int length() {
		return m_kwd.length();
	}

	public String toString() {
		String buf = m_kwd;
		for (Addr addr : m_addrs) {
			buf += ", " + addr.getName();
		}
		return buf;
	}
}