package allen.clusterer.alg.kmodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import allen.base.common.AAI_IO;
import allen.base.common.Common;
import allen.base.dataset.DataSet;
import allen.base.dataset.Obj;
import allen.clusterer.Clusterer;
import allen.sim.measure.SimMeasure;

/**
 * k-modes clustering algorithm.
 * 
 * @author Allen Lin, 18 Mar 2016
 */
public class Kmodes extends Clusterer {
	private static final long serialVersionUID = -6449584423416160388L;

	/** k modes[] */
	private ArrayList<Mode> m_modes;

	/** mapping [obj, mode]: mapping of object to its hosting mode */
	private HashMap<Obj, Mode> m_mapObjMode = new HashMap<Obj, Mode>();

	/** manipulation functions *****************************************/
	/** get starting/seed modes[k] from the obj_list[] */
	private ArrayList<Mode> getSeedModes(final ArrayList<Obj> objs, int k) throws Exception {
		Common.Assert((objs != null) && (k > 0) && objs.size() >= k);
		// shuffle objects[] and use the first k ones as the starting seeds[]
		ArrayList<Obj> seedObjs = new ArrayList<Obj>(objs);
		Collections.shuffle(seedObjs);
		// create seed modes from the seed objects[]
		ArrayList<Mode> modes = new ArrayList<Mode>();
		for (int i = 0; i < k; i++) {
			Obj obj = seedObjs.get(i);
			Mode mode = new Mode();
			mode.name("mode_" + (i + 1));
			mode.addObj(obj);
			m_mapObjMode.put(obj, mode);
			modes.add(mode);
		}
		return modes;
	}

	/** Main Function: clustering data set with k-modes */
	@Override
	protected int[] clusteringAlg(final DataSet dataSet, SimMeasure simMeasure) throws Exception {
		// 1. get starting/seed modes[k] from the obj_list[]
		m_modes = getSeedModes(dataSet.getObjs(), m_k);
		// 2. k-modes iterations
		for (int iterations = 0, changes = 1; changes > 0; iterations++) {
			outputDbg("Modes:-----\n" + getModesStr() + "--------------------");
			changes = 0; // # of objects changed hosting modes
			for (Obj obj : dataSet.getObjs()) {
				Mode hostMode = m_mapObjMode.get(obj);
				Common.Assert((hostMode == null) || (hostMode.size() > 0));
				// 0. if host mode has this object only, skip it
				if ((hostMode != null) && (hostMode.size() == 1)) {
					continue;
				}
				// 1. otherwise, choose the nearest mode for the object
				Mode nearestMode = null;
				double nearestSim = -1;
				for (Mode mode : m_modes) {
					double sim = simMeasure.sim(obj, mode.getObj());
					if (sim > nearestSim) {
						nearestSim = sim;
						nearestMode = mode;
					}
				}
				Common.Assert(nearestMode != null);
				// 2. move object into the nearest mode
				if (hostMode != nearestMode) {
					changes++;
					// 1. remove object from old mode
					if (hostMode != null) {
						hostMode.removeObj(obj);
					}
					// 2. add object to the new mode
					nearestMode.addObj(obj);
					m_mapObjMode.put(obj, nearestMode);
				}
			}
			outputDbg("iteration " + (iterations + 1) + ": " + changes + " changes.");
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

	/** output functions *************************************************/
	/** save clusters to file: [obj_name, obj_class, mode_name, obj_values] */
	@Override
	public void saveClusters(String clusterCSV) throws Exception {
		String buf = new String();
		for (Mode mode : m_modes) {
			buf += mode.toString() + "\n";
		}
		AAI_IO.saveFile(clusterCSV, buf);
	}

	public String getModesStr() {
		String buf = new String();
		for (Mode mode : m_modes) {
			buf += mode.toString() + "\n";
		}
		return buf;
	}

	public static String help() {
		return "K-modes algorithm.\n\n" + Clusterer.help();
	}

	public static String version() {
		return "v1, Created on 18 Mar 2016, Allen Lin.\n"
				+ "v2, Fixed bug: empty cluster casued by moving out object. Solution: add the most isolated object to the empty cluster. 27 Mar 2016, Allen Lin.\n"
				+ "V2.1, Rewrite k-modes where a Mode is a FtrSet instead of an Obj, Allen, 17 June 2016."
				+ "V2.2, Refine starting modes selection and remove empty-mode solution (not needed), Allen, 8 July 2016.";
	}

	public static void main(String[] args) throws Exception {
		exec(Thread.currentThread().getStackTrace()[1].getClassName(), args);
	}
}