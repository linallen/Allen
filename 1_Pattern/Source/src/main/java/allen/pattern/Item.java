package allen.pattern;

import java.util.HashMap;
import java.util.HashSet;

import allen.base.common.Common;

/**
 * item.
 * 
 * @author Allen Lin, 22 Jan 2015
 */
public class Item {
	/** global items [name, item] */
	private static HashMap<String, Item> m_globalItems = new HashMap<String, Item>();

	/** global id */
	private static int m_globalId = 0;

	/** item id */
	private int m_id = m_globalId++;

	/** item name */
	private String m_name = new String();

	/** count of this item in the database */
	private int m_count = 0;

	/** order in frequent items, starting from 1 for the most frequent item */
	private int m_rank = -1;

	/** transactions[] that contain this item. [tranId, tranObj] */
	private HashSet<Tran> m_trans;

	/** constructors *********************************************/
	public Item() {
		m_name = new String();
		m_trans = new HashSet<Tran>();
	}

	public Item(String name) {
		m_name = name;
	}

	/** property functions ***************************************/
	public void addTran(Tran tran) {
		m_trans.add(tran);
	}

	public void removeTran(Tran tran) {
		m_trans.remove(tran);
	}

	public int id() {
		return m_id;
	}

	public void id(int id) {
		m_id = id;
	}

	/** 1. get item's name */
	public String name() {
		return m_name;
	}

	/** 1. set item's name */
	public void name(String name) {
		m_name = name;
	}

	/** 2. get item's count */
	public int count() {
		return m_count;
	}

	/** 2. set item's count */
	public void count(int count) {
		m_count = count;
	}

	/** 2. increase item's count */
	public void countAdd(int count) {
		m_count += count;
	}

	/** 3. get item's rank */
	public int rank() {
		return m_rank;
	}

	/** 3. set item's rank */
	public void rank(int rank) {
		m_rank = rank;
	}

	/** operation functions **************************************/
	/** get object item[name] from global items[] */
	public static Item get(String name) {
		Item item = m_globalItems.get(name);
		if (item == null) {
			item = new Item(name);
			m_globalItems.put(name, item);
		}
		return item;
	}

	/** output functions *****************************************/
	public String toString() {
		return Common.quote(m_name) + ": rank " + m_rank + ", count " + m_count;
	}
}