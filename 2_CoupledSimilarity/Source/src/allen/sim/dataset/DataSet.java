package allen.sim.dataset;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;

import allen.base.common.AAI_IO;
import allen.base.common.Timer;
import allen.base.module.AAI_Module;
import allen.base.set.AllenSet;

/**
 * A data set containing a feature table.<br>
 * Format: ftr1,...,ftrn, label
 * 
 * @author Allen Lin, 24 Mar 2016
 */
public class DataSet extends AAI_Module implements AllenSet {
	private static final long serialVersionUID = -6871669684345314842L;

	private static final String RELATION = "@relation";
	private static final String ATTRIBUTE = "@attribute";
	private static final String DATA = "@data";
	// private static final String NUMERIC = "numeric"; // numeric feature
	// private static final String STRING = "string"; // string feature
	// private static final String MISSING = "?"; // missing value

	/** object[n] (i.e., instances[n]) */
	private ObjLst m_objLst = new ObjLst();
	/** feature[m] */
	private FtrSet m_ftrSet = new FtrSet();
	/** class[s] */
	private ClsSet m_clsSet = new ClsSet();

	/** file name of data set */
	private String m_dataFile;
	/** absolute directory of data set */
	private String m_dataDir;
	/** name (@relation) of data set */
	private String m_dataName;

	/** property functions ***************************************/
	@Override
	public int size() {
		return m_objLst.size();
	}

	public String getDataFile() {
		return m_dataFile;
	}

	public String getDataDir() {
		return m_dataDir;
	}

	public int objNum() {
		return m_objLst.size();
	}

	public ObjLst objLst() {
		return m_objLst;
	}

	public ArrayList<Obj> getObjs() {
		return m_objLst.getObjs();
	}

	public int ftrNum() {
		return m_ftrSet.size();
	}

	public int clsNum() {
		return m_clsSet.size();
	}

	public Obj getObj(int i) {
		return m_objLst.getObj(i);
	}

	public Feature ftr(String ftrName) throws Exception {
		return m_ftrSet.getFtr(ftrName);
	}

	public Collection<Feature> ftrs() {
		return m_ftrSet.ftrs();
	}

	public FtrSet ftrSet() {
		return m_ftrSet;
	}

	/** manipulation functions ***************************************/
	/**
	 * line=" @relation  hi Neo ", key="@RELATION", then return "hi Neo".
	 * 
	 * @param line
	 *            the input text line (non-null)
	 * @param key
	 *            the key word to match (non-null)
	 * @param delimiter
	 *            the delimiter that separate the parts[] (non-null)
	 * @param ignoreCase
	 *            ignore case or not for the key-matching
	 * @return the value if key is found, or null otherwise
	 */
	public static String getKeyValue(String line, String key, String delimiter, boolean ignoreCase) {
		String lineKey = null, lineValue = null;
		int pos = line.indexOf(delimiter);
		if (pos == -1) {
			lineKey = line;
			lineValue = new String();
		} else {
			lineKey = line.substring(0, pos);
			lineValue = line.substring(pos + delimiter.length(), line.length());
		}

		if (ignoreCase ? lineKey.equalsIgnoreCase(key) : lineKey.equals(key)) {
			return lineValue;
		}
		return null;
	}

	public static String getKeyValue(String line, String key) {
		return getKeyValue(line, key, " ", true);
	}

	/** return a new feature created from \@attribute ftrName type/values */
	private Feature parseAttrLine(String attrLine) throws Exception {
		attrLine = attrLine.trim().replaceAll("\\s+", " ").replace("{ ", "{").replace(", ", ",");
		String ftrStr = getKeyValue(attrLine, ATTRIBUTE);
		if (ftrStr == null || ftrStr.isEmpty()) {
			throw new Exception("Wrong " + ATTRIBUTE + ": " + attrLine);
		}
		String parts[] = ftrStr.trim().split(" ");
		Feature ftr = new Feature();
		ftr.name(parts[0]);
		if (parts.length != 2) {
			throw new Exception("Wrong " + ATTRIBUTE + ": " + attrLine);
		}
		if (parts[1].startsWith("{") && parts[1].endsWith("}")) {
			// Categorical feature: ftr_name {value1,value2,...}
			ftr.type(FtrType.CATEGORICAL);
			for (String valueStr : parts[1].substring(1, parts[1].length() - 1).split(",")) {
				Value value = new Value(valueStr, ftr);
				ftr.addValue(value);
			}
		} else {
			// Numeric, String, or Date features: ftr_name ftr_type
			ftr.type(FtrType.getFtrType(parts[1]));
		}
		return ftr;
	}

	/** @return if the line is arff comment. */
	private static boolean isComment(String line) {
		line = line.trim();
		return (line.isEmpty() || line.startsWith("%"));
	}

	public void loadArff(String arffFile) throws Exception {
		loadArff(arffFile, false);
	}

	public void loadArffHdr(String arffFile) throws Exception {
		loadArff(arffFile, true);
	}

	/** load arff or arff header from file */
	private void loadArff(String arffFile, boolean hdrOnly) throws Exception {
		String loadType = (hdrOnly ? "header" : "data");
		output("Started loading arff " + loadType + " from " + arffFile);
		Timer timer = new Timer();
		m_dataFile = AAI_IO.getFileName(arffFile);
		m_dataDir = AAI_IO.getAbsDir(arffFile);
		BufferedReader br = new BufferedReader(new FileReader(arffFile));
		try {
			long total = AAI_IO.getFileSize(arffFile), finished = 0;
			String line;
			// 1. @RELATION
			while ((line = br.readLine()) != null) {
				progress(finished += line.length() + 2, total);
				line = line.trim();
				if (!isComment(line)) {
					if ((m_dataName = getKeyValue(line, RELATION)) == null) {
						throw new Exception("Missing header: " + RELATION);
					}
					break;
				}
			}
			// 2. @ATTRIBUTE - read attributes until reach @DATA
			while (((line = br.readLine()) != null) && (getKeyValue(line, DATA) == null)) {
				progress(finished += line.length() + 2, total);
				line = line.trim();
				if (!isComment(line)) {
					// parse ATTRIBUTE line to ftr
					Feature ftr = parseAttrLine(line);
					m_ftrSet.addFtr(ftr.name(), ftr);
					m_ftrSet.setFtrIdx(ftr.name(), m_ftrSet.size() - 1);
					outputDbg(ftr.toString());
				}
			}
			// 3. @DATA
			if (!hdrOnly) {
				for (int objNum = 0; (line = br.readLine()) != null;) {
					progress(finished += line.length() + 2, total);
					line = line.trim();
					if (!isComment(line)) {
						String[] values = line.trim().split(",");
						if (values.length > m_ftrSet.size()) {
							warning("Too many values: " + line);
						}
						// 1. add values[] to object
						Obj obj = new Obj();
						obj.name("Obj_" + (++objNum));
						for (int j = 0; j < Math.min(values.length, m_ftrSet.size()); j++) {
							String valueStr = values[j].trim();
							if ((valueStr.length() > 1) || (!valueStr.equals("?"))) {
								// add value to object's value list
								Feature ftr = m_ftrSet.getFtr(j);
								Value value;
								if (ftr.type() == FtrType.CATEGORICAL) {
									value = ftr.getValue(valueStr);
									if (value == null) {
										throw new Exception("Value " + valueStr + " does not defined in " + ftr.name());
									}
								} else {
									value = new Value(valueStr, ftr);
								}
								obj.value(ftr, value);
							}
						}
						// 2. add object to object set
						m_objLst.addObj(obj);
					}
				}
			}
		} finally {
			AAI_IO.close(br);
			output("Finished loading arff " + loadType + " from " + arffFile + ". " + timer);
		}
		if (debug()) {
			dbgOutputSummary();
			dbgOutputData2Matlab();
			System.out.println(m_objLst);
			System.out.println(m_ftrSet);
			System.out.println(m_clsSet);
		}
	}

	/** Test */
	public static void main(String[] args) throws Exception {
		DataSet module = new DataSet();
		module.debug(true);
		module.loadArff("c:/test.arff", false);
	}

	/**
	 * output summary of data set: (for debug only)<br>
	 * [ds_name obj_num(M) ftr_num(N) cls_num min_val_num max_val_num
	 * ave_val_num]
	 */
	public void dbgOutputSummary() throws Exception {
		// if (debug()) {
		// String buf = m_dataName + "\t" + objNum() + "\t" + ftrNum() + "\t" +
		// clsNum() + "\t";
		// int minValNum = Integer.MAX_VALUE, maxValNum = Integer.MIN_VALUE,
		// totalValNum = 0;
		// for (int j = 0; j < m_ftrSet.size(); j++) {
		// Feature ftr = m_ftrSet.get(j);
		// int valNum = ftr.size();
		// minValNum = Math.min(minValNum, valNum);
		// maxValNum = Math.max(maxValNum, valNum);
		// totalValNum += valNum;
		// }
		// buf += minValNum + "\t" + maxValNum + "\t" + (1. * totalValNum /
		// ftrNum());
		// AAI_IO.saveFile(m_dataDir + m_dataName + ".ds_summary.txt", buf);
		// // System.out.println("Data Summary: " + buf);
		// }
	}

	public String dataSummary() {
		return "##### " + m_dataName + ", " + objNum() + "_objs_" + ftrNum() + "_ftrs_" + clsNum() + "classes";
	}

	/** output data set to Matlab matrix format: A = {'t','n','won';... } */
	public void dbgOutputData2Matlab() throws Exception {
		// String matlabFile = m_dataDir + m_dataName + ".m";
		// output("Outputing Matlab data started. " + matlabFile);
		// Timer timer = new Timer();
		// BufferedWriter bw = new BufferedWriter(new FileWriter(matlabFile));
		// try {
		// bw.write("clc;\nclear;\nA = {");
		// // String buf = "clc;\nclear;\nA = {";
		// int objNum = objNum();
		// for (int i = 0; i < objNum; i++) {
		// Obj obj = getObj(i);
		// int valNum = obj.values().size();
		// progress(i + 1, objNum);
		// for (int j = 0; j < valNum; j++) {
		// Value val = obj.getValue(j);
		// bw.write(Common.quote(val.getValue(), '\''));
		// if (j < valNum - 1) {
		// bw.write(",");
		// }
		// }
		// // bw.write(Common.quote(obj.getCls().getName(), '\'') +
		// // ";...\n");
		// bw.write(";...\n");
		// }
		// bw.write("};\n");
		// } finally {
		// AAI_IO.close(bw);
		// output("Loading data finished. " + timer);
		// }
		// // AAI_IO.saveFile(, buf);
		// // System.out.println("Data Summary: " + buf);
		// output("Outputing Matlab data finished. " + timer);
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("cls_num = " + clsNum() + "\n" + m_clsSet.toString() + "\n");
		buf.append("ftr_num = " + ftrNum() + "\n" + m_ftrSet.toString() + "\n");
		buf.append("obj_num = " + objNum() + "\n");
		buf.append(m_objLst.toString());
		return buf.toString();
	}

	public String version() {
		return "Created by Allen Lin, 24 Mar 2016.\n" + "Changed to operate ARFF only. 16 June 2016, Allen";
	}
}