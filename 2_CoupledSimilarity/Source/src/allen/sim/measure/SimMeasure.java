package allen.sim.measure;

import java.io.BufferedWriter;
import java.io.FileWriter;

import allen.base.common.*;
import allen.base.module.AAI_Module;
import allen.sim.dataset.DataSet;
import allen.sim.dataset.Obj;

/**
 * interface of similarity measures.
 * 
 * @author Allen Lin, 25 Mar 2016
 */
public abstract class SimMeasure extends AAI_Module {
	private static final long serialVersionUID = 7217652874275084535L;

	/** return sim(x,y, data) */
	public double sim(Obj objX, Obj objY, DataSet data) throws Exception {
		return 1. / (distance(objX, objY, data) + 1);
	}

	/** return distance(x,y, data) */
	public double distance(Obj objX, Obj objY, DataSet data) throws Exception {
		double sim = sim(objX, objY, data);
		// return (sim < 1e-6) ? Double.MAX_VALUE : (1 / sim - 1);
		return (sim < 1e-6) ? Double.MAX_VALUE : (1 / sim);
	}

	/** similarity measures: SMD, OFD, ADD, COS, INTRA, INTER */
	public static SimMeasure getSimMeasure(String simName) throws Exception {
		simName = simName.toUpperCase().trim();
		if (simName.contains("COS") || simName.contains("CMS")) {
			SimCouple coupleSim = new SimCouple();
			coupleSim.setSimName(simName);
			return coupleSim;
		}
		switch (simName) {
		case "SMD":
			return new SimSMD();
		case "OFD":
			return new SimOFD();
		case "ADD":
			// TODO
		}
		throw new Exception(simName + ": similarity measure not found!");
	}

	/** save similarity matrix to file */
	public void saveSimMatrix(String simMatrixFile, DataSet data) throws Exception {
		System.out.println("Saving similarity matrix to " + simMatrixFile);
		Timer timer = new Timer();
		BufferedWriter bw = new BufferedWriter(new FileWriter(simMatrixFile));
		bw.write(data.objNum() + "\n");
		int percOld = 0, percNew = 0, objNum = data.objNum();
		for (int i = 0; i < objNum; i++) {
			percNew = 100 * (i + 1) / objNum;
			if (percNew > percOld) {
				percOld = percNew;
				System.out.print(percNew + "%" + (percNew % 10 == 0 ? "\n" : ", "));
			}
			for (int j = i + 1; j < objNum; j++) {
				double simScore = sim(data.getObj(i), data.getObj(j), data);
				if (simScore > 0) {
					String simScoreStr = Common.decimal(simScore, 4);
					bw.write(i + "," + j + "," + simScoreStr + "\n");
				}
			}
		}
		bw.close();
		System.out.println("Saving similarity matrix finished. " + timer);
	}

	/** save similarity graph (adjacent similarity matrix) to file */
	public void saveSimGraph(String simGraphFile, DataSet data) throws Exception {
		System.out.println("Saving similarity graph to " + simGraphFile);
		Timer timer = new Timer();
		BufferedWriter bw = new BufferedWriter(new FileWriter(simGraphFile));
		bw.write("SimGraph=[\n");
		int percOld = 0, percNew = 0, objNum = data.objNum();
		for (int i = 0; i < objNum; i++) {
			percNew = 100 * (i + 1) / objNum;
			if (percNew > percOld) {
				percOld = percNew;
				System.out.print(percNew + "%" + (percNew % 10 == 0 ? "\n" : ", "));
			}
			for (int j = 0; j < objNum; j++) {
				double simScore = sim(data.getObj(i), data.getObj(j), data);
				String simScoreStr = Common.decimal(simScore, 4);
				bw.write(((j == 0) ? "" : ",") + simScoreStr);
			}
			bw.write(";\n");
		}
		bw.write("];\n");
		bw.close();
		System.out.println("Saving similarity matrix finished. " + timer);
	}
}