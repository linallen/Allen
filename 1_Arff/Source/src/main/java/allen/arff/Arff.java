package allen.arff;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

import allen.base.common.*;
import allen.base.feature.*;
import allen.base.module.*;

/**
 * Arff class for manipulating Arff objects, including loading and saving ARFF
 * file, alerting, splitting, swapping, and deleting features, etc.
 * 
 * @author Allen Lin, 29 Jan 2015
 */
public class Arff extends AAI_Module {
	private static final long serialVersionUID = -3963864435580789229L;

	public static final String RELATION = "@relation";
	public static final String ATTRIBUTE = "@attribute";
	public static final String DATA = "@data";
	public static final String NUMERIC = "numeric"; // numeric feature
	public static final String STRING = "string"; // string feature
	public static final String MISSING = "?"; // missing value

	/** relation name */
	private String m_relation;

	/** feature table */
	private ArrayList<Feature> m_features = new ArrayList<Feature>();

	/** instance number */
	private int m_instNum;

	/** index of class feature. Default is the last feature */
	private int m_clsIdx = -1;

	/** constructors *********************************************/
	public Arff() {
	}

	public Arff(AAI_Module owner) {
		owner(owner);
	}

	/** property functions ***************************************/
	public Integer clsIdx() {
		return m_clsIdx;
	}

	public void clsIdx(Integer clsIdx) throws Exception {
		if (clsIdx == null) {
			return;
		}
		if (clsIdx == -1) {
			m_clsIdx = ftrNum() - 1;
		} else if ((clsIdx >= 0) && (clsIdx < ftrNum())) {
			m_clsIdx = clsIdx;
		} else {
			throw new Exception("Invalid class index: " + clsIdx);
		}
	}

	public int instNum() {
		return m_instNum;
	}

	public void instNum(int instNum) {
		m_instNum = instNum;
	}

	public int ftrNum() {
		return m_features.size();
	}

	/** get feature by name */
	public Feature getFeature(String ftrName) {
		for (Feature ftr : m_features) {
			if (ftr.name().equalsIgnoreCase(ftrName)) {
				return ftr;
			}
		}
		return null;
	}

	/** get feature names[] */
	public String[] getFtrNames() {
		String[] ftrNames = new String[ftrNum()];
		for (int i = 0; i < ftrNum(); i++) {
			ftrNames[i] = new String(m_features.get(i).name());
		}
		return ftrNames;
	}

	/** get class feature */
	public Feature getClassFeature() {
		return getFeature(clsIdx());
	}

	public void relation(String relation) {
		m_relation = relation;
	}

	public String relation() {
		return m_relation;
	}

	/** get feature[i] */
	public Feature getFeature(Integer idx) {
		return (idx == null) ? null : m_features.get(idx);
	}

	/** get features[] of certain type excluding the class feature */
	public ArrayList<Feature> getFeatures(FtrType ftrType) {
		ArrayList<Feature> ftrs = new ArrayList<Feature>();
		for (int i = 0; i < ftrNum(); i++) {
			Feature ftr = getFeature(i);
			if ((ftr.type() == ftrType) && (i != clsIdx())) {
				ftrs.add(ftr);
			}
		}
		return ftrs;
	}

	/** get feature index */
	public int getFeatureIdx(Feature ftr) {
		return m_features.indexOf(ftr);
	}

	/**
	 * return the index of a feature. (Added on 20 May 2015)
	 * 
	 * @param option
	 *            index or name of feature
	 * @return index of feature. -1 means "not found".
	 */
	public int getFtrIdx(String option) {
		try {
			int ftrIdx = -1; // default feature not found
			option = (option == null) ? null : option.trim();
			if (Common.notNullEmpty(option)) {
				if (Common.isInteger(option)) {
					ftrIdx = Integer.parseInt(option);
					if ((ftrIdx >= 0) && (ftrIdx < ftrNum())) {
						return ftrIdx;
					} else if (ftrIdx == -1) {
						return ftrNum() - 1;
					}
					return -1;
				} else {
					Feature ftr = getFeature(option);
					return m_features.indexOf(ftr);
				}
			}
			return ftrIdx;
		} catch (Exception e) {
			return -1;
		}
	}

	/**
	 * return class index.
	 * 
	 * @param clsOpt
	 *            could be class index or class name
	 * @return index of class, 0 if not found
	 */
	public static int getClsIdx(String arffFile, String clsOpt) throws Exception {
		int clsIdx = -1;
		// 1. load arff header (the features[])
		Arff arff = new Arff();
		arff.loadHeader(arffFile);
		// 2. get class index (from 0 to ftr_num -1 )
		if ((clsOpt == null) || clsOpt.trim().isEmpty()) {
			clsOpt = "-1"; // default the last one
		}
		clsOpt = clsOpt.trim();
		if (Common.isInteger(clsOpt)) {
			clsIdx = Integer.parseInt(clsOpt);
			clsIdx = (clsIdx < 0) ? (arff.ftrNum() - 1) : clsIdx;
			if (clsIdx > arff.ftrNum() - 1) {
				throw new Exception("Class index out of bound. " + Common.quote(clsOpt));
			}
			clsIdx = arff.getFeatureIdx(arff.getFeature(clsIdx));
		} else {
			clsIdx = arff.getFeatureIdx(arff.getFeature(clsOpt));
		}
		if (clsIdx < 0) {
			throw new Exception("Class not found. " + Common.quote(clsOpt));
		}
		if (arff.getFeature(clsIdx).type() != FtrType.CATEGORICAL) {
			throw new Exception(
					"Class is not CATEGORICAL. " + Common.quote(arff.getFeature(clsIdx).name() + " (" + clsOpt + ")"));
		}
		return clsIdx;
	}

	/** operation functions **************************************/
	/** merge this arff with other arff object. New arff = this + other */
	public void merge(Arff arff) {
		instNum(Math.max(instNum(), arff.instNum()));
		for (int i = 0; i < arff.ftrNum(); i++) {
			addFeature(arff.getFeature(i));
		}
	}

	/** save feature[i] into an ARFF file */
	public void saveFeature(String ftrARFF, int i) throws Exception {
		Arff ftrArff = new Arff();
		ftrArff.relation(relation());
		ftrArff.instNum(m_instNum);
		ftrArff.addFeature(0, getFeature(i));
		ftrArff.saveARFF(ftrARFF);
	}

	public void removeFeature(String ftrName) {
		Feature ftr = getFeature(ftrName);
		m_features.remove(ftr);
	}

	public void removeFeature(Feature ftr) {
		m_features.remove(ftr);
	}

	public void addFeature(Feature ftr) {
		m_features.add(ftr);
	}

	public void addFeature(int idx, Feature ftr) {
		m_features.add(idx, ftr);
	}

	public void removeInstances() {
		for (Feature ftr : m_features) {
			ftr.removeInstances();
		}
		m_instNum = 0;
	}

	/** save to an ARFF file */
	private void saveARFF(String arffFile, boolean hdrOnly) throws Exception {
		AAI_IO.createFile(arffFile);
		String type = (hdrOnly ? "header" : "file");
		output("Saving arff " + type + " started. " + arffFile);
		Timer timer = new Timer();
		FileWriter fw = null;
		BufferedWriter bw = null;
		try {
			fw = new FileWriter(new File(arffFile).getAbsoluteFile());
			bw = new BufferedWriter(fw);
			// 1. @RELATION
			bw.write(RELATION + " " + m_relation + "\n\n");
			// 2. @ATTRIBUTE
			for (Feature ftr : m_features) {
				bw.write(ATTRIBUTE + " " + ftr.toArff() + "\n");
			}
			// 3. @DATA
			bw.write("\n" + DATA + "\n");
			if (hdrOnly) {
				return;
			}
			for (int i = 0; i < m_instNum; i++) {
				progress(i + 1, m_instNum);
				for (int j = 0; j < m_features.size(); j++) {
					Feature ftr = m_features.get(j);
					Object instValue = ftr.getInstValue(i);
					instValue = (instValue == null) ? MISSING : instValue;
					bw.write(((j == 0) ? "" : ",") + instValue);
				}
				bw.write("\n");
			}
		} finally {
			AAI_IO.close(bw);
			AAI_IO.close(fw);
			output("Saving arff " + type + " finished. " + timer);
		}
	}

	/** save ARFF header only */
	public void saveHeader(String arffFile) throws Exception {
		saveARFF(arffFile, true);
	}

	/** save ARFF */
	public void saveARFF(String arffFile) throws Exception {
		saveARFF(arffFile, false);
	}

	/** save to a CSV file */
	public void saveCSV(String csvFile) throws Exception {
		output("Saving to CSV started. " + csvFile);
		Timer timer = new Timer();
		FileWriter fw = null;
		BufferedWriter bw = null;
		try {
			fw = new FileWriter(new File(csvFile).getAbsoluteFile());
			bw = new BufferedWriter(fw);
			// 1. title row
			for (int i = 0; i < m_features.size(); i++) {
				Feature ftr = m_features.get(i);
				bw.write((i == 0 ? "" : ", ") + ftr.name());
			}
			bw.write("\n");
			// 2. data rows
			for (int i = 0; i < m_instNum; i++) {
				progress(i + 1, m_instNum);
				for (int j = 0; j < m_features.size(); j++) {
					Feature ftr = m_features.get(j);
					Object instValue = ftr.getInstValue(i);
					instValue = (instValue == null) ? MISSING : instValue;
					bw.write(((j == 0) ? "" : ", ") + instValue);
				}
				bw.write("\n");
			}
			output("Saving to CSV finished. " + timer);
		} finally {
			AAI_IO.close(bw);
			AAI_IO.close(fw);
		}
	}

	/**
	 * e.g., str=" @relation  hi Neo ", key="@RELATION", then return "hi Neo"
	 */
	private static String getKeyValue(String line, String key) {
		line = line.trim();
		String parts[] = line.split(" ");
		if ((parts.length > 0) && parts[0].equalsIgnoreCase(key)) {
			return line.substring(parts[0].length()).trim();
		}
		return null;
	}

	/** load values from line = {value1,value2,...} */
	private static void readFtrValues(Feature ftr, String line) throws Exception {
		// remove leftmost "{" and rightmost "}"
		line = line.substring(1, line.length() - 1);
		String values[] = line.split(",");
		ftr.removeFtrValues();
		for (String value : values) {
			ftr.addFtrValue(value);
		}
	}

	/** return a new feature created from \@attribute ftrName type/values */
	private Feature readAttribute(String line) throws Exception {
		line = line.replaceAll("\\s+", " ");
		line = line.replace("{ ", "{");
		line = line.replace(", ", ",");
		Feature ftr = null;
		String parts[] = line.split(" ");
		if ((parts.length >= 3) && parts[0].equalsIgnoreCase(ATTRIBUTE)) {
			ftr = new Feature();
			ftr.name(parts[1]);
			if (parts[2].startsWith("{") && parts[2].endsWith("}")) {
				// Categorical feature
				ftr.type(FtrType.CATEGORICAL);
				readFtrValues(ftr, parts[2]);
			} else {
				// Numeric, String, or Date feature
				FtrType ftrType = FtrType.getFtrType(parts[2]);
				if (ftrType == null) {
					return null;
				}
				ftr.type(ftrType);
			}
		}
		return ftr;
	}

	/** reset all data of this ARFF object */
	public void reset() {
		m_relation = null;
		m_features.clear();
		m_instNum = 0;
	}

	/** load arff or arff header from file */
	private void loadArff(String arffFile, boolean hdrOnly) throws Exception {
		reset();
		String type = (hdrOnly ? "header" : "file");
		output("Loading arff " + type + " started. " + arffFile);
		Timer timer = new Timer();
		if (!AAI_IO.fileExist(arffFile)) {
			throw new Exception(arffFile + ": file not found.");
		}
		BufferedReader br = new BufferedReader(new FileReader(arffFile));
		try {
			long total = AAI_IO.getFileSize(arffFile), finished = 0;
			String line = null;
			// 1. @RELATION
			while ((m_relation == null) && (line = br.readLine()) != null) {
				progress(finished += line.length() + 2, total);
				if (line.trim().startsWith("%")) {
					continue; // skip comments
				}
				m_relation = getKeyValue(line, RELATION);
			}
			// 2. @ATTRIBUTE - read attributes until reach @DATA
			while (((line = br.readLine()) != null) && (getKeyValue(line, DATA) == null)) {
				progress(finished += line.length() + 2, total);
				if (line.trim().startsWith("%")) {
					continue; // skip comments
				}
				if (!line.trim().isEmpty()) {
					Feature ftr = readAttribute(line.trim());
					Common.Assert(ftr != null);
					m_features.add(ftr);
					dbgPrint(ftr.toString());
				}
			}

			// 3. @DATA
			if (hdrOnly || (getKeyValue(line, DATA) == null)) {
				return; // load header only or has no @DATA lines
			}
			for (m_instNum = 0; (line = br.readLine()) != null; m_instNum++) {
				progress(finished += line.length() + 2, total);
				if (line.trim().startsWith("%")) {
					continue; // skip comments
				}
				String[] values = line.trim().split(",");
				Common.Assert(values.length <= m_features.size());
				for (int j = 0; j < values.length; j++) {
					values[j] = values[j].trim();
					if ((values[j].length() > 0) && (!values[j].equals("?"))) {
						// only add non-missing values to features
						Feature ftr = m_features.get(j);
						ftr.setInstValue(m_instNum, Common.stdString(values[j]));
					}
				}
			}
		} finally {
			AAI_IO.close(br);
			output("Loading arff " + type + " finished. " + timer);
		}
	}

	/** load arff file */
	public void load(String arffFile) throws Exception {
		loadArff(arffFile, false);
	}

	/** load arff header */
	public void loadHeader(String arffFile) throws Exception {
		loadArff(arffFile, true);
	}

	/**
	 * split arff file on a specific feature.
	 * 
	 * @return sub-files
	 */
	public String[] splitArff(String arffFile, String ftrName) throws Exception {
		output("Splitting " + arffFile + " on feature " + ftrName + " started.");
		Timer timer = new Timer();
		if (!AAI_IO.fileExist(arffFile)) {
			throw new Exception(arffFile + ": file not found.");
		}
		reset();
		BufferedReader br = new BufferedReader(new FileReader(arffFile));
		String[] subArffs = new String[0];
		FileWriter[] subWriters = new FileWriter[0];
		try {
			long total = AAI_IO.getFileSize(arffFile), finished = 0;
			String line = null;
			// 1. @RELATION
			while ((m_relation == null) && (line = br.readLine()) != null) {
				progress(finished += line.length() + 2, total);
				if (line.trim().startsWith("%")) {
					continue; // skip comments
				}
				m_relation = getKeyValue(line, RELATION);
			}
			// 2. @ATTRIBUTE - read attributes until reach @DATA
			while (((line = br.readLine()) != null) && (getKeyValue(line, DATA) == null)) {
				progress(finished += line.length() + 2, total);
				if (line.trim().startsWith("%")) {
					continue; // skip comments
				}
				if (!line.trim().isEmpty()) {
					Feature ftr = readAttribute(line.trim());
					Common.Assert(ftr != null);
					m_features.add(ftr);
					dbgPrint(ftr.toString());
				}
			}

			// 3. create sub-files: *+ftrName=ftrValue.arff
			Feature splitFtr = getFeature(ftrName);
			if (splitFtr == null) {
				throw new Exception(ftrName + ": no such feature");
			}
			Common.Assert(splitFtr.ftrValueNum() > 0);
			subArffs = new String[splitFtr.ftrValueNum()];
			subWriters = new FileWriter[splitFtr.ftrValueNum()];
			for (int i = 0; i < splitFtr.ftrValueNum(); i++) {
				subArffs[i] = arffFile + "(" + ftrName + "=" + splitFtr.getFtrValue(i) + ").arff";
				saveARFF(subArffs[i]); // save header to sub-files
				subWriters[i] = new FileWriter(new File(subArffs[i]), true);
			}
			// 3. split @DATA
			if (getKeyValue(line, DATA) != null) {
				// index of splitting feature
				int splitIdx = getFeatureIdx(splitFtr);
				for (m_instNum = 0; (line = br.readLine()) != null; m_instNum++) {
					progress(finished += line.length() + 2, total);
					if (line.trim().startsWith("%")) {
						continue; // skip comments
					}
					String[] values = line.trim().split(",");
					String splitValue = null;
					int subIdx = -1;
					if (splitIdx < values.length) {
						splitValue = values[splitIdx];
						subIdx = splitFtr.getFtrValueIdx(splitValue);
						if (subIdx >= 0) {
							subWriters[subIdx].write(line + "\n");
						}
					}
				}
			}
		} finally {
			AAI_IO.close(br);
			for (int i = 0; i < subWriters.length; i++) {
				AAI_IO.close(subWriters[i]);
			}
		}
		output("Splitting " + arffFile + " finished. " + timer);
		return subArffs;
	}

	/** return factors[] of a feature (feature name + all values) */
	public String[] getFtrFactors(String ftrName) throws Exception {
		Feature ftr = this.getFeature(ftrName);
		if ((ftr == null)) {
			throw new Exception("Feature not found! " + ftrName);
		}
		if (ftr.type() != FtrType.CATEGORICAL) {
			throw new Exception("Feature not Categorical! " + ftrName);
		}
		String[] factors = new String[ftr.ftrValueNum()];
		for (int i = 0; i < ftr.ftrValueNum(); i++) {
			String ftrValue = ftr.getFtrValue(i);
			factors[i] = factorStr(ftr.name(), ftrValue);
		}
		return factors;
	}

	/** return factor: ftr_name=ftr_value */
	public static String factorStr(String ftrName, String ftrValue) throws Exception {
		if (ftrName.trim().isEmpty() || ftrValue.trim().isEmpty()) {
			throw new Exception("empty feature name or value: " + ftrName + "=" + ftrValue);
		}
		return ftrName + "=" + ftrValue;
	}

	/** "ftr1,ftr2" to [["ftr1=A","ftr1=B"],["ftr2=C","ftr2=D"]] */
	public String[][] getFtrFactors(String ftrNames[]) throws Exception {
		String[][] ftrFactors = new String[ftrNames.length][];
		for (int i = 0; i < ftrNames.length; i++) {
			ftrFactors[i] = getFtrFactors(ftrNames[i]);
		}
		return ftrFactors;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(RELATION + " " + relation() + "\n\n");
		for (int i = 0; i < ftrNum(); i++) {
			sb.append(ATTRIBUTE + " " + getFeature(i).toString() + "\n");
		}
		return sb.toString();
	}

	public static void main(String[] args) throws Exception {
		Arff module = new Arff(null);
		System.out.println("\n" + Arff.version());
		if (args.length == 0) {
			System.out.println(Arff.help() + "\n");
			return;
		}
		module.setOptions(args);
		module.start();
		module.join();
	}
}