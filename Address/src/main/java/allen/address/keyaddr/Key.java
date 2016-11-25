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

	/** sort keys[] by length in ascending order */
	public int compareTo(Key key) {
		return (int) Math.signum(this.length() - key.length());
	}

	public String toString() {
		String buf = "<\"" + m_key + "\", length = " + length() + ", indexes = " + size() + ">: {";
		for (Key idx : getIndxes()) {
			buf += "[" + idx.getKey() + "], ";
		}
		return buf.trim() + "}";
	}
}