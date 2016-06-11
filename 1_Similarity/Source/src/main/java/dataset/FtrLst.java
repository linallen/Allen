package dataset;

import java.util.ArrayList;

import common.Common;

/** Global Features[n] */
public class FtrLst {
	private ArrayList<Feature> m_ftrLst = new ArrayList<Feature>();

	/** get feature[i] */
	public Feature get(int i) throws Exception {
		Common.Assert(i >= 0);
		// extend global features[]
		for (int j = m_ftrLst.size(); j <= i; j++) {
			m_ftrLst.add(new Feature("Ftr_" + i));
		}
		return m_ftrLst.get(i);
	}

	public int size() {
		return m_ftrLst.size();
	}

	public ArrayList<Feature> getFtrs() {
		return m_ftrLst;
	}

	/** return mapping (value, object) */
	public String getValueObjMapping() {
		String buf = new String();
		for (Feature ftr : m_ftrLst) {
			buf += ftr.getValueObjMapping() + "\n";
		}
		return buf;
	}

	public String toString() {
		String buf = new String();
		for (Feature ftr : m_ftrLst) {
			buf += ftr.toString() + "\n";
		}
		return buf;
	}
}