package allen.base.module;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;

import allen.base.common.AAI_IO;
import allen.base.common.ClassManager;
import allen.base.common.Common;
import allen.base.common.Timer;

/**
 * The definitions of common interfaces for all AAI modules.
 * <p>
 * 1. Every AAI module is packaged in a .jar file which has a main class.<br>
 * 2. AAI modules can be used in both command line and Java programming.<br>
 * 3. When used in command line, the main() function of the main class is the
 * entry of the module. For example,
 * "Java -jar module_name.jar -i input.csv -o output.csv" <br>
 * 4. When used in Java programming, AAI modules can run as threads. The
 * setOptions() function accepts options (same format as in command line mode)
 * to set up parameters. The run() function (inherited from the Thread Class)
 * runs the module.<br>
 * Example<br>
 * <br>
 * Module module = new Module();<br>
 * module.setOptions("-i input.csv -o output.csv");<br>
 * module.start();<br>
 * 
 * @author Allen Lin, 22 Oct 2014.
 */
public class AAI_Module implements Runnable, Serializable {
	private static final long serialVersionUID = -2260425849446619100L;

	/** constant characters */
	protected static final String OPTSPACE = "~"; // options' space
	protected static final String SPACE = " ";
	protected static final String COMMA = ",";
	protected static final String CR = "\n";
	protected static final String LF = "\r";
	protected static final String CRLF = "\n\r";

	/** global module id (internal, for debug) */
	private static int m_globalId = 0;

	/** module name */
	protected String m_name = new String();

	/** (internal) unique id of the module */
	protected int m_id;

	/**
	 * owner (parent) of this module. use owner's out-stream if owner != null.
	 */
	protected transient AAI_Module m_owner;

	/** commonly used timer */
	protected transient Timer m_timer = new Timer();

	/** debug switch (for debug only) */
	protected boolean m_debug;

	/** input and output streams */
	protected transient InputStream m_inStream;
	protected transient OutputStream m_outStream;

	/** constructors *********************************************/
	public void init() {
		m_inStream = System.in;
		m_outStream = System.out;
		m_id = m_globalId++;
		m_name = getClass().getSimpleName() + "_" + m_id;
		// workDir(null);
		status(Status.NEWBORN);

		// options
		m_queueOptions = new ConcurrentLinkedQueue<String[]>();
		m_oldOpts = new ArrayList<String>();
		m_lockThread = new Object();
	}

	public AAI_Module() {
		init();
	}

	public AAI_Module(AAI_Module owner) {
		init();
		owner(owner);
	}

	/** PROPERTY functions **************************************/
	public OutputStream outStream() {
		return m_outStream;
	}

	public static boolean isAAI_Module(Object module) {
		return Common.subInstance(AAI_Module.class, module);
	}

	public static boolean isAAI_Module(Class<?> sub) {
		return Common.subClass(AAI_Module.class, sub);
	}

	/** 8. set owner */
	public void owner(AAI_Module owner) {
		m_owner = owner;
	}

	/** 8. get owner */
	public AAI_Module owner() {
		return m_owner;
	}

	/** 9. get this */
	public AAI_Module getThis() {
		return this;
	}

	/** set input stream */
	public void setInStream(InputStream inStream) {
		m_inStream = inStream;
	}

	/** set output stream */
	public void setOutStream(OutputStream outStream) {
		m_outStream = outStream;
	}

	/** get module name */
	public String moduleName() {
		return getClass().getSimpleName();
	}

	/** return "object_name (module_name)" */
	public String fullName() {
		return name() + " (" + moduleName() + ")";
	}

	/** set object name */
	public void name(String name) {
		m_name = name.intern();
	}

	/** get object name */
	public String name() {
		return m_name;
	}

	/** get object's name */
	public int id() {
		return m_id;
	}

	/** 2. set status */
	public void status(Status status) {
		if (m_status != status) {
			outLog("Status changed from " + m_status + " to " + status);
			curJob(status.toString());
			switch (status) {
			case NEWBORN:
				m_creationTime = new Date();
				// outLog(name() + " is created.");
				break;
			case RUNNING:
				// outLog(name() + " started.");
				m_startTime = new Date();
				break;
			case FINISHED:
				progress(100);
			case STOP:
				// outLog(name() + " " + status.toString());
				m_endTime = new Date();
				break;
			case EXCEPTION:
				// outLog(name() + " stopped with Exception: " +
				// m_exception.getMessage());
				m_endTime = new Date();
				break;
			default:
			}
			// 1. update own status
			m_status = status;
		}
	}

	/** 2. get status */
	public Status status() {
		return m_status;
	}

	/** 4. set debug */
	public void debug(boolean debug) {
		m_debug = debug;
	}

	/** 4. get debug */
	public boolean debug() {
		return (owner() != null) ? owner().debug() : m_debug;
	}

	/** get current module thread */
	public Thread thread() {
		return m_thread;
	}

	/** PROGRESS functions **************************************/
	/** progress update: ProcFinished / ProcTotal */
	protected double m_finished, m_total = 100;
	/** percentage of current processing process [0% ~ 100%] */
	protected int m_progress = 0;
	/**
	 * number of major stages (child-procedures) in the main procedure.<br>
	 * each child-procedure occupies 1/stage_num of the main progress.
	 */
	protected int m_stageNum = 1;
	/** current stage no. Starting from 1. */
	protected int m_stageNo = 1;
	/** description of current job that thread is doing */
	protected String m_curJob = new String();

	/** 3. update progress [0% ~ 100%] */
	public void progress(int progress) {
		// 1. convert progress to actual progress
		int base = (int) (100. * (m_stageNo - 1.) / m_stageNum);
		int plus = (int) (100. / m_stageNum * (progress / 100.));
		progress = Math.min(100, base + plus);
		// in debug mode, show progress
		if (debug() && (owner() == null)) {
			if (progress > m_progress) {
				String crlf = (progress % 10 == 0) ? "\n" : " ";
				System.out.print(progress + "%(" + m_timer.elapsed() + "s)" + crlf);
			}
		}
		m_progress = progress;
		// 2. update parent's progress
		if (owner() != null) {
			owner().progress(progress);
		}
	}

	/** 3. update progress [0% ~ 100%] */
	public void progress(double finished, double total) {
		progress((int) (100. * finished / total));
	}

	/** 3. get progress [0% ~ 100%] */
	public int progress() {
		return m_progress;
	}

	/** set # of major stages (for progress update) */
	public void stageStart(int stageNum) {
		stage(stageNum, 1);
	}

	/** increase stage # (for progress update) */
	public void stageNext() {
		m_stageNo = Math.min(m_stageNo + 1, m_stageNum);
	}

	/** set stage number and current stage no. */
	public void stage(int stageNum, int stageNo) {
		m_stageNum = stageNum;
		m_stageNo = Math.min(stageNo, stageNum);
	}

	/** set number of stages */
	public void stageNum(int stageNum) {
		m_stageNum = stageNum;
	}

	/** get number of stages */
	public int stageNum() {
		return m_stageNum;
	}

	/** set current stage no. */
	public void stageNo(int stageNo) {
		m_stageNo = stageNo;
	}

	public int stageNo() {
		return m_stageNo;
	}

	public void curJob(String curJob) {
		m_curJob = curJob;
	}

	public String curJob() {
		return m_curJob;
	}

	/** WORK_DIR & TEMP_DIR functions ***********************************/
	/** work directory */
	protected String m_workDir = new String();
	/** temporary directory, default = work_dir + "temp/" */
	protected String m_tempDir = new String();
	/** log file, default = work_dir + name() + ".log" */
	protected String m_logFile = new String();

	/** get owner's work dir */
	private String ownerWorkDir() {
		if ((m_owner != null) && (m_owner.workDir() != null)) {
			return m_owner.workDir();
		}
		return System.getProperty("user.dir") + "/";
	}

	/** 6. set work directory (create a work_dir for each module) */
	public void workDir(String workDir) {
		if (workDir == null) {
			m_workDir = ownerWorkDir() + name();
		} else {
			// 1. transform workDir to directory
			if (!(new File(workDir)).isDirectory()) {
				workDir += "/";
			}
			// 2. Standardize workDir
			File dir = new File(workDir);
			if (dir.isAbsolute()) {
				m_workDir = workDir;
			} else {
				m_workDir = ownerWorkDir() + dir + "/";
			}
		}
		m_workDir = m_workDir.replaceAll("/+", "/");
		if (!m_workDir.endsWith("/") && !m_workDir.endsWith("\\")) {
			m_workDir += "/";
		}
	}

	/** update work_dir, temp_dir, and log_file */
	public void updateDirs(String workDir) {
		workDir(workDir);
		m_tempDir = workDir() + "temp/";
		m_logFile = workDir() + name() + ".log";
	}

	/** get default work dir */
	public String defaultWorkDir() {
		// 1. for a root module, its default work dir is current dir
		// 2. for a child module, its default work dir is parent_dir\name()
		workDir((owner() == null) ? AAI_IO.getCurDir() : (owner().workDir() + name()));
		return workDir();
	}

	/** 6. get work directory */
	public String workDir() {
		return !m_workDir.isEmpty() ? m_workDir : defaultWorkDir();
	}

	/** 7. set temporary directory */
	public void tempDir(String tempDir) {
		m_tempDir = tempDir;
	}

	/** 7. get temporary directory */
	public String tempDir() {
		return !m_tempDir.isEmpty() ? m_tempDir : (workDir() + "temp/");
	}

	/** save str to temp file */
	protected final void saveTempFile(String fileName, String str) {
		String message = null;
		try {
			fileName = tempDir() + AAI_IO.getFileName(fileName);
			message = "Saving temp-file " + Common.quote(fileName);
			AAI_IO.saveFileEx(fileName, str, false);
			message += " was succeed.";
		} catch (Exception e) {
			message += " was failed.";
		}
		output(message);
	}

	/** OUTPUT functions **************************************/
	/**
	 * output message to out-stream.<br>
	 * Bug: too fast out-stream to socket may combine messages.
	 */
	public synchronized boolean outStream(String msg) {
		boolean success = true;
		if (m_outStream != null) {
			if (m_outStream == System.out) {
				msg = "<" + name() + "> " + msg + "\n";
			}
			try {
				m_outStream.write(msg.getBytes());
				m_outStream.flush();
				// force to flush buffer
				Thread.sleep(5);
			} catch (Exception e) {
				System.out.println("Failed to outstream \"" + msg + "\". " + e.getMessage());
				success = false;
			}
		} else if (m_owner != null) {
			success = m_owner.outStream(msg);
		}
		return success;
	}

	/** output message to log file */
	public synchronized void outLog(String msg) {
		if (Common.notNullEmpty(m_logFile)) {
			SimpleDateFormat ft = new SimpleDateFormat("E yyyy.MM.dd 'at' hh:mm:ss.SSS a");
			AAI_IO.saveFile(m_logFile, (ft.format(new Date())) + ": <" + name() + "> " + msg + "\n", true);
		} else if (owner() != null) {
			owner().outLog(msg);
		}
	}

	/** output message to out-stream and log file */
	public synchronized boolean output(String msg) {
		msg += " (time used: " + runningTime() + "s)"; // debug
		if (!outStream(msg)) {
			return false;
		}
		outLog(msg);
		return true;
	}

	/** output debug message to out-stream and log file */
	public synchronized void outputDbg(String msg) {
		if (debug()) {
			output("[Debug]: " + Common.quote(msg));
		}
	}

	/** output message to out-stream and log file */
	public synchronized void output(String msg, boolean curJob) {
		if (curJob) {
			curJob(msg);
		}
		output(msg);
	}

	/** output a warning message for DEBUG */
	public synchronized void outputWarning(String msg) {
		output("Warning: " + msg);
	}

	/** output a error message for DEBUG */
	public synchronized void outputError(String msg) {
		output("Error: " + msg);
	}

	// TODO: delete
	protected static void printException(String name, Throwable e) {
		System.out.println(name + ": " + e);
	}

	// TODO: delete
	protected void printException(Throwable e) {
		System.out.println(name() + ": " + e);
	}

	/**
	 * AAI_Module: [module_name, owner_name, status, creation_time, module_jar]
	 */
	public String description() {
		AAI_Module owner = owner();
		String ownerName = ((owner != null) ? owner.name() : "System");
		return fullName() + ", " + ownerName + ", " + statusStr() + ", " + runningTime() + "s" + ", ";
	}

	/** [Override] */
	public String toString() {
		return description();
	}

	/** THREAD functions **************************************/
	/** lock for thread starting */
	private transient Object startLock = new Object();
	/** indicator of thread starting, successful or not */
	private transient Boolean startSuccess = null;
	/** creation, start and end time */
	protected Date m_creationTime, m_startTime, m_endTime;
	/** thread running the module */
	protected transient Thread m_thread;
	/** latest exception message and call stack trace */
	public transient Exception m_exception;
	/** running status: NEWBORN, RUNNING, PAUSED, STOPPED, FINISHED */
	protected Status m_status;
	/** daemon thread? */
	protected boolean m_daemon = true;

	public boolean daemon() {
		return m_daemon;
	}

	public void daemon(boolean daemon) {
		m_daemon = daemon;
	}

	public boolean isAlive() {
		return (m_thread != null) && m_thread.isAlive();
	}

	/** start the module as a thread */
	public boolean start() {
		try {
			if (isAlive()) {
				return false;
			}
			// 1. start a thread to start and monitor module thread
			new Thread(new Runnable() {
				public void run() {
					try {
						// start the main thread, which maintains its own status
						m_thread = new Thread(null, getThis(), name());
						m_thread.start();
						// status(Status.RUNNING);
						synchronized (startLock) {
							startSuccess = true;
							startLock.notify();
						}
						// wait till module thread ended
						m_thread.join();
						if (status() == Status.RUNNING) {
							status(Status.FINISHED);
						}
					} catch (Exception e) {
						m_thread = null;
						m_exception = e;
						status(Status.EXCEPTION);
						synchronized (startLock) {
							startSuccess = false;
							startLock.notify();
						}
					}
				}
			}).start();
			// 2. wait till module thread started or failed
			synchronized (startLock) {
				while (startSuccess == null) {
					startLock.wait();
				}
			}
		} catch (Exception e) {
			m_thread = null;
			m_exception = e;
			status(Status.EXCEPTION);
			startSuccess = false;
		}
		return startSuccess;
	}

	public void join() throws Exception {
		m_thread.join();
	}

	/** stop the module thread */
	public void stop() {
		status(Status.STOP);
	}

	/** exit the module thread */
	public void exit() {
		status(Status.EXIT);
		wakeUp();
	}

	/** inform the module thread to pause */
	public boolean pause() {
		status(Status.PAUSE);
		return true;
	}

	/** inform the module thread to resume */
	public void resume() {
		wakeUp();
	}

	/** set current module thread */
	public void thread(Thread thread) {
		m_thread = thread;
	}

	/** return current calling method */
	public String execPoint() {
		return Common.execPoint(this.thread());
	}

	/** throws an exception */
	public void throwException(String msg) throws Exception {
		outputError(msg);
		throw new Exception(msg);
	}

	/** return exception */
	public String exception() {
		return getException();
	}

	/** return exception */
	public String getException() {
		if (status() == Status.EXCEPTION) {
			return Common.exception(m_exception);
		}
		return null;
	}

	public String callStack() {
		if (status() == Status.EXCEPTION) {
			return Common.callStack(m_exception);
		}
		return execPoint();
	}

	public String statusStr() {
		String buf = status().toString();
		// if (status() == Status.EXCEPTION) {
		// buf = Common.exception(m_exception);
		// } else if (status() == Status.RUNNING) {
		// buf += " (" + progress() + "%)";
		// }
		return buf;
	}

	/** get running time (seconds) of the module */
	public long runningTime() {
		try {
			switch (status()) {
			case NEWBORN:
				return 0;
			case RUNNING:
				return (((new Date()).getTime() - m_startTime.getTime()) / 1000);
			case PAUSE:
			case STOP:
			case EXCEPTION:
			case FINISHED:
				return ((m_endTime.getTime() - m_startTime.getTime()) / 1000);
			default:
				return -1;
			}
		} catch (Exception e) {
			return -1;
		}
	}

	/** OPTIONS functions *******************************************/
	/** options queue[] */
	protected transient ConcurrentLinkedQueue<String[]> m_queueOptions;
	/** options executed history[] */
	protected transient ArrayList<String> m_oldOpts;
	/** thread lock for synchronization */
	protected transient Object m_lockThread = new Object();
	/** current options being executed by thread */
	protected transient String m_curOpts;

	/** [Override] add options to the pool and wake thread up */
	public synchronized boolean addOptions(String[] options) {
		try {
			if ((options != null) && (options.length > 0)) {
				String optLine = Common.strArraytoStr(options, SPACE);
				if (m_queueOptions.add(options)) {
					output("Added options " + optLine);
					return true;
				} else {
					outputWarning("Failed to add options " + optLine);
				}
			}
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	protected String[] getNextOpts() {
		try {
			return m_queueOptions.remove();
		} catch (Exception e) {
			return null;
		}
	}

	protected boolean hasOptions() {
		return !m_queueOptions.isEmpty();
	}

	/** return current options being executed by thread */
	public String curOptions() {
		return m_curOpts;
	}

	/** [Override] pass options to the module. */
	public void setOptions(String[] options) throws Exception {
		// -debug
		m_debug = Common.getOptionBool("debug", options);
		// -daemon
		m_daemon = Common.getOptionBool("daemon", options);
		// warning if there are excess options, 25 May 2015
		String excessOpts = Common.strArraytoStr(options, SPACE);
		excessOpts = excessOpts.trim();
		if (!excessOpts.isEmpty()) {
			outputWarning("excess options " + Common.quote(excessOpts));
		}
	}

	/** MAIN functions *******************************************/
	/** [Override] entry of thread. */
	protected void mainProc() throws Exception {
	}

	/** wake up the task thread. */
	protected void wakeUp() {
		synchronized (m_lockThread) {
			m_lockThread.notifyAll();
		}
	}

	/** [Override] task thread goes to sleep. */
	protected void goSleep() throws Exception {
		synchronized (m_lockThread) {
			m_lockThread.wait();
		}
	}

	/** the main function of AAI thread */
	// @Override
	public void run() {
		try {
			while (status() != Status.EXIT) {
				// retrieve options from option_queue & execute one by one
				String[] curOpts = getNextOpts();
				if (curOpts != null) {
					status(Status.RUNNING);
					m_curOpts = Common.strArraytoStr(curOpts, SPACE).trim();
					setOptions(curOpts);
					output("Executing options " + m_curOpts);
					mainProc();
					if (status() == Status.STOP) {
						output("Thread stopped while executing " + m_curOpts);
						m_queueOptions.clear();
						continue;
					}
					if (status() == Status.EXIT) {
						output("Thread terminated while executing " + m_curOpts);
						break;
					}
					m_oldOpts.add(m_curOpts);
					output("Executed options " + m_curOpts);
				} else {
					output("Finished all options.");
					status(Status.FINISHED);
					if (daemon()) {
						output("Go to sleep...");
						goSleep();
					} else {
						output("Non-daemon task terminating...");
						break;
					}
				}
			}
			output("Task terminated normally.");
		} catch (Exception e) {
			output("Task terminated exceptionally: " + Common.exception(e));
			if (debug()) {
				e.printStackTrace();
			}
			m_exception = e;
			status(Status.EXCEPTION);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	/** global registered mappings [clsName, clsClass] */
	private static ClassManager s_clsManager = new ClassManager();

	/**
	 * register [clsName, clsClass].
	 * 
	 * @param clsName
	 *            a unique name for the class.
	 * @param clsClass
	 *            must has default constructor
	 */
	public static boolean register(String clsName, Class<?> clsClass) {
		clsName = clsName.trim().toUpperCase();
		return s_clsManager.register(clsName, clsClass);
	}

	/** return an instance of a registered class */
	public static Object getInstance(String clsName) throws Exception {
		Object instance = s_clsManager.getInstance(clsName.trim().toUpperCase());
		if (instance == null) {
			throw new Exception(clsName + "： class not registered, please register it with register() first. ");
		}
		return instance;
	}
	////////////////////////////////////////////////////////////////////////////

	/** [STUB] return help */
	public static String help() {
		return "-debug output debug information.\n" + "-deamon daemon version which can add options at run time.";
	};

	/** [STUB] return revision history */
	public static String version() {
		return "v0.0.1, 19 June 2016, Allen Lin.\n" + "v0.0.2, 23 June 2016, Allen Lin, added ClassManager.";
	};

	/** the Entry function of all AAI_Module sub-classes */
	protected static final void exec(String clsName, String[] args) throws Exception {
		String argsStr = Common.strArraytoStr(args, " ");
		// DEBUG
		// System.out.println("Started class " + clsName + "(" + argsStr + ")");
		// System.out.println("args.length = " + args.length);
		// for (String arg : args) {
		// System.out.println("arg = " + arg);
		// }
		// DEBUG

		// 1. get AAI_Module class from clsName
		Class<?> clsClass = ModuleLoader.getClass(clsName);
		if (clsClass == null) {
			throw new Exception(clsName + ": failed to get this Class!");
		}
		if (!isAAI_Module(clsClass)) {
			throw new Exception(clsName + ": not an AAI Class!");
		}
		// 2. display help() and version() of class
		String help = (String) ModuleLoader.invoke(clsClass, "help");
		String version = (String) ModuleLoader.invoke(clsClass, "version");
		System.out.println(version + "\n");
		if (args.length == 0) {
			System.out.println(help + "\n");
			return;
		}
		// 3. create an Instance from class and pass args[] to it
		AAI_Module module = (AAI_Module) ModuleLoader.newInstance(clsClass);
		if (module != null) {
			System.out.println("Started task " + module.name() + " (" + argsStr.trim() + ")");
			Timer timer = new Timer();
			module.addOptions(args);
			module.start();
			module.join();
			System.out.println("Finished task " + module.name() + " (" + argsStr.trim() + "), " + timer);
		}
		// DEBUG
		// System.out.println("Finished class " + clsName + "(" + argsStr +
		// ")");
		// DEBUG
	}

	/** copy this function to sub-classes */
	public static void main(String[] args) throws Exception {
		exec(Thread.currentThread().getStackTrace()[1].getClassName(), args);
	}
}