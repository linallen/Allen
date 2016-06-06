package pkgPatten;

import java.util.Collection;

/**
 * transaction.
 * 
 * @author Allen Lin, 22 Jan 2015
 */
public class Tran extends ItemList {
	/** construct transaction from items[] */
	public Tran() {
	}

	/** construct transaction from items[] */
	public Tran(Collection<Item> items) {
		if (items != null) {
			m_items.addAll(items);
		}
	}

	/** copy function */
	public Tran(Tran trans) {
		add(trans);
	}

	/** append a new transaction (no duplication checking) */
	public void add(Tran trans) {
		if (trans.items() != null) {
			m_items.addAll(trans.items());
		}
	}
}