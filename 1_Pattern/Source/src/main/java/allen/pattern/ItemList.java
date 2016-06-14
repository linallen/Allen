package allen.pattern;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

/**
 * item list.
 * 
 * NOTE: Items are separated by " ".
 * 
 * @author Allen Lin, 22 Jan 2015
 */
public class ItemList {
	/** items[] */
	protected ArrayList<Item> m_items = new ArrayList<Item>();

	/** weight of the item set */
	protected int m_weight;

	/** property functions ***************************************/
	public void weight(int weight) {
		m_weight = weight;
	}

	public int weight() {
		return m_weight;
	}

	public int size() {
		return m_items.size();
	}

	/** get item[i] */
	public Item get(int i) {
		return m_items.get(i);
	}

	/** get item[name] */
	public Item get(String name) {
		name = name.intern();
		for (Item item : m_items) {
			if (item.name().equals(name)) {
				return item;
			}
		}
		return null;
	}

	/** get items[] */
	public ArrayList<Item> items() {
		return m_items;
	}

	/** operation functions **************************************/
	/** add one item */
	public void add(Item item) {
		m_items.add(item);
	}

	/** TODO: NO USE? add a unique item to items[] */
	public void addUnique(Item item) {
		if (!m_items.contains(item)) {
			m_items.add(item);
		}
	}

	/** add items[] */
	public void addAll(ArrayList<Item> items) {
		m_items.addAll(items);
	}

	public boolean contains(Item item) {
		return m_items.contains(item);
	}

	public boolean containsAll(Collection<Item> items) {
		return m_items.containsAll(items);
	}

	/** remove duplicate items */
	public void removeDup() {
		HashSet<Item> itemSet = new HashSet<Item>(m_items);
		m_items.clear();
		m_items.addAll(itemSet);
	}

	/** remove item[i] */
	public void remove(int i) {
		m_items.remove(i);
	}

	/** remove item[last] */
	public void removeLast() {
		m_items.remove(m_items.size() - 1);
	}

	/** filter out infrequent items whose count &gt;= minSup */
	public void filter(int minSup) {
		ArrayList<Item> freqs = new ArrayList<Item>();
		for (Item item : m_items) {
			if (item.count() >= minSup) {
				freqs.add(item);
			}
		}
		m_items = freqs;
	}

	/** sort items by id in ascending order */
	public void sortItemsById(final boolean ascending) {
		Collections.sort(m_items, new Comparator<Item>() {
			// @Override
			public int compare(Item item1, Item item2) {
				return ascending ? (item1.id() - item2.id()) : (item2.id() - item1.id());
			}
		});
	}

	/** sort items by rank in ascending order */
	public void sortItemsByRank(final boolean ascending) {
		Collections.sort(m_items, new Comparator<Item>() {
			// @Override
			public int compare(Item item1, Item item2) {
				return ascending ? (item1.rank() - item2.rank()) : (item2.rank() - item1.rank());
			}
		});
	}

	/** sort items by count in decreasing order */
	public void sortItemsByCount(final boolean ascending) {
		Collections.sort(m_items, new Comparator<Item>() {
			public int compare(Item item1, Item item2) {
				return ascending ? (item1.count() - item2.count()) : (item2.count() - item1.count());
			}

		});
	}

	/** assign ranks to items (from 0 to n) */
	public void assignRanks() {
		for (int i = 0; i < m_items.size(); i++) {
			m_items.get(i).rank(i);
		}
	}

	/** output functions *****************************************/
	public String toString() {
		String buf = new String();
		for (Item item : m_items) {
			buf += (buf.isEmpty() ? "" : ", ") + item.name();
		}
		return "{" + buf + "}";
	}
}