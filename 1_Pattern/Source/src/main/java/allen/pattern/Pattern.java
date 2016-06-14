package allen.pattern;

import java.util.ArrayList;
import java.util.Collection;

/**
 * pattern = item1 item2 ..., count
 * 
 * @author Allen Lin, 22 Jan 2015
 */
public class Pattern extends ItemList {
	/** count (support) of the pattern */
	private int m_count;

	/** constructors *********************************************/
	public Pattern() {
	}

	public Pattern(Item item) {
		m_items.add(item);
	}

	public Pattern(Pattern patn) {
		m_items.addAll(patn.items());
	}

	public Pattern(Pattern a, Pattern b) {
		m_items.addAll(a.items());
		m_items.addAll(b.items());
	}

	public Pattern(ArrayList<Item> items, int count) {
		addAll(items);
		m_count = count;
	}

	/** property functions ***************************************/
	/** get support or count of pattern */
	public int count() {
		return m_count;
	}

	/** operation functions **************************************/
	/** check if this pattern equals to a given sub-pattern */
	public boolean equals(Pattern patn) {
		return (size() == patn.size()) && contains(patn) && patn.contains(this);
	}

	/** check if this pattern contains a given sub-pattern */
	public boolean contains(Pattern subPatn) {
		return m_items.containsAll(subPatn.items());
	}

	public boolean contains(Collection<Item> items) {
		return m_items.containsAll(items);
	}

	/** return (this - pattern) */
	public Pattern minus(Pattern patn) {
		Pattern minus = new Pattern();
		for (Item item : m_items) {
			if (!patn.contains(item)) {
				minus.add(item);
			}
		}
		return minus;
	}

	/** calculate union = A &amp; B */
	public static Pattern union(Pattern A, Pattern B) {
		Pattern union = new Pattern();
		for (Item a : A.items()) {
			for (Item b : B.items()) {
				if (a == b) {
					union.add(a);
					break;
				}
			}
		}
		return union;
	}

	/** output functions *****************************************/
	public String toString() {
		return "{" + toCSV() + "}";
	}

	/** get string of items[]: item1 item2 ... */
	public String toStringItems() {
		String buf = new String();
		for (Item item : m_items) {
			buf += (buf.isEmpty() ? "" : " ") + item.name();
		}
		return buf;
	}

	/** pattern to CSV format: item1 item2 ..., count */
	public String toCSV() {
		String buf = new String();
		for (Item item : m_items) {
			buf += (buf.isEmpty() ? "" : " ") + item.name();
		}
		return buf + ", " + m_count;
	}
}