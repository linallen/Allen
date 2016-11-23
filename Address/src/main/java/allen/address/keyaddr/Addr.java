package allen.address.keyaddr;

import java.util.ArrayList;
import java.util.Collection;

/** address object */
public class Addr extends Key {
	/** address real length */
	private int m_length;

	/** kwds[] contained in this addr */
	private ArrayList<Key> m_kwds = new ArrayList<Key>();

	@Override
	public int length() {
		return m_length;
	}

	/** set the length of address to its real length */
	public void length(int length) {
		// address length = all keywords in its standard format (excluding
		// building name)
		m_length = length;
	}

	@Override
	public void addIndex(Key idx) {
		m_kwds.add(idx);
	}

	@Override
	public Collection<Key> getIndxes() {
		return m_kwds;
	}
}
