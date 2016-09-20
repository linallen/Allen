package pkgShellService;

/**
 * Syntax of command: keyword0 value0 keyword1 value1 ...
 * <p>
 * 
 * @author Allen Lin, 20 Nov 2014
 */

public abstract class CmdFunc {
	/** keywords supported by this command */
	private String[] m_keywrods;

	/** return message */
	private String m_retMsg = new String();

	/** get the message returned by the command */
	public String getRetMsg() {
		return m_retMsg;
	}

	/** initialize with supported keywords */
	public CmdFunc(String... keywords) {
		m_keywrods = new String[keywords.length];
		for (int i = 0; i < keywords.length; i++) {
			m_keywrods[i] = keywords[i].toUpperCase();
		}
	}

	/** TODO: DELETE check if commands[] fit keywords[]. */
	public static boolean fitCmdOld(String[] commands, String... keywords) {
		for (int i = 0; i < commands.length; i += 2) {
			boolean fit = false;
			for (int j = 0; j < keywords.length; j++) {
				if (keywords[j].equalsIgnoreCase(commands[i])) {
					fit = true;
					break;
				}
			}
			if (fit == false) {
				return false;
			}
		}
		return true;
	}

	/** check if commands[] fit keywords[]. */
	public static boolean fitCmd(String[] commands, String... keywords) {
		if (commands.length > keywords.length * 2) {
			return false;
		}
		for (int i = 0; i < commands.length; i += 2) {
			if (!commands[i].equalsIgnoreCase(keywords[i / 2])) {
				return false;
			}
		}
		return true;
	}

	/**
	 * return the parameter of a given keyword from command[] and remove them.
	 * <br>
	 * return null if the keyword does not exist.
	 */
	public static String parseCommand(String keyword, String[] command) {
		for (int i = 0; i < command.length; i++) {
			if (keyword.equalsIgnoreCase(command[i])) {
				command[i] = "";
				if (i < command.length - 1) {
					String parameter = command[i + 1];
					command[i + 1] = "";
					return parameter;
				}
				return "";
			}
		}
		return null;
	}

	/** check if commands[] fit keywords[]. */
	public boolean support(String[] commands) {
		if (commands.length > m_keywrods.length * 2) {
			return false;
		}
		for (int i = 0; i < commands.length; i += 2) {
			if (!commands[i].equalsIgnoreCase(m_keywrods[i / 2])) {
				return false;
			}
		}
		return true;
	}

	/**
	 * execute command and return result.
	 * 
	 * @return retCodeMsg(return_code return_message).<br>
	 */
	public abstract Return execute(String[] command);

	public String toString() {
		String buf = new String();
		for (String keyword : m_keywrods) {
			buf += keyword + " ";
		}
		return buf;
	}
}
