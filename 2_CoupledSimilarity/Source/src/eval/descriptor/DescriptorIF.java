package eval.descriptor;

import sim.dataset.DataSet;
import sim.measure.SimMeasure;

/**
 * interfaces of discriptor
 * 
 * @author Allen Lin, 29 Mar 2016
 */
public interface DescriptorIF {

	public default String getSimName() {
		return getClass().getSimpleName();
	}

	public static double divide(double sum, double num) {
		return (num == 0) ? 0 : sum / num;
	}

	/** return the descriptor value */
	public String getDescriptor(SimMeasure simMeasure, DataSet data, boolean useSimilarity) throws Exception;

	/** descriptors: RD, SD, DI, DBI */
	public static Descriptor getDescriptor(String descriptorName) throws Exception {
		descriptorName = descriptorName.toUpperCase().trim();
		switch (descriptorName) {
		case "RD":
			return new RD();
		case "SD":
			return new SD();
		case "DI":
		case "DBI":
		}
		return null;
	}
}