package allen.clusterer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.HashSet;

import allen.base.common.Common;
import allen.base.common.Timer;
import allen.base.module.AAI_Module;
import allen.sim.dataset.DataSet;
import allen.sim.dataset.Obj;
import allen.sim.measure.SimMeasure;

/**
 * Base class of clustering algorithms.
 * 
 * <b>Syntax:</b><br>
 * Java -jar cluster_alg.jar -i input_file -s sim_name [-k k] [-o output_file]
 * <br>
 * <ul>
 * <li><i>-i input_file</i>: [input] the data file (CSV with no title) with the
 * last column being label and others being features (attributes)</li>
 * <li><i>-k k</i>: [para] k-modes. Default class number.</li>
 * <li><i>-s sim_name</i>: [para] similarity measure names: SMD, OFD, ADD, COS,
 * INTRA, INTER.</li>
 * <li><i>-r</i>: [para] randomly select the initial k modes if set, otherwise
 * initial with the first k objects.</li>
 * <li><i>-o output_file</i>: [output] objects and modes.<br>
 * Format: [obj_name, obj_class, mode_name + values]</li>
 * </ul>
 * 
 * @author Allen Lin, 14 June 2016
 */
public abstract class Clusterer extends AAI_Module {
	private static final long serialVersionUID = -4489564528508002488L;

	/** -i input_file: [input] the input categorical data file. */
	protected String m_inputArff;
	/** -r randomize: [parameter] randomize the algorithm, default NO */
	protected boolean m_randomize;
	/**
	 * -s sim_name: [parameter] similarity measure:<br>
	 * SMD, OFD, ADD, COS, INTRA, INTER
	 */
	protected String m_simName;
	/** -o output_file: [output] objects with top_k similar objects. */
	protected String m_outputFile;

	/** [input 1] data set */
	protected DataSet m_dataSet;
	/** [input 2] similarity measure */
	protected SimMeasure m_simMeasure;
	/** [input 3] k - pre-defined cluster number */
	protected int m_k;
	/** [output] clusters[] */
	protected int[] m_clusters = new int[0];

	/** property functions ***************************************/
	/** set similarity measure, data set, and k */
	public void setParams(DataSet dataSet, SimMeasure simMeasure, int k) {
		m_simMeasure = simMeasure;
		if (m_simMeasure != null) {
			m_simMeasure.owner(this);
		}
		m_dataSet = dataSet;
		if (m_dataSet != null) {
			m_dataSet.owner(this);
		}
		m_k = ((k <= 0) ? dataSet.clsNum() : k);
	}

	/** set/load data set */
	public void dataSet(String dataFile) throws Exception {
		DataSet dataSet = new DataSet();
		dataSet.owner(this);
		dataSet.debug(this.debug());
		dataSet.loadArff(m_inputArff);
		dataSet(dataSet);
	}

	/** set/load data set */
	public void dataSet(DataSet dataSet) throws Exception {
		m_dataSet = dataSet;
		m_dataSet.owner(this);
	}

	/** get data set */
	public DataSet dataSet() {
		return m_dataSet;
	}

	/** set similarity measure */
	public void simMeasure(String simName) throws Exception {
		SimMeasure simMeasure = SimMeasure.getSimMeasure(simName);
		simMeasure.owner(this);
		simMeasure.debug(this.debug());
		outputDbg("SIM_alg: " + Common.quote(simMeasure.name() + "/" + m_simName));
		simMeasure(simMeasure);
	}

	/** set similarity measure */
	public void simMeasure(SimMeasure simMeasure) {
		m_simMeasure = simMeasure;
		m_simMeasure.owner(this);
	}

	/** get similarity measure */
	public SimMeasure simMeasure() {
		return m_simMeasure;
	}

	/** set k */
	public void k(int k) {
		m_k = (m_k == 0) ? m_dataSet.clsNum() : m_k;
	}

	/** get k */
	public int k() {
		return m_k;
	}

	/** return number of resulting clusters */
	public int clusterNum() {
		HashSet<Integer> distClusters = new HashSet<Integer>();
		for (int cluster : m_clusters) {
			distClusters.add(cluster);
		}
		return distClusters.size();
	}

	/** return result clusters */
	public int[] clusters() {
		return m_clusters;
	}

	/** manipulation functions ***************************************/
	/**
	 * clustering given Data Set with given Similarity Measure.
	 * 
	 * @param simMeasure
	 *            similarity measure object
	 * @param DataSet
	 *            data set object
	 * @param k
	 *            cluster number
	 * @return cluster id array, starting from 0
	 */
	public int[] clustering(DataSet dataSet, SimMeasure simMeasure, int k) throws Exception {
		setParams(dataSet, simMeasure, k);
		return clustering();
	}

	public int[] clustering() throws Exception {
		String text = "clusterer " + name() + " on " + m_dataSet.name() + " with sim_measure = " + m_simMeasure.name();
		output("Started " + text + ", k = " + m_k);
		Timer timer = new Timer();
		m_clusters = clusteringAlg();
		output("Finished " + text + ", produced " + clusterNum() + " clusters. " + timer);
		return m_clusters;
	}

	/** Main function of clustering algorithm */
	protected abstract int[] clusteringAlg() throws Exception;

	/** save result clusters[] to file */
	public void saveClusters(String clusterCSV) throws Exception {
		if (m_clusters.length != m_dataSet.objNum()) {
			throw new Exception("ERROR!");
		}
		BufferedWriter bw = new BufferedWriter(new FileWriter(clusterCSV));
		for (int i = 0; i < m_dataSet.objNum(); i++) {
			Obj obj = m_dataSet.getObj(i);
			// bw.write(obj.name() + "," + obj.cls().getName() + "," +
			// m_clusters[i] + "," + obj.strValues() + "\n");
			bw.write(obj.name() + "," + m_clusters[i] + "\n");
		}
		bw.close();
	}

	@Override
	protected void mainProc() throws Exception {
		// 1. set parameters: dataSet, simMeasure, and k
		dataSet(m_inputArff);
		simMeasure(m_simName);
		k(m_k);
		// 2. run k-modes on data with CoupleSim
		clustering(m_dataSet, m_simMeasure, m_k);
		// 3. output results
		if (!Common.validString(m_outputFile)) {
			m_outputFile = m_inputArff + "." + m_simName + ".csv";
		}
		saveClusters(m_outputFile);
	}

	@Override
	public void setOptions(String[] options) throws Exception {
		// -i input_file
		m_inputArff = Common.getOption("i", options);
		// -i input_file: [input] the input categorical data file
		m_k = Common.getOptionInt("k", options, m_k);
		// -s sim_name: SMD, OFD, ADD, COS, INTRA, INTER
		m_simName = Common.getOptionString("s", options, m_simName);
		Common.Assert(m_simName != null);
		// -r
		m_randomize = Common.getOptionBool("r", options);
		// ¨Co output_file
		m_outputFile = Common.getOption("o", options);
		// debug, daemon, etc
		super.setOptions(options);
	}

	public String help() {
		return "Clusterer base class.\n\n";
	}

	public String version() {
		return "v1, Created on 14 June 2016, Allen Lin.\n";
	}
}