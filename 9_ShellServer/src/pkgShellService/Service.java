package pkgShellService;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import allen.base.common.AAI_IO;
import allen.base.common.Common;
import allen.base.module.AAI_Module;
import allen.base.module.Module;
import allen.base.module.Status;
import pkgShellService.Return.RetCode;
import pkgShellService.User.UserType;

/**
 * One service thread, created by the Server, provides service for one user at a
 * time via client socket.
 * 
 * @author Allen Lin, 26 Nov 2014
 */
public class Service extends AAI_Module {
	private static final long serialVersionUID = -357356243709756353L;

	/** global service set */
	public static Set<Service> m_services = new HashSet<Service>();

	/** Server object */
	private Server m_server;

	/** the client socket via which to communicate with client */
	private Socket m_socket;

	/** the user using this service thread */
	private User m_user;

	/** system keywords */
	private static final String KWD_ADDUSER = "ADDUSER";
	private static final String KWD_VIEWUSER = "VIEWUSER";
	private static final String KWD_SWITCHUSER = "SU";
	private static final String KWD_DELUSER = "DELUSER";
	private static final String KWD_LOAD = "LOAD";
	private static final String KWD_FROM = "FROM";
	private static final String KWD_VIEWMODULE = "VIEWMODULE";
	private static final String KWD_DELMODULE = "DELMODULE";
	private static final String KWD_ALL = "ALL";
	private static final String KWD_HELP = "HELP";
	private static final String KWD_VERSION = "VERSION";

	/** system command set */
	private CmdFunc[] m_cmdFuncs;

	/** constructor of service */
	public Service(Server server, Socket socket) throws Exception {
		// initialize this service
		m_server = server;
		m_socket = socket;
		setInStream(socket.getInputStream());
		setOutStream(socket.getOutputStream());
		name("Service_" + (m_services.size() + 1));
		// create a temporary "visitor" user for this service
		// String addr = m_socket.getInetAddress() + ":" + m_socket.getPort();
		m_user = new User("Visitor", UserType.VISITOR, this);
		/** user commands supported so far */
		m_cmdFuncs = new CmdFunc[] { cmdAddUser, cmdViewUser, cmdSwitchUser, cmdDelUser, cmdLoadFrom, cmdViewModule,
				cmdDelModule, cmdHelp, cmdVersion };
	}

	/** 1. set current user using this service thread */
	public void user(User user) {
		m_user = user;
	}

	/** 1. get current user using this service thread */
	public User user() {
		return m_user;
	}

	/** check if client is still connected */
	private boolean clientConnected() {
		if ((m_user != null) && (m_user.outStream() != null)) {
			// debug
			// System.out.println("Debug: user's outStream is null!");
			m_server.outLog("Debug: user's outStream is null! " + m_user.name());
			return false;
		}
		return (m_socket != null) && !m_socket.isClosed() && m_socket.isConnected();
		// return (m_socket != null) && !m_socket.isClosed()
		// && m_socket.isConnected()
		// && ((m_user != null) && (m_user.outStream() != null));
	}

	private boolean isJson(String msg) {
		try {
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(new StringReader(msg));
			JSONObject jsonObject = (JSONObject) obj;
			return jsonObject.get("MsgType") != null;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * all current user's tasks must output streams to client via this service
	 */
	@Override
	public synchronized boolean outStream(String msg) {
		boolean success = false;
		if (clientConnected()) {
			if (isJson(msg)) {
				return super.outStream(msg + "\n");
				// System.out.print("Sent JSON: " + msg);
			} else {
				System.out.print("Non JSON found: " + msg);
				// Thread.currentThread().getStackTrace();
				new Exception().printStackTrace(); // debug
			}
		}
		return success;
	}

	/** (system) ADDUSER user_name */
	private CmdFunc cmdAddUser = new CmdFunc(KWD_ADDUSER) {
		public Return execute(String[] command) {
			if (m_user.userType() != UserType.ADMIN) {
				return new Return(RetMsg.adminCmd());
			}
			String userName = parseCommand(KWD_ADDUSER, command);
			if (userName.equalsIgnoreCase(KWD_ALL)) {
				return new Return(RetMsg.isKeyword(KWD_ALL));
			}
			if (User.m_users.get(userName) != null) {
				return new Return(RetMsg.isExist("user ", userName, true));
			}
			// create a new user account
			User user = new User(userName, UserType.USER, null);
			user.updateDirs(m_server.workDir() + user.name());
			User.m_users.put(userName, user);
			return new Return(RetCode.SUCCESS, "user " + userName + " created.");
		}
	};

	/** (system) VIEWUSER user_name | ALL */
	private CmdFunc cmdViewUser = new CmdFunc(KWD_VIEWUSER) {
		public Return execute(String[] command) {
			String userName = parseCommand(KWD_VIEWUSER, command);
			String retMsg = "user_name, status, user_type, task_num, creation_time";
			if (userName.equalsIgnoreCase(KWD_ALL)) {
				if (User.m_users.isEmpty()) {
					return new Return(RetCode.SUCCESS);
				}
				for (User user : User.m_users.values()) {
					retMsg += "\n" + user.description();
				}
				return new Return(RetCode.SUCCESS, retMsg);
			}
			User user = User.m_users.get(userName);
			if (user == null) {
				return new Return(RetCode.WARNING, RetMsg.isExist("user", userName, false));
			}
			return new Return(RetCode.SUCCESS, retMsg + "\n" + user.description());
		}
	};

	/** (system) SU user_name */
	private CmdFunc cmdSwitchUser = new CmdFunc(KWD_SWITCHUSER) {
		public synchronized Return execute(String[] command) {
			String userName = parseCommand(KWD_SWITCHUSER, command);
			User user = User.m_users.get(userName);
			if (user == null) {
				return new Return(RetMsg.isExist("user", userName, false));
			}
			if (m_user == user) {
				return new Return(RetCode.SUCCESS, "(DBG) no need to switch.");
			}
			if (user.owner() != null) {
				return new Return("user " + userName + " is busy.");
			}
			// log the event
			m_server.output(user.name() + " logged in, " + m_user.name() + " logged out.");
			user.outLog("logged in.");
			m_user.outLog("logged out.");
			// switch user
			user.login(m_user.owner());
			m_user.logout();
			m_user = user;
			name(moduleName() + "_" + m_user.name());
			return new Return(RetCode.SUCCESS, "switched to " + m_user.name());
		}
	};

	/** (system) DELUSER user_name | ALL */
	private CmdFunc cmdDelUser = new CmdFunc(KWD_DELUSER) {
		public Return execute(String[] command) {
			if (m_user.userType() != UserType.ADMIN) {
				return new Return(RetMsg.adminCmd());
			}
			String userName = parseCommand(KWD_DELUSER, command);
			if (userName.equalsIgnoreCase(KWD_ALL)) {
				String retMsg = new String();
				for (User user : User.m_users.values()) {
					if (user.userType() == UserType.ADMIN) {
						continue;
					}
					retMsg += (retMsg.isEmpty() ? "" : "\n");
					Return ret = user.delete();
					if (ret.retCode() == RetCode.SUCCESS) {
						User.m_users.remove(user.name());
						retMsg += RetMsg.deleted("user", user.name(), true);
					} else {
						retMsg += RetMsg.deleted("user", user.name(), false) + ": " + ret.retMsg();
					}
				}
				return new Return(RetCode.SUCCESS, retMsg);
			}
			User user = User.m_users.get(userName);
			if (user == null) {
				return new Return(RetMsg.isExist("user ", userName, false));
			}
			if (user.userType() == UserType.ADMIN) {
				return new Return("Admin can not be deleted.");
			}
			boolean ret = User.m_users.remove(userName) != null;
			return new Return(RetCode.SUCCESS, RetMsg.deleted("user", userName, ret));
		}
	};

	/** (system) LOAD module_name FROM module_file */
	private CmdFunc cmdLoadFrom = new CmdFunc(KWD_LOAD, KWD_FROM) {
		public Return execute(String[] command) {
			if (m_user.userType() != UserType.ADMIN) {
				return new Return(RetMsg.adminCmd());
			}
			String moduleName = parseCommand(KWD_LOAD, command);
			String moduleFile = parseCommand(KWD_FROM, command);
			if (Server.m_modules.get(moduleName) != null) {
				return new Return(RetMsg.isExist("module", moduleName, true));
			}
			// 1. load module from file
			Module module = new Module();
			try {
				module.loadModule(moduleName, moduleFile);
				// if (Common.subInstance(AAI_Module.class, module.obj())) {
				// ((AAI_Module) module.obj()).name(moduleName);
				// }
			} catch (Exception e) {
				return new Return(RetMsg.loadModule(moduleName, moduleFile, false) + " " + e);
			}
			// 2. add new module to module set
			Server.m_modules.put(moduleName, module);
			return new Return(RetCode.SUCCESS, RetMsg.loadModule(moduleName, moduleFile, true));
		}
	};

	/** (system) VIEWMODULE module_name | ALL */
	private CmdFunc cmdViewModule = new CmdFunc(KWD_VIEWMODULE) {
		public Return execute(String[] command) {
			String moduleName = parseCommand(KWD_VIEWMODULE, command);
			String retMsg = "moduleName, isAAI, isJar, main_class, jarRunnable, threadRunnable, moduleFile";
			if (moduleName.equalsIgnoreCase(KWD_ALL)) {
				if (Server.m_modules.isEmpty()) {
					return new Return(RetCode.SUCCESS);
				}
				for (String name : Server.m_modules.keySet()) {
					Module module = Server.m_modules.get(name);
					retMsg += "\n" + module.description();
				}
				return new Return(RetCode.SUCCESS, retMsg);
			}
			Module module = Server.m_modules.get(moduleName);
			if (module == null) {
				return new Return(RetCode.WARNING, RetMsg.isExist("module", moduleName, false));
			}
			return new Return(RetCode.SUCCESS, retMsg + "\n" + module.description());
		}
	};

	/** (system) DELMODULE module_name | ALL */
	private CmdFunc cmdDelModule = new CmdFunc(KWD_DELMODULE) {
		public Return execute(String[] command) {
			if (m_user.userType() != UserType.ADMIN) {
				return new Return(RetMsg.adminCmd());
			}
			String moduleName = parseCommand(KWD_DELMODULE, command);
			if (moduleName.equalsIgnoreCase(KWD_ALL)) {
				Server.m_modules.clear();
				return new Return(RetCode.SUCCESS);
			}
			return (Server.m_modules.remove(moduleName) != null) ? new Return(RetCode.SUCCESS)
					: new Return(RetMsg.isExist("module", moduleName, false));
		}
	};

	/** HELP module_name */
	private CmdFunc cmdHelp = new CmdFunc(KWD_HELP) {
		public Return execute(String[] command) {
			String moduleName = parseCommand(KWD_HELP, command);
			Module module = Server.m_modules.get(moduleName);
			if (module == null) {
				return new Return(RetMsg.isExist("module", moduleName, false));
			}
			return new Return(RetCode.SUCCESS, module.getInfo("help"));
		}
	};

	/** VERSION module_name */
	private CmdFunc cmdVersion = new CmdFunc(KWD_VERSION) {
		public Return execute(String[] command) {
			String moduleName = parseCommand(KWD_VERSION, command);
			Module module = Server.m_modules.get(moduleName);
			if (module == null) {
				return new Return(RetMsg.isExist("module", moduleName, false));
			}
			return new Return(RetCode.SUCCESS, module.getInfo("version"));
		}
	};

	/** return : [return_code return_message]. */
	public Return execCmd(String[] cmd) {
		for (CmdFunc cmdFunc : m_cmdFuncs) {
			if (cmdFunc.support(cmd)) {
				return cmdFunc.execute(cmd);
			}
		}
		return new Return(RetCode.INVALID, RetMsg.errInvalidCmd(UserType.ADMIN.toString(), Common.getString(cmd, " ")));
	}

	/** client JSON: [MsgType:"CMD", CmdId, CmdBody[]] */
	public static class JsonCmd {
		public String MsgType;
		public String CmdId;
		public ArrayList<String> CmdBody = new ArrayList<String>();
	}

	/** client JSON: [MsgType:"STATUS", TaskName] */
	public static class JsonStatus {
		public String MsgType;
		public String TaskName;
	}

	/**
	 * read JSON message from client<br>
	 * 1. [MsgType:"CMD", CmdId, CmdBody[]] or<br>
	 * 2. [MsgType:"STATUS", TaskName]
	 */
	private static Object parseJsonMsg(String clientMsg) {
		JSONParser parser = new JSONParser();
		try {
			Object obj = parser.parse(new StringReader(clientMsg));
			JSONObject jsonObject = (JSONObject) obj;
			String MsgType = (String) jsonObject.get("MsgType");
			// 1. [MsgType:"CMD", CmdId, CmdBody]
			if (MsgType.equals("CMD")) {
				JsonCmd jsonCmd = new JsonCmd();
				jsonCmd.MsgType = MsgType;
				jsonCmd.CmdId = (String) jsonObject.get("CmdId");
				JSONArray CmdBody = (JSONArray) jsonObject.get("CmdBody");
				@SuppressWarnings("unchecked")
				Iterator<String> iterator = CmdBody.iterator();
				while (iterator.hasNext()) {
					jsonCmd.CmdBody.add(iterator.next());
				}
				return jsonCmd;
			}
			// 2. [MsgType:"STATUS", TaskName]
			if (MsgType.equals("STATUS")) {
				JsonStatus JsonStatus = new JsonStatus();
				JsonStatus.MsgType = MsgType;
				JsonStatus.TaskName = (String) jsonObject.get("TaskName");
				return JsonStatus;
			}
			return null;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * write ["CMDRESP", CmdId, RetStatus, RespBody]
	 */
	@SuppressWarnings("unchecked")
	private static String writeJsonCmdResp(String cmdId, RetCode retCode, String retMsg) {
		try {
			JSONObject obj = new JSONObject();
			obj.put("MsgType", "CMDRESP");
			obj.put("CmdId", cmdId);
			obj.put("RetStatus", retCode.toString());
			obj.put("RespBody", retMsg);
			StringWriter out = new StringWriter();
			obj.writeJSONString(out);
			return out.toString();
		} catch (IOException e) {
			return null;
		}
	}

	/** write ["STATUS", TaskName, TaskStatus, Percentage, CurOption, CurJob] */
	@SuppressWarnings("unchecked")
	private static String writeJsonTaskStatus(Module task) {
		try {
			JSONObject obj = new JSONObject();
			obj.put("MsgType", "STATUS");
			obj.put("TaskName", task.name());
			obj.put("TaskStatus", task.statusStr());
			// added by Allen on 13 Apr 2015
			if (task.status() == Status.EXCEPTION) {
				obj.put("Exception", task.exception());
				obj.put("CallStack", task.callStack());
			}
			// added by Allen on 13 Apr 2015
			obj.put("Percentage", (Integer) task.progress());
			obj.put("CurOption", task.curOptions());
			obj.put("CurJob", task.curJob());
			StringWriter out = new StringWriter();
			obj.writeJSONString(out);
			return out.toString();
		} catch (IOException e) {
			return null;
		}
	}

	private String userFullName() {
		return m_user.name() + "(" + Common.IpAddrPort(m_socket) + ")";
	}

	/** trim chip from string */
	public String trim(String str, String sub) {
		// from head
		while (str.indexOf(sub) == 0) {
			m_server.outputDbg("head trim: " + Common.quote(str));
			str = str.substring(sub.length());
		}
		// from tail
		while (str.lastIndexOf(sub) == (str.length() - sub.length())) {
			m_server.outputDbg("tail trim: " + Common.quote(str));
			str = str.substring(0, str.length() - sub.length());
		}
		return str;
	}

	/** remove comment from command line */
	private static String removeComment(String command) {
		command = command.replaceAll(" +", " ").trim();
		if (command.startsWith("#")) {
			return new String();
		}
		int idx = command.indexOf(" #");
		if (idx >= 0) {
			command = command.substring(0, idx);
		}
		return command;
	}

	/** transfer options in command to be separated by "~" */
	private static String transferOpts(String command) {
		command = command.replaceAll(" +", " ").trim();
		int idx = command.indexOf(" -");
		if (idx >= 0) {
			String cmd = command.substring(0, idx);
			String opt = command.substring(idx + 1);
			opt = opt.replaceAll(SPACE, OPTSPACE);
			return cmd + " " + opt;
		}
		return command;
	}

	@Override
	public void run() {
		try {
			final int msgMaxLen = 40960;
			byte bytes[] = new byte[msgMaxLen];
			String log = new String();
			// work till die (normally or unexpectedly)
			String cltMsgsExec = new String();
			String cltMsgsBuf = new String();
			String cltMsgsPart = new String();
			while (status() != Status.EXIT) {
				// 1. read in a bunch of commands from client
				// Bug fix: receive the whole client message which may need to
				// read in multiple times
				for (int n; (n = m_inStream.read(bytes)) > 0;) {
					String newMsg = new String(bytes, 0, n);
					newMsg = newMsg.replaceAll("\r", "");
					newMsg = newMsg.replaceAll("\n+", "\n");
					newMsg = trim(newMsg, "\n");
					newMsg = trim(newMsg, "\r");
					cltMsgsBuf += newMsg;
					// debug
					m_server.outputDbg("cltMsgsBuffer: " + cltMsgsBuf);
					cltMsgsExec = new String();
					cltMsgsPart = new String();
					if (cltMsgsBuf.contains("\n")) {
						// debug
						m_server.outputDbg("spliting cltMsgsBuffer.");
						String jsonMsgs[] = cltMsgsBuf.split("\n");
						for (int i = 0; i < jsonMsgs.length; i++) {
							jsonMsgs[i] = jsonMsgs[i].trim();
							// debug
							if (parseJsonMsg(jsonMsgs[i]) == null) {
								if (i < (jsonMsgs.length - 1)) {
									String errMsg = "(non JSON exception): " + "i = " + i + ", "
											+ Common.quote(jsonMsgs[i]);
									m_server.output(errMsg);
									throw new Exception(errMsg);
								} else {
									cltMsgsPart = jsonMsgs[i];
								}
							} else {
								cltMsgsExec += ((i == 0) ? "" : "\n") + jsonMsgs[i].trim();
							}
						}
						// debug
						m_server.outputDbg("cltMsgsExec: " + cltMsgsExec);
						m_server.outputDbg("cltMsgsPart: " + cltMsgsPart);
						cltMsgsBuf = cltMsgsPart.trim();
						break;
					}
					// 10 Apr 2015
					if (parseJsonMsg(cltMsgsBuf) != null) {
						cltMsgsExec = cltMsgsBuf;
						cltMsgsBuf = new String();
						break;
					} else {
						m_server.outputDbg(Common.quote(cltMsgsBuf) + " is not JSON.");
					}
				}
				// execute cltMsgsExec[] one by one
				if (cltMsgsExec.isEmpty()) {
					continue;
				}
				// debug
				m_server.outputDbg("cltMsgsExec: " + cltMsgsExec);
				String[] cltMsgs = cltMsgsExec.trim().replaceAll(" +", " ").split("\n");
				// debug
				if (cltMsgs.length > 1) {
					for (int i = 0; i < cltMsgs.length; i++) {
						m_server.outputDbg("cltMsgs[" + i + "]: " + cltMsgs[i]);
					}
				}
				// debug
				// 2. execute client JSON messages one by one
				for (String cltMsg : cltMsgs) {
					cltMsg = cltMsg.trim().replaceAll(" +", " ");
					if (cltMsg.isEmpty()) {
						continue;
					}
					// log client JSON message
					log = "Message from " + userFullName() + ": " + cltMsg;
					m_server.output(log);
					// this.outLog(log);
					m_user.outLog(log);
					// parse client JSON message
					Object cltJson = parseJsonMsg(cltMsg);
					if (cltJson == null) {
						output("Unrecognized client message! " + log);
						continue;
					}
					// handle client message
					if (cltJson instanceof JsonCmd) {
						// 1. [MsgType:"CMD", CmdId, CmdBody]
						JsonCmd jsonCmd = (JsonCmd) cltJson;
						String CmdId = jsonCmd.CmdId;
						RetCode retCode = RetCode.SUCCESS;
						String retMsg = new String();
						int cmdNum = jsonCmd.CmdBody.size();
						log = "Found <" + cmdNum + "> command(s) in " + userFullName() + " [CmdId " + CmdId + "]: "
								+ jsonCmd.CmdBody.toString();
						m_server.output(log);
						// this.outLog(log);
						m_user.outLog(log);
						for (int i = 0; (i < cmdNum) && (retCode == RetCode.SUCCESS); i++) {
							String command = jsonCmd.CmdBody.get(i);
							// Added by Allen 13/05/2016, remove comments
							command = removeComment(command);
							// Added by Allen 13/05/2016, transfer opts -> "~"
							command = transferOpts(command);
							if (command.isEmpty()) {
								continue; // ignore comment lines
							}
							log = "[CmdId " + CmdId + ", " + (i + 1) + " of " + cmdNum + "] <-- " + command;
							m_server.output(log);
							// this.outLog(log);
							m_user.outLog(log);
							// execute command
							Return ret;
							String[] cmd = command.split(" ");
							if ((cmd.length == 1) && cmd[0].equalsIgnoreCase("exit")) {
								// ret = new Return(RetCode.SUCCESS, "EXIT");
								retMsg += "\nEXIT";
								status(Status.EXIT); // exit service
								break;
							}
							ret = execCmd(cmd); // admin command
							if (ret.retCode() == RetCode.INVALID) {
								ret = m_user.execCmd(cmd); // user command
							}
							log = "[CmdId " + CmdId + ", " + (i + 1) + " of " + cmdNum + "] --> " + ret.toString();
							m_server.output(log);
							// this.outLog(log);
							m_user.outLog(log);
							retMsg += ret + "\n";
							retCode = ret.retCode();
						}
						// response execution result to client and log it
						if (retCode != RetCode.SUCCESS) {
							retCode = RetCode.FAIL;
						}
						String resp = writeJsonCmdResp(CmdId, retCode, retMsg);
						log = (m_user.outStream(resp) ? "Succeed" : "Falied") + " to send " + userFullName() + ": "
								+ resp;
						m_user.outLog(log);
						m_server.output(log);
					} else if (cltJson instanceof JsonStatus) {
						// debug
						m_server.output("Allen Dbg (Client check task status): " + Common.quote(cltMsg));
						// 2. [MsgType:"STATUS", TaskName]
						String TaskName = ((JsonStatus) cltJson).TaskName;
						Module task = m_user.task(TaskName);
						if (task == null) {
							m_server.output("Task " + Common.quote(TaskName) + " not found.");
						} else {
							// TODO: DEBUG "status returns -2"
							String JsonStatus = writeJsonTaskStatus(task);
							// report task "STATUS" to client
							log = (m_user.outStream(JsonStatus) ? "Succeed" : "Falied") + " to response STATUS to "
									+ userFullName() + ": " + JsonStatus;
							// log the "STATUS"-checking event
							m_server.output(log);
						}
					} else {
						output("Unrecognized client message type! " + log);
					}
					if (!clientConnected()) {
						throw new Exception("client connection lost.");
					}
				}
			}
		} catch (Exception e) {
			// service stops unexpectedly
			m_exception = e;
		} finally {
			// service stops
			try {
				// assure to logout user and stop service
				String service = Common.quote(name()) + " stopped ";
				service += ((m_exception == null) ? "successfully."
						: ("with exception: " + Common.exception(m_exception)));
				m_server.output(Common.quote(m_user.name()) + " logged out. " + service);
				m_user.logout();
				User.m_users.remove(m_user);
				m_services.remove(this);
				AAI_IO.close(m_socket);
			} catch (Exception e) {
				m_server.output("Service " + name() + " post-exception: " + e);
			}
		}
	}
}