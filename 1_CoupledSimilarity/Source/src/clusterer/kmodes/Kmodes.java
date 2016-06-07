package clusterer.kmodes;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import clusterer.Clusterer;
import pkgCommon.Common;
import pkgCommon.Timer;
import pkgDataSet.DataSet;
import pkgDataSet.Obj;
import pkgModule.AAI_Module;
import pkgSimMeasure.SimCouple;
import pkgSimMeasure.SimMeasure;

/**
 * k-modes algorithm implementation.
 * 
 * <b>Syntax:</b><br>
 * Java -jar kmodes.jar -i input_file [-k k] -s sim_name [-o output_file]<br>
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
 * @author Allen Lin, 18 Mar 2016
 */
public class Kmodes extends AAI_Module implements Clusterer {
	private static final long serialVersionUID = -6449584423416160388L;

	/** -i input_file: [input] the input categorical data file. */
	private String m_inputFile;

	/** -k k: [parameter] k-modes */
	private int m_k;

	/** -r random_seeds: [parameter] randomly select the initial k modes */
	private boolean m_randomSeeds;

	/**
	 * -s sim_name: [parameter] similarity measure:<br>
	 * SMD, OFD, ADD, COS, INTRA, INTER
	 */
	private String m_simName;

	/** -o output_file: [output] objects with top_k similar objects. */
	private String m_outputFile;

	/** modes[k] */
	private ArrayList<Mode> m_modes;

	/** mapping <obj, mode> recording which mode a object assigned to */
	private HashMap<Obj, Mode> m_mapObjMode = new HashMap<Obj, Mode>();

	/** recording the minimal similarity between objects to mode. */
	private HashMap<Obj, Double> m_mapObjSim = new HashMap<Obj, Double>();

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

	/** select k initial modes */
	private void initModes(int k, DataSet data, boolean random) throws Exception {
		Common.Assert(data.objNum() >= k);
		// 1. select k objects as the initial k modes
		ArrayList<Obj> seedObjs = seedObjs(k, data, random);
		Common.Assert(seedObjs.size() == k);
		// 2. transfer k objects to k modes
		m_modes = new ArrayList<Mode>();
		for (Obj seedObj : seedObjs) {
			Mode mode = new Mode(data, data.ftrNum());
			mode.objAddRemove(seedObj, true);
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

	private void initModes_old(int k, DataSet data) throws Exception {
		m_modes = new ArrayList<Mode>(k);
		Common.Assert(data.objNum() >= k);
		for (int i = 0; i < k; i++) {
			Mode mode = new Mode(data, data.ftrNum());
			Obj obj = data.getObj(i);
			// Obj obj = data.getObj(data.objNum() - 1 - i);
			mode.objAddRemove(obj, true);
			m_mapObjSim.put(obj, Double.MAX_VALUE);
			m_mapObjMode.put(obj, mode);
			m_modes.add(mode);
		}
		// debug
		// output("initial modes:");
		// for (Mode mode : m_modes) {
		// output(mode.toString());
		// }
		// output("");
		// debug
	}

	@Override
	public int[] clustering(SimMeasure simMeasure, DataSet data, int k) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	/** k-modes algorithm */
	public void kModes(int k, DataSet data, SimMeasure simMeasure, boolean random) throws Exception {
		// 1. select k seeds
		initModes(k, data, random);

		// 2. k-modes
		int roundNum = 0;
		for (int changeNum = 1; changeNum > 0; roundNum++) {
			changeNum = 0; // # of objects changed cluster in this round
			for (int i = 0; i < data.objNum(); i++) {
				// progress(i + 1, data.objNum());
				Obj obj = data.getObj(i);
				// 1. assign obj[i] to the closest mode[j]
				Mode closestMode = null;
				double simMax = -1;
				for (Mode mode : m_modes) {
					double sim = simMeasure.sim(obj, mode, data);
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
						oldMode.objAddRemove(obj, false);
					}
					// add object to new mode
					closestMode.objAddRemove(obj, true);
					Common.Assert(simMax >= 0);
					// obj.m_modeSim = simMax;
					m_mapObjSim.put(obj, simMax);
					m_mapObjMode.put(obj, closestMode);
					changeNum++;
					// if old mode is empty, move an object into it
					if ((oldMode != null) && oldMode.isEmpty()) {
						addFarObj(oldMode, data);
					}
				}
			}
			outputDbg("KM round " + (roundNum + 1) + ": " + changeNum + " changes.");
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
			modeOld.objAddRemove(objMin, false);
		}
		modeNew.objAddRemove(objMin, true);
		// objMin.m_modeSim = Double.MAX_VALUE;
		m_mapObjSim.put(objMin, Double.MAX_VALUE);
		// move objMin from modeOrg to mode
		output("Adding isolated object to empty mode finished, " + timer);
	}

	/** save clusters to file: [obj_name, obj_class, mode_name, obj_values] */
	public void saveClusters(String clusterCSV) throws Exception {
		BufferedWriter bw = new BufferedWriter(new FileWriter(clusterCSV));
		for (Mode mode : m_modes) {
			bw.write(mode.toCSV());
		}
		bw.close();
	}

	@Override
	protected void mainProc() throws Exception {
		// 1. load categorical data
		DataSet data = new DataSet();
		data.owner(this);
		// data.debug(this.debug());
		data.loadData(m_inputFile);

		// 2. create similarity measure
		SimMeasure simMeasure = SimMeasure.getSimMeasure(m_simName);
		if (simMeasure == null) {
			throw new Exception("similarity not found! " + m_simName);
		}
		output("SIM algorithm: " + Common.quote(simMeasure.getSimName() + "/" + m_simName));
		simMeasure.debug(true);
		simMeasure.setOptions(new String[] { "-s", m_simName });

		// 3. similarity pre-computation
		simMeasure.preComp(data);

		// 4. run k-modes on data with CoupleSim
		Kmodes kmodes = new Kmodes();
		kmodes.debug(true);
		m_k = (m_k == 0) ? data.clsNum() : m_k;
		kmodes.kModes(m_k, data, simMeasure, m_randomSeeds);

		// 5. output results
		if (!Common.validString(m_outputFile)) {
			m_outputFile = m_inputFile + "." + m_simName + ".csv";
		}
		kmodes.saveClusters(m_outputFile);
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
		m_randomSeeds = Common.getOptionBool("r", options);
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
		return "k-mode module.\n\n";
	}

	public static String version() {
		return "v1, Created on 18 Mar 2016, Allen Lin.\n"
				+ "v2, Fixed bug: empty cluster casued by moving out object. Solution: add the most isolated object to the empty cluster. 27 Mar 2016, Allen Lin.\n";
	}

	@Override
	public int numberOfClusters() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int[] clustering(String simGraphFile, int k) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}