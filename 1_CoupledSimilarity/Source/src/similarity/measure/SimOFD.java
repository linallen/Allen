package similarity.measure;

import pkgDataSet.DataSet;
import pkgDataSet.Obj;
import pkgDataSet.Value;

/** Occurrence Frequency Dissimilarity */
public class SimOFD implements SimMeasure {
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