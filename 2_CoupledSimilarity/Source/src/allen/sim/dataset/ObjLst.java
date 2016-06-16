package allen.sim.dataset;

import java.util.ArrayList;

/** Global Objects[n] */
public class ObjLst {
	private ArrayList<Obj> m_objLst = new ArrayList<Obj>();

	public ObjLst() {
	}

	public ObjLst(int k) {
		m_objLst = new ArrayList<Obj>(k);
	}

	public int size() {
		return m_objLst.size();
	}

	public void add(Obj obj) {
		m_objLst.add(obj);
	}

	public Obj get(int i) {
		return m_objLst.get(i);
	}

	public ArrayList<Obj> getObjs() {
		return m_objLst;
	}

	public String getName(int i) {
		return m_objLst.get(i).name();
	}

	public String toString() {
		String buf = new String();
		for (Obj obj : m_objLst) {
			buf += obj.toString() + "\n";
		}
		return buf;
	}
}