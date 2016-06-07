package sim.datastructure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import sim.dataset.Obj;

/**
 * Store similar objects to the target object.<br>
 * 
 * @author Allen Lin, 20 Feb 2016
 */

/** similar object with similarity score */
class SimObj {
	public Obj m_simObj;
	public double m_simScore;

	public SimObj(Obj simObj, double simScore) {
		m_simObj = simObj;
		m_simScore = simScore;
	}

	public String toString() {
		return m_simObj.fullName() + " " + String.format("%.4f", m_simScore);
	}
}

class SimObjComparator implements Comparator<SimObj> {
	@Override
	public int compare(SimObj o1, SimObj o2) {
		return (int) Math.signum(o2.m_simScore - o1.m_simScore);
	}
}

public class SimObjs {
	/** similar objects with similar scores */
	private ArrayList<SimObj> m_simObjs = new ArrayList<SimObj>();

	public int size() {
		return m_simObjs.size();
	}

	public Obj getSimObj(int i) {
		return m_simObjs.get(i).m_simObj;
	}

	public double getSimScore(int i) {
		return m_simObjs.get(i).m_simScore;
	}

	/** add [simObj, simScore] to similar object list */
	public void add(Obj simObj, double simScore) {
		m_simObjs.add(new SimObj(simObj, simScore));
	}

	/** sort similar objects by similar scores in descending order */
	public void sort() {
		Collections.sort(m_simObjs, new SimObjComparator());
	}

	public String toString() {
		String buf = new String();
		for (int i = 0; i < m_simObjs.size(); i++) {
			SimObj simObj = m_simObjs.get(i);
			buf += simObj.toString() + ((i < m_simObjs.size() - 1) ? ", " : "");
		}
		return buf;
	}
}