package allen.address.keyaddr;

import java.util.Collection;

/**
 * base class of Kwd and Addr. The structure is <key, indexes[]>, where
 * indexes[] are objects (Key sub-class) indexed by key.
 */
public abstract class Key {
	private String m_key;

	public String get() {
		return m_key;
	}

	public void set(String key) {
		m_key = key.intern();
	}

	/** add an index */
	public abstract void addIndex(Key idx);

	/** get the indexes[] */
	public abstract Collection<Key> getIndxes();

	/** get # of indexes */
	public int idxNum() {
		return getIndxes().size();
	}

	public int length() {
		return m_key.length();
	}

	public String toString() {
		String buf = "<" + m_key + ", length = " + length() + ", indexes = " + idxNum() + ">: {";
		for (Key idx : getIndxes()) {
			buf += "[" + idx.get() + "], ";
		}
		return buf.trim() + "}";
	}
}