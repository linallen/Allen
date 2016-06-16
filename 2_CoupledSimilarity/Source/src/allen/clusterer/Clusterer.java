package allen.clusterer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.HashSet;

import allen.base.common.Common;
import allen.base.common.Timer;
import allen.base.module.AAI_Module;
import allen.sim.dataset.DataSet;
import allen.sim.dataset.Obj;
import allen.sim.measure.SimCouple;
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
	protected String m_inputFile;
	/** -r randomize: [parameter] randomize the algorithm, default NO */
	protected boolean m_randomize;
	/**
	 * -s sim_name: [parameter] similarity measure:<br>
	 * SMD, OFD, ADD, COS, INTRA, INTER
	 */
	protected String m_simName;
	/** -o output_file: [output] objects with top_k similar objects. */
	protected String m_outputFile;

	/** [input 1] similarity measure */
	protected SimMeasure m_simMeasure;
	/** [input 2] data set */
	protected DataSet m_data;
	/** [input 3] k - pre-defined cluster number */
	protected int m_k;
	/** [output] clusters[] */
	protected int[] m_clusters = new int[0];

	/** set similarity measure, data set, and k */
	public void setParams(SimMeasure simMeasure, DataSet data, int k) {
		m_simMeasure = simMeasure;
		if (m_simMeasure != null) {
			m_simMeasure.owner(this);
		}
		m_data = data;
		if (m_data != null) {
			m_data.owner(this);
		}
		m_k = ((k <= 0) ? data.clsNum() : k);
	}
	
	public void setDataSet(String dataFile){
		// 1. load categorical data
		DataSet data = new DataSet();
		data.owner(this);
		data.debug(this.debug());
		data.loadData(m_inputFile);
		outputDbg(data.dataSummary());

		// 2. create similarity measure
		SimMeasure simMeasure = SimMeasure.getSimMeasure(m_simName);
		simMeasure.owner(this);
		simMeasure.debug(this.debug());
		outputDbg("SIM_alg: " + Common.quote(simMeasure.name() + "/" + m_simName));

		// 3. run k-modes on data with CoupleSim
		m_k = (m_k == 0) ? data.clsNum() : m_k;
		clustering(simMeasure, data, m_k);
	}

	/** get similarity measure */
	public SimMeasure simMeasure() {
		return m_simMeasure;
	}

	/** get data set */
	public DataSet data() {
		return m_data;
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
	public int[] clustering(SimMeasure simMeasure, DataSet dataSet, int k) throws Exception {
		setParams(simMeasure, dataSet, k);
		return clustering();
	}

	public int[] clustering() throws Exception {
		output("Clusterer " + name() + " started on " + m_data.name() + " with k = " + m_k);
		Timer timer = new Timer();
		m_clusters = clusteringAlg();
		output("Clusterer " + name() + " finished on " + m_data.name() + " with " + clusterNum() + " clusters. "
				+ timer);
		return m_clusters;
	}

	/** Main function of clustering algorithm */
	protected abstract int[] clusteringAlg() throws Exception;

	public void saveClusters(String clusterCSV) throws Exception {
		if (m_clusters.length != m_data.objNum()) {
			throw new Exception("ERROR!");
		}
		BufferedWriter bw = new BufferedWriter(new FileWriter(clusterCSV));
		for (int i = 0; i < m_data.objNum(); i++) {
			Obj obj = m_data.getObj(i);
			bw.write(obj.name() + "," + obj.cls().getName() + "," + m_clusters[i] + "," + obj.strValues() + "\n");
		}
		bw.close();
	}

	@Override
	protected void mainProc() throws Exception {
		// 1. load categorical data
		DataSet data = new DataSet();
		data.owner(this);
		data.debug(this.debug());
		data.loadData(m_inputFile);
		outputDbg(data.dataSummary());

		// 2. create similarity measure
		SimMeasure simMeasure = SimMeasure.getSimMeasure(m_simName);
		simMeasure.owner(this);
		simMeasure.debug(this.debug());
		outputDbg("SIM_alg: " + Common.quote(simMeasure.name() + "/" + m_simName));

		// 3. run k-modes on data with CoupleSim
		m_k = (m_k == 0) ? data.clsNum() : m_k;
		clustering(simMeasure, data, m_k);

		// 4. output results
		if (!Common.validString(m_outputFile)) {
			m_outputFile = m_inputFile + "." + m_simName + ".csv";
		}
		saveClusters(m_outputFile);
	}

	@Override
	public void setOptions(String[] options) throws Exception {
		// -i input_file
		m_inputFile = Common.getOption("i", options);
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

	public static void main(String[] args) throws Exception {
		SimCouple module = new SimCouple();
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
		return "Clusterer base class.\n\n";
	}

	public static String version() {
		return "v1, Created on 14 June 2016, Allen Lin.\n";
	}
}