package step7.spmf;

import allen.base.common.Timer;
import pkgEnsp.AlgSpade;

// TODO
public class Spmf {

	static String WORK_DIR = "C:/Users/allen/Desktop/2016_09_12_Student_Behavior/";
	// input
	static String SEQ_NUMBER = WORK_DIR + "seq_number.txt";
	// output
	static String PATN_FILE = WORK_DIR + "patns.txt";

	static double minSupDbl = 0.5;
	static int maxLen = 100;

	public static void main(String[] args) throws Exception {
		// 1. extract Positive Sequences with SPMF: SPADE, SPAM, etc
		System.out.println("SPMF algorithm started");
		Timer timerPos = new Timer();
		AlgSpade algSpade = new AlgSpade();
		algSpade.setParameters(SEQ_NUMBER, PATN_FILE, minSupDbl, maxLen);
		algSpade.run();
		System.out.println("SPMF algorithm finished. " + timerPos);
		algSpade = null;
	}
}