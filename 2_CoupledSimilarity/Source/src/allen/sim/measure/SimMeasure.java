package allen.sim.measure;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import allen.base.common.AAI_IO;
import allen.base.common.Common;
import allen.base.common.Timer;
import allen.base.module.AAI_Module;
import allen.sim.dataset.DataSet;
import allen.sim.dataset.Feature;
import allen.sim.dataset.Obj;
import allen.sim.dataset.Value;
import allen.sim.datastructure.SimObjs;
import allen.sim.measure.coupling.SimCoupleCms;
import allen.sim.measure.coupling.SimCoupleCmsInter;
import allen.sim.measure.coupling.SimCoupleCmsIntra;
import allen.sim.measure.coupling.SimCoupleCos;
import allen.sim.measure.coupling.SimCoupleCosInter;
import allen.sim.measure.coupling.SimCoupleCosIntra;

/**
 * Base class of similarity measures.
 * 
 * <b>Syntax:</b><br>
 * Java -jar sim_measure.jar -i input_file -s sim_name [-k top_k] [-o
 * output_file]<br>
 * <ul>
 * <li><i>-i input_file</i>: [input] the feature table (CSV with no title) with
 * the last column containing labels and others being features (attributes)</li>
 * <li><i>-s sim_name</i>: [para] name of similarity measures: COS, COS_INTRA,
 * COS_INTER, CMS, CMS_INTRA, CMS_INTER.</li>
 * <li><i>-k top_k</i>: [para] top K similar objects. Default 100.</li>
 * <li><i>-o output_file</i>: [output] objects and similar objects.<br>
 * Format: label[ obj_name cls_size], label[ sim_obj1 score], ...<br>
 * where cls_size is the objects having same label with the target.</li>
 * </ul>
 * 
 * @author Allen Lin, 25 Mar 2016
 */
public class SimMeasure extends AAI_Module {
	private static final long serialVersionUID = 7217652874275084535L;

	/** -i input_file: [input] the input categorical data file. */
	private String m_dataFile;
	/** -k top_k: top K similar objects */
	private int m_topK = 100;
	/** -s sim_name: COS, INTRA, INTER */
	private String m_simName;
	/** -o output_file: [output] objects with top_k similar objects. */
	private String m_outputFile;

	/** [INPUT] input data set */
	private DataSet m_dataSet;

	/** [TEMP] features[] used for similarity computation */
	private ArrayList<Feature> m_ftrLSt;

	/** [TEMP] map [Value, Obj] */
	private HashMap<Value, HashSet<Obj>> m_mapValObjs;

	/** property functions ***************************************/
	public void dataSet(DataSet dataSet) {
		m_dataSet = dataSet;
		m_ftrLSt = dataSet.ftrSet().ftrLst();
	}

	public DataSet dataSet() {
		return m_dataSet;
	}

	protected ArrayList<Feature> getFtrs() {
		return m_ftrLSt;
	}

	/** set similarity measure name (ALL UPPER-CASE) */
	@Override
	public void name(String simName) {
		super.name(simName.toUpperCase().intern());
	}

	/**
	 * TODO build inverted index mapping [Value, Objs[]] indicating which value
	 * belong to which objs[].
	 */
	private void buildMapValObj() {
		m_mapValObjs = new HashMap<Value, HashSet<Obj>>();
		for (Obj obj : m_dataSet.getObjs()) {
			for (Value value : obj.values()) {
				if (m_mapValObjs.keySet().contains(value) == false) {
					m_mapValObjs.put(value, new HashSet<Obj>());
				}
				m_mapValObjs.get(value).add(obj);
			}
		}
	}

	/** @return objs[] that contain the specific value */
	protected Collection<Obj> getOwnerObjs(Value value) {
		if (m_mapValObjs == null) {
			buildMapValObj();
		}
		return m_mapValObjs.get(value);
	}

	/**
	 * Note: override sim() and/or distance functions to define your own
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

	/** similarity measures: COS, CMS, (_INTRA, _INTER), SMD, OFD */
	public static SimMeasure getSimMeasure(String simName) throws Exception {
		switch (simName.toUpperCase().trim()) {
		case "COS":
			return new SimCoupleCos();
		case "COS_INTRA":
			return new SimCoupleCosIntra();
		case "COS_INTER":
			return new SimCoupleCosInter();
		case "CMS":
			return new SimCoupleCms();
		case "CMS_INTRA":
			return new SimCoupleCmsIntra();
		case "CMS_INTER":
			return new SimCoupleCmsInter();
		case "SMD":
			return new SimSMD();
		case "OFD":
			return new SimOFD();
		case "ADD":
			// TODO
		}
		throw new Exception("Similarity measure not supported: " + simName);
	}

	/**
	 * calculate & output top-k similar objects. Output format:<br>
	 * label obj_name, label sim_obj1 score, ..., label sim_objk score
	 */
	public void calcTopSimObjs(DataSet dataSet, int topK, String outputFile) throws Exception {
		output("Started calculating top " + topK + " similar objects. " + outputFile);
		Timer timer = new Timer();
		BufferedWriter bw = null;
		try {
			// 1. compute top k similar objects to object[i], i = 0, ..., n-1
			bw = new BufferedWriter(new FileWriter(outputFile));
			int objNum = dataSet.objNum();
			for (int x = 0; x < objNum; x++) {
				progress(x + 1, objNum);
				Obj objX = dataSet.getObj(x);
				SimObjs simObjs = new SimObjs();
				// 1. calculate sim(x, *)
				for (int y = 0; y < objNum; y++) {
					if (y == x) {
						continue;
					}
					Obj objY = dataSet.getObj(y);
					double sim = sim(objX, objY);
					if (sim > 0) {
						simObjs.add(objY, sim);
					}
				}
				simObjs.sort();
				// 2. output sim(x, *)
				// label[ obj_name cls_size], label[ sim_obj1 score], ...
				// bw.write(objX.fullName() + " " + objX.cls().size() + ", ");
				bw.write(objX.fullName() + ", ");
				simObjs.sort();
				int simNum = Math.min(topK, simObjs.size());
				for (int i = 0; i < simNum; i++) {
					bw.write(simObjs.getSimObj(i).fullName() + " ");
					bw.write(String.format("%.4f", simObjs.getSimScore(i)));
					bw.write((i < (simNum - 1)) ? ", " : "\n");
				}
			}
		} finally {
			AAI_IO.close(bw);
			output("Finished calculating top " + topK + " similar objects. " + outputFile + ". " + timer);
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
				double sim = sim(dataSet().getObj(i), dataSet().getObj(j));
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
				double sim = sim(dataSet().getObj(i), dataSet().getObj(j));
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
		// 1. load categorical data
		m_dataSet = new DataSet();
		m_dataSet.loadArff(m_dataFile);
		// 2. calculate coupled similarity between Objects
		// (or find out top K similar objects to object[i], i = 0 , ..., n-1)
		// and output to file
		calcTopSimObjs(m_dataSet, m_topK, m_outputFile);
	}

	@Override
	public void setOptions(String[] options) throws Exception {
		// -i input_file
		m_dataFile = Common.getOption("i", options);
		// -k top_k
		m_topK = Common.getOptionInt("k", options, m_topK);
		// -s sim_name
		m_simName = Common.getOptionString("s", options, m_simName);
		m_simName = m_simName.toUpperCase();
		// �Co output_file
		m_outputFile = Common.getOption("o", options);
		// debug, daemon, etc
		super.setOptions(options);
	}

	@Override
	public String help() {
		return "Java -jar sim_measure.jar -i input_file -s sim_name [-k top_k] [-o output_file]\n"
				+ "-i input_file</i>: [input] the feature table (CSV without title) with the last column containing labels and others features (attributes)\n"
				+ "-s sim_name</i>: [para] name of similarity measures: COS, COS_INTRA, COS_INTER, CMS, CMS_INTRA, CMS_INTER, SMD, OFD.\n"
				+ "-k top_k</i>: [para] top K similar objects. Default 100.\n"
				+ "-o output_file</i>: [output] objects and similar objects.\n"
				+ "    Format: label[ obj_name cls_size], label[ sim_obj1 score], ...\n"
				+ "            where cls_size is the objects having same label with the target.";
	}

	@Override
	public String version() {
		return "v0.0.1, 19 June 2016, Allen Lin.";
	}

	public static void main(String[] args) throws Exception {
		getModule(Thread.currentThread().getStackTrace()[1].getClassName()).Main(args);
	}
}