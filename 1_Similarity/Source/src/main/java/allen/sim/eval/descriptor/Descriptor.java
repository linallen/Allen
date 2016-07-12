package allen.sim.eval.descriptor;

import allen.base.dataset.DataSet;
import allen.sim.measure.SimMeasure;

public abstract class Descriptor {
	public static double divide(double sum, double num) {
		return (num == 0) ? 0 : sum / num;
	}

	/** return the descriptor value */
	public abstract double getDescriptor(SimMeasure simMeasure, DataSet dataSet) throws Exception;

	/** descriptors: RD, SD, DI, DBI */
	public static Descriptor getDescriptor(String descriptorName) throws Exception {
		descriptorName = descriptorName.toUpperCase().trim();
		switch (descriptorName) {
		case "DBI":
			return new DBI();
		case "RD":
			return new RD();
		case "SD":
			return new SD();
		case "DI":
		}
		return null;
	}
}