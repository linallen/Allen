package allen.sim.measure;

import allen.sim.dataset.DataSet;
import allen.sim.dataset.Obj;
import allen.sim.dataset.Value;

/** Occurrence Frequency Dissimilarity */
public class SimOFD extends SimMeasure {
	private static final long serialVersionUID = 8890547065385749434L;

	@Override
	public double sim(Obj objX, Obj objY, DataSet data) throws Exception {
		double objSim = 0, N = data.objNum();
		for (int i = 0; i < Math.min(objX.valueNum(), objY.valueNum()); i++) {
			Value valX = objX.getValue(i);
			Value valY = objY.getValue(i);
			if (!Value.isMissing(valX) && !Value.isMissing(valY)) {
				if (valX == valY) {
					objSim += 1.;
				} else {
					objSim += 1. / (1. + Math.log(N / valX.getObjNum()) * Math.log(N / valY.getObjNum()));
				}
			}
		}
		return objSim;
	}
}