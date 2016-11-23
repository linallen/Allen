package allen.base.module;

/**
 * Status reflecting or influencing running behavior of an AAI module.
 * <p>
 * NEWBORN: a new created module. No option has been executed.<br>
 * RUNNING: thread is executing an option.<br>
 * PAUSE: notify current process to pause.<br>
 * STOP: notify current process to stop.<br>
 * EXIT: notify current thread to terminate.<br>
 * EXCEPTION: thread stopped with exception.<br>
 * FINISHED: thread finished all options with no exceptions.<br>
 * 
 * Note: the user-written "mainProc()" function should monitor status and take
 * actions to response "PAUSE", "STOP" and "EXIT" notifications.<br>
 * 1. for "PAUSE" command, it should call "goSleep()" at current position.<br>
 * 2. for "STOP" and "EXIT" commands, it should exit "mainProc()".<br>
 */
public enum Status {
	NEWBORN("NEWBORN"), RUNNING("RUNNING"), PAUSE("PAUSED"), STOP("STOPPED"), EXIT(
			"TERMINATED"), EXCEPTION("EXCEPTION"), FINISHED("FINISHED");
	private String m_status;

	Status(String status) {
		m_status = status;
	}

	public String toString() {
		return m_status;
	}
}