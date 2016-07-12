package allen.sim.eval.descriptor;

import allen.base.dataset.DataSet;
import allen.base.module.AAI_Module;
import allen.sim.measure.SimMeasure;

public abstract class Descriptor extends AAI_Module {
	private static final long serialVersionUID = 1232513819198470095L;

	/** return the descriptor value */
	public abstract double getMetric(SimMeasure simMeasure, DataSet dataSet) throws Exception;

	public static double divide(double sum, double num) {
		return (num == 0) ? 0 : sum / num;
	}
}