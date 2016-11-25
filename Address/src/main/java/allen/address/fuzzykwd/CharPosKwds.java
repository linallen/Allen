package allen.address.fuzzykwd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

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

	/** get the closet kwd to a given key, which is not in the global kwds[] */
	public String getClosetKwd(String kwdKey) {
		// similar idea as the fuzzy-addr matching.
		// suppose kwdKey="bank" --> bankshill, banksia
		char[] chars = kwdKey.toCharArray();
		// 1. key is the leftmost part: bank...
		ArrayList<Key> closedKwdLst = new ArrayList<Key>();
		FuzzyKwds fuzzyKwds = new FuzzyKwds();
		for (int i = 0; i < chars.length; i++) {
			HashSet<Key> kwds = m_charPosKwds.get(genKey(chars[i], i));
			fuzzyKwds.addKwds(chars[i], kwds);
			if (fuzzyKwds.isEmpty()) {
				break;
			}
		}
		if (!fuzzyKwds.isEmpty()) {
			closedKwdLst.addAll(fuzzyKwds.getKwds());
			Collections.sort(closedKwdLst);
			return closedKwdLst.get(0).getKey();
		}
		// 2. one wrong char: *ank, b*nk, ba*k, ban*
		for (int s = 0; s < chars.length; s++) {
			fuzzyKwds = new FuzzyKwds();
			for (int i = 0; i < chars.length; i++) {
				if (i == s) {
					continue;
				}
				HashSet<Key> kwds = m_charPosKwds.get(genKey(chars[i], i));
				fuzzyKwds.addKwds(chars[i], kwds);
				if (fuzzyKwds.isEmpty()) {
					break;
				}
			}
			closedKwdLst.addAll(fuzzyKwds.getKwds());
		}
		if (!closedKwdLst.isEmpty()) {
			Collections.sort(closedKwdLst);
			return closedKwdLst.get(0).getKey();
		}
		// 3. one extra char: *bank, b*ank, ba*nk, ban*k,
		for (int s = 0; s < chars.length; s++) {
			fuzzyKwds = new FuzzyKwds();
			for (int i = 0; i < chars.length; i++) {
				if (i == s) {
					continue;
				}
				int pos = i - ((i >= s) ? 1 : 0);
				fuzzyKwds.addKwds(chars[i], m_charPosKwds.get(genKey(chars[i], pos)));
				if (fuzzyKwds.isEmpty()) {
					break;
				}
			}
			closedKwdLst.addAll(fuzzyKwds.getKwds());
		}
		if (!closedKwdLst.isEmpty()) {
			Collections.sort(closedKwdLst);
			return closedKwdLst.get(0).getKey();
		}
		// 4. one missing char: ank, bnk, bak, ban
		for (int s = 0; s < chars.length; s++) {
			fuzzyKwds = new FuzzyKwds();
			for (int i = 0; i < chars.length; i++) {
				int pos = i + ((i >= s) ? 1 : 0);
				fuzzyKwds.addKwds(chars[i], m_charPosKwds.get(genKey(chars[i], pos)));
				if (fuzzyKwds.isEmpty()) {
					break;
				}
			}
			closedKwdLst.addAll(fuzzyKwds.getKwds());
		}
		if (!closedKwdLst.isEmpty()) {
			Collections.sort(closedKwdLst);
			return closedKwdLst.get(0).getKey();
		}
		// 5. swap one chars: abnk, bnak, bakn
		for (int s = 0; s < chars.length - 1; s++) {
			fuzzyKwds = new FuzzyKwds();
			for (int i = 0; i < chars.length; i++) {
				if (i == s) {
					fuzzyKwds.addKwds(chars[i + 1], m_charPosKwds.get(genKey(chars[i + 1], i)));
					fuzzyKwds.addKwds(chars[i], m_charPosKwds.get(genKey(chars[i], i + 1)));
					i++;
				} else {
					fuzzyKwds.addKwds(chars[i], m_charPosKwds.get(genKey(chars[i], i)));
				}
				if (fuzzyKwds.isEmpty()) {
					break;
				}
			}
			closedKwdLst.addAll(fuzzyKwds.getKwds());
		}
		if (!closedKwdLst.isEmpty()) {
			Collections.sort(closedKwdLst);
			return closedKwdLst.get(0).getKey();
		}
		return null;
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