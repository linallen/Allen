package step6.trans2num;

import allen.base.common.AAI_IO;

// string sequences to number sequences
public class Trans2Num {
	static String WORK_DIR = "C:/Users/allen/Desktop/2016_09_12_Student_Behavior/";
	// input
	static String SEQ_STRING = WORK_DIR + "seq_string.txt";
	// output
	static String SEQ_NUMBER = WORK_DIR + "seq_number.txt";

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
				String items[] = event.split(" ");
				for (String item : items) {
					Integer code = item.hashCode();
					if (code < 0) {
						System.out.println("???");
						code = Math.abs(code);
					}
					output += code + " ";
				}
				output += " -1 ";
			}
			output += " -2\n";
		}
		AAI_IO.saveFile(SEQ_NUMBER, output.replaceAll("  ", " "));
		System.out.println("Trans2Num finished!");
	}
}