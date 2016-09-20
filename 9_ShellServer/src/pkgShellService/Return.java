package pkgShellService;

/**
 * return result for AAI modules. [Return Code, Return Message]
 * 
 * @author Allen Lin, 27 Nov 2014
 */
public class Return {
	/** return code */
	private RetCode m_retCode = RetCode.FAIL;

	/** return message */
	private String m_retMsg = new String();

	/** constructor */
	public Return() {
	}

	public Return(RetCode retCode) {
		m_retCode = retCode;
	}

	public Return(String retMsg) {
		m_retMsg = retMsg;
	}

	public Return(RetCode retCode, String retMsg) {
		m_retCode = retCode;
		m_retMsg = retMsg;
	}

	public RetCode retCode() {
		return m_retCode;
	}

	public String retMsg() {
		return m_retMsg;
	}

	/** return code from executing a command. */
	public enum RetCode {
		SUCCESS("SUCCESS"), WARNING("WARNING"), FAIL("FAIL"), INVALID("INVALID");

		RetCode(String retCode) {
			m_retCode = retCode.intern();
		}

		private String m_retCode;

		public String toString() {
			return m_retCode;
		}
	}

	public String toString() {
		if (m_retMsg.contains("\n")) {
			return "[" + m_retCode + "]\n" + m_retMsg;
		}
		return "[" + m_retCode + "] " + m_retMsg;
	}

	public boolean toBoolean() {
		return false;

	}
}