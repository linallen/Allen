package pkgShellService;

import java.util.concurrent.ConcurrentHashMap;

import allen.base.common.Common;
import allen.base.module.AAI_Module;
import allen.base.module.Module;
import allen.base.module.Status;
import pkgShellService.Return.RetCode;

/**
 * Each User object servers one user with one task manager.
 * 
 * @author Allen Lin, 26 Nov 2014
 */
public class User extends AAI_Module {
	private static final long serialVersionUID = -3425024846166684465L;

	/** global user set */
	public static ConcurrentHashMap<String, User> m_users = new ConcurrentHashMap<String, User>();

	/** user type: admin, user, or visitor */
	private UserType m_userType;

	/** user type: admin, user or anonymous */
	public enum UserType {
		ADMIN("Admin"), USER("User"), VISITOR("Visitor");
		private String m_type;

		UserType(String type) {
			m_type = type;
		}

		public String toString() {
			return m_type;
		}
	}

	/** user's task set. */
	private transient ConcurrentHashMap<String, Module> m_tasks = new ConcurrentHashMap<String, Module>();

	/** user keywords */
	private static final String KWD_CREATE = "CREATE";
	private static final String KWD_FROM = "FROM";
	private static final String KWD_VIEW = "VIEW";
	private static final String KWD_DELETE = "DELETE";
	private static final String KWD_ADD = "ADD";
	private static final String KWD_RUN = "RUN";
	private static final String KWD_OPTIONS = "OPTIONS";
	private static final String KWD_STOP = "STOP";
	private static final String KWD_PAUSE = "PAUSE";
	private static final String KWD_RESUME = "RESUME";
	private static final String KWD_ALL = "ALL";

	/** user command set */
	private CmdFunc[] m_cmdFuncs;

	/** [user_name, user_type, owner, ] */
	public User(String userName, UserType userType, AAI_Module owner) {
		name((userName != null) ? userName : userType.toString().toLowerCase());
		m_userType = userType;
		owner(owner);
		setOutStream(null);
		setInStream(null);
		/** user commands */
		m_cmdFuncs = new CmdFunc[] { cmdCreateFrom, cmdAddOptions, cmdRunOptions, cmdStop, cmdPause, cmdResume,
				cmdDelete, cmdView };
	}

	public Module task(String TaskName) {
		return m_tasks.get(TaskName);
	}

	/** 2. get user type */
	public UserType userType() {
		return m_userType;
	}

	/** user logout: unlink service */
	public void logout() {
		owner(null);
	}

	/** user login: link to an active service */
	public void login(AAI_Module owner) {
		owner(owner);
	}

	public boolean online() {
		return owner() != null;
	}

	public Return delete() {
		if (online()) {
			return new Return("user " + name() + " is online.");
		}
		for (AAI_Module task : m_tasks.values()) {
			if (task.status() == Status.RUNNING) {
				return new Return("stop running task(s) first.");
			}
		}
		return new Return(RetCode.SUCCESS);
	}

	/** CREATE task_name FROM module_name */
	private CmdFunc cmdCreateFrom = new CmdFunc(KWD_CREATE, KWD_FROM) {
		public Return execute(String[] command) {
			String taskName = parseCommand(KWD_CREATE, command);
			String moduleName = parseCommand(KWD_FROM, command);
			if (taskName.equalsIgnoreCase(KWD_ALL)) {
				return new Return(RetMsg.isKeyword(KWD_ALL));
			}
			if (m_tasks.get(taskName) != null) {
				return new Return(RetMsg.isExist("task", taskName, true));
			}
			Module module = Server.m_modules.get(moduleName);
			if (module == null) {
				return new Return(RetMsg.isExist("module", moduleName, false));
			}
			// create a new task (module object) by copying module and start it
			try {
				Module task = module.newInstance(taskName, getThis());
				task.output("Task created successfully.");
				// task.goSleep();
				// task.start();
				m_tasks.put(taskName, task);
				return new Return(RetCode.SUCCESS, RetMsg.createTask(task.name(), true));
			} catch (Exception e) {
				return new Return(RetMsg.createTask(taskName, false) + " " + e);
			}
		}
	};

	/**
	 * TODO added by Allen 12/05/2016<br>
	 * ADD task_name OPTIONS options - add OPTIONS to task's option queue ONLY
	 */
	private CmdFunc cmdAddOptions = new CmdFunc(KWD_ADD, KWD_OPTIONS) {
		public Return execute(String[] command) {
			String taskName = parseCommand(KWD_ADD, command);
			String options = parseCommand(KWD_OPTIONS, command);
			options = (options == null) ? "" : options;
			if (!options.isEmpty()) {
				Module task = m_tasks.get(taskName);
				if (task == null) {
					return new Return(RetMsg.isExist("task", taskName, false));
				}
				options = options.replace(OPTSPACE, " ").trim();
				options = options.replaceAll(" +", " ").trim();
				task.addOptions(options.split(" "));
				task.daemon(true);
			}
			return new Return(RetCode.SUCCESS);
		}
	};

	/** RUN task_name [OPTIONS options] */
	private CmdFunc cmdRunOptions = new CmdFunc(KWD_RUN, KWD_OPTIONS) {
		public Return execute(String[] command) {
			String taskName = parseCommand(KWD_RUN, command);
			String options = parseCommand(KWD_OPTIONS, command);
			options = ((options == null) ? "" : options).trim();
			Module task = m_tasks.get(taskName);
			if (task == null) {
				return new Return(RetMsg.isExist("task", taskName, false));
			}
			if (!options.isEmpty()) {
				options = options.replace(OPTSPACE, " ").trim();
				options = options.replaceAll(" +", " ").trim();
				task.addOptions(options.split(" "));
			}
			task.daemon(true);
			boolean started = task.start();
			// return new Return(RetCode.SUCCESS);
			return new Return(RetCode.SUCCESS, RetMsg.taskStarted(task.name(), started, options));
		}
	};

	/** STOP task_name */
	private CmdFunc cmdStop = new CmdFunc(KWD_STOP) {
		public Return execute(String[] command) {
			String taskName = parseCommand(KWD_STOP, command);
			Module task = m_tasks.get(taskName);
			if (task == null) {
				return new Return(RetMsg.isExist("task", taskName, false));
			}
			task.stop();
			return new Return(RetCode.SUCCESS);
		}
	};

	/** TODO PAUSE task_name */
	private CmdFunc cmdPause = new CmdFunc(KWD_PAUSE) {
		public Return execute(String[] command) {
			String taskName = parseCommand(KWD_PAUSE, command);
			Module task = m_tasks.get(taskName);
			if (task == null) {
				return new Return(RetMsg.isExist("task", taskName, false));
			}
			if (task.pause()) {
				return new Return(RetCode.SUCCESS);
			} else {
				return new Return(RetCode.FAIL, "Can not pause task " + Common.quote(task.name()));
			}
		}
	};

	/** TODO RESUME task_name */
	private CmdFunc cmdResume = new CmdFunc(KWD_RESUME) {
		public Return execute(String[] command) {
			String taskName = parseCommand(KWD_RESUME, command);
			Module task = m_tasks.get(taskName);
			if (task == null) {
				return new Return(RetMsg.isExist("task", taskName, false));
			}
			task.resume();
			return new Return(RetCode.SUCCESS);
		}
	};

	/** DELETE {task_name | ALL} */
	private CmdFunc cmdDelete = new CmdFunc(KWD_DELETE) {
		public Return execute(String[] command) {
			String taskName = parseCommand(KWD_DELETE, command);
			if (taskName.equalsIgnoreCase(KWD_ALL)) {
				String retMsg = new String();
				boolean successAll = true;
				for (Module task : m_tasks.values()) {
					if (task.status() == Status.RUNNING) {
						retMsg += RetMsg.isRunning(task.name(), true) + "\n";
					} else {
						task.exit();
						boolean success = m_tasks.remove(task.name()) != null;
						successAll = successAll && success;
						retMsg += RetMsg.deleted("task", task.name(), success) + "\n";
					}
				}
				return new Return(successAll ? RetCode.SUCCESS : RetCode.WARNING, retMsg);
			}
			if (m_tasks.get(taskName) == null) {
				return new Return(RetMsg.isExist("task", taskName, false));
			}
			Module task = m_tasks.get(taskName);
			task.exit();
			boolean success = (m_tasks.remove(taskName) != null);
			return new Return(success ? RetCode.SUCCESS : RetCode.FAIL, RetMsg.deleted("task", taskName, success));
		}
	};

	/** VIEW {task_name | ALL} */
	private CmdFunc cmdView = new CmdFunc(KWD_VIEW) {
		public Return execute(String[] command) {
			String taskName = parseCommand(KWD_VIEW, command);
			String retMsg = "task, user, status, progress, run_time, cur_options, cur_job";
			if (taskName.equalsIgnoreCase(KWD_ALL)) {
				if (m_tasks.isEmpty()) {
					return new Return(RetCode.SUCCESS);
				}
				for (Module task : m_tasks.values()) {
					retMsg += "\n" + task.taskDesc();
				}
				return new Return(RetCode.SUCCESS, retMsg);
			}
			Module task = m_tasks.get(taskName);
			if (task == null) {
				return new Return(RetCode.WARNING, RetMsg.isExist("task", taskName, false));
			}
			return new Return(RetCode.SUCCESS, retMsg + "\n" + task.taskDesc());
		}
	};

	/** return : [return_code return_message]. */
	public Return execCmd(String[] cmd) {
		for (CmdFunc cmdFunc : m_cmdFuncs) {
			if (cmdFunc.support(cmd)) {
				return cmdFunc.execute(cmd);
			}
		}
		return new Return(RetCode.INVALID, RetMsg.errInvalidCmd(UserType.USER.toString(), Common.getString(cmd, " ")));
	}

	/**
	 * User: [user_name, status(online/off-line), user_type, task_num,
	 * creation_time]
	 */
	@Override
	public String description() {
		String status = online() ? "online" : "offline";
		return name() + ", " + status + ", " + userType() + ", " + m_tasks.size() + ", " + m_creationTime;
	}
}