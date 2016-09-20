package pkgShellService;

import allen.base.common.Common;

public class RetMsg {

	public static String stopTaskCmd(String taskName, boolean success) {
		return "task " + Common.quote(taskName) + " stopped " + (success ? "" : "un") + "successfully.";
	}

	public static String errInvalidCmd(String owner, String cmd) {
		return Common.quote(cmd) + " is not a valid " + owner + " command.";
	}

	public static String isKeyword(String keyword) {
		return Common.quote(keyword) + " is a keyword.";
	}

	public static String adminCmd() {
		return "only Admin can execute this command.";
	}

	public static String isExist(String type, String name, boolean exist) {
		name = name.trim();
		if (name.isEmpty()) {
			return "please specify a " + type + " name.";
		}
		return type + " " + Common.quote(name) + (exist ? " already exists." : " does not exist.");
	}

	public static String createTask(String taskName, boolean success) {
		return "task " + Common.quote(taskName) + " created " + (success ? "" : "un") + "successfully.";
	}

	// TODO DEBUG
	public static String taskStarted(String taskName, boolean started, String options) {
		String ret = "task " + Common.quote(taskName) + " started ";
		ret += (started ? "" : "un") + "successfully";
		ret += (options.trim().isEmpty() ? "" : (" with options " + Common.quote(options))) + ".";
		return ret;
	}

	public static String loadModule(String moduleName, String moduleJar, boolean success) {
		if (success) {
			return "module " + Common.quote(moduleName) + " loaded from " + Common.quote(moduleJar) + ".";
		}
		return "failed to load " + Common.quote(moduleName) + " from " + Common.quote(moduleJar) + ".";
		// return (success ? "successfully" : "failed to") + " load module " +
		// Common.quote(moduleName) + " from "
		// + Common.quote(moduleJar) + ".";
	}

	public static String isRunning(String taskName, boolean running) {
		return "task " + Common.quote(taskName) + " is" + (running ? "" : " not") + " running.";
	}

	public static String errSetOptions(String taskName, String options) {
		return "failed to set task " + Common.quote(taskName) + "'s options " + Common.quote(options) + ".";
	}

	public static String deleted(String type, String name, boolean deleted) {
		if (deleted) {
			return type + " " + Common.quote(name) + " has been deleted.";
		}
		return "Failed to delete " + type + " " + Common.quote(name) + ".";
	}
}