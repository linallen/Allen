package allen.clusterer.alg.kmodes;

import java.util.HashSet;

import allen.base.dataset.Feature;
import allen.base.dataset.FtrSet;
import allen.base.dataset.FtrType;
import allen.base.dataset.Obj;
import allen.base.dataset.Value;

/**
 * The mode object in k-modes algorithm.
 * 
 * @author Allen Lin, 17 June 2016
 */
public class Mode_OLD extends FtrSet {
	private static final long serialVersionUID = 7372456540829556338L;

	/** the objs belonging to this mode (for DEBUG only) */
	private HashSet<Obj> m_objs = new HashSet<Obj>();

	/** the representative object of this mode */
	private Obj m_modeObj;

	/** property functions ***************************************/
	@Override
	public int size() {
		return m_objs.size();
	}

	/** @return objects[] in this mode */
	public HashSet<Obj> getObjs() {
		return m_objs;
	}

	/** manipulation functions ***************************************/
	/**
	 * @return the representative object (the Mode-Obj) of the mode, whose value
	 *         on a particular feature is the one with maximum counts.
	 */
	public Obj buildModeObj() {
		if (m_modeObj == null) {
			m_modeObj = new Obj();
			m_modeObj.name(name() + "_obj");
			for (Feature ftr : this.getFtrSet()) {
				if (ftr.type() == FtrType.CATEGORICAL) {
					// get the Mode-Value of this feature
					Value valueMax = null;
					int countMax = 0;
					for (Value value : ftr.values()) {
						int count = value.count();
						if (count > countMax) {
							valueMax = value;
							countMax = count;
						}
					}
					m_modeObj.setValue(ftr, valueMax);
				}
			}
		}
		return m_modeObj;
	}

	/** add an object to the mode (cluster) */
	public void addObj(Obj obj) throws Exception {
		m_objs.add(obj);
		// update value counts of the mode
		for (Value value : obj.getValues()) {
			Feature ftr = value.getFtr();
			if (ftr.type() == FtrType.CATEGORICAL) {
				Feature modeFtr = this.getFtr(ftr.name());
				Value modeValue = modeFtr.getValue(value.name());
				modeValue.countInc();
			}
		}
	}

	/** remove an object from the mode (cluster) */
	public void removeObj(Obj obj) throws Exception {
		m_objs.remove(obj);
		// update value counts of the mode
		for (Value value : obj.getValues()) {
			Feature ftr = value.getFtr();
			if (ftr.type() == FtrType.CATEGORICAL) {
				Feature modeFtr = this.getFtr(ftr.name());
				Value modeValue = modeFtr.getValue(value.name());
				modeValue.countDec();
			}
		}
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		// 1. objs[]
		sb.append(name() + " has " + size() + " objs: ");
		for (Obj obj : m_objs) {
			sb.append(obj.name() + " ");
		}
		// 2. value counts[]
		sb.append("\n");
		for (Feature ftr : this.getFtrSet()) {
			sb.append(ftr.getValueCounts()).append("\n");
		}
		return sb.toString();
	}

	/** [obj_name, obj_class, mode_name, mode_values] */
	public String toCSV() {
		StringBuffer sb = new StringBuffer();
		// for (Obj obj : m_objs)
		{
			// sb.append(obj.toCSV() + "," + name() + "," + obj.strValues() +
			// "\n");
		}
		return sb.toString();
	}
}