package allen.pattern;

/**
 * item table.
 * 
 * @author Allen Lin, 22 Jan 2015
 */
public class ItemTbl extends ItemList {

	/** minimal support of the item table. Default -1 (not set) */
	protected int m_minSup = -1;

	@Override
	public String toString() {
		String buf = "item, rank, count", itemStr;
		for (Item item : m_items) {
			itemStr = item.name() + ", " + item.rank() + ", " + item.count();
			buf += (buf.isEmpty() ? "" : "\n") + itemStr;
		}
		return buf;
	}
}