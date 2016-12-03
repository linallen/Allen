package backup;

import java.io.Serializable;
import java.util.Collection;

import aai.base.common.Common;

/**
 * base class of Kwd and Addr. The structure is <key, indexes[]>, where
 * indexes[] are objects (Key sub-class) indexed by key.
 */
public abstract class KeyDEL implements Serializable, Comparable<KeyDEL> {
	private static final long serialVersionUID = 7337253019623573474L;

	private String m_key;

	public String getKey() {
		return m_key;
	}

	public void setKey(String key) {
		m_key = key.intern();
	}

	/** add an index */
	public abstract void addIndex(KeyDEL idx);

	/** get the indexes[] */
	public abstract Collection<KeyDEL> getIndxes();

	/** get # of indexes, i.e., it's DF (Document Frequency) */
	public int size() {
		return getIndxes().size();
	}

	/** weight of the key = 1 / size(key), IDF */
	public double wt() {
		return size();
	}

	/** length of the key, default is string length */
	public int length() {
		return m_key.length();
	}

	/** sort keys[] by 1) length in ascending order and 2) dictionary */
	public int compareTo(KeyDEL key) {
		// TODO 1st ordered by DF from high to low
		int diff = (int) Math.signum(key.wt() - this.wt());
		if (diff == 0) {
			// 2nd ordered by length from low to high
			diff = (int) Math.signum(this.length() - key.length());
			// 3rd ordered by key in alphabetical order
			if (diff == 0) {
				diff = this.getKey().compareTo(key.getKey());
			}
		}
		return diff;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append(Common.quote(m_key) + "<");
		buf.append("len=" + length() + "|");
		buf.append("wt=" + wt() + ">{");
		for (KeyDEL idx : getIndxes()) {
			buf.append("[" + idx.getKey() + "], ");
		}
		buf.trimToSize();
		return buf.append("}").toString();
	}
}