package similarity.measure;

import pkgDataSet.DataSet;
import pkgDataSet.Obj;
import pkgDataSet.Value;

/** Simple Matching Dissimilarity, or Hamming distance */
public class SimSMD implements SimMeasure {
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