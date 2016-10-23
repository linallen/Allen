package allen.discretizate.csv;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import allen.base.common.AAI_IO;
import allen.base.common.Common;
import allen.csv.converter.CsvConverter;
import weka.core.converters.CSVSaver;

public class Feature {
	/** feature ID */
	private int m_id;
	/** feature name */
	public String m_name;
	/** indicator of if the feature is numerical */
	public boolean m_numeric;

	/** minimum / maximum number of the feature's values */
	public double m_min = Double.POSITIVE_INFINITY;
	public double m_max = Double.NEGATIVE_INFINITY;

	/** name of the CSV file storing the original numerical values */
	public String m_numericInCSV = new String();
	/** name of the ARFF file storing the original numerical values */
	public String m_numericInARFF = new String();

	/** name of the ARFF file storing the discretized nominal values */
	public String m_nominalOutARFF = new String();
	/** name of the CSV file storing the discretized nominal values */
	public String m_nominalOutCSV = new String();

	/** reader and writer of temporary data file */
	private BufferedWriter m_bw;
	private BufferedReader m_br;

	/** [OUTPUT] cut points */
	public double m_cutPts[];

	public Feature(int id, String name) {
		m_id = id;
		m_name = name;
	}

	/** transfer m_numericCSV to m_numericARFF */
	public void Csv2Arff() throws Exception {
		CsvConverter csvConverter = new CsvConverter();
		// -i input_csv [-o output_file] [-D id_num] [[-r row_num] | [-s
		// sub_num]] [-v val_num] [-S] [-C]
		String arffs[] = csvConverter.convert(m_numericInCSV, null, 0, 0, 0, 0, false, false);
		m_numericInARFF = arffs[0];
	}

	public void setTmpFiles(String tempDir) throws Exception {
		m_numericInCSV = tempDir + getDesc() + "-numeric-In.csv";
		m_numericInARFF = tempDir + getDesc() + "-numeric-In.arff";
		m_nominalOutARFF = tempDir + getDesc() + "-nominal-Out.arff";
		m_nominalOutCSV = tempDir + getDesc() + "-nominal-Out.csv";
	}

	/** transfer ARFF result into CSV */
	public void Arff2Csv() throws Exception {
		String options = "-i " + m_nominalOutARFF + " -o " + m_nominalOutCSV;
		CSVSaver.main(options.split(" "));
	}

	/** add a value to values[] */
	public void addValue(String valueStr) throws Exception {
		if (m_numeric && !valueStr.isEmpty()) {
			m_numeric = (m_numeric && Common.isNumeric(valueStr));
			if (m_numeric) {
				double value = Double.parseDouble(valueStr);
				m_min = Math.min(m_min, value);
				m_max = Math.max(m_max, value);
			}
		}
	}

	public void writeNumericCSV(String row) throws IOException {
		if (m_numeric) {
			if (m_bw == null) {
				AAI_IO.saveFile(m_numericInCSV, "", false);
				m_bw = new BufferedWriter(new FileWriter(m_numericInCSV));
			}
			m_bw.write(row + "\n");
		}
	}

	public String readNominalCSV() throws IOException {
		if (m_br == null) {
			m_br = new BufferedReader(new FileReader(m_nominalOutCSV));
			m_br.readLine(); // skip the title row
		}
		return m_br.readLine();
	}

	public void close() throws IOException {
		if (m_br != null) {
			m_br.close();
		}
		if (m_bw != null) {
			m_bw.close();
		}
		m_br = null;
		m_bw = null;
	}

	public String getDesc() {
		return m_id + "." + m_name;
	}

	public String toString() {
		return getDesc() + (m_numeric ? "[numeric]" : "[nominal]");
	}
}