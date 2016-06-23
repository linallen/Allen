package allen.sim.measure.coupling;

import allen.base.dataset.Value;

/**
 * COS-INTER algorithm.
 * 
 * @author Allen Lin, 19 June 2016
 */
public class SimCoupleCosInter extends SimCoupleCos {
	private static final long serialVersionUID = -4894999364924747488L;

	/** calculate sim(val1, val2). */
	@Override
	protected double calcValSim(Value val1, Value val2) throws Exception {
		return interSim(val1, val2);
	}
}