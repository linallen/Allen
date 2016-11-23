package step5.mergeactions;

import java.util.HashMap;

import allen.base.common.AAI_IO;

// [sutid, week, label, libgate, libweb, ..., room] -> 
// [stuid, label, (week 1)libgate libweb ... room, (week 2)..., ...]
public class MergeActions {
	static String WORK_DIR = "C:/Users/allen/Desktop/2016_09_12_Student_Behavior/";
	static String WEEKLOG = WORK_DIR + "1_weeklogs.csv";
	// input
	static String disFtrsCSV = WEEKLOG + ".disc.csv";
	// output
	static String seqText = WEEKLOG + ".seq.csv";

	// [stuid, stuWeekLogs[]]
	static HashMap<String, StuWeekLogs> stuWeekLogsMap = new HashMap<String, StuWeekLogs>();

	public static void main(String[] args) throws Exception {
		// 1. load data from weeklogs.csv to stuWeekLogs
		String buf = AAI_IO.readFile(disFtrsCSV);
		buf = buf.replaceAll("\r", "");
		String logs[] = buf.split("\n");
		String titles[] = logs[0].split(",");
		for (int i = 1; i < logs.length; i++) {
			// 1. get seqText
			String columns[] = logs[i].split(",");
			String stuid = columns[0];
			Integer week = Integer.parseInt(columns[1]);
			String label = columns[2];
			String seqText = new String();
			for (int j = 3; j <= 6; j++) {
				String discValue = columns[j];
				String seqValue = titles[j];
				if (discValue.contains("(-inf")) {
					seqValue += "L";
				} else if (discValue.contains("inf)")) {
					seqValue += "H";
				} else {
					seqValue += "M";
				}
				seqText += seqValue.trim() + " ";
			}
			seqText = seqText.trim();
			// 2. put seqText to [stuid_label, weeklog]
			// String key = stuid + "_" + label;
			String key = stuid + "," + label;
			StuWeekLogs stuWeekLogs = stuWeekLogsMap.get(key);
			if (stuWeekLogs == null) {
				stuWeekLogs = new StuWeekLogs();
				stuWeekLogsMap.put(key, stuWeekLogs);
			}
			stuWeekLogs.m_weekLogs.put(week, seqText);
		}
		// 2. save data to file
		buf = new String();
		for (String key : stuWeekLogsMap.keySet()) {
			StuWeekLogs stuWeekLogs = stuWeekLogsMap.get(key);
			buf += key + "," + stuWeekLogs.toString() + "\n";
		}
		AAI_IO.saveFile(seqText, buf);
	}
}