package allen.address;

import allen.address.keyaddr.KeySet;

/** Stores <char ch, int pos, kwds[]>, where kwds[] contain 'ch' at pos. */
public class CharPosKwds {
	/** maximum position. position from 0, 1, ..., maxPos-1 */
	private int m_maxPos;
	private KeySet[][] m_kwdSet;

	public CharPosKwds(int maxPos) {
		m_maxPos = maxPos;
		m_kwdSet = new KeySet[26][m_maxPos];
	}

	/** add a kwd to the kwd set[] at <ch, pos> */
	public void addKwd(char ch, int pos, Object kwd) throws Exception {
//		getKwdSet(ch, pos).add(kwd);
	}

	public KeySet getKwdSet(char ch, int pos) throws Exception {
		return m_kwdSet[toLower(ch) - 'a'][pos];
	}

	private char toLower(char ch) throws Exception {
		if ((ch >= 'a') && (ch <= 'z')) {
			return ch;
		} else if ((ch >= 'A') && (ch <= 'Z')) {
			return (char) ('a' + (ch - 'A'));
		}
		throw new Exception("Non alphabet character: " + ch);
	}
}