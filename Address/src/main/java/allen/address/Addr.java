package allen.address;

import java.util.ArrayList;

/** address object */
public class Addr {
	/** address */
	private String m_addr;
	/** address real length */
	private int m_length;

	/** kwds[] contained in this addr */
	private ArrayList<Kwd> m_kwds = new ArrayList<Kwd>();

	public Addr(String addr) {
		m_addr = addr.intern();
	}

	public String getName() {
		return m_addr;
	}

	public void addKwd(Kwd kwd) {
		m_kwds.add(kwd);
	}

	public int length() {
		return m_length;
	}

	public void length(int length) {
		m_length = length;
	}

	public String toString() {
		String buf = m_addr;
		for (Kwd kwd : m_kwds) {
			buf += ", " + kwd.getName();
		}
		return buf;
	}
}
