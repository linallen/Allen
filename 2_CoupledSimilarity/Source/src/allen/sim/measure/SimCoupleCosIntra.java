package allen.sim.measure;

import allen.sim.dataset.Value;

/**
 * COS-INTRA algorithm.
 * 
 * @author Allen Lin, 19 June 2016
 */
public class SimCoupleCosIntra extends SimCoupleCos {
	private static final long serialVersionUID = -4894999364924747488L;

	/** calculate sim(val1, val2). */
	@Override
	protected double calcValSim(Value val1, Value val2) throws Exception {
		return intraSim(val1, val2);
	}
}