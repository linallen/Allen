package allen.base.module;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;

import allen.base.common.*;

/**
 * General module class which<br>
 * 1. can be loaded from file (loadModule()), both local and remote.<br>
 * 2. can make a new copy (copy()).<br>
 * 3. a Module can be: AAI_Module, the main class loaded from a .jar file, or
 * any external file.
 * 
 * @author Allen Lin, 3 Dec 2014
 */
public class Module extends AAI_Module {
	private static final long serialVersionUID = -7360027778759477361L;

	/** module file */
	private String m_moduleFile;

	/**
	 * main class name.<br>
	 * "" means moduleFile is a jar file but has no main class,<br>
	 * null means moduleFile is not a jar file.
	 */
	private String m_mainClassName;

	/** main class (the actual module) */
	private transient Class<?> m_mainClass;

	/** an instance of main class */
	private transient Object m_module;

	/** main() method of the main class */
	private transient Method m_mainMethod;

	/** process of non-thread-able modules */
	private transient Process m_process;

	/** load module from file */
	public void loadModule(String moduleName, String moduleFile) throws Exception {
		name(moduleName);
		m_moduleFile = moduleFile;
		m_mainClassName = ModuleLoader.getMainClassName(moduleFile);
		m_mainClass = ModuleLoader.getMainClass(moduleFile);
		m_module = m_mainClass.newInstance();
		m_mainMethod = ModuleLoader.getMainMethod(m_mainClass);
	}

	/** TODO: DELETE module copy */
	public Module copy() throws Exception {
		Module moduleCopy = (Module) Common.deepCopy(this);
		moduleCopy.m_mainClass = m_mainClass;
		moduleCopy.m_module = ((m_mainClass != null) ? m_mainClass.newInstance() : null);
		moduleCopy.m_mainMethod = m_mainMethod;
		return moduleCopy;
	}

	/** module copy */
	public Module newInstance(String name, AAI_Module owner) throws Exception {
		Module newModule = (Module) Common.deepCopy(this);
		newModule.init();
		// newModule.goSleep(); // TODO DEBUG
		newModule.name(name);
		newModule.owner(owner);
		newModule.updateDirs(owner.workDir() + name);
		newModule.setOutStream(System.out); // module can only output to console
		newModule.m_mainClass = m_mainClass;
		newModule.m_module = ((m_mainClass != null) ? m_mainClass.newInstance() : null);
		if (isAAI_Module(newModule.module())) {
			((AAI_Module) newModule.module()).name(newModule.name());
			((AAI_Module) newModule.module()).owner(newModule);
			((AAI_Module) newModule.module()).updateDirs(newModule.workDir());
		}
		newModule.m_mainMethod = m_mainMethod;
		return newModule;
	}

	/** get main class */
	public Class<?> mainClass() {
		return m_mainClass;
	}

	/** get module object */
	public Object module() {
		return m_module;
	}

	/** get module progress [0% ~ 100%] */
	@Override
	public int progress() {
		if (isAAI_Module(m_module)) {
			return ((AAI_Module) m_module).progress();
		}
		return -2;
	}

	/** 1. get task name */
	@Override
	public String fullName() {
		if (isAAI_Module(m_module)) {
			return name() + " (" + ((AAI_Module) m_module).moduleName() + ")";
		}
		return name();
	}

	@Override
	public boolean isAlive() {
		if (isAAI_Module(m_module)) {
			return ((AAI_Module) m_module).isAlive();
		}
		return status() == Status.RUNNING;
	}

	/** return current options being executed by thread */
	@Override
	public String curOptions() {
		if (isAAI_Module(m_module)) {
			return ((AAI_Module) m_module).curOptions();
		}
		return m_curOpts;
	}

	@Override
	public Status status() {
		// 1. AAI_Module
		if (isAAI_Module(m_module)) {
			return ((AAI_Module) m_module).status();
		}
		// 2. non-AAI_Module
		return m_status;
	}

	@Override
	public String statusStr() {
		// 1. AAI_Module
		if (isAAI_Module(m_module)) {
			return ((AAI_Module) m_module).statusStr();
		}
		// 2. non-AAI_Module
		return m_status.toString();
	}

	@Override
	public long runningTime() {
		// 1. AAI_Module
		if (isAAI_Module(m_module)) {
			return ((AAI_Module) m_module).runningTime();
		}
		// 2. non-AAI_Module
		return super.runningTime();
	}

	/** TODO add options to queue[] */
	@Override
	public synchronized boolean addOptions(String[] options) {
		boolean ret = true;
		// boolean ret = super.addOptions(options);
		if (isAAI_Module(m_module)) {
			// for an AAI module, add options to its pool
			ret = ((AAI_Module) m_module).addOptions(options);
		}
		return ret;
	}

	/**
	 * for AAI_Module, we can monitor its process and communicate with it. for
	 * others, we just run it in a separated process and get the output.
	 */
	@Override
	public boolean start() {
		if (isAAI_Module(m_module)) {
			// for a AAI_Module, start it as a daemon thread waiting for options
			return ((AAI_Module) m_module).start();
		} else {
			return startNonAAI();
		}
	}

	/** start a non-AAI module as a thread */
	private boolean startNonAAI() {
		try {
			while (hasOptions()) {
				// TODO: DEBUG start a non-AAI module as a thread
				m_curOpts = Common.strArraytoStr(getNextOpts(), SPACE).trim();
				new Thread(new Runnable() {
					public void run() {
						String command = (jarRunnable() ? "Java -jar " : "") + m_moduleFile + " " + m_curOpts;
						try {
							m_process = Runtime.getRuntime().exec(command);
							status(Status.RUNNING);
							m_process.waitFor();
							BufferedReader reader = new BufferedReader(
									new InputStreamReader(m_process.getInputStream()));
							String output = new String(), line;
							while ((line = reader.readLine()) != null) {
								output += line + "\n";
							}
							output(output);
							status((m_process.exitValue() == 0) ? Status.FINISHED : Status.EXCEPTION);
						} catch (Exception e) {
							m_thread = null;
							m_exception = e;
							status(Status.EXCEPTION);
						}
					}
				}).start();
				status(Status.RUNNING);
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/** TODO Added by Allen 12/05/2016, suspend the module thread */
	@Override
	public void goSleep() throws Exception {
		if (isAAI_Module(m_module)) {
			((AAI_Module) m_module).goSleep();
		} else {
			// status(Status.STOP);
		}
	}

	/** stop the module thread */
	@Override
	public void stop() {
		if (isAAI_Module(m_module)) {
			((AAI_Module) m_module).stop();
		} else {
			m_process.destroy();
			status(Status.STOP);
		}
	}

	/** pause the module thread */
	@Override
	public boolean pause() {
		if (isAAI_Module(m_module)) {
			return ((AAI_Module) m_module).pause();
		}
		return false;
	}

	/** resume the module thread */
	@Override
	public void resume() {
		if (isAAI_Module(m_module)) {
			((AAI_Module) m_module).resume();
		}
	}

	/** exit the module thread */
	@Override
	public void exit() {
		if (isAAI_Module(m_module)) {
			((AAI_Module) m_module).exit();
		} else {
			m_process.destroy();
			status(Status.EXIT);
		}
	}

	private Boolean isJar() {
		return m_mainClassName != null;
	}

	private Boolean isAAI() {
		return isAAI_Module(m_mainClass);
	}

	private String mainClassName() {
		return m_mainClassName != null ? m_mainClassName : "";
	}

	/** module file is a runnable jar */
	private Boolean jarRunnable() {
		return isJar() && (m_mainMethod != null);
	}

	/** module file is a runnable jar, and main class implements Runnable */
	private Boolean threadRunnable() {
		return jarRunnable() && Common.subClass(Runnable.class, m_mainClass);
	}

	// TODO: DELETE
	public String getHelpOld() {
		try {
			if (isAAI_Module(m_mainClass)) {
				return (String) ModuleLoader.invoke(m_mainClass, "help");
			}
			return "no help.";
		} catch (Exception e) {
			return "no help. " + Common.exception(e);
		}
	}

	// TODO: DELETE
	public String getVersionOld() {
		try {
			if (isAAI_Module(m_mainClass)) {
				return (String) ModuleLoader.invoke(m_mainClass, "version");
			}
			return "no version.";
		} catch (Exception e) {
			return "no version. " + Common.exception(e);
		}
	}

	/** get information about the module. */
	public String getInfo(String infoName) {
		try {
			if (isAAI_Module(m_mainClass)) {
				return (String) ModuleLoader.invoke(m_mainClass, infoName);
			}
			return "no " + infoName + ".";
		} catch (Exception e) {
			return "no " + infoName + ". " + Common.exception(e);
		}
	}

	/** return exception */
	@Override
	public String exception() {
		if (isAAI_Module(m_module)) {
			return ((AAI_Module) m_module).exception();
		}
		return "NA";
	}

	/** return current call stack */
	@Override
	public String callStack() {
		if (isAAI_Module(m_module)) {
			return ((AAI_Module) m_module).callStack();
		}
		return "NA";
	}

	/** return current calling method */
	@Override
	public String execPoint() {
		if (isAAI_Module(m_module)) {
			return Common.execPoint(((AAI_Module) m_module).thread());
		}
		return "NA";
	}

	/** TODO: description of current job that thread is doing */
	@Override
	public String curJob() {
		if (isAAI_Module(m_module)) {
			return ((AAI_Module) m_module).curJob();
		}
		return "NA";
	}

	/**
	 * Module: [moduleName, isAAI, isJar, main_class, jarRunnable,
	 * threadRunnable, moduleFile]
	 */
	@Override
	public String description() {
		return name() + ", " + isAAI() + ", " + isJar() + ", " + mainClassName() + ", " + jarRunnable() + ", "
				+ threadRunnable() + ", " + m_moduleFile;
	}

	/**
	 * Task: [task name, user, status, progress, running time, current options,
	 * current job]
	 */
	public String taskDesc() {
		return fullName() + ", " + owner().name() + ", " + status() + ", " + progress() + "%, " + runningTime() + "s, "
				+ Common.quote(curOptions()) + ", " + Common.quote(curJob());
		// execPoint();
	}
}