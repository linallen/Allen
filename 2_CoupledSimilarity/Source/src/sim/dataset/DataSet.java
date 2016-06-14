package sim.dataset;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

import common.*;
import module.*;

/**
 * Stores a data set containing a (categorical) feature table.<br>
 * Format: ftr1,...,ftrn, label
 * 
 * @author Allen Lin, 24 Mar 2016
 */
public class DataSet extends AAI_Module {
	private static final long serialVersionUID = -6871669684345314842L;

	/** object[n] */
	private ObjLst m_objLst = new ObjLst();
	/** feature[m] */
	private FtrLst m_ftrLst = new FtrLst();
	/** class[s] */
	private ClsSet m_clsSet = new ClsSet();

	/** data set directory and name */
	public String m_dataFile, m_dataDir, m_dataName;

	public boolean isEmpty() {
		return objNum() == 0;
	}

	public int objNum() {
		return m_objLst.size();
	}

	public int ftrNum() {
		return m_ftrLst.size();
	}

	public int clsNum() {
		return m_clsSet.size();
	}

	public Obj getObj(int i) {
		return m_objLst.get(i);
	}

	public ArrayList<Feature> ftrLst() {
		return m_ftrLst.getFtrs();
	}

	public Feature ftr(int i) {
		return ftrLst().get(i);
	}

	/** load data from file. Format: ftr1,...,ftrn, [label] */
	public void loadData(String dataFile) throws Exception {
		output("Loading data started. " + dataFile);
		Timer timer = new Timer();
		m_dataFile = dataFile;
		m_dataName = AAI_IO.getFileNamePre(dataFile);
		m_dataDir = AAI_IO.getAbsDir(dataFile);
		m_objLst = new ObjLst();
		m_ftrLst = new FtrLst();
		m_clsSet = new ClsSet();
		BufferedReader br = new BufferedReader(new FileReader(dataFile));
		try {
			String line = null;
			for (int i = 0; (line = br.readLine()) != null; i++) {
				int pos = line.lastIndexOf(COMMA);
				String valuesLie = line.substring(0, pos);
				String clsLabel = line.substring(pos + 1);
				Cls cls = m_clsSet.add(clsLabel);
				Obj obj = new Obj();
				obj.name("Obj_" + i);
				obj.cls(cls);
				// read in object[i]
				String[] valuesStr = valuesLie.split(COMMA);
				// obj.setValueNum(valuesStr.length); // init value number
				for (int j = 0; j < valuesStr.length; j++) {
					// 1. update feature's value list
					Feature ftr = m_ftrLst.get(j);
					String valueStr = valuesStr[j].trim();
					Value value = ftr.addValue(valueStr);
					// 2. set object's value[j]
					obj.setValue(j, value);
					// 3. build mapping<value, object> used by CoupleSim
					if (!Value.isMissing(value)) {
						value.addObj(obj);
					}
				}
				// setObjValues(obj, valuesLie.split(COMMA));
				m_objLst.add(obj);
			}
		} finally {
			AAI_IO.close(br);
			output("Loading data finished. " + timer);
		}
		// output for debug
		if (debug()) {
			dbgOutputDataSummary(); // debug
			dbgOutputData2Matlab();
			System.out.println(m_objLst);
			System.out.println(m_ftrLst);
			System.out.println(m_clsSet);
		}
	}

	/** TODO: DELETE set object's value list */
	public void setObjValues(Obj obj, String[] ftrValues) throws Exception {
		// debug
		// if (obj.valueNum() > 0) {
		// System.out.println("debug");
		// }
		// debug
		for (int i = 0; i < ftrValues.length; i++) {
			Value val = m_ftrLst.get(i).getValue(ftrValues[i]);
			obj.setValue(i, val);
		}
	}

	/**
	 * output summary of data set: (for debug only)<br>
	 * ds_name obj_num(M) ftr_num(N) cls_num min_val_num max_val_num ave_val_num
	 */
	public void dbgOutputDataSummary() {
		String buf = m_dataName + "\t" + objNum() + "\t" + ftrNum() + "\t" + clsNum() + "\t";
		int minValNum = Integer.MAX_VALUE, maxValNum = Integer.MIN_VALUE, totalValNum = 0;
		for (Feature ftr : ftrLst()) {
			int valNum = ftr.getValueNum();
			minValNum = Math.min(minValNum, valNum);
			maxValNum = Math.max(maxValNum, valNum);
			totalValNum += valNum;
		}
		buf += minValNum + "\t" + maxValNum + "\t" + (1. * totalValNum / ftrNum());
		AAI_IO.saveFile(m_dataDir + m_dataName + ".ds_summary.txt", buf);
		// System.out.println("Data Summary: " + buf);
	}

	public void dbgSummary() {
		String buf = "##### " + m_dataName + ", " + objNum() + "_objs_" + ftrNum() + "_ftrs_" + clsNum() + "classes";
		System.out.println(buf);
	}

	/** output data set to Matlab matrix format: A = {'t','n','won';... } */
	public void dbgOutputData2Matlab() throws Exception {
		String matlabFile = m_dataDir + m_dataName + ".m";
		output("Outputing Matlab data started. " + matlabFile);
		Timer timer = new Timer();
		BufferedWriter bw = new BufferedWriter(new FileWriter(matlabFile));
		try {
			bw.write("clc;\nclear;\nA = {");
			// String buf = "clc;\nclear;\nA = {";
			int objNum = objNum();
			for (int i = 0; i < objNum; i++) {
				Obj obj = getObj(i);
				int valNum = obj.values().size();
				progress(i + 1, objNum);
				for (int j = 0; j < valNum; j++) {
					Value val = obj.getValue(j);
					bw.write(Common.quote(val.getValue(), '\''));
					if (j < valNum - 1) {
						bw.write(",");
					}
				}
				// bw.write(Common.quote(obj.getCls().getName(), '\'') +
				// ";...\n");
				bw.write(";...\n");
			}
			bw.write("};\n");
		} finally {
			AAI_IO.close(bw);
			output("Loading data finished. " + timer);
		}
		// AAI_IO.saveFile(, buf);
		// System.out.println("Data Summary: " + buf);
		output("Outputing Matlab data finished. " + timer);
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("cls_num = " + clsNum() + "\n" + m_clsSet.toString() + "\n");
		buf.append("ftr_num = " + ftrNum() + "\n" + m_ftrLst.toString() + "\n");
		buf.append("obj_num = " + objNum() + "\n");
		buf.append(m_objLst.toString());
		return buf.toString();
	}
}