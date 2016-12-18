package allen.address.basic;

import java.io.Serializable;

/** keyword object */
public class Kwd implements Serializable, Comparable<Kwd> {
	private static final long serialVersionUID = -5757073443172228079L;

	/** kwd key */
	private String m_kwdStr;

	/** weight of the kwd */
	private double m_wt = 0;

	/** sorted addrs[] containing this kwd */
	private OrderedLst m_addrIds = new OrderedLst();

	public Kwd(String kwdStr) {
		m_kwdStr = kwdStr.intern();
	}

	public String str() {
		return m_kwdStr;
	}

	public int length() {
		return m_addrIds.objNum();
	}

	public int size() {
		return m_addrIds.intNum();
	}

	/** add a new addr to addrs[] */
	public void addAddr(Integer addrId) {
		m_addrIds.add(addrId);
	}

	/** TODO: REFINE weight of the key = size(key), IDF */
	public double wt() {
		if (m_wt == 0 && m_addrIds.intNum() > 0) {
			// m_wt = m_addrIds.intNum();
			m_wt = 1. / m_addrIds.intNum(); // IDF
		}
		return m_wt;
	}

	public String wtStr() {
		return (int) (wt() * 10000) + "";
	}

	public OrderedLst hostAddrs() {
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

	/** [kwd, length, size, ratio%, addrs[]] */
	public String toString() {
		// int length = m_addrIds.objNum();
		// int size = m_addrIds.intNum();
		// int ratio = 100 * length / size;
		// return m_kwdStr + ": " + length + "/" + size + "=" + ratio + "%";
		return m_kwdStr;
	}
}