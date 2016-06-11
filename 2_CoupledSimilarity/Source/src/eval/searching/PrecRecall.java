package eval.searching;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

import pkgCommon.AAI_IO;
import pkgCommon.Common;
import pkgCommon.Timer;
import pkgModule.AAI_Module;

/**
 * Evaluate searching results with Precision, Recall, and F-measure metrics.
 * <br>
 * <b>Syntax:</b><br>
 * Java -jar eval_prf.jar -i input_file [-k top_k] [-o output_file]<br>
 * <ul>
 * <li><i>-i input_file</i>: [input] the searching results for evaluation.
 * Format: label[ obj_name cls_size], label[ sim_obj1 score], ...</li>
 * <li><i>-k top_k</i>: [para] top K metrics. Default 100.</li>
 * <li><i>-o output_file</i>: [output] Matlab file containing top k precisions,
 * recalls, and F-measures for drawing figures.</li>
 * </ul>
 * * @author Allen Lin, 22 Feb 2016
 */

public class PrecRecall extends AAI_Module {
	private static final long serialVersionUID = 2471215655070371273L;

	/** -i input_file: [input] the searching results for evaluation. */
	private String m_inputFile;
	/** -k top_k: top K metrics */
	private int m_topK = 100;
	/** -o output_file: [output] Matlab file containing top k metrics. */
	private String m_outputFile;

	/** object = "label[ obj_name number]" */
	static class ObjInfo {
		String m_clsName;
		String m_objName; // for debug only
		double m_number;

		/** parse object "label[ obj_name number]" */
		static ObjInfo parse(String objText) {
			objText = objText.trim().replaceAll("\\s+", " ");
			ObjInfo objInfo = null;
			String parts[] = objText.split(SPACE);
			if (parts.length > 0) {
				objInfo = new ObjInfo();
				objInfo.m_clsName = parts[0];
				if (parts.length > 1) {
					objInfo.m_objName = parts[1];
					if (parts.length > 2) {
						objInfo.m_number = Double.parseDouble(parts[2]);
					}
				}
			}
			return objInfo;
		}
	}

	/** class for recording hitting info of one target object */
	static class HitInfo {
		int[] m_sumHit;
		int m_clsNum;

		HitInfo(int[] sumHit, int clsNum) {
			m_sumHit = sumHit;
			m_clsNum = clsNum;
		}
	}

	/** get hit_num[i=1,...] from input data line: target_obj, sim_objs[] */
	private static HitInfo getHitNum(String line, int topK) {
		String objs[] = line.split(COMMA);
		int[] hitNum = new int[Math.min(objs.length, topK + 1)];
		// 1. get target label
		ObjInfo tarObj = ObjInfo.parse(objs[0]);
		// 2. get other labels and get hit[j], j = 1, 2, ... top_k
		for (int j = 1; j < hitNum.length; j++) {
			ObjInfo simObj = ObjInfo.parse(objs[j]);
			hitNum[j] += tarObj.m_clsName.equals(simObj.m_clsName) ? 1 : 0;
		}
		// 3. accumulate hit numbers
		for (int j = 1; j < hitNum.length; j++) {
			hitNum[j] += hitNum[j - 1];
		}
		return new HitInfo(hitNum, (int) tarObj.m_number);
	}

	/**
	 * load data from file and output Prec, Rec, and F metrics.<br>
	 * Input format: label[ obj_name cls_size], label[ sim_obj1 score], ...<br>
	 * Output format: prec[k] = .8; rec[k] = .5; f[k] = .6; k = 1, 2, ..., top_k
	 */
	public void main(String inputFile, int topK, String outputFile) throws Exception {
		output("Analyzing data started. " + "From " + inputFile + " to " + outputFile);
		Timer timer = new Timer();
		BufferedReader br = null;
		BufferedWriter bw = null;
		try {
			br = new BufferedReader(new FileReader(inputFile));
			String line = null;
			double sumPrec[] = new double[topK + 1];
			double sumRec[] = new double[topK + 1];
			double sumF[] = new double[topK + 1];
			int objNum = 0;
			double total = AAI_IO.getFileSize(inputFile), finished = 0;
			for (; (line = br.readLine()) != null; objNum++) {
				progress(finished += line.length() + 2, total);
				// 1. get hit_num[i=1, ...] from data line
				HitInfo hitInfo = getHitNum(line, topK);
				int[] sumHit = hitInfo.m_sumHit;
				if (sumHit.length < 2) { // sumHit[i], i starts from 1
					continue;
				}
				// 2. get class size
				int clsNum = hitInfo.m_clsNum;
				// 3. accumulate Prec[], Rec[] and F[]
				for (int i = 1; i < Math.min(topK + 1, sumHit.length); i++) {
					double prec = (double) sumHit[i] / i;
					double rec = (double) sumHit[i] / Math.min(i, clsNum);
					double f = (prec + rec == 0.) ? 0 : 2 * (prec * rec) / (prec + rec);
					sumPrec[i] += prec;
					sumRec[i] += rec;
					sumF[i] += f;
				}
			}
			// output avePrec[topK], aveRec[topK] and aveF[topK]
			bw = new BufferedWriter(new FileWriter(outputFile));
			for (int i = 1; i < topK + 1; i++) {
				bw.write("prec(" + i + ") = " + String.format("%.4f", sumPrec[i] / objNum) + ";\t");
				bw.write("rec(" + i + ") = " + String.format("%.4f", sumRec[i] / objNum) + ";\t");
				bw.write("f(" + i + ") = " + String.format("%.4f", sumF[i] / objNum) + ";\n");
			}
			bw.write("plot(prec);");
		} finally {
			AAI_IO.close(br);
			AAI_IO.close(bw);
			output("Analyzing data finished. " + timer);
		}
	}

	@Override
	protected void mainProc() throws Exception {
		main(m_inputFile, m_topK, m_outputFile);
	}

	@Override
	public void setOptions(String[] options) throws Exception {
		// -i input_file
		m_inputFile = Common.getOption("i", options);
		// -k top_k
		m_topK = Common.getOptionInt("k", options, m_topK);
		// ¨Co output_file
		m_outputFile = Common.getOption("o", options);
		// debug, daemon, etc
		super.setOptions(options);
	}

	public static void main(String[] args) throws Exception {
		PrecRecall module = new PrecRecall();
		System.out.println("\n" + version() + "\n");
		if (args.length == 0) {
			System.out.println(help() + "\n");
			return;
		}
		module.setOptions(args);
		module.start();
		module.join();
	}

	public static String help() {
		return "Evaluate searching results with Precision, Recall, and F-measure metrics.";
	}

	public static String version() {
		return "v1, Created on 22 Feb 2016, Allen Lin.";
	}
}