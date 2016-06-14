package sim.measure.coupled;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashSet;

import sim.dataset.DataSet;
import sim.dataset.Feature;
import sim.dataset.Obj;
import sim.dataset.Value;
import sim.datastructure.SimObjs;
import sim.datastructure.ValueSim;
import sim.measure.SimMeasure;

import common.*;
import module.AAI_Module;

/**
 * COS (Coupled Object Similarity) and CMS (Coupled Metric Similarity)
 * algorithms for measuring object-object similarities based on value-value
 * similarities. COS is proposed in TNNLS-2013 paper:
 * "Coupled Attribute Similarity Learning on Categorical Data" authored by Can
 * Wang et al. And CMS is proposed in
 * "Coupled Metric Similarity Learning for Non-IID Categorical Data" authored by
 * Songlei Jian et al.
 * 
 * <b>Syntax:</b><br>
 * Java -jar couplesim.jar -i input_file [-k top_k] [-s sim_name] [-o
 * output_file]<br>
 * <ul>
 * <li><i>-i input_file</i>: [input] the feature table (CSV with no title) with
 * the last column containing labels and others being features (attributes)</li>
 * <li><i>-k top_k</i>: [para] top K similar objects. Default 100.</li>
 * <li><i>-s sim_name</i>: [para] name of similarity measures: COS, COS_INTRA,
 * COS_INTER, CMS, CMS_INTRA, CMS_INTER. Default COS.</li>
 * <li><i>-o output_file</i>: [output] objects and similar objects.<br>
 * Format: label[ obj_name cls_size], label[ sim_obj1 score], ...<br>
 * where cls_size is the objects having same label with the target.</li>
 * </ul>
 * 
 * @author Allen Lin, 20 Jan 2016
 */
public class SimCouple extends AAI_Module implements SimMeasure {
	private static final long serialVersionUID = -1189132738106530336L;

	/** -i input_file: [input] the input categorical data file. */
	private String m_inputFile;
	/** -k top_k: top K similar objects */
	private int m_topK = 100;
	/** -t sim_type: COS, INTRA, INTER */
	private String m_simName = "COS";
	/** -o output_file: [output] objects with top_k similar objects. */
	private String m_outputFile;

	/** [INPUT] input data set */
	private DataSet m_inputData = new DataSet();

	/** [OUTPUT] similarity values[] between values (of same feature) */
	private ValueSim m_valSim = new ValueSim();

	public void dataSet(DataSet dataSet) {
		m_inputData = dataSet;
	}

	public void setSimName(String simName) {
		m_simName = simName;
	}

	/** calculate COS intra-coupled similarity of Value 1 and Value 2 */
	private static double intraAttrSimCOS(Value val1, Value val2) throws Exception {
		int objNum1 = val1.getObjNum();
		int objNum2 = val2.getObjNum();
		double product = 1. * objNum1 * objNum2;
		return product / (objNum1 + objNum2 + product);
	}

	/** calculate COS inter-coupled similarity of Value 1 and Value 2 */
	private static double interAttrSimCOS(Value val1, Value val2, ArrayList<Feature> ftrLst) throws Exception {
		double wt = 1. / (ftrLst.size() - 1);
		double interObjSim = 0;
		for (int k = 0; k < ftrLst.size(); k++) {
			// if (ftrLst.get(k).getName() != val1.getFtrName()) {
			if (!ftrLst.get(k).getName().equals(val1.getFtrName())) {
				double interAttrSim = interAttrK_COS(val1, val2, k);
				interObjSim += wt * interAttrSim;
			}
		}
		return interObjSim;
	}

	////////////////////////////////////////////////////////////////////////////////
	// CMS intra- and inter-coupling similarity computation
	// Added by Allen on 26 Apr 2016
	/** calculate CMS intra-coupled similarity of Value 1 and Value 2 */
	private static double intraAttrSimCMS(Value val1, Value val2) throws Exception {
		if (val1 == val2) {
			return 1;
		}
		int p = val1.getObjNum() + 1;
		int q = val2.getObjNum() + 1;
		double logP = Math.log(p);
		double logQ = Math.log(q);
		double intraSim = (logP * logQ) / (logP + logQ + logP * logQ);
		return intraSim;
	}

	/** calculate CMS inter-coupled similarity of Value 1 and Value 2 */
	private static double interAttrSimCMS(Value val1, Value val2, ArrayList<Feature> ftrLst) throws Exception {
		// double wt = 1. / (ftrLst.size() - 1);
		double interObjSim = 0;
		for (int k = 0; k < ftrLst.size(); k++) {
			// if (ftrLst.get(k).getName() != val1.getFtrName()) {
			if (!ftrLst.get(k).getName().equals(val1.getFtrName())) {
				double interAttrSim = interAttrK_CMS(val1, val2, k);
				interObjSim += interAttrSim;
			}
		}
		return interObjSim / (ftrLst.size() - 1);
	}

	private static double interAttrSimCMS_old(Value val1, Value val2, ArrayList<Feature> ftrLst) throws Exception {
		double wt = 1. / (ftrLst.size() - 1);
		double interObjSim = 0;
		for (int k = 0; k < ftrLst.size(); k++) {
			// if (ftrLst.get(k).getName() != val1.getFtrName()) {
			if (!ftrLst.get(k).getName().equals(val1.getFtrName())) {
				double interAttrSim = interAttrK_CMS(val1, val2, k);
				interObjSim += wt * interAttrSim;
			}
		}
		return interObjSim;
	}

	/**
	 * calculate the Inter-attribute Similarity of Attribute Values w.r.t.
	 * Another Attribute.<br>
	 * See EQ(7) in the CMS paper - "Coupled Metric Similarity"
	 */
	private static double interAttrK_CMS(Value val1, Value val2, int k) throws Exception {
		if (Value.isMissing(val1) || Value.isMissing(val2)) {
			return 0;
		}
		if (val1 == val2) {
			return 1;
		}
		// 1. get the intersection of values 1 and 2 on feature[k]
		HashSet<Value> interK = IIF(val1, k);
		interK.retainAll(IIF(val2, k));
		// 2. calculate minimal over the intersection I
		double maxSum = 0, minSum = 0;
		for (Value valueK : interK) {
			if (!Value.isMissing(valueK)) {
				double icp1 = ICP(valueK.getObjs(), val1.getObjs());
				double icp2 = ICP(valueK.getObjs(), val2.getObjs());
				maxSum += Math.max(icp1, icp2);
				minSum += Math.min(icp1, icp2);
			}
		}
		return (maxSum == 0) ? 0 : maxSum / (2 * maxSum - minSum);
	}
	////////////////////////////////////////////////////////////////////////////////

	/**
	 * calculate the IRSI similarity between values A and B (of the same
	 * feature) based on Feature[k]. EQ(5.8)
	 */
	private static double interAttrK_COS(Value val1, Value val2, int k) throws Exception {
		if (Value.isMissing(val1) || Value.isMissing(val2)) {
			return 0;
		}
		// 1. get the intersection of values 1 and 2 on feature[k]
		HashSet<Value> interK = IIF(val1, k);
		interK.retainAll(IIF(val2, k));
		// 2. calculate minimal over the intersection I
		double delta = 0;
		for (Value valueK : interK) {
			if (!Value.isMissing(valueK)) {
				double icp1 = ICP(valueK.getObjs(), val1.getObjs());
				double icp2 = ICP(valueK.getObjs(), val2.getObjs());
				delta += Math.min(icp1, icp2);
			}
		}
		return delta;
	}

	/**
	 * For a given a value srcVal, get the objects containing it, and then
	 * return the values dstValues that the objects have on Feature[k]. EQ(3.3)
	 */
	private static HashSet<Value> IIF(Value srcVal, int k) {
		// 1. get objects containing the given feature value
		HashSet<Obj> objs = srcVal.getObjs();
		// 2. get the values on feature[k] of the objects
		HashSet<Value> dstValues = new HashSet<Value>();
		for (Obj obj : objs) {
			dstValues.add(obj.getValue(k));
		}
		return dstValues;
	}

	/** calculate ICP(set1, set2) = |set1 n set2|/|set2|. EQ(3.4) */
	private static double ICP(HashSet<Obj> set1, HashSet<Obj> set2) {
		HashSet<Obj> inter = new HashSet<Obj>(set1);
		inter.retainAll(set2);
		return 1. * inter.size() / set2.size();
	}

	/** pre-computing value-value similarities */
	@Override
	public void preComp(DataSet data) throws Exception {
		output("Calculating value-value similarities started. " + m_simName);
		Timer timer = new Timer();
		String simName = m_simName.toUpperCase();
		m_valSim.clear();
		m_inputData = data;
		int ftrNum = data.ftrNum();
		for (int ftrIdx = 0; ftrIdx < ftrNum; ftrIdx++) {
			progress(ftrIdx + 1, ftrNum);
			ArrayList<Value> ftrValues = data.ftr(ftrIdx).valLst();
			for (int i = 0; i < ftrValues.size(); i++) {
				Value val1 = ftrValues.get(i);
				if (Value.isMissing(val1)) { // missing value
					continue;
				}
				// for (int b = a + 1; b < ftrValues.size(); b++) {
				for (int j = i; j < ftrValues.size(); j++) {
					Value val2 = ftrValues.get(j);
					if (Value.isMissing(val2)) { // missing value
						continue;
					}
					double valSim = 0;
					if (simName.contains("COS")) {
						// COS algorithm
						double intraSim = intraAttrSimCOS(val1, val2);
						double interSim = interAttrSimCOS(val1, val2, data.ftrLst());
						if (simName.equals("COS")) {
							valSim = intraSim * interSim;
						} else if (simName.contains("INTRA")) {
							valSim = intraSim;
						} else if (simName.contains("INTER")) {
							valSim = interSim;
						}
					} else if (simName.contains("CMS")) {
						// CMS algorithm, Added by Allen on 26/04/2016
						double intraSim = intraAttrSimCMS(val1, val2);
						double interSim = interAttrSimCMS(val1, val2, data.ftrLst());
						if (simName.equals("CMS")) {
							valSim = intraSim / 2 + interSim / 2;
						} else if (simName.contains("INTRA")) {
							valSim = intraSim;
						} else if (simName.contains("INTER")) {
							valSim = interSim;
						}
					} else {
						throw new Exception("Undefined similarity measure!");
					}
					m_valSim.addSim(val1, val2, valSim);
					m_valSim.addSim(val2, val1, valSim);
				}
			}
		}
		output("Calculating value-value similarities finished. " + timer);
	}

	/**
	 * calculate obj-obj similarity scores and output objects and their similar
	 * objects to file. Output format:<br>
	 * label obj_name, label sim_obj1 score, ..., label sim_objk score
	 */
	public void calcTopSim(DataSet inputData, int topK, String outputFile) throws Exception {
		output("Calculating obj-obj similarities started. " + outputFile);
		Timer timer = new Timer();
		if (!Common.validString(outputFile)) {
			outputFile = m_inputFile + ".sim_obj.txt";
		}
		BufferedWriter bw = null;
		try {
			// 1. make sure value-value similarities have been pre-computed
			if (m_valSim.isEmpty()) { // value-value similarities exist?
				if (inputData.isEmpty()) { // input data exists?
					throw new Exception("Input data set is empty.");
				}
				// pre-compute value-value similarities
				preComp(inputData);
			}
			// 2. compute top k similar objects to object[i], i = 0, ..., n-1
			bw = new BufferedWriter(new FileWriter(outputFile));
			int objNum = inputData.objNum();
			for (int x = 0; x < objNum; x++) {
				progress(x + 1, objNum);
				Obj objX = inputData.getObj(x);
				SimObjs simObjs = new SimObjs();
				// 1. calculate sim(x, *)
				for (int y = 0; y < objNum; y++) {
					if (y == x) {
						continue;
					}
					Obj objY = inputData.getObj(y);
					///////////////////////////////////
					double simScoreOld = 0;
					int ftrNum = inputData.ftrNum();
					for (int j = 0; j < ftrNum; j++) {
						Value valX = objX.getValue(j);
						Value valY = objY.getValue(j);
						if (m_simName.contains("COS")) {
							simScoreOld += m_valSim.getSim(valX, valY);
						} else if (m_simName.contains("CMS")) {
							simScoreOld += m_valSim.getSim(valX, valY) / ftrNum;
						}
					}
					double simScore = sim(objX, objY, m_inputData);
					// TODO debug
					if (simScore != simScoreOld) {
						throw new Exception("Debug Error!!!!!");
					}
					// debug
					///////////////////////////////////
					if (simScore > 0) {
						simObjs.add(objY, simScore);
					}
				}
				simObjs.sort();
				// 2. output sim(x, *)
				// label[ obj_name cls_size], label[ sim_obj1 score], ...
				bw.write(objX.fullName() + " " + objX.cls().size() + ", ");
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
			output("Calculating obj-obj similarities finished. " + timer);
		}
	}

	/**
	 * calculate obj-obj similarity score. <br>
	 * Added by Allen on 23/03/2016 for incorporating with k-modes
	 */
	@Override
	public double sim(Obj objX, Obj objY, DataSet data) throws Exception {
		// pre-computed?
		if (m_valSim.isEmpty()) {
			preComp(data);
		}
		double simScore = 0;
		int ftrNum = m_inputData.ftrNum();
		for (int j = 0; j < ftrNum; j++) {
			Value valX = ((Obj) objX).getValue(j);
			Value valY = ((Obj) objY).getValue(j);
			if (m_simName.contains("COS")) {
				simScore += m_valSim.getSim(valX, valY);
			} else if (m_simName.contains("CMS")) {
				simScore += m_valSim.getSim(valX, valY) / ftrNum;
			} else {
				throw new Exception("Undefined similarity measure!");
			}
		}
		// output("sim(" + objX + "," + objY + ") = " + simScore + "\n");
		return simScore;
	}

	@Override
	protected void mainProc() throws Exception {
		output(version());
		// 1. load categorical data
		m_inputData.loadData(m_inputFile);
		// 2. pre-compute value-value similarities
		preComp(m_inputData);
		// 3. calculate coupled similarity between Objects
		// (or find out top K similar objects to object[i], i = 0 , ..., n-1)
		// and output to file
		calcTopSim(m_inputData, m_topK, m_outputFile);
	}

	@Override
	public void setOptions(String[] options) throws Exception {
		// -i input_file
		m_inputFile = Common.getOption("i", options);
		// -k top_k
		m_topK = Common.getOptionInt("k", options, m_topK);
		// -s sim_name
		m_simName = Common.getOptionString("s", options, m_simName);
		m_simName = m_simName.toUpperCase();
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
		return "COS (Coupled Object Similarity) algorithm for measuring object-object similarities based on value-value similarities.\n"
				+ "COS is proposed in TNNLS-2013 paper: \"Coupled Attribute Similarity Learning on Categorical Data\" authored by Can Wang et al.\n\n"
				+ "Syntax: Java -jar couplesim.jar -i input_file [-k top_k] [-s sim_name] [-o output_file]\n"
				+ "-i input_file</i>: [input] the feature table (CSV without title) with the last column containing labels and others features (attributes)\n"
				+ "-k top_k</i>: [para] top K similar objects. Default 100.\n"
				+ "-s sim_name</i>: [para] name of similarity measures: COS, INTRA, INTER. Default COS.\n"
				+ "-o output_file</i>: [output] objects and similar objects.\n"
				+ "    Format: label[ obj_name cls_size], label[ sim_obj1 score], ...\n"
				+ "            where cls_size is the objects having same label with the target.";
	}

	public static String version() {
		return "v1, No buffer version. Created on 20 Jan 2016, Allen Lin.\n"
				+ "v2, Buffered version. 3 Feb 2016, Allen Lin.\n"
				+ "v2.1, draw obj-obj similarity matrix in Matlab. 5 Feb 2016, Allen Lin.\n"
				+ "v2.2, added \"-t sim_type\" to support intra- and inter- similarity scores. 25 Feb 2016, Allen Lin.\n"
				+ "v2.3, extracted DataSet class from CoupleSim class, so that the DataSet class can be shared among similarity measures. 24 Mar 2016, Allen Lin";
	}
}