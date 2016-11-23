package step8.trans2txt;

import java.util.HashMap;

import allen.base.common.AAI_IO;

// string sequences to number sequences
public class Trans2Txt {
	static String WORK_DIR = "C:/Users/allen/Desktop/2016_09_12_Student_Behavior/";
	// input
	static String PATNS_NUM = WORK_DIR + "patns_num.txt";
	// output
	static String PATNS_TXT = WORK_DIR + "patns_txt.txt";

	// mapping [seqText, seqNum]
	static String MAPPINGS = WORK_DIR + "mapping.txt";
	static HashMap<String, String> mapTxtNum = new HashMap<String, String>();

	static void loadMapTxtNum(String fileName) {
		String buf = AAI_IO.readFile(fileName);
		buf = buf.replaceAll("\r", "");
		String maps[] = buf.split("\n");
		for (String map : maps) {
			String items[] = map.split(",");
			mapTxtNum.put(items[0], items[1]);
		}
	}

	static String transTxt(String patnNum) {
		String ret = "";
		String buf = patnNum;
		String eventNums[] = buf.split(" -1");
		for (String eventNum : eventNums) {
			String items[] = eventNum.trim().split(" ");
			for (String item : items) {
				// TODO
				String itemNum = mapTxtNum.get(item.trim());
				if (itemNum == null) {
					itemNum = null;
				}
				ret += itemNum + " ";
			}
			ret += " -1 ";
		}
		return ret.trim().replaceAll("  ", " ");
	}

	public static void main(String[] args) throws Exception {
		loadMapTxtNum(MAPPINGS);
		String output = "";
		String buf = AAI_IO.readFile(PATNS_NUM);
		buf = buf.replace("\r", "");
		String patns[] = buf.split("\n");
		for (String patn : patns) {
			String items[] = patn.split(" #SUP: ");
			String patnTxt = transTxt(items[0]);
			output += patnTxt + " #SUP: " + items[1] + "\n";
		}
		AAI_IO.saveFile(PATNS_TXT, output.replaceAll("  ", " "));
		System.out.println("Trans2Txt finished!");
	}
}