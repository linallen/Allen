package pkgPatten;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

import pkgCommon.AAI_IO;
import pkgCommon.Common;
import pkgCommon.Timer;
import pkgModule.AAI_Module;

/**
 * transaction data set.
 * 
 * @author Allen Lin, 14 Jan 2015
 */
public class TranList extends AAI_Module {
	private static final long serialVersionUID = -4786338531226816989L;

	/** CSV title row */
	private String m_csvHeader = new String();

	/** transactions[] */
	private ArrayList<Tran> m_trans = new ArrayList<Tran>();

	/** item table (for debug only) */
	public ItemTbl m_itemTbl = new ItemTbl();

	/** index [item, trans[]]: item and the transactions containing it */
	private HashMap<Item, TranList> m_indexTbl = new HashMap<Item, TranList>();

	/** property functions ***************************************/
	public int size() {
		return m_trans.size();
	}

	/** get tran[i] */
	public Tran get(int i) {
		return m_trans.get(i);
	}

	public ArrayList<Tran> getTrans() {
		return m_trans;
	}

	public String csvHeader() {
		return m_csvHeader;
	}

	/** operation functions **************************************/
	/** filter out infrequent items whose count >= minSup */
	public void filter(int minSup) {
		for (Tran tran : m_trans) {
			tran.filter(minSup);
		}
	}

	/** sort items by rank in ascending order */
	public void sortItemsByRank(final boolean ascending) {
		for (Tran tran : m_trans) {
			tran.sortItemsByRank(ascending);
		}
	}

	public void add(Tran tran) {
		m_trans.add(tran);
	}

	public void addAll(TranList trans) {
		m_trans.addAll(trans.getTrans());
	}

	public void addAllUnique(TranList trans) {
		for (Tran tran : trans.getTrans()) {
			if (!m_trans.contains(tran)) {
				m_trans.add(tran);
			}
		}
	}

	public void remove(Tran tran) {
		m_trans.remove(tran);
	}

	/** OLD: load transaction data from TXT to trans[] and item_tbl. */
	public ItemTbl loadTrans(String transTXT) throws Exception {
		ItemTbl itemTbl = new ItemTbl();
		loadTrans(transTXT, itemTbl, Item.class, false);
		return itemTbl;
	}

	/** OLD: load transaction data from TXT to trans[] and item_tbl. */
	public ItemTbl loadTrans(String transTXT, Class<?> itemType) throws Exception {
		ItemTbl itemTbl = new ItemTbl();
		loadTrans(transTXT, itemTbl, itemType, false);
		return itemTbl;
	}

	/** NEW: load transaction data from CSV to trans[] and item_tbl. */
	public void loadTransCSV(String transCSV, ItemTbl itemTbl) throws Exception {
		loadTrans(transCSV, itemTbl, Item.class, true);
	}

	/**
	 * load transaction data from file to trans[] and item_tbl.
	 * 
	 * @param transFile
	 *            [input] transaction file: [item1 item2 ...]
	 * @param itemTbl
	 *            [output] item table
	 * @param itemType
	 *            [input] class type of items. Must be a sub-class of Item class
	 *            and must has a nullary constructor.
	 * @param CSV
	 *            transFile is CSV or TXT
	 */
	private void loadTrans(String transFile, ItemTbl itemTbl, Class<?> itemType, boolean CSV) throws Exception {
		output("Loading transactions started. " + transFile, true);
		Timer timer = new Timer();
		// itemType must be a sub-class of "Item" class
		Common.Assert(Common.inherit(Item.class, itemType));
		m_trans.clear();// = new ArrayList<Tran>();
		// ItemTbl itemTbl = new ItemTbl();
		// read transactions into transactions[] and item table
		BufferedReader br = new BufferedReader(new FileReader(transFile));
		try {
			long total = AAI_IO.getFileSize(transFile), finished = 0;
			String line;
			if (CSV) {
				line = br.readLine(); // skip CSV title
				m_csvHeader = line;
				progress(finished += line.length() + 2, total);
			}
			for (; (line = br.readLine()) != null;) {
				progress(finished += line.length() + 2, total);
				// a. extract items from the transaction line
				String[] itemNames = Common.distinct(line.split(SPACE));
				// b. update item table and trans[]
				Tran tran = new Tran();
				for (String itemName : itemNames) {
					Item item = (Item) itemTbl.get(itemName);
					if (item == null) {
						// item = new Item(itemName);
						item = (Item) itemType.newInstance();
						item.name(itemName);
						itemTbl.add(item);
					}
					item.countAdd(1);
					tran.add(item);
				}
				if (tran.size() > 0) {
					add(tran);
				}
			}
			output("Loading transactions finished. " + timer + ". " + size() + " transactions, " + itemTbl.size()
					+ " items.", true);
			// return itemTbl;
			m_itemTbl = itemTbl;
		} finally {
			AAI_IO.close(br);
		}
	}

	/** build index [item, trans[]] */
	public void buildIndex() {
		output("Loading index <item, trans[]> started.", true);
		Timer timer = new Timer();
		m_indexTbl = new HashMap<Item, TranList>();
		for (int i = 0; i < m_trans.size(); i++) {
			progress(i + 1, m_trans.size());
			Tran tran = m_trans.get(i);
			for (int j = 0; j < tran.size(); j++) {
				Item item = tran.get(j);
				TranList trans = m_indexTbl.get(item);
				if (trans == null) {
					trans = new TranList();
					m_indexTbl.put(item, trans);
				}
				trans.add(tran);
			}
		}
		output("Loading index <item, trans[]> finished. " + timer, true);
	}

	/** return transactions[] that contain the given item */
	public TranList getTrans(Item item) {
		return m_indexTbl.get(item);
	}

	/** output functions *****************************************/
	public String toString() {
		String buf = m_trans.size() + " transactions\n";
		// for (int i = 0; i < m_trans.size(); i++) {
		// buf += m_trans.get(i) + "\n";
		// }
		return buf;
	}
}