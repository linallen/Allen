package allen.sim.measure;

import allen.sim.dataset.Feature;
import allen.sim.dataset.Obj;
import allen.sim.dataset.Value;

/** Occurrence Frequency Dissimilarity */
public class SimOFD extends SimMeasure {
	private static final long serialVersionUID = 8890547065385749434L;

	@Override
	public double sim(Obj objX, Obj objY) throws Exception {
		double objSim = 0, N = dataSet().objNum();
		for (Feature ftr : this.getFtrs()) {
			Value valX = objX.value(ftr);
			Value valY = objY.value(ftr);
			if (valX == valY) {
				objSim += 1.;
			} else {
				objSim += 1. / (1. + Math.log(N / getOwnerObjs(valX).size()) * Math.log(N / getOwnerObjs(valY).size()));
			}
		}
		return objSim;
	}
}