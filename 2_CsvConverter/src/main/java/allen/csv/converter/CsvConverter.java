package allen.csv.converter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import allen.base.common.AAI_IO;
import allen.base.common.Common;
import allen.base.common.Timer;
import allen.base.module.AAI_Module;

/**
 * Convert and/or split a CSV file to a (set of) ARFF or CSV file(s).
 * <p>
 * <b>Syntax</b>:<br>
 * Java -jar CsvConverter.jar -i input_csv [-o output_file] [-D id_num] [[-r
 * row_num] | [-s sub_num]] [-v val_num] [-S] [-C]
 * <p>
 * <ul>
 * <li><i>-i input_csv<i>: the input CSV file to convert and/or split. It must
 * has a title row and values are separated by ",".</li>
 * <li><i>[-o output_file]<i>: the output CSV or ARFF file(s). Default is
 * input_csv + "_output.csv" or "_output.arff". Multiple output sub-files are
 * named as: output_file + "_#" + suffix (.csv or .arff).</li>
 * <li><i>[-D id_num]<i>: # of ID columns from left-most. Default 0.</li>
 * <li><i>[-r row_num]<i>: # of instances an output sub-file has. Default
 * unlimited (no split).</li>
 * <li><i>[-s sub_num]<i>: # of output sub-files. Default 1 (no split). Ignored
 * if "-r row_num" is set.</li>
 * <li><i>[-v val_num]<i>: upper bound of distinct values a nominal feature has.
 * If exceeds, feature is considered as a STRING. Default 100.</li>
 * <li><i>[-S]<i>: shuffle instances or not. Default NO.</li>
 * <li><i>[-C]<i>: output CSV or ARFF. Default ARFF.</li>
 * </ul>
 * 
 * @author Allen Lin, 2 Oct 2014
 */
public class CsvConverter extends AAI_Module {
	private static final long serialVersionUID = -7789190482212675338L;

	/** -i input_csv the CSV file to convert and/or split. */
	private String m_inputCSV = new String();
	/**
	 * [-o output_file] the output CSV or ARFF file(s). Default is input_csv +
	 * "_output.csv" or "_output.arff". Multiple output sub-files are named as:
	 * output_file + "_#" + suffix (.csv or .arff).
	 */
	private String outputFile = new String();
	/** [-D id_num] the number of ID columns from left-most. Default 0. */
	private int m_idNum = 0;
	/**
	 * [-r row_num] # of instances an output sub-file has. Default unlimited.
	 */
	private int rowNum = Integer.MAX_VALUE;
	/** [-s sub_num] # of sub-files. Default 1. Ignored when row_num is set. */
	private int subNum = 1;
	/**
	 * [-v val_num] upper bound of distinct values a nominal feature has. If
	 * exceeds, feature is considered as a STRING. Default 100.
	 */
	private int m_valNum = 100;
	/** [-S] shuffle instances or not. Default NO. */
	private boolean shuffle;
	/** [-C] output CSV or ARFF. Default ARFF. */
	private boolean outFmtCSV;

	/** total # of instances */
	private int m_instNum;
	/** features of input CSV */
	private Feature[] m_features;
	/** ARFF header generated from input CSV */
	private String m_arffHeader = new String();
	/** mapping of [inst_id, sub_id] */
	private HashMap<Integer, Integer> m_mapInstSub;
	/** [output] the sub-files */
	private String[] m_subFiles;

	/** standardize string from That's fine. --> 'That\'s fine.' */
	private static String stdString(String value) {
		value.replaceAll("''", "\\''");
		if (value.contains(" ")) {
			value = "\'" + value + "\'";
		}
		return value;
	}

	private String[] getFtrNames(String line) throws Exception {
		String[] ftrNames = line.split(",");
		for (int i = 0; i < ftrNames.length; i++) {
			if (ftrNames[i].isEmpty()) {
				throw new Exception("feature[" + (i + 1) + "] has no name.");
			}
		}
		if (ftrNames.length <= 0) {
			throw new Exception("Did not find any feature name.");
		}
		return ftrNames;
	}

	/** generate ARFF features[] from input CSV */
	@SuppressWarnings("resource")
	private int genFeatures(String inputCSV) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(inputCSV));
		// 1. initialize features[] with the first header row
		String line = br.readLine();
		m_finished += line.length() + 2;
		String[] ftrNames = getFtrNames(line);
		m_features = new Feature[ftrNames.length];
		for (int i = 0; i < ftrNames.length; i++) {
			Common.Assert(ftrNames[i].length() > 0);
			m_features[i] = new Feature(ftrNames[i], m_valNum);
		}
		// set ID features to be STRING
		for (int i = 0; i < m_idNum; i++) {
			m_features[i].setTypeString();
		}
		// 2. parse data rows
		int instNum = 0;
		for (; (line = br.readLine()) != null;) {
			m_finished += line.length() + 2;
			progress(m_finished, m_total);
			String[] values = line.split(",");
			if (values.length > m_features.length) {
				throw new Exception("instance " + (instNum + 1) + " contains too many values. " + Common.quote(line));
			}
			for (int j = 0; j < values.length; j++) {
				if ((values[j].length() > 0) && (!values[j].equals("?"))) {
					// only add non-missing values to features
					m_features[j].addValue(stdString(values[j]));
				}
			}
			instNum++;
		}
		br.close();
		return instNum;
	}

	/** generate ARFF header */
	private int genArffHeader(String inputCSV) throws Exception {
		Timer timer = new Timer();
		outLog("Generating ARFF headers... " + "(nominal features having more than " + m_valNum
				+ " distinct values will be regarded as STRING type.)");
		int instNum = genFeatures(inputCSV);
		// "@attribute"
		String attr = new String();
		for (Feature ftr : m_features) {
			attr += ("@attribute " + ftr.toString() + "\n");
		}
		// ARFF header
		String relation = stdString((new File(inputCSV)).getName().replaceAll("\\.[cC][sS][vV]$", ""));
		m_arffHeader = "@relation " + relation + "\n\n" + attr + "\n@data";
		outLog("Done. " + instNum + " instances. " + timer);
		return instNum;
	}

	// TODO: delete
	public String[] getSubFiles() {
		return m_subFiles;
	}

	/**
	 * Convert and/or split a CSV file to a (set of) ARFF or CSV file(s).<br>
	 * -i input_csv [-o output_file] [-D id_num] [[-r row_num] | [-s sub_num]]
	 * [-v val_num] [-S] [-C]
	 */
	public String[] convert(String inputCSV, String outputFile, int idNum, int rowNum, int subNum, int valNum,
			boolean shuffle, boolean outFmtCSV) throws Exception {
		// inputCSV must exist
		Common.Assert(AAI_IO.fileExist(inputCSV));
		// for progress updating (Added by Allen on 9 Dec 2014)
		m_total = AAI_IO.getFileSize(inputCSV) * 2;
		// Default outputFile = input_csv + "_output.csv" or "_output.arff"
		if (!Common.notNullEmpty(outputFile)) {
			outputFile = inputCSV + "_output." + (outFmtCSV ? "csv" : "arff");
		}
		// 1. get instance number and generate ARFF header (if needed)
		output("Counting instance number started. " + inputCSV);
		Timer timer = new Timer();
		m_instNum = outFmtCSV ? Common.getInstNum(inputCSV) : genArffHeader(inputCSV);
		output("Counting instance number finished. " + timer);
		// 2. determine the number of sub files
		if (rowNum > 0) {
			subNum = Math.max(1, (int) Math.ceil((double) m_instNum / rowNum));
		} else {
			if (subNum <= 0) {
				subNum = 1;
			}
			rowNum = (int) Math.ceil((double) m_instNum / subNum);
		}
		// 3. generate mapping of which instance goes to which sub file
		ArrayList<Integer> instIdLst = new ArrayList<Integer>(m_instNum);
		for (int i = 0; i < m_instNum; i++) {
			instIdLst.add(i);
		}
		if (shuffle) {
			Collections.shuffle(instIdLst); // shuffle instances?
		}
		m_mapInstSub = new HashMap<Integer, Integer>(m_instNum);
		for (int i = 0; i < instIdLst.size(); i++) {
			m_mapInstSub.put(instIdLst.get(i), i / rowNum);
		}
		// 4. distribute instances to sub files according to the mapping
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(inputCSV));
			String csvHeader = br.readLine(); // the title row
			m_finished += csvHeader.length() + 2;
			m_subFiles = new String[subNum];
			BufferedWriter[] bw = new BufferedWriter[subNum];
			//
			// String outputName = FilenameUtils.getBaseName(outputFile);
			String outputName = AAI_IO.getFileNamePre(outputFile);
			// String outputExt = FilenameUtils.getExtension(outputFile);
			String outputExt = AAI_IO.getFileNameExt(outputFile);
			// String outputPath = FilenameUtils.getFullPath(outputFile);
			String outputPath = AAI_IO.getCurDir(outputFile) + "/";
			// TODO: testing update progress
			for (int subNo = 0; subNo < subNum; subNo++) {
				m_subFiles[subNo] = outputPath + outputName + (subNum > 1 ? "_" + subNo : "") + "." + outputExt;
				bw[subNo] = new BufferedWriter(new FileWriter(m_subFiles[subNo]));
				// write CSV/ARFF header
				bw[subNo].write((outFmtCSV ? csvHeader : m_arffHeader) + "\n");
			}
			// distribute instances to sub files
			for (int instNo = 0; instNo < m_instNum; instNo++) {
				String line = br.readLine();
				m_finished += line.length() + 2;
				progress(m_finished, m_total);
				int subNo = m_mapInstSub.get(instNo);
				if (outFmtCSV) {
					// CSV data lines
					bw[subNo].write(line);
				} else {
					// ARFF data lines
					String[] values = line.split(",");
					for (int i = 0; i < m_features.length; i++) {
						if ((i < values.length) && (values[i].length() > 0)) {
							bw[subNo].write(stdString(values[i]));
						} else {
							bw[subNo].write("?");
						}
						bw[subNo].write((i < m_features.length - 1) ? "," : "");
					}
				}
				bw[subNo].newLine();
			}
			for (int subNo = 0; subNo < subNum; subNo++) {
				bw[subNo].close();
			}
		} finally {
			AAI_IO.close(br);
		}
		return m_subFiles;
	}

	/** entry of thread */
	@Override
	protected void mainProc() throws Exception {
		convert(m_inputCSV, outputFile, m_idNum, rowNum, subNum, m_valNum, shuffle, outFmtCSV);
	}

	/**
	 * -i input_csv -o output_file [-D id_num] [[-r row_num] | [-s sub_num]] [-v
	 * val_num] [-S] [-C]
	 */
	public void setOptions(String[] options) throws Exception {
		// -i input_csv
		m_inputCSV = Common.getOption("i", options);
		// -o output_file
		outputFile = Common.getOption("o", options);
		// [-D id_num]
		m_idNum = Common.getOptionInt("D", options, 0);
		// [-r row_num]
		rowNum = Common.getOptionInt("r", options, 0);
		// [-s sub_num]
		subNum = Common.getOptionInt("s", options, 0);
		// [-v val_num]
		m_valNum = Common.getOptionInt("r", options, 100);
		// [-S]
		shuffle = Common.getOptionBool("S", options);
		// [-C]
		outFmtCSV = Common.getOptionBool("C", options);
		// debug
		m_debug = Common.getOptionBool("debug", options);
	}

	public static void main(String[] args) throws Exception {
		CsvConverter module = new CsvConverter();
		System.out.println(CsvConverter.version());
		if (args.length == 0) {
			System.out.println("\n" + CsvConverter.help() + "\n");
			return;
		}
		module.addOptions(args);
		module.start();
		module.join();
		System.out.println("\n" + module);
		String outputFiles[] = module.getSubFiles();
		System.out.println(outputFiles.length + " sub file(s) generated:");
		for (int i = 0; i < outputFiles.length; i++) {
			System.out.println(outputFiles[i]);
		}
	}

	public static String help() {
		return "Convert and/or split a CSV file to a (list of) ARFF or CSV file(s).\n\n"
				+ "Syntax:\nJava -jar CsvConverter.jar -i input_csv [-o output_file] [[-r row_num] | [-s sub_num]] [-v val_num] [-S] [-C]\n\n"
				+ "-i input_csv: the input CSV file to convert and/or split. It must has a title row and values are separated by \",\".\n"
				+ "[-o output_file]: the output CSV or ARFF file(s). Default is input_csv + \"_output.csv\" or \"_output.arff\". Multiple output sub-files are named as: output_file + \"_#\" + .csv or .arff)."
				+ "[-D id_num]: # of ID columns from left-most. Default 0.\n"
				+ "[-r row_num]: # of instances an output sub-file has. Default unlimited (no split).\n"
				+ "[-s sub_num]: # of output sub-files. Default 1 (no split). Ignored if \"-r row_num\" is set.\n"
				+ "[-v val_num]: upper bound of distinct values a nominal feature has. If exceeds, feature is considered as a STRING. Default 100.\n"
				+ "[-S]: shuffle instances or not. Default NO.\n" + "[-C]: output CSV or ARFF. Default ARFF.";
	}

	public static String version() {
		return "Last modified on 8 Jan 2015, change convert() to public so that it can be called by other Java objects.\n"
				+ "Last modified on 5 Jan 2015, update status\n"
				+ "V0.2.0.2, migrating to AAI Platform, 9 Dec 2014 Allen.\n"
				+ "V0.2.0.1, added -o output_file option, 17 Nov 2014 Allen.\n"
				+ "V0.2, Convert and/or split a CSV file to a (list of) ARFF or CSV file(s)\n"
				+ "V0.1, Jar file created on 11 Sep 2014.\n"
				+ "V0.1, Convert a CSV file into a (list of) ARFF file(s), Created on 2 June 2014 by Allen Lin.";
	}
}