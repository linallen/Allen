package allen.address;

import java.util.HashMap;
import java.util.HashSet;

import allen.address.keyaddr.Key;

/** Stores <char ch, int pos, kwds[]>, where kwds[] contain 'ch' at pos. */
public class CharPosKwds {
	/** maximum position. position from 0, 1, ..., maxPos-1 */
	private HashMap<String, HashSet<Key>> m_charPosKwds = new HashMap<String, HashSet<Key>>();

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