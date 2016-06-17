package allen.sim.dataset;

import java.util.HashMap;

/** Feature Set[n] of a data set */
public class FtrSet {
	/** the owner data set */
	private DataSet m_dataSet;

	/** feature set[n]: [ftrName, ftrObj] */
	private HashMap<String, Feature> m_ftrSet = new HashMap<String, Feature>();
	/** feature indexes[n]: [ftrIdx, ftrName], ftrIdx = 0, ..., n-1 */
	private HashMap<Integer, String> m_ftrIdx = new HashMap<Integer, String>();

	/** property functions ***************************************/
	public int size() {
		return m_ftrSet.size();
	}

	public boolean isEmpty() {
		return size() == 0;
	}

	public void dataSet(DataSet dataSet) {
		m_dataSet = dataSet;
	}

	public DataSet dataSet() {
		return m_dataSet;
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

	/** return feature[ftrName] */
	public Feature getFtr(String ftrName) throws Exception {
		return m_ftrSet.get(ftrName);
	}

	/** return feature[i] */
	public Feature getFtr(int i) throws Exception {
		String ftrName = m_ftrIdx.get(i);
		return m_ftrSet.get(ftrName);
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