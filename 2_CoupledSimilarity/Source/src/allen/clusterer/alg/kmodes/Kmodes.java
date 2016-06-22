package allen.clusterer.kmodes;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import allen.base.common.Common;
import allen.base.common.Timer;
import allen.clusterer.Clusterer;
import allen.sim.dataset.DataSet;
import allen.sim.dataset.Obj;

/**
 * k-modes algorithm implementation.
 * 
 * 
 * @author Allen Lin, 18 Mar 2016
 */
public class Kmodes extends Clusterer {
	private static final long serialVersionUID = -6449584423416160388L;

	/** modes[] */
	private ArrayList<Mode> m_modes;

	/** mapping <obj, mode> recording which mode a object assigned to */
	private HashMap<Obj, Mode> m_mapObjMode = new HashMap<Obj, Mode>();

	/** recording the minimal similarity between objects to mode. */
	private HashMap<Obj, Double> m_mapObjSim = new HashMap<Obj, Double>();

	/** select k objs as the seed objs of the k modes */
	private static ArrayList<Obj> seedObjs(int k, DataSet data, boolean random) {
		ArrayList<Obj> AllObjs = new ArrayList<Obj>();
		for (int i = 0; i < data.objNum(); i++) {
			AllObjs.add(data.getObj(i));
		}
		if (random) {
			Collections.shuffle(AllObjs);
		}
		ArrayList<Obj> seedObjs = new ArrayList<Obj>();
		for (int i = 0; i < k; i++) {
			seedObjs.add(AllObjs.get(i));
		}
		return seedObjs;
	}

	/** create k initial modes */
	private void initModes(DataSet dataSet, int k, boolean random) throws Exception {
		// 1. select k objects as the initial k modes
		ArrayList<Obj> seedObjs = seedObjs(k, dataSet, random);
		// 2. transfer k objects to k modes
		m_modes = new ArrayList<Mode>();
		for (Obj seedObj : seedObjs) {
			Mode mode = new Mode();
			mode.addObj(seedObj);
			m_mapObjSim.put(seedObj, Double.MAX_VALUE);
			m_mapObjMode.put(seedObj, mode);
			m_modes.add(mode);
		}
		if (debug()) {
			outputDbg("initial modes:-----");
			for (Mode mode : m_modes) {
				output(mode.toString());
			}
			outputDbg("--------------------");
		}
	}

	/** select the most isolated object as a new mode */
	private void addFarObj(Mode modeNew, DataSet data) throws Exception {
		output("Adding isolated object to empty mode started ...");
		Timer timer = new Timer();
		Common.Assert(modeNew.size() == 0);
		double modeSimMin = Double.MAX_VALUE;
		Obj objMin = null;
		for (int i = 0; i < data.objNum(); i++) {
			Obj obj = data.getObj(i);
			Double modeSim = m_mapObjSim.get(obj);
			modeSim = (modeSim == null) ? 0 : modeSim;
			// if (obj.m_modeSim < modeSimMin) {
			if (modeSim < modeSimMin) {
				objMin = obj;
				// modeSimMin = obj.m_modeSim;
				modeSimMin = modeSim;
			}
		}
		Common.Assert(objMin != null);
		Mode modeOld = m_mapObjMode.get(objMin);
		if (modeOld != null) {
			modeOld.removeObj(objMin);
		}
		modeNew.addObj(objMin);
		// objMin.m_modeSim = Double.MAX_VALUE;
		m_mapObjSim.put(objMin, Double.MAX_VALUE);
		// move objMin from modeOrg to mode
		output("Adding isolated object to empty mode finished, " + timer);
	}

	/** Main Function: clustering data set with k-modes */
	@Override
	protected int[] clusteringAlg() throws Exception {
		// 1. initialize k modes
		initModes(m_dataSet, m_k, m_randomize);
		// 2. k-modes
		int roundNum = 0;
		for (int changeNum = 1; changeNum > 0; roundNum++) {
			changeNum = 0; // # of objs changed their modes in this round
			for (int i = 0; i < m_dataSet.objNum(); i++) {
				// progress(i + 1, data.objNum());
				Obj obj = m_dataSet.getObj(i);
				// 1. assign obj[i] to the closest mode[j]
				Mode closestMode = null;
				double simMax = -1;
				for (Mode mode : m_modes) {
					double sim = m_simMeasure.sim(obj, mode.getModeObj());
					// obj.m_simMin = Math.min(obj.m_simMin, sim);
					if (sim > simMax) {
						simMax = sim;
						closestMode = mode;
					}
				}
				// 2. move obj[i] to the closest mode from old mode
				Mode oldMode = m_mapObjMode.get(obj);
				if (oldMode != closestMode) {
					if (oldMode != null) {
						// remove object from old mode
						oldMode.removeObj(obj);
					}
					// add object to new mode
					closestMode.addObj(obj);
					Common.Assert(simMax >= 0);
					// obj.m_modeSim = simMax;
					m_mapObjSim.put(obj, simMax);
					m_mapObjMode.put(obj, closestMode);
					changeNum++;
					// if old mode is empty, move an object into it
					if ((oldMode != null) && oldMode.isEmpty()) {
						addFarObj(oldMode, m_dataSet);
					}
				}
			}
			outputDbg("KM round " + (roundNum + 1) + ": " + changeNum + " changes.");
		}
		// 3. transfer modes[] to clusters[]
		m_clusters = new int[m_dataSet.objNum()];
		for (int i = 0; i < m_dataSet.objNum(); i++) {
			Obj obj = m_dataSet.getObj(i);
			Mode mode = m_mapObjMode.get(obj);
			m_clusters[i] = m_modes.indexOf(mode);
		}
		return clusters();
	}

	/** save clusters to file: [obj_name, obj_class, mode_name, obj_values] */
	@Override
	public void saveClusters(String clusterCSV) throws Exception {
		BufferedWriter bw = new BufferedWriter(new FileWriter(clusterCSV));
		for (Mode mode : m_modes) {
			bw.write(mode.toCSV());
		}
		bw.close();
	}

	public String help() {
		return "k-modes algorithm implementation.";
	}

	public String version() {
		return "v1, Created on 18 Mar 2016, Allen Lin.\n"
				+ "v2, Fixed bug: empty cluster casued by moving out object. Solution: add the most isolated object to the empty cluster. 27 Mar 2016, Allen Lin.\n"
				+ "V2.1, Rewrite k-modes where a Mode is a FtrSet instead of an Obj, Allen, 17 June 2016.";
	}

	public static void main(String[] args) throws Exception {
		getModule(Thread.currentThread().getStackTrace()[1].getClassName()).Main(args);
	}
}