package allen.clusterer;

import java.util.HashSet;

import allen.base.common.AAI_IO;
import allen.base.common.Common;
import allen.base.common.Timer;
import allen.base.dataset.DataSet;
import allen.base.dataset.Obj;
import allen.base.module.AAI_Module;
import allen.sim.measure.SimMeasure;

/**
 * Base class of clustering algorithms.
 * 
 * <b>Syntax:</b><br>
 * Java -jar cluster_alg.jar -i input_arff -s sim_name [-k top_k] [-r] -o
 * output_csv <br>
 * <ul>
 * <li><i>-i input_arff</i>: [input] the ARFF data file.</li>
 * <li><i>-s sim_name</i>: [para] name of similarity measures: COS, COS_INTRA,
 * COS_INTER, CMS, CMS_INTRA, CMS_INTER, SMD, OFD, etc.</li>
 * <li><i>-k k</i>: [para] user-defined cluster number. Default -1 (# of
 * classes).</li>
 * <li><i>-c cls_idx</i>: [para] class index. Default -1 (the last feature).
 * </li>
 * <li><i>-o output_csv</i>: [output, debug] objects and modes.<br>
 * Format: [obj_name, obj_class, mode_name + values]</li>
 * </ul>
 * 
 * @author Allen Lin, 14 June 2016
 */
public abstract class Clusterer extends AAI_Module {
	private static final long serialVersionUID = -4489564528508002488L;

	/** -i input_arff: [input] the input categorical data file. */
	protected String m_dataArff;
	/** -k k: user-defined cluster number. Default -1 (# of classes). */
	protected int m_k = -1;
	/** -s sim_name: COS, CMS, SMD, etc */
	protected String m_simName;
	/** -c cls_idx: class index */
	protected int m_clsIdx = -1;

	/** -o output_file: [output] objects with top_k similar objects. */
	protected String m_outputCSV;

	/** [temp] data set */
	protected DataSet m_dataSet;
	/** [temp] similarity measure */
	protected SimMeasure m_simMeasure;
	/** [output] cluster ids[] produced by the clusterer */
	protected int[] m_clusters = new int[0];

	/** property functions ***************************************/
	/** set similarity measure, data set, and k */
	public void setParams(DataSet dataSet, SimMeasure simMeasure, int k) throws Exception {
		if ((m_simMeasure = simMeasure) != null) {
			m_simMeasure.owner(this);
			m_simMeasure.dataSet(dataSet);
		}
		dataSet(dataSet);
		if (dataSet() != null) {
			dataSet().owner(this);
		}
		m_k = ((k <= 0) ? dataSet.clsNum() : k);
	}

	/** set/load data set */
	public void dataSet(String dataFile) throws Exception {
		DataSet dataSet = new DataSet();
		dataSet.owner(this);
		dataSet.debug(this.debug());
		dataSet.loadArff(m_dataArff);
		dataSet(dataSet);
	}

	/** set/load data set */
	public void dataSet(DataSet dataSet) throws Exception {
		m_dataSet = dataSet;
		m_dataSet.owner(this);
		tempDir(m_dataSet.dataDir() + "temp/");
		AAI_IO.createDir(tempDir());
	}

	/** get data set */
	public DataSet dataSet() {
		return m_dataSet;
	}

	/** set similarity measure */
	public void simMeasure(String simName) throws Exception {
		// SimMeasure simMeasure = SimMeasure.getSimMeasure(simName);
		SimMeasure simMeasure = (SimMeasure) SimMeasure.getInstance(simName);
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
	public void setK(int k) {
		m_k = (m_k <= 0) ? m_dataSet.clsNum() : m_k;
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

	/** @return produced clusters[] */
	public int[] clusters() {
		return m_clusters;
	}

	/** @return object labels[] */
	public int[] labels() {
		return m_dataSet.getLabelIds();
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
	public int[] clustering(final DataSet dataSet, SimMeasure simMeasure, int k) throws Exception {
		setParams(dataSet, simMeasure, k);
		return clustering();
	}

	public int[] clustering() throws Exception {
		String text = moduleName() + " clustering on " + m_dataSet.dataName() + " with " + m_simMeasure.getUniqeName();
		output("Started " + text + ", k = " + m_k);
		Timer timer = new Timer();
		Common.Assert((m_dataSet != null) && (m_simMeasure != null));
		m_clusters = clusteringAlg(m_dataSet, m_simMeasure);
		if (debug()) {
			String debug = "Clusters[]: ";
			for (int cluster : m_clusters) {
				debug += cluster;
			}
			outputDbg(debug);
		}
		output("Finished " + text + ", produced " + clusterNum() + " clusters. " + timer);
		return m_clusters;
	}

	/** Main function of clustering algorithm */
	protected abstract int[] clusteringAlg(final DataSet dataSet, SimMeasure simMeasure) throws Exception;

	/** save [obj_name, obj_values, obj_label, obj_flag] */
	public void saveClusters(String clusterCSV) throws Exception {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < m_dataSet.objNum(); i++) {
			Obj obj = m_dataSet.getObj(i);
			buf.append(
					obj.name() + "," + obj.valuesStr() + "," + obj.getLabel().toString() + "," + m_clusters[i] + "\n");
		}
		AAI_IO.saveFile(clusterCSV, buf.toString());
	}

	@Override
	protected void mainProc() throws Exception {
		// 1. prepare objects
		// SimMeasure simMeasure =
		// SimMeasure.getSimMeasure(m_simName.toUpperCase());
		SimMeasure simMeasure = (SimMeasure) SimMeasure.getInstance(m_simName.toUpperCase());
		DataSet dataSet = new DataSet();
		dataSet.loadArff(m_dataArff);
		dataSet.setClass(m_clsIdx);
		simMeasure.dataSet(dataSet);
		if (debug()) {
			// simMeasure.saveSimObjs(m_dataArff + ".sim_objs.dbg.txt");
			// simMeasure.saveSimGraph(m_dataArff + ".sim_graph.dbg.txt");
			// simMeasure.saveSimMatrix(m_dataArff + ".sim_matrix.dbg.txt");
		}
		// 2. run clusterer on data set with similarity measure
		clustering(dataSet, simMeasure, m_k);
		// 3. output results
		if (Common.validString(m_outputCSV)) {
			saveClusters(m_outputCSV);
		}
	}

	@Override
	public void setOptions(String[] options) throws Exception {
		// -i input_arff
		m_dataArff = Common.getOption("i", options);
		if (!AAI_IO.fileExist(m_dataArff)) {
			throw new Exception("File not found. " + m_dataArff);
		}
		// -s sim_name
		m_simName = Common.getOptionString("s", options, m_simName);
		// -k k
		m_k = Common.getOptionInt("k", options, m_k);
		// -c cls_idx
		m_clsIdx = Common.getOptionInt("c", options, m_clsIdx);
		// -o output_file
		m_outputCSV = Common.getOption("o", options);
		// debug, daemon, etc
		super.setOptions(options);
	}

	public static String help() {
		return "[Clusterer]\n\n"
				+ "Java -jar clusterer_xxx.jar -i input_arff -s sim_name [-k top_k] [-r] -o output_csv\n"
				+ "-i input_arff: [input] the ARFF data file.\n"
				+ "-s sim_name: [para] name of similarity measures: COS, COS_INTRA, COS_INTER, CMS, CMS_INTRA, CMS_INTER, SMD, OFD, etc.\n"
				+ "-k k: [para] user-defined cluster number. Default -1 (# of classes).\n"
				+ "-c cls_idx: [para] class index. Default -1 (the last feature).\n"
				+ "-r: [para] randomized algorithm (e.g., for k-modes).\n"
				+ "-o output_csv: [output, debug] objects and modes.\n"
				+ "    Format: [obj_name, obj_class, mode_name + values]\n";
	}

	public static String version() {
		return "v1, Created on 14 June 2016, Allen Lin.";
	}

	public static void main(String[] args) throws Exception {
		exec(Thread.currentThread().getStackTrace()[1].getClassName(), args);
	}
}