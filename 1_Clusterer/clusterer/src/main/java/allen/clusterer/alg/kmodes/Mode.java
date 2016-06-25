package allen.clusterer.alg.kmodes;

import java.util.HashMap;
import java.util.HashSet;

import allen.base.common.Common;
import allen.base.dataset.Feature;
import allen.base.dataset.Obj;
import allen.base.dataset.Value;
import allen.base.module.AAI_Module;
import allen.base.set.AllenSet;

/**
 * The mode (or cluster) in the k-modes algorithm.
 * 
 * @author Allen Lin, 17 June 2016
 */
public class Mode extends AAI_Module implements AllenSet {
	private static final long serialVersionUID = 5200149297219669083L;

	/** objs[] belonging to this mode (for DEBUG only) */
	private HashSet<Obj> m_objs = new HashSet<Obj>();

	/** distribution table [value, count] of the objects[] in the mode */
	private HashMap<Value, Integer> m_mapValCount = new HashMap<Value, Integer>();

	/** object of the mode, i.e., whose values have the highest count. */
	private Obj m_objMode;

	/** property functions ***************************************/
	public int size() {
		return m_objs.size();
	}

	/** @return objects[] in this mode */
	public HashSet<Obj> getObjs() {
		return m_objs;
	}

	/** @return value's count in this mode */
	private Integer getCount(Value value) {
		Integer count = m_mapValCount.get(value);
		return count == null ? 0 : count;
	}

	/** increase value count */
	private void addValue(Value val) {
		Integer count = m_mapValCount.get(val);
		m_mapValCount.put(val, (count == null) ? 1 : (count + 1));
	}

	/** decrease value count */
	private void delValue(Value val) {
		Integer count = m_mapValCount.get(val);
		Common.Assert(count != null);
		m_mapValCount.put(val, (count == 0) ? 0 : (count - 1));
	}

	/** manipulation functions ***************************************/
	/** add an object to the mode (cluster) */
	public void addObj(Obj obj) throws Exception {
		m_objMode = null; // reset object of mode
		m_objs.add(obj);
		// update value counts
		for (Value value : obj.getValues()) {
			addValue(value);
		}
	}

	/** remove an object from the mode (cluster) */
	public void removeObj(Obj obj) throws Exception {
		m_objMode = null; // reset object of mode
		m_objs.remove(obj);
		// update value counts
		for (Value value : obj.getValues()) {
			delValue(value);
		}
	}

	/** @return the object of the mode */
	public Obj getObj() {
		if (m_objMode == null) {
			m_objMode = new Obj();
			m_objMode.name(name() + "_obj");
			for (Value newValue : m_mapValCount.keySet()) {
				Integer newCount = m_mapValCount.get(newValue);
				Feature ftr = newValue.getFtr();
				Value oldValue = m_objMode.getValue(ftr);
				Integer oldCount = getCount(oldValue);
				if (newCount > oldCount) {
					m_objMode.setValue(ftr, newValue);
				}
			}
		}
		return m_objMode;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		// 1. objs[]
		sb.append(name() + " has " + size() + " objs: ");
		for (Obj obj : m_objs) {
			sb.append(obj.name() + " ");
		}
		// 2. value and counts[]
		sb.append(", mode = |");
		for (Value value : m_mapValCount.keySet()) {
			sb.append(value.getFtr().name() + "~" + value.name() + "(" + getCount(value) + ")|");
		}
		return sb.toString();
	}
}