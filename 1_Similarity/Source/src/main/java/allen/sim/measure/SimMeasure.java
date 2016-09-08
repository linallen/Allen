package allen.sim.measure;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import allen.base.common.AAI_IO;
import allen.base.common.Common;
import allen.base.common.Timer;
import allen.base.dataset.DataSet;
import allen.base.dataset.Feature;
import allen.base.dataset.Obj;
import allen.base.dataset.Value;
import allen.base.module.AAI_Module;

/**
 * Base class of similarity measures.
 * 
 * <b>Syntax:</b><br>
 * Java -jar sim_measure.jar -i input_arff -s sim_name [-k top_k] [-c cls_idx]
 * <br>
 * <ul>
 * <li><i>-i input_arff</i>: [input] the ARFF data file.</li>
 * <li><i>-s sim_name</i>: [para] name of similarity measures: COS, COS_INTRA,
 * COS_INTER, CMS, CMS_INTRA, CMS_INTER, SMD, OFD, etc.</li>
 * <li><i>-k top_k</i>: [para] top K similar objects. Default 100.</li>
 * <li><i>-c cls_idx</i>: [para] class index. Default -1 meaning the last
 * feature.</li>
 * </ul>
 * 
 * Output: sim_objs.txt, sim_graph.txt, and sim_matrix.txt
 * 
 * @author Allen Lin, 25 Mar 2016
 */
public class SimMeasure extends AAI_Module {
	private static final long serialVersionUID = 7217652874275084535L;

	/** define if the similarity measure is symmetric */
	private boolean m_symmetric = true;

	/** -i input_arff: [input] the input categorical data file. */
	private String m_dataArff;
	/** -s sim_name: COS, CMS, SMD, etc */
	private String m_simName;
	/** -k top_k: top K similar objects */
	private int m_topK = 100;
	/** -c cls_idx: class index */
	private int m_clsIdx = -1;

	/** [INPUT] input data set */
	private DataSet m_dataSet;
	/** [TEMP] features[] used for similarity computation */
	private ArrayList<Feature> m_ftrLSt;
	/** [TEMP] map [Value, Obj] */
	private HashMap<Value, HashSet<Obj>> m_mapValObjs;

	private String m_uniqueName = name();

	/** property functions ******************************/
	public void setUniqeName(String uniqueName) {
		m_uniqueName = uniqueName;
	}

	public String getUniqeName() {
		return m_uniqueName;
	}

	protected void symmetric(boolean symmetric) {
		m_symmetric = symmetric;
	}

	protected boolean symmetric() {
		return m_symmetric;
	}

	public void dataSet(DataSet dataSet) throws Exception {
		m_dataSet = dataSet;
		m_ftrLSt = dataSet.ftrSet().getFtrLst();
	}

	public DataSet dataSet() {
		return m_dataSet;
	}

	public void topK(int topK) {
		m_topK = topK;
	}

	public void clsIdx(int clsIdx) {
		m_clsIdx = (clsIdx == -1) ? (m_ftrLSt.size() - 1) : clsIdx;
	}

	public int topK() {
		return m_topK;
	}

	protected ArrayList<Feature> getFtrs() {
		return m_ftrLSt;
	}

	/**
	 * build inverted index [Value, Objs[]] to record which value belongs to
	 * which objs[].
	 */
	private void buildMapValObj() {
		m_mapValObjs = new HashMap<Value, HashSet<Obj>>();
		for (Obj obj : m_dataSet.getObjs()) {
			for (Value value : obj.getValues()) {
				if (m_mapValObjs.keySet().contains(value) == false) {
					m_mapValObjs.put(value, new HashSet<Obj>());
				}
				m_mapValObjs.get(value).add(obj);
			}
		}
	}

	/** @return objs[] that contain the specific value */
	protected final Collection<Obj> getOwnerObjs(Value value) {
		if (m_mapValObjs == null) {
			buildMapValObj();
		}
		return m_mapValObjs.get(value);
	}

	/**
	 * Note: override sim() and/or distance() functions to define your own
	 * similarity measure!
	 */
	/** @return sim(obj1, obj2). */
	public double sim(Obj obj1, Obj obj2) throws Exception {
		return 1. / (distance(obj1, obj2) + 1);
	}

	/** @return distance(obj1, obj2). */
	public double distance(Obj obj1, Obj obj2) throws Exception {
		double sim = sim(obj1, obj2);
		// return (sim < 1e-6) ? Double.MAX_VALUE : (1 / sim - 1);
		return (sim < 1e-6) ? Double.MAX_VALUE : (1 / sim);
	}

	/**
	 * calculate and output top-k similar objects. Output format:<br>
	 * label obj_name, label sim_obj1 score, ..., label sim_objk score
	 */
	public void saveSimObjs(String outputFile) throws Exception {
		output("Started saving top " + m_topK + " similar objects. " + outputFile);
		Timer timer = new Timer();
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(outputFile));
			int objNum = dataSet().objNum();
			for (int x = 0; x < objNum; x++) {
				progress(x + 1, objNum);
				Obj objX = dataSet().getObj(x);
				HashMap<Double, Obj> simObjs = new HashMap<Double, Obj>();
				// 1. calculate Sim(objX, obj*)
				for (int y = 0; y < objNum; y++) {
					if (y != x) {
						Obj objY = dataSet().getObj(y);
						double sim = sim(objX, objY);
						if (sim > 0) {
							simObjs.put(sim, objY);
						}
					}
				}
				// 2. sort sim(x, *) by simScore
				ArrayList<Double> simScores = new ArrayList<Double>(simObjs.keySet());
				Collections.sort(simScores);
				// 2. output sim(x, *)
				// label[ obj_name cls_size], label[ sim_obj1 score], ...
				// bw.write(objX.fullName() + " " + objX.cls().size() + ", ");
				bw.write(objX.name() + ": ");
				int simNum = Math.min(m_topK, simObjs.size());
				for (int i = 0; i < simNum; i++) {
					Double simScore = simScores.get(i);
					Obj simObj = simObjs.get(simScore);
					bw.write(simObj.name() + " ");
					bw.write(String.format("%.4f", simScore));
					bw.write((i < (simNum - 1)) ? ", " : "\n");
				}
			}
		} finally {
			AAI_IO.close(bw);
			output("Finished saving top " + m_topK + " similar objects. " + outputFile + ". " + timer);
		}
	}

	/** save similarity matrix to file */
	public void saveSimMatrix(String simMatrixFile) throws Exception {
		output("Started saving similarity matrix to " + simMatrixFile);
		Timer timer = new Timer();
		BufferedWriter bw = new BufferedWriter(new FileWriter(simMatrixFile));
		bw.write(dataSet().objNum() + "\n");
		int objNum = dataSet().objNum();
		for (int i = 0; i < objNum; i++) {
			progress(i + 1, objNum);
			for (int j = i + 1; j < objNum; j++) {
				Obj obj1 = dataSet().getObj(i);
				Obj obj2 = dataSet().getObj(j);
				double sim = sim(obj1, obj2);
				if (sim > 0) {
					String simStr = Common.decimal(sim, 4);
					bw.write(i + "," + j + "," + simStr + "\n");
				}
			}
		}
		bw.close();
		output("Finished saving similarity matrix to " + simMatrixFile + ". " + timer);
	}

	/** save similarity graph (adjacent similarity matrix) to file */
	public void saveSimGraph(String simGraphFile) throws Exception {
		output("Started saving similarity graph to " + simGraphFile);
		Timer timer = new Timer();
		BufferedWriter bw = new BufferedWriter(new FileWriter(simGraphFile));
		bw.write("SimGraph=[\n");
		int objNum = dataSet().objNum();
		for (int i = 0; i < objNum; i++) {
			progress(i + 1, objNum);
			for (int j = 0; j < objNum; j++) {
				Obj obj1 = dataSet().getObj(i);
				Obj obj2 = dataSet().getObj(j);
				double sim = sim(obj1, obj2);
				String simStr = Common.decimal(sim, 4);
				bw.write(((j == 0) ? "" : ",") + simStr);
			}
			bw.write(";\n");
		}
		bw.write("];\n");
		bw.close();
		output("Finished saving similarity graph to " + simGraphFile + ". " + timer);
	}

	@Override
	protected void mainProc() throws Exception {
		// 0. register pre-defined similarity measures
		SimRegister.register();
		// 1. get similarity measure object
		SimMeasure simMeasure = (m_simName != null) ? (SimMeasure) SimMeasure.getInstance(m_simName) : this;
		m_dataSet = new DataSet();
		// 2. load source data set and set class attribute
		m_dataSet.loadArff(m_dataArff);
		m_dataSet.setClass(m_clsIdx);
		// 3. set similarity input and parameter k
		simMeasure.dataSet(m_dataSet);
		simMeasure.topK(m_topK);
		simMeasure.saveSimObjs(m_dataArff + ".sim_objs.txt");
		simMeasure.saveSimGraph(m_dataArff + ".sim_graph.txt");
		simMeasure.saveSimMatrix(m_dataArff + ".sim_matrix.txt");
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
		// -k top_k
		m_topK = Common.getOptionInt("k", options, m_topK);
		if (m_topK <= 0) {
			throw new Exception("top K must > 0. " + m_topK);
		}
		// -c cls_idx
		m_clsIdx = Common.getOptionInt("c", options, m_clsIdx);
		// debug, daemon, etc
		super.setOptions(options);
	}

	public static String help() {
		return "[A similarity measure.]\n\n"
				+ "Java -jar sim_measure.jar -i input_arff -s sim_name [-k top_k] [-c cls_idx]\n"
				+ "-i input_arff: [input] the ARFF data file.\n"
				+ "-s sim_name: [para] name of similarity measures: COS, COS_INTRA, COS_INTER, CMS, CMS_INTRA, CMS_INTER, SMD, OFD, etc.\n"
				+ "-k top_k: [para] top K similar objects. Default 100.\n"
				+ "-c cls_idx: [para] class index. Default -1 meaning the last feature.\n"
				+ "\nOutput: sim_objs.txt, sim_graph.txt, and sim_matrix.txt.";
	}

	public static String version() {
		return "v0.0.1, 19 June 2016, Allen Lin.";
	}

	public static void main(String[] args) throws Exception {
		exec(Thread.currentThread().getStackTrace()[1].getClassName(), args);
	}
}