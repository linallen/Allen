package allen.address.keyaddr;

import java.io.Serializable;
import java.util.Collection;

/**
 * base class of Kwd and Addr. The structure is <key, indexes[]>, where
 * indexes[] are objects (Key sub-class) indexed by key.
 */
public abstract class Key implements Serializable, Comparable<Key> {
	private static final long serialVersionUID = 7337253019623573474L;

	private String m_key;

	public String getKey() {
		return m_key;
	}

	public void setKey(String key) {
		m_key = key.intern();
	}

	/** add an index */
	public abstract void addIndex(Key idx);

	/** get the indexes[] */
	public abstract Collection<Key> getIndxes();

	/** get # of indexes, i.e., it's DF (Document Frequency) */
	public int size() {
		return getIndxes().size();
	}

	/** weight of the key = 1 / size(key), IDF */
	public double wt() {
		// return (size() == 0) ? 0 : 1. / size();
		return 1. / size();
	}

	/** length of the key, default is string length */
	public int length() {
		return m_key.length();
	}

	/** sort keys[] by 1) length in ascending order and 2) dictionary */
	public int compareTo(Key key) {
		// 1. by length
		int diff = (int) Math.signum(this.length() - key.length());
		// 2. by key in alphabetical order
		if (diff == 0) {
			diff = this.getKey().compareTo(key.getKey());
		}
		return diff;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer()
				.append("<\"" + m_key + "\", length = " + length() + ", indexes = " + size() + ">: {");
		for (Key idx : getIndxes()) {
			buf.append("[" + idx.getKey() + "], ");
		}
		buf.trimToSize();
		return buf.append("}").toString();
	}
}