package allen.pattern;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;

import allen.base.common.AAI_IO;
import allen.base.module.AAI_Module;

/**
 * pattern list
 * 
 * @author Allen Lin, 22 Jan 2015
 */
public class PatternList extends AAI_Module {
	private static final long serialVersionUID = -604928193127761826L;

	/** patns[] */
	private ArrayList<Pattern> m_patns = new ArrayList<Pattern>();

	/** constructors *********************************************/
	public PatternList() {
	}

	public PatternList(ArrayList<Pattern> patns) {
		if (patns != null) {
			m_patns.addAll(patns);
		}
	}

	public PatternList(Collection<Pattern> patns) {
		if (patns != null) {
			m_patns.addAll(patns);
		}
	}

	/** property functions ***************************************/
	public int size() {
		return m_patns.size();
	}

	/** return pattern[i] */
	public Pattern get(int i) {
		return m_patns.get(i);
	}

	/** get patns[] */
	public ArrayList<Pattern> patns() {
		return m_patns;
	}

	/** operation functions **************************************/
	/** check if this contains super set of items */
	public boolean containsSupper(ArrayList<Item> items) {
		for (Pattern pattern : m_patns) {
			if (pattern.m_items.containsAll(items)) {
				return true;
			}
		}
		return false;
	}

	/** check if this pattern list contains a given pattern */
	public boolean contains(Pattern patnObj) {
		for (Pattern patn : m_patns) {
			if (patn.equals(patnObj)) {
				return true;
			}
		}
		return false;
	}

	/** add a pattern to patns[] */
	public void add(Pattern patn) {
		m_patns.add(patn);
	}

	/** add a collection of patterns to patns[] */
	public void addAll(Collection<Pattern> patns) {
		m_patns.addAll(patns);
	}

	/** add a pattern uniquely to patns[] */
	public void addUnique(Pattern patn) {
		if (!contains(patn)) {
			add(patn);
		}
	}

	/** TODO: DELETE add a collection of patterns uniquely to patns[] */
	public void addAllUniqueOld(Collection<Pattern> patns) {
		for (Pattern patn : patns) {
			addUnique(patn);
		}
	}

	/** add a collection of patterns uniquely to patns[] */
	public void addAllUnique(Collection<Pattern> patns) {
		for (Pattern patn : patns) {
			patn.sortItemsById(true);
			addUnique(patn);
		}
	}

	/** TODO: NOT USED add a maximal pattern to patns[] */
	public void addPatnMax(Pattern patnMax) {
		for (int i = 0; i < m_patns.size(); i++) {
			Pattern patn = m_patns.get(i);
			// return if not a max-pattern
			if (patn.contains(patnMax)) {
				return;
			}
			// replace and return if is a super set
			if (patnMax.contains(patn)) {
				m_patns.set(i, patnMax);
				return;
			}
		}
		// add a new max-pattern
		m_patns.add(patnMax);
	}

	/** output functions *****************************************/
	public String toString() {
		String buf = new String();
		for (Pattern patn : m_patns) {
			buf += (buf.isEmpty() ? "" : " ") + patn.toString();
		}
		return buf;
	}

	/** patterns[] to CSV format: pattern1, count\n... */
	public String toCSV() {
		String buf = "pattern, count"; // CSV title row
		for (Pattern patn : m_patns) {
			buf += (buf.isEmpty() ? "" : "\n") + patn.toCSV();
		}
		return buf;
	}

	/** save patterns[] to CSV file: pattern, size, count\n... */
	public void saveCSV(String patnsCSV, boolean withCount) throws Exception {
		FileWriter writer = new FileWriter(new File(patnsCSV));
		try {
			// CSV title row
			writer.write("pattern, size" + (withCount ? ", count" : "") + "\n");
			for (int i = 0; i < m_patns.size(); i++) {
				progress(i + 1, m_patns.size());
				Pattern patn = m_patns.get(i);
				writer.write(
						patn.toStringItems() + ", " + patn.size() + (withCount ? (", " + patn.count()) : "") + "\n");
				writer.flush();
			}
		} finally {
			AAI_IO.close(writer);
		}
	}
}