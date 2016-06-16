package allen.sim.measure;

import allen.sim.dataset.DataSet;
import allen.sim.dataset.Obj;
import allen.sim.dataset.Value;

/** Simple Matching Dissimilarity, or Hamming distance */
public class SimSMD extends SimMeasure {
	private static final long serialVersionUID = -1953586334190446044L;

	/** sim = number of same values between x and y */
	@Override
	public double sim(Obj objX, Obj objY, DataSet data) throws Exception {
		double sim = 0;
		for (int i = 0; i < Math.min(objX.valueNum(), objY.valueNum()); i++) {
			Value valX = objX.getValue(i);
			Value valY = objY.getValue(i);
			if (!Value.isMissing(valX) && !Value.isMissing(valY)) {
				sim += ((valX == valY) ? 1 : 0);
			}
		}
		return sim;
	}
}