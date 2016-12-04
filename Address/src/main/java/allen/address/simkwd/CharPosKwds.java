package allen.address.simkwd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import aai.base.common.AAI_IO;
import aai.base.common.Common;
import aai.base.common.Timer;
import aai.base.module.AAI_Module;
import allen.address.basic.CommFunc;
import allen.address.basic.Kwd;
import allen.address.basic.KwdSet;

/** Stores <char ch, int pos, kwds[]>, where kwds[] contain 'ch' at pos. */
public class CharPosKwds extends AAI_Module {
	private static final long serialVersionUID = -3929749576412850339L;

	/** [index] maximum position. position from 0, 1, ..., maxPos-1 */
	private HashMap<String, HashSet<Kwd>> m_charPosKwds = new HashMap<String, HashSet<Kwd>>();

	/** [intermediate] matched kwds[] */
	// private ArrayList<FuzzyKwds> fuzzyKwdGrps = new ArrayList<FuzzyKwds>();

	public int size() {
		return m_charPosKwds.size();
	}

	private static String genKey(char ch, int pos) {
		return ch + "_" + pos;
	}

	/** add a kwd to the kwd set[] at <ch, pos> */
	private void addKwd(String key, Kwd kwd) {
		HashSet<Kwd> kwdSet = m_charPosKwds.get(key);
		if (kwdSet == null) {
			kwdSet = new HashSet<Kwd>();
			m_charPosKwds.put(key, kwdSet);
		}
		kwdSet.add(kwd);
	}

	/** add a kwd to the kwd set[] at <ch, pos> */
	public void addKwd(char ch, int pos, Kwd kwd) {
		String key = genKey(ch, pos);
		addKwd(key, kwd);
	}

	public HashSet<Kwd> getKwdSet(char ch, int pos) {
		return m_charPosKwds.get(genKey(ch, pos));
	}

	/**
	 * return top fuzzy kwds[] to a given key. if the key is in the global
	 * kwds[], return itself.
	 */
	public Collection<Kwd> getSimKwd(String kwdKey, int topKeyNum) {
		// 1. search close sim kwds[]
		List<Kwd> simKwdLst = new ArrayList<Kwd>(getSimKwds(kwdKey, topKeyNum));
		// 2. sort and trunk sim kwds[]
		Collections.sort(simKwdLst);
		simKwdLst = simKwdLst.subList(0, Math.min(simKwdLst.size(), topKeyNum));
		// 3. return sim kwds[]
		return simKwdLst;
	}

	/**
	 * Same idea as the fuzzy-addr matching. Suppose kwdKey="bank", then similar
	 * kwds[] include bankshill, banksia, ...
	 */
	private HashSet<Kwd> getSimKwds(String kwdKey, int topKeyNum) {
		HashSet<Kwd> simKwdSet = new HashSet<Kwd>();
		char[] chars = kwdKey.toCharArray();
		// 1. key is the leftmost part: bank...
		SimKwds simKwds = new SimKwds();
		for (int i = 0; (i < chars.length) && (!simKwds.hasChars() || simKwds.hasKwds()); i++) {
			HashSet<Kwd> kwds = m_charPosKwds.get(genKey(chars[i], i));
			simKwds.addKwds(chars[i], kwds);
		}
		simKwdSet.addAll(simKwds.getKwds());
		if (simKwdSet.size() > 0) {
			return simKwdSet;
		}
		// 2. one wrong char: *ank, b*nk, ba*k, ban*
		List<Kwd> simKwdsCase = new ArrayList<Kwd>();
		for (int i = 0; i < chars.length; i++) {
			simKwds = new SimKwds();
			for (int j = 0; (j < chars.length) && (!simKwds.hasChars() || simKwds.hasKwds()); j++) {
				if (j != i) {
					HashSet<Kwd> kwds = m_charPosKwds.get(genKey(chars[j], j));
					simKwds.addKwds(chars[j], kwds);
				}
			}
			simKwdsCase.addAll(simKwds.getKwds());
		}
		simKwdSet.addAll(simKwdsCase);
		// 3. one extra char: *bank, b*ank, ba*nk, ban*k,
		simKwdsCase.clear();
		for (int i = 0; i < chars.length; i++) {
			simKwds = new SimKwds();
			for (int j = 0; (j < chars.length) && (!simKwds.hasChars() || simKwds.hasKwds()); j++) {
				if (j != i) {
					int pos = j - ((j >= i) ? 1 : 0);
					simKwds.addKwds(chars[j], m_charPosKwds.get(genKey(chars[j], pos)));
				}
			}
			simKwdsCase.addAll(simKwds.getKwds());
		}
		simKwdSet.addAll(simKwdsCase);
		// 4. one missing char: ank, bnk, bak, ban
		simKwdsCase.clear();
		for (int i = 0; i < chars.length; i++) {
			simKwds = new SimKwds();
			for (int j = 0; (j < chars.length) && (!simKwds.hasChars() || simKwds.hasKwds()); j++) {
				int pos = j + ((j >= i) ? 1 : 0);
				simKwds.addKwds(chars[j], m_charPosKwds.get(genKey(chars[j], pos)));
			}
			simKwdsCase.addAll(simKwds.getKwds());
		}
		simKwdSet.addAll(simKwdsCase);
		// 5. swap one chars: abnk, bnak, bakn
		simKwdsCase.clear();
		for (int i = 0; i < chars.length - 1; i++) {
			simKwds = new SimKwds();
			for (int j = 0; (j < chars.length) && (!simKwds.hasChars() || simKwds.hasKwds()); j++) {
				if (j == i) {
					simKwds.addKwds(chars[j + 1], m_charPosKwds.get(genKey(chars[j + 1], j)));
					simKwds.addKwds(chars[j], m_charPosKwds.get(genKey(chars[j], j + 1)));
					j++;
				} else {
					simKwds.addKwds(chars[j], m_charPosKwds.get(genKey(chars[j], j)));
				}
			}
			simKwdsCase.addAll(simKwds.getKwds());
		}
		simKwdSet.addAll(simKwdsCase);
		return simKwdSet;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		for (String key : m_charPosKwds.keySet()) {
			buf.append(key);
			HashSet<Kwd> kwdSet = m_charPosKwds.get(key);
			for (Kwd kwd : kwdSet) {
				buf.append(" " + Kwd.str(kwd));
			}
			buf.append("\n");
		}
		return buf.toString();
	}

	// TODO REVISE load index charPosKwds[] to file
	public void loadChPosFile(String chposKwdsFile, KwdSet kwdSet) {
		output("Started loading chPosKwds[] from file " + chposKwdsFile);
		Timer timer = new Timer();
		String buf = AAI_IO.readFile(chposKwdsFile);
		String lines[] = buf.split("\n");
		for (int i = 0; i < lines.length; i++) {
			progress(i + 1, lines.length);
			String items[] = lines[i].split(" ");
			String key = items[0];
			for (int j = 1; j < items.length; j++) {
				addKwd(key, kwdSet.add(items[j]));
			}
		}
		output("Finished loading chPosKwds[] from file. " + m_charPosKwds.size() + " loaded. " + timer);
	}
}