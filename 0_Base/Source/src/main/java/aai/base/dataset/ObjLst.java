package aai.base.dataset;

import java.util.ArrayList;

import aai.base.set.AllenSet;

/** Global Objects[n] */
public class ObjLst implements AllenSet {
	private ArrayList<Obj> m_objLst = new ArrayList<Obj>();

	public ObjLst() {
	}

	public ObjLst(int k) {
		m_objLst = new ArrayList<Obj>(k);
	}

	/** property functions ***************************************/
	@Override
	public int size() {
		return m_objLst.size();
	}

	public void addObj(Obj obj) {
		m_objLst.add(obj);
	}

	public Obj getObj(int i) {
		return m_objLst.get(i);
	}

	public ArrayList<Obj> getObjs() {
		return m_objLst;
	}

	public String getName(int i) {
		return m_objLst.get(i).name();
	}

	/** output functions ***************************************/
	public String toString() {
		String buf = new String();
		for (Obj obj : m_objLst) {
			buf += obj.toString() + "\n";
		}
		return buf;
	}
}