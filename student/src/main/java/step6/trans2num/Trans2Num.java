package step6.trans2num;

import java.util.HashMap;

import allen.base.common.AAI_IO;

// string sequences to number sequences
public class Trans2Num {
	static String WORK_DIR = "C:/Users/allen/Desktop/2016_09_12_Student_Behavior/";
	// input
	static String SEQ_STRING = WORK_DIR + "seq_string.txt";
	// output
	static String SEQ_NUMBER = WORK_DIR + "seq_number.txt";

	// mapping [seqText, seqNum]
	static Integer no = 1;
	static String MAPPING = WORK_DIR + "mapping.txt";
	static HashMap<String, Integer> mapTxtNum = new HashMap<String, Integer>();

	static Integer getSeqNum(String seqTxt) {
		Integer seqNum = mapTxtNum.get(seqTxt);
		if (seqNum == null) {
			seqNum = no++;
			mapTxtNum.put(seqTxt, seqNum);
		}
		return seqNum;
	}

	static void saveMapTxtNum(String fileName) {
		String buf = "";
		for (String seqTxt : mapTxtNum.keySet()) {
			Integer seqNum = mapTxtNum.get(seqTxt);
			buf += seqNum + "," + seqTxt + "\n";
		}
		AAI_IO.saveFile(fileName, buf);
	}

	public static void main(String[] args) throws Exception {
		String output = "";
		String text = AAI_IO.readFile(SEQ_STRING);
		text = text.replace("\r", "");
		String seqs[] = text.split("\n");
		for (String seq : seqs) {
			seq = seq.replace(" -2", "");
			seq = seq + " ";
			String events[] = seq.split(" -1 ");
			for (String event : events) {
				String seqTxts[] = event.split(" ");
				for (String seqTxt : seqTxts) {
					Integer seqNum = getSeqNum(seqTxt);
					output += seqNum + " ";
				}
				output += " -1 ";
			}
			output += " -2\n";
		}
		AAI_IO.saveFile(SEQ_NUMBER, output.replaceAll("  ", " "));
		saveMapTxtNum(MAPPING);
		System.out.println("Trans2Num finished!");
	}
}