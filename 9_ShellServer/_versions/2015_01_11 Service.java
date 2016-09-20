package pkgShellService;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import pkgCommon.AAI_IO;
import pkgCommon.AAI_Module;
import pkgCommon.Common;
import pkgModule.Module;
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
		String addr = m_socket.getInetAddress() + ":" + m_socket.getPort();
		m_user = new User("visitor_" + addr, UserType.VISITOR, this);
		/** user commands supported so far */
		m_cmdFuncs = new CmdFunc[] { cmdAddUser, cmdViewUser, cmdSwitchUser,
				cmdDelUser, cmdLoadFrom, cmdViewModule, cmdDelModule, cmdHelp,
				cmdVersion };
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
		return (m_socket != null) && !m_socket.isClosed()
				&& m_socket.isConnected();
	}

	// TODO: DEBUG
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

	/** all current user's tasks must output streams to client via this service */
	@Override
	public synchronized void outStream(String msg) {
		if (clientConnected()) {
			if (isJson(msg)) {
				super.outStream(msg + "\n");
				// System.out.print("Sent JSON: " + msg);
			} else {
				System.out.print("Non JSON found: " + msg);
			}
		}
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
				return new Return(RetCode.WARNING, RetMsg.isExist("user",
						userName, false));
			}
			return new Return(RetCode.SUCCESS, retMsg + "\n"
					+ user.description());
		}
	};

	/** (system) SU user_name */
	private CmdFunc cmdSwitchUser = new CmdFunc(KWD_SWITCHUSER) {
		public synchronized Return execute(String[] command) {
			String userName = parseCommand(KWD_SWITCHUSER, command);
			User user = User.m_users.get(userName);
			if (user == null) {
				return new Return(RetMsg.isExist("user ", userName, false));
			}
			if (m_user == user) {
				return new Return(RetCode.SUCCESS, "no need to switch.");
			}
			if (user.owner() != null) {
				return new Return("user " + userName + " is busy.");
			}
			// log the event
			m_server.output(user.name() + " logged in, " + m_user.name()
					+ " logged out.");
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
						retMsg += RetMsg.deleted("user", user.name(), false)
								+ ": " + ret.retMsg();
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
			return new Return(RetCode.SUCCESS, RetMsg.deleted("user", userName,
					ret));
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
				return new Return(RetMsg.loadModule(moduleName, moduleFile,
						false) + " " + e);
			}
			// 2. add new module to module set
			Server.m_modules.put(moduleName, module);
			return new Return(RetCode.SUCCESS, RetMsg.loadModule(moduleName,
					moduleFile, true));
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
				return new Return(RetCode.WARNING, RetMsg.isExist("module",
						moduleName, false));
			}
			return new Return(RetCode.SUCCESS, retMsg + "\n"
					+ module.description());
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
			return (Server.m_modules.remove(moduleName) != null) ? new Return(
					RetCode.SUCCESS) : new Return(RetMsg.isExist("module",
					moduleName, false));
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
		return new Return(RetCode.INVALID, RetMsg.errInvalidCmd(
				UserType.ADMIN.toString(), Common.getString(cmd, " ")));
	}

	/** client JSON: [MsgType:"CMD", CmdID, CmdBody[]] */
	public static class JsonCmd {
		public String MsgType;
		public String CmdID;
		public ArrayList<String> CmdBody = new ArrayList<String>();
	}

	/** client JSON: [MsgType:"CMD", CmdID, CmdBody[]] */
	public static class JsonStatus {
		public String MsgType;
		public String TaskName;
	}

	/**
	 * read JSON message from client<br>
	 * 1. [MsgType:"CMD", CmdID, CmdBody[]] or<br>
	 * 2. [MsgType:"STATUS", TaskName]
	 */
	private static Object readJsonMsg(String clientMsg) {
		JSONParser parser = new JSONParser();
		try {
			Object obj = parser.parse(new StringReader(clientMsg));
			JSONObject jsonObject = (JSONObject) obj;
			String MsgType = (String) jsonObject.get("MsgType");
			// 1. [MsgType:"CMD", CmdID, CmdBody]
			if (MsgType.equals("CMD")) {
				JsonCmd jsonCmd = new JsonCmd();
				jsonCmd.MsgType = MsgType;
				jsonCmd.CmdID = (String) jsonObject.get("CmdID");
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

	private static Object[] readJsonMsgOld(String clientMsg) {
		ArrayList<Object> cltMsg = new ArrayList<Object>();
		JSONParser parser = new JSONParser();
		try {
			Object obj = parser.parse(new StringReader(clientMsg));
			JSONObject jsonObject = (JSONObject) obj;
			String MsgType = (String) jsonObject.get("MsgType");
			cltMsg.add(MsgType);
			// 1. [MsgType:"CMD", CmdID, CmdBody]
			if (MsgType.equals("CMD")) {
				cltMsg.add(jsonObject.get("CmdID"));
				cltMsg.add(jsonObject.get("CmdBody"));
			}
			// 2. [MsgType:"STATUS", TaskName]
			if (MsgType.equals("STATUS")) {
				cltMsg.add(jsonObject.get("TaskName"));
			}
			return cltMsg.toArray();
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * write ["CMDRESP", CmdID, RetStatus, RespBody]
	 */
	@SuppressWarnings("unchecked")
	private static String writeJsonCmdResp(String cmdId, Return ret) {
		try {
			JSONObject obj = new JSONObject();
			obj.put("MsgType", "CMDRESP");
			obj.put("CmdID", cmdId);
			obj.put("RetStatus", ret.retCode().toString());
			obj.put("RespBody", ret.retMsg());
			StringWriter out = new StringWriter();
			obj.writeJSONString(out);
			return out.toString();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * write ["STATUS", TaskName, TaskStatus, [Exception, Stack,] Percentage]<br>
	 * write ["STATUS", TaskName, TaskStatus, Percentage, Detail]<br>
	 * where TaskStatus={"RUNNING", "FINISHED", "STOPPED", "ERROR", "EXCEPTION"}
	 */
	@SuppressWarnings("unchecked")
	private static String writeJsonTaskStatus(Module task) {
		try {
			JSONObject obj = new JSONObject();
			obj.put("MsgType", "STATUS");
			obj.put("TaskName", task.name());
			obj.put("TaskStatus", task.status().toString());
			obj.put("Percentage", (Integer) task.progress());
			// TODO: write "Detail" field
			String detail = "null";
			if ((task.status() == Status.EXCEPTION)
					&& (task.m_exception != null)) {
				Exception e = task.m_exception;
				detail = e.getMessage() + " "
						+ Arrays.toString(e.getStackTrace());
			}
			if (task.status() == Status.RUNNING) {
				detail = task.currentDetail();
			}
			obj.put("Detail", detail);
			StringWriter out = new StringWriter();
			obj.writeJSONString(out);
			return out.toString();
		} catch (IOException e) {
			return null;
		}
	}

	@Override
	public void run() {
		String exception = null;
		try {
			byte bytes[] = new byte[4096];
			// work till die (normally or unexpectedly)
			while (status() != Status.STOPPED) {
				// 1. read in a bunch of commands from client
				int n = m_inStream.read(bytes);
				String[] cltMsgs = new String(bytes, 0, n).trim()
						.replaceAll(" +", " ").split("\n");
				// 2. execute commands one by one
				for (String cltMsg : cltMsgs) {
					cltMsg = cltMsg.trim().replaceAll(" +", " ");
					if (cltMsg.isEmpty()) {
						continue;
					}
					// log client JSON message
					m_server.output("Client Message: " + cltMsg);
					// parse client JSON message
					Object cltJson = readJsonMsg(cltMsg);
					// "CMD" message or "STATUS" message?
					boolean retOK = true;
					String retMsg = new String();
					if ((cltJson != null) && (cltJson instanceof JsonCmd)) {
						JsonCmd jsonCmd = (JsonCmd) cltJson;
						// 1. Client msg = [MsgType:"CMD", CmdID, CmdBody]
						for (String command : jsonCmd.CmdBody) {
							m_user.outLog(jsonCmd.CmdID + " " + command);
							// m_server.output("Client command: " + command);
							// execute command
							Return ret;
							String[] cmd = command.split(" ");
							if ((cmd.length == 1)
									&& cmd[0].equalsIgnoreCase("exit")) {
								ret = new Return(RetCode.SUCCESS, "EXIT");
								status(Status.STOPPED); // thread exit
							} else {
								if (m_user.userType() == UserType.ADMIN) {
									ret = execCmd(cmd); // admin command
								} else {
									ret = m_user.execCmd(cmd); // user command
								}
							}
							m_user.outLog(jsonCmd.CmdID + " " + ret.toString());
							// TODO
							retOK = retOK && (ret.retCode() == RetCode.SUCCESS);
							retMsg += ret.retMsg() + "\n";
							if (!retOK) {
								break;
							}
						}
						String JsonCmdResp = writeJsonCmdResp(jsonCmd.CmdID,
								ret);
						// TODO: report command execution result to client
						m_user.outStream(JsonCmdResp.toString());
						// log the "COMMAND" event
						m_server.output("Server CmdResp: "
								+ JsonCmdResp.toString());
					} else if ((cltJson != null)
							&& (cltJson instanceof JsonStatus)) {
						// 2. Client msg = [MsgType:"STATUS", TaskName]
						String TaskName = (String) cltJson[1];
						Module task = m_user.task(TaskName);
						if (task == null) {
							m_server.output("Task " + Common.quote(TaskName)
									+ " not found.");
						} else {
							String JsonStatus = writeJsonTaskStatus(task);
							// report task "STATUS" to client
							if (debug()) {
								m_user.output(JsonStatus);
							} else {
								m_user.outStream(JsonStatus);
							}
							// log the "STATUS"-checking event
							m_server.output("Status response :" + JsonStatus);
						}
					} else {
						output("Unrecognized JSON message from user "
								+ Common.quote(m_user.name()) + ": " + cltMsg);
					}
					if (!clientConnected()) {
						throw new Exception("client connection lost.");
					}
				}
			}
		} catch (Exception e) {
			// service stops unexpectedly
			exception = e.getMessage();
		} finally {
			// service stops
			try {
				// assure to logout user and stop service
				String service = Common.quote(name()) + " stopped ";
				service += ((exception == null) ? "successfully."
						: ("with exception: " + exception));
				m_server.output(Common.quote(m_user.name()) + " logged out. "
						+ service);
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