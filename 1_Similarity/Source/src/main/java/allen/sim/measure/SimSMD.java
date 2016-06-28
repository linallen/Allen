package allen.sim.measure;

import allen.base.dataset.Feature;
import allen.base.dataset.Obj;
import allen.base.dataset.Value;

/** Simple Matching Dissimilarity, or Hamming distance */
public class SimSMD extends SimMeasure {
	private static final long serialVersionUID = -1953586334190446044L;

	/** sim = number of same values between x and y */
	@Override
	public double sim(Obj objX, Obj objY) throws Exception {
		double sim = 0;
		for (Feature ftr : this.getFtrs()) {
			Value valX = objX.getValue(ftr);
			Value valY = objY.getValue(ftr);
			if (Value.isMissing(valX) || Value.isMissing(valY)) {
				continue;
			}
			sim += ((valX == valY) ? 1 : 0);
		}
		return sim;
	}
}