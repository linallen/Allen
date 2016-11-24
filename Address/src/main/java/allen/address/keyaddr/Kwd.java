package allen.address.keyaddr;

import java.util.Collection;
import java.util.HashSet;

/** keyword object */
public class Kwd extends Key {
	private static final long serialVersionUID = -5757073443172228079L;

	/** addrs[] containing this kwd */
	private HashSet<Key> m_addrs = new HashSet<Key>();

	@Override
	public void addIndex(Key idx) {
		m_addrs.add(idx);
	}

	@Override
	public Collection<Key> getIndxes() {
		return m_addrs;
	}
}