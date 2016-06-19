package allen.sim.dataset;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import allen.base.module.AAI_Module;
import allen.base.set.AllenSet;

/** Feature Set[n] of a data set */
public class FtrSet extends AAI_Module implements AllenSet {
	private static final long serialVersionUID = 5383351457021516092L;

	/** the owner data set (for Debug only) */
	private DataSet m_dataSet;

	/** feature set[n]: [ftrName, ftrObj] */
	private HashMap<String, Feature> m_ftrSet = new HashMap<String, Feature>();
	/** feature indexes[n]: [ftrIdx, ftrName], ftrIdx = 0, ..., n-1 */
	private HashMap<Integer, String> m_ftrIdx = new HashMap<Integer, String>();

	/** property functions ***************************************/
	@Override
	public int size() {
		return m_ftrSet.size();
	}

	public void dataSet(DataSet dataSet) {
		m_dataSet = dataSet;
	}

	public DataSet dataSet() {
		return m_dataSet;
	}

	/** @return deep copy of this feature set (except the owner data set) */
	public FtrSet deepCopy() throws Exception {
		FtrSet ftrSetCopy = new FtrSet();
		ftrSetCopy.name(name());
		ftrSetCopy.owner(owner());
		ftrSetCopy.m_ftrSet = new HashMap<String, Feature>();
		for (String ftrName : m_ftrSet.keySet()) {
			// TODO deep copy ftr's properties
			Feature ftr = m_ftrSet.get(ftrName).deepCopy();
			ftrSetCopy.m_ftrSet.put(ftrName, ftr);
		}
		ftrSetCopy.m_ftrIdx = new HashMap<Integer, String>();
		for (Integer ftrIdx : m_ftrIdx.keySet()) {
			ftrSetCopy.m_ftrIdx.put(ftrIdx, m_ftrIdx.get(ftrIdx));
		}
		return ftrSetCopy;
	}

	/** return feature[ftrName] */
	public Feature getFtr(String ftrName) throws Exception {
		return m_ftrSet.get(ftrName);
	}

	/** return feature[i] */
	public Feature getFtr(int i) throws Exception {
		String ftrName = m_ftrIdx.get(i);
		return m_ftrSet.get(ftrName);
	}

	/** return feature set[] */
	public Collection<Feature> ftrs() {
		return m_ftrSet.values();
	}

	/** @return feature list[] */
	public ArrayList<Feature> ftrLst() {
		return new ArrayList<Feature>(m_ftrSet.values());
	}

	/** @return feature list[] of a specific type */
	public ArrayList<Feature> ftrLst(FtrType ftrType) {
		ArrayList<Feature> ftrLst = new ArrayList<Feature>();
		for (Feature ftr : m_ftrSet.values()) {
			if (ftr.type() == ftrType) {
				ftrLst.add(ftr);
			}
		}
		return ftrLst;
	}

	/** manipulation functions ***************************************/
	/** add a feature to the feature set. */
	public void addFtr(String ftrName, Feature ftr) {
		m_ftrSet.put(ftrName, ftr);
	}

	/** set feature's index */
	public void setFtrIdx(String ftrName, int index) throws Exception {
		if (m_ftrSet.get(ftrName) == null) {
			throw new Exception("Feature not found: " + ftrName);
		}
		m_ftrIdx.put(index, ftrName);
	}

	/** return mapping <value, object> */
	// public String getValueObjMapping() {
	// String buf = new String();
	// for (Feature ftr : m_ftrSet.values()) {
	// buf += ftr.getValueObjMapping() + "\n";
	// }
	// return buf;
	// }

	public String toString() {
		String buf = new String();
		for (Feature ftr : m_ftrSet.values()) {
			buf += ftr.toString() + "\n";
		}
		return buf;
	}
}