package allen.base.common;

/**
 * Class for showing progress in percentage.
 * 
 * @author Allen Lin, 2 July 2014
 */
public class Progress {
	private long m_total;
	private int m_curPrecent;

	public Progress(long total) {
		m_total = total;
	}

	public boolean updated(long completed) {
		int newPercent = (int) (100. * completed / m_total + .5) / 10;
		if (newPercent > m_curPrecent) {
			m_curPrecent = newPercent;
			return true;
		}
		return false;
	}

	public String toString() {
		return m_curPrecent + "0%";
	}
}
