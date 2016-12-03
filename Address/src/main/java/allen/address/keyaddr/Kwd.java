package allen.address.keyaddr;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;

/** keyword object */
public class Kwd implements Serializable, Comparable<Kwd> {
	private static final long serialVersionUID = -5757073443172228079L;

	/** kwd key */
	private String m_kwdStr;

	/** addrs[] containing this kwd */
	private HashSet<Addr> m_addrs = new HashSet<Addr>();

	public Kwd(String kwdStr) {
		m_kwdStr = kwdStr.intern();
	}

	/** add a new addr to addrs[] */
	public void addAddr(Addr addr) {
		m_addrs.add(addr);
	}

	/** TODO: REFINE weight of the key = size(key), IDF */
	public double wt() {
		return m_addrs.size();
	}

	public Collection<Addr> hostAddrs() {
		return m_addrs;
	}

	/** sort kwds[] by 1) length in ascending order and 2) dictionary */
	public int compareTo(Kwd kwd) {
		// 1st ordered by weight from high to low
		int diff = (int) Math.signum(kwd.wt() - this.wt());
		if (diff == 0) {
			// 2nd ordered by length from low to high
			diff = (int) Math.signum(m_kwdStr.length() - kwd.toString().length());
			// 3rd ordered by key in alphabetical order
			if (diff == 0) {
				diff = m_kwdStr.compareTo(kwd.toString());
			}
		}
		return diff;
	}

	public static String toFile(Kwd kwd) {
		StringBuffer sb = new StringBuffer(kwd.m_kwdStr);
		for (Addr addr : kwd.m_addrs) {
			sb.append(" ").append(addr.id);
		}
		return sb.toString();
	}

	public String toString() {
		return m_kwdStr;
	}
}