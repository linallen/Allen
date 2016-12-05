package allen.address.basic;

import java.io.Serializable;

/** keyword object */
public class Kwd implements Serializable, Comparable<Kwd> {
	private static final long serialVersionUID = -5757073443172228079L;

	/** kwd key */
	private String m_kwdStr;

	/** sorted addrs[] containing this kwd */
	private SortedIntLst m_addrIds = new SortedIntLst();

	public Kwd(String kwdStr) {
		m_kwdStr = kwdStr.intern();
	}

	/** add a new addr to addrs[] */
	public void addAddr(Addr addr) {
		m_addrIds.add(addr.id);
	}

	/** add a new addr to addrs[] */
	public void addAddr(Integer addrId) {
		m_addrIds.add(addrId);
	}

	/** TODO: REFINE weight of the key = size(key), IDF */
	public double wt() {
		return m_addrIds.size();
	}

	public SortedIntLst hostAddrs() {
		return m_addrIds;
	}

	/** sort kwds[] by 1) length in ascending order and 2) dictionary */
	public int compareTo(Kwd kwd) {
		// 1st ordered by weight from high to low
		int diff = (int) Math.signum(kwd.wt() - this.wt());
		if (diff == 0) {
			// 2nd ordered by length from low to high
			diff = (int) Math.signum(m_kwdStr.length() - kwd.m_kwdStr.length());
			// 3rd ordered by key in alphabetical order
			if (diff == 0) {
				diff = m_kwdStr.compareTo(kwd.m_kwdStr);
			}
		}
		return diff;
	}

	public static String toString(Kwd kwd) {
		// StringBuffer sb = new StringBuffer(kwd.m_kwdStr);
		// for (Integer addrId : kwd.m_addrIds) {
		// sb.append(" ").append(addrId);
		// }
		// return sb.toString();
		int ratio = 100 * kwd.m_addrIds.length() / kwd.m_addrIds.size();
		return kwd.m_kwdStr + ": <" + kwd.m_addrIds.length() + "/" + kwd.m_addrIds.size() + "=" + ratio + "%>"
				+ SortedIntLst.toString(kwd.m_addrIds);
	}

	public static String str(Kwd kwd) {
		return kwd.m_kwdStr;
	}
}