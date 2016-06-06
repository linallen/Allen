package pkgCommon;

public class Timer {
	private long m_startTime;
	private long m_endTime;

	public Timer() {
		// m_startTime = System.nanoTime();
		start();
	}

	public void start() {
		m_startTime = System.nanoTime();
	}

	public void end() {
		m_endTime = System.nanoTime();
	}

	/** return nanoseconds used so far. 1 nanosecond = 1e-9 second */
	public long elapsedNano() {
		long endTime = m_endTime >= m_startTime ? m_endTime : System.nanoTime();
		return endTime - m_startTime;
	}

	/** return microseconds used so far. 1 millisecond = 1e-6 second */
	public long elapsedMicro() {
		long endTime = m_endTime >= m_startTime ? m_endTime : System.nanoTime();
		return (long) ((endTime - m_startTime) / 1000);
	}

	/** return milliseconds used so far. 1 millisecond = 1e-3 second */
	public long elapsedMilli() {
		long endTime = m_endTime >= m_startTime ? m_endTime : System.nanoTime();
		return (long) ((endTime - m_startTime) / 1000000);
	}

	/** return seconds used so far */
	public long elapsed() {
		long endTime = m_endTime >= m_startTime ? m_endTime : System.nanoTime();
		return (long) ((endTime - m_startTime) / 1000000000);
	}

	public String elapsedStr() {
		return "(" + elapsed() + "s)";
	}

	public String toString() {
		return "Time used: " + elapsed() + "s";
	}
}