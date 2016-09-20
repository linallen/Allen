package pkgShellService;

/** Keywords used in commands */
public enum Keyword {
	/** system keywords */
	KWD_USER("USER"), KWD_LOAD("LOAD"), KWD_FROM("FROM"), KWD_SYSTEM("SYSTEM");

	private String m_name;

	Keyword(String name) {
		m_name = name.intern();
	}

	public String toString() {
		return m_name;
	}
}
