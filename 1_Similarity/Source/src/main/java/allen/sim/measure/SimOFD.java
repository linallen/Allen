package allen.sim.measure;

import allen.base.dataset.Feature;
import allen.base.dataset.Obj;
import allen.base.dataset.Value;

/** Occurrence Frequency Dissimilarity */
public class SimOFD extends SimMeasure {
	private static final long serialVersionUID = 8890547065385749434L;

	@Override
	public double sim(Obj objX, Obj objY) throws Exception {
		double objSim = 0, N = dataSet().objNum();
		for (Feature ftr : this.getFtrs()) {
			Value valX = objX.getValue(ftr);
			Value valY = objY.getValue(ftr);
			if (valX == valY) {
				objSim += 1.;
			} else {
				objSim += 1. / (1. + Math.log(N / getOwnerObjs(valX).size()) * Math.log(N / getOwnerObjs(valY).size()));
			}
		}
		return objSim;
	}
}