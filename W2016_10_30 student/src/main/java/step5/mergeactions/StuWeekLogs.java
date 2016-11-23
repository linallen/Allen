package step5.mergeactions;

import java.util.HashMap;

import allen.base.common.Common;

public class StuWeekLogs {
	// [week, behaviours (libgateH libwebL ...)]
	HashMap<Integer, String> m_weekLogs = new HashMap<Integer, String>();

	public String toString() {
		String buf = new String();
		String weekLogs[] = new String[100];
		for (Integer week : m_weekLogs.keySet()) {
			Common.Assert(weekLogs[week] == null);
			weekLogs[week] = m_weekLogs.get(week);
		}
		for (String weekLog : weekLogs) {
			if (weekLog != null) {
				buf += weekLog + " -1 ";
			}
		}
		buf = (buf + " -2\n").replaceAll("  ", " ");
		return buf.trim();
	}
}