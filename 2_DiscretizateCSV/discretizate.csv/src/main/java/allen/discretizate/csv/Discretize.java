package allen.discretizate.csv;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import allen.base.common.AAI_IO;
import allen.base.common.Common;
import allen.base.common.Timer;
//import allen.base.dataset.Feature;
//import allen.base.feature.Feature;
import allen.base.module.AAI_Module;
import weka.core.Utils;
import weka.filters.Filter;

/**
 * Class for discretizing a CSV feature table.
 * <p>
 * <b>Syntax:</b><br>
 * Java -jar discretize.jar -i inuptCSV [-o outputCSV] [-p pointsCSV] [-B
 * bin_num] [-D discard_num]
 * <ul>
 * <li>-i orgFtrs.csv ([Input] the original feature table)</li>
 * <li>-o disFtrs.csv ([Output] the discretized feature table)</li>
 * <li>-p cutPoints.csv ([Output] the cut points + boundaries of features)</li>
 * <li>-B bin_num ([Input] number of bins. >1 for unsupervised and others for
 * supervised)</li>
 * <li>-D discard_num ([Input] number of ID columns of the feature table)</li>
 * </ul>
 * 
 * <b>Note:</b><br>
 * to get around the memory overflow problem, we never read the whole feature
 * table into memory. Instead, we extract and save each numeric feature into
 * temporary files on which we do the (supervised or unsupervised)
 * discritization on the feature one by one.
 * 
 * @author Allen Lin, 16 July 2014
 */
public class Discretize extends AAI_Module {
	private static final long serialVersionUID = -2752881853010656400L;

	/** [Input] store the original feature table */
	private String m_orgFtrsCSV;
	/** [Output] store the discretized feature table */
	private String m_disFtrsCSV;
	/**
	 * [Output] store the cut points and min & max values of numerical features
	 */
	private String m_cutPointsCSV;
	/** [Input] number of bins. >1 for unsupervised and others for supervised */
	private int m_binNum = 10;
	/** [Input] number of ID columns of the feature table */
	private int m_discardNum = 0;

	/** [Tmp] features[] */
	private Feature[] m_features;
	/** [Dbg] number of data rows in the input CSV data file */
	private int m_rowNum;
	/** [Dbg]number of numerical features */
	private int m_numericFtrNum;

	/** 1. first scan: build numerical features m_features[] */
	private void examFeatures() throws Exception {
		long fileSize = AAI_IO.getFileSize(m_orgFtrsCSV);
		long readSize = 0, curPercent = 0;
		Timer timer = new Timer();

		BufferedReader br = new BufferedReader(new FileReader(m_orgFtrsCSV));
		// 1. initialize features[] with title row
		String line = br.readLine();
		readSize += line.length() + 2;
		String ftrNames[] = line.split(",");
		m_features = new Feature[ftrNames.length - 1];
		for (int i = 0; i < m_features.length; i++) {
			Common.Assert(!ftrNames[i].isEmpty());
			m_features[i] = new Feature(i, ftrNames[i]);
			// discard left-most ID columns (if any)
			m_features[i].m_numeric = (i >= m_discardNum);
		}
		m_rowNum = 0;
		// 2. generate features[] from data rows
		for (; (line = br.readLine()) != null; m_rowNum++) {
			readSize += line.length() + 2;
			String ftrValues[] = line.split(",");
			Common.Assert(ftrValues.length == ftrNames.length);
			for (int i = 0; i < m_features.length; i++) {
				m_features[i].addValue(ftrValues[i].trim());
			}
			// show progress
			long newPercent = (long) (100 * readSize / fileSize);
			if (((newPercent % 10) == 0) && (newPercent > curPercent)) {
				curPercent = newPercent;
				System.out.print(newPercent + "%(" + timer.elapsed() + "s) ");
			}
		}
		br.close();
	}

	/** 2. second scan: generate temporary numerical feature CSV files */
	private void splitFeatures() throws Exception {
		long fileSize = AAI_IO.getFileSize(m_orgFtrsCSV);
		long readSize = 0, curPercent = 0;
		Timer timer = new Timer();

		BufferedReader br = new BufferedReader(new FileReader(m_orgFtrsCSV));
		// Initialize temporary files
		for (int i = 0; i < m_features.length; i++) {
			m_features[i].setTmpFiles(tempDir());
		}
		for (String line; (line = br.readLine()) != null;) {
			readSize += line.length() + 2;
			String cols[] = line.split(",");
			String clsLabel = cols[cols.length - 1];
			for (int i = 0; i < m_features.length; i++) {
				// write numerical features into temporary files
				m_features[i].writeNumericCSV(cols[i] + "," + clsLabel);
			}
			// show progress
			long newPercent = (long) (100 * readSize / fileSize);
			if (((newPercent % 10) == 0) && (newPercent > curPercent)) {
				curPercent = newPercent;
				System.out.print(newPercent + "%(" + timer.elapsed() + "s) ");
			}
		}
		br.close();
		for (int i = 0; i < m_features.length; i++) {
			m_features[i].close();
		}
	}

	/** 3. discretize features and save the results into temporary files */
	private void discretizeFeatures() throws Exception {
		for (int i = 0, ftrNum = 0; i < m_features.length; i++) {
			if (m_features[i].m_numeric == false) {
				continue;
			}
			Timer timer = new Timer();
			System.out.print("Dsicretizing " + i + ": ");
			// conver CSV to ARFF to avoid memory overflow caused by CSVLoader
			m_features[i].Csv2Arff();
			String opt = "-i " + m_features[i].m_numericInARFF + " -o " + m_features[i].m_nominalOutARFF;
			String filterName, options[];
			if (m_binNum > 1) {
				filterName = "weka.filters.unsupervised.attribute.Discretize";
				options = (opt + " -B " + m_binNum).split(" ");
			} else {
				filterName = "weka.filters.supervised.attribute.Discretize";
				options = (opt + " -c last").split(" ");
			}
			//
			Filter f = (Filter) Class.forName(filterName).newInstance();
			Filter.runFilter(f, options);
			if (filterName.contains("unsupervised")) {
				m_features[i].m_cutPts = ((weka.filters.unsupervised.attribute.Discretize) f).getCutPoints(0);
			} else {
				m_features[i].m_cutPts = ((weka.filters.supervised.attribute.Discretize) f).getCutPoints(0);
			}
			// transfer ARFF to CSV temporary file
			m_features[i].Arff2Csv();
			// Arrays.sort(m_features[i].m_cutPts); // necessary?
			System.out.println((++ftrNum) + "/" + m_numericFtrNum + " done. " + timer + ". " + m_features[i]);
		}
	}

	/** output cut points of the numeric features */
	private void savePoints() throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(m_cutPointsCSV));
		int maxDepth = 0;
		// 1. write title row
		for (int i = 0; i < m_features.length; i++) {
			if (m_features[i].m_numeric) {
				maxDepth = Math.max(maxDepth, m_features[i].m_cutPts.length);
				bw.write(m_features[i].m_name + ",");
			}
		}
		bw.write("\n");
		// 2. write minimum value
		for (int i = 0; i < m_features.length; i++) {
			if (m_features[i].m_numeric) {
				bw.write(m_features[i].m_min + ",");
			}
		}
		bw.write("\n");
		// 3. write cut points
		for (int j = 0; j < maxDepth; j++) {
			for (int i = 0; i < m_features.length; i++) {
				if (m_features[i].m_numeric && (j < m_features[i].m_cutPts.length)) {
					bw.write(m_features[i].m_cutPts[j] + ",");
				}
			}
			bw.write("\n");
		}
		// 4. write maximum value
		for (int i = 0; i < m_features.length; i++) {
			if (m_features[i].m_numeric) {
				bw.write(m_features[i].m_max + ",");
			}
		}
		bw.write("\n");
		bw.close();
	}

	/** 4. third scan: combine the results into a final nominal CSV */
	private void saveFeatures() throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(m_orgFtrsCSV));
		BufferedWriter bw = new BufferedWriter(new FileWriter(m_disFtrsCSV));
		bw.write(br.readLine() + "\n"); // title row
		for (String line; (line = br.readLine()) != null;) {
			String cols[] = line.split(",");
			String label = cols[cols.length - 1];
			String row = new String();
			for (int i = 0; i < m_features.length; i++) {
				if (m_features[i].m_numeric) {
					String values[] = m_features[i].readNominalCSV().split(",");
					row += values[0] + ",";
				} else {
					row += cols[i] + ",";
				}
			}
			bw.write(row + label + "\n");
		}
		br.close();
		bw.close();
		for (int i = 0; i < m_features.length; i++) {
			m_features[i].close();
		}
	}

	private void runFilter() throws Exception {
		Timer timer = new Timer();
		// 1. first scan: build numerical features m_features[]
		System.out.println("\n1. Examing numerical features...");
		examFeatures();
		for (Feature ftr : m_features) {
			m_numericFtrNum += (ftr.m_numeric ? 1 : 0);
		}
		System.out.println("\nDone. " + timer + ".\n");
		System.out.println(m_rowNum + " data rows,");
		System.out.println(m_features.length + " features,");
		System.out.println(m_numericFtrNum + " numerical features.");

		// 2. second scan: generate temporary numerical feature ARFF files
		System.out.println("\n2. Spliting numerical features into sub-CSVs...");
		splitFeatures();
		System.out.println("\nDone. " + timer);

		// 3. discretize features and save the results into temporary files
		System.out.println("\n3. Discretizing numerical sub-CSVs...");
		discretizeFeatures();
		System.out.println("\nDone. " + timer);

		// 4. third scan: combine the results into a final nominal CSV
		if (!m_disFtrsCSV.isEmpty()) {
			System.out.println("\n4. Saving discretized feature table... ");
			saveFeatures();
			System.out.println("\nDone. " + timer);
		}

		// 5. output cut points
		if (!m_cutPointsCSV.isEmpty()) {
			System.out.println("\n4. Saving cut points and boundaries... ");
			savePoints();
			System.out.println("\nDone. " + timer);
		}
		System.out.println("\n\nAll done. " + timer);
	}

	@Override
	protected void mainProc() throws Exception {
		runFilter();
	}

	@Override
	public void setOptions(String[] options) throws Exception {
		m_orgFtrsCSV = Utils.getOption('i', options);
		if (new File(m_orgFtrsCSV).exists() == false) {
			throw new Exception("Invalid input CSV: \"" + m_orgFtrsCSV + "\"");
		}
		m_disFtrsCSV = Utils.getOption('o', options);
		m_cutPointsCSV = Utils.getOption('p', options);
		String binNum = Utils.getOption('B', options);
		if (Common.validString(binNum)) {
			m_binNum = Math.max(0, Integer.parseInt(binNum));
		}
		String discardNum = Utils.getOption('D', options);
		if (Common.validString(discardNum)) {
			m_discardNum = Math.max(0, Integer.parseInt(discardNum));
		}
		// debug, daemon, etc
		super.setOptions(options);
	}

	/**
	 * parameters: unsupervised[0] or supervised[bin number>0] + inCSV + outCSV
	 */
	public static void main(String[] args) throws Exception {
		// String clsName =
		// Thread.currentThread().getStackTrace()[1].getClassName();
		// String argsStr = Common.strArraytoStr(args, "_");
		// System.out.println("DEBUG start executing class " + clsName + "(" +
		// argsStr + ")");

		exec(Thread.currentThread().getStackTrace()[1].getClassName(), args);
		// System.out.println("DEBUG finish executing class " + clsName + "(" +
		// argsStr + ")");
	}

	public static String help() {
		return "Discretize a CSV feature table.\n"
				+ "The last column is class labels, which is used by supervised discretization or ignored by unsupervised discretization.\n\n"
				+ "Syntax:\nJava -Xmx1024m -jar discretize.jar -i orgFtrs.csv [-o orgFtrs.csv] [-p cutPoints.csv] [-B bin_num] [-D discard_num]\n\n"
				+ "Parameters:\n"
				+ "-i orgFtrs.csv ([Input] a CSV file storing the original feature table with a title row)\n"
				+ "-o disFtrs.csv ([Output] a CSV file storing the discretized feature table, under debugging)\n"
				+ "-p cutPoints.csv ([Output] a CSV file storing the cut points, min and max values of numerical features)\n"
				+ "-B bin_num ([Input] number of bins. bin_num <= 1 indicates supervised discretization, "
				+ "bin_num > 1 indicates unsupervised discretization, default is 10)\n"
				+ "-D discard_num ([Input] left-most ID columns to be ignored, default is 0)";
	}

	public static String version() {
		return "History:\n" + "V0.1, created on 16 July 2014 by Allen Lin.\n"
				+ "Changed weka.filters.Filter.runFilter() from protected to public.\n";
	}
}