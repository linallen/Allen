package allen.address.fuzzykwd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import allen.address.fuzzy.FuzzyKwd;
import allen.address.keyaddr.Key;

/** Stores <char ch, int pos, kwds[]>, where kwds[] contain 'ch' at pos. */
public class CharPosKwds {
	/** [index] maximum position. position from 0, 1, ..., maxPos-1 */
	private HashMap<String, HashSet<Key>> m_charPosKwds = new HashMap<String, HashSet<Key>>();

	/** [intermediate] matched kwds[] */
	// private ArrayList<FuzzyKwds> fuzzyKwdGrps = new ArrayList<FuzzyKwds>();

	public int size() {
		return m_charPosKwds.size();
	}

	private static String genKey(char ch, int pos) {
		return ch + "_" + pos;
	}

	/** add a kwd to the kwd set[] at <ch, pos> */
	public void addKwd(char ch, int pos, Key kwd) throws Exception {
		String key = genKey(ch, pos);
		HashSet<Key> kwdSet = m_charPosKwds.get(key);
		if (kwdSet == null) {
			kwdSet = new HashSet<Key>();
			m_charPosKwds.put(key, kwdSet);
		}
		kwdSet.add(kwd);
	}

	public HashSet<Key> getKwdSet(char ch, int pos) throws Exception {
		return m_charPosKwds.get(genKey(ch, pos));
	}

	/**
	 * return fuzzy kwds[] to a given key. if the key is in the global kwds[],
	 * return itself.
	 */
	public FuzzyKwd getFuzzyKwd(String kwdKey, int topKeyNum) {
		FuzzyKwd fuzzyKwd = new FuzzyKwd(kwdKey);
		// 1. search close fuzzy kwds[]
		List<Key> fuzzyKwdLst = new ArrayList<Key>(getFuzzyKwds(kwdKey, topKeyNum));
		// 2. sort and trunk fuzzy kwds[]
		Collections.sort(fuzzyKwdLst);
		fuzzyKwdLst = fuzzyKwdLst.subList(0, Math.min(fuzzyKwdLst.size(), topKeyNum));
		// 3. return fuzzy kwds[]
		fuzzyKwd.addFuzzyKwds(fuzzyKwdLst);
		return fuzzyKwd;
	}

	/**
	 * Similar idea as the fuzzy-addr matching. Suppose kwdKey="bank", then
	 * fuzzy kwds[] include bankshill, banksia, ...
	 */
	private HashSet<Key> getFuzzyKwds(String kwdKey, int topKeyNum) {
		HashSet<Key> fuzzyKwdSet = new HashSet<Key>();
		char[] chars = kwdKey.toCharArray();
		// 1. key is the leftmost part: bank...
		FuzzyKwds fuzzyKwds = new FuzzyKwds();
		for (int i = 0; (i < chars.length) && (!fuzzyKwds.hasChars() || fuzzyKwds.hasKwds()); i++) {
			HashSet<Key> kwds = m_charPosKwds.get(genKey(chars[i], i));
			fuzzyKwds.addKwds(chars[i], kwds);
		}
		fuzzyKwdSet.addAll(fuzzyKwds.getKwds());
		if (fuzzyKwdSet.size() > 0) {
			return fuzzyKwdSet;
		}
		// 2. one wrong char: *ank, b*nk, ba*k, ban*
		List<Key> fuzzyKwdsCase = new ArrayList<Key>();
		for (int i = 0; i < chars.length; i++) {
			fuzzyKwds = new FuzzyKwds();
			for (int j = 0; (j < chars.length) && (!fuzzyKwds.hasChars() || fuzzyKwds.hasKwds()); j++) {
				if (j != i) {
					HashSet<Key> kwds = m_charPosKwds.get(genKey(chars[j], j));
					fuzzyKwds.addKwds(chars[j], kwds);
				}
			}
			fuzzyKwdsCase.addAll(fuzzyKwds.getKwds());
		}
		fuzzyKwdSet.addAll(fuzzyKwdsCase);
		if (fuzzyKwdSet.size() >= topKeyNum) {
			return fuzzyKwdSet;
		}
		// 3. one extra char: *bank, b*ank, ba*nk, ban*k,
		fuzzyKwdsCase = new ArrayList<Key>();
		for (int i = 0; i < chars.length; i++) {
			fuzzyKwds = new FuzzyKwds();
			for (int j = 0; (j < chars.length) && (!fuzzyKwds.hasChars() || fuzzyKwds.hasKwds()); j++) {
				if (j != i) {
					int pos = j - ((j >= i) ? 1 : 0);
					fuzzyKwds.addKwds(chars[j], m_charPosKwds.get(genKey(chars[j], pos)));
				}
			}
			fuzzyKwdsCase.addAll(fuzzyKwds.getKwds());
		}
		fuzzyKwdSet.addAll(fuzzyKwdsCase);
		if (fuzzyKwdSet.size() >= topKeyNum) {
			return fuzzyKwdSet;
		}
		// 4. one missing char: ank, bnk, bak, ban
		fuzzyKwdsCase = new ArrayList<Key>();
		for (int i = 0; i < chars.length; i++) {
			fuzzyKwds = new FuzzyKwds();
			for (int j = 0; (j < chars.length) && (!fuzzyKwds.hasChars() || fuzzyKwds.hasKwds()); j++) {
				int pos = j + ((j >= i) ? 1 : 0);
				fuzzyKwds.addKwds(chars[j], m_charPosKwds.get(genKey(chars[j], pos)));
			}
			fuzzyKwdsCase.addAll(fuzzyKwds.getKwds());
		}
		fuzzyKwdSet.addAll(fuzzyKwdsCase);
		if (fuzzyKwdSet.size() >= topKeyNum) {
			return fuzzyKwdSet;
		}
		// 5. swap one chars: abnk, bnak, bakn
		fuzzyKwdsCase = new ArrayList<Key>();
		for (int i = 0; i < chars.length - 1; i++) {
			fuzzyKwds = new FuzzyKwds();
			for (int j = 0; (j < chars.length) && (!fuzzyKwds.hasChars() || fuzzyKwds.hasKwds()); j++) {
				if (j == i) {
					fuzzyKwds.addKwds(chars[j + 1], m_charPosKwds.get(genKey(chars[j + 1], j)));
					fuzzyKwds.addKwds(chars[j], m_charPosKwds.get(genKey(chars[j], j + 1)));
					j++;
				} else {
					fuzzyKwds.addKwds(chars[j], m_charPosKwds.get(genKey(chars[j], j)));
				}
			}
			fuzzyKwdsCase.addAll(fuzzyKwds.getKwds());
		}
		fuzzyKwdSet.addAll(fuzzyKwdsCase);
		return fuzzyKwdSet;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		for (String key : m_charPosKwds.keySet()) {
			buf.append(key + ", [");
			HashSet<Key> kwdSet = m_charPosKwds.get(key);
			for (Key kwd : kwdSet) {
				buf.append(kwd.getKey() + " ");
			}
			buf.append("]\n");
		}
		return buf.toString();
	}
}