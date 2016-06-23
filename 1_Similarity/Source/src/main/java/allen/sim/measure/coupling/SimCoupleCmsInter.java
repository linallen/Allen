package allen.sim.measure.coupling;

import allen.base.dataset.Value;

/**
 * CMS-INTER algorithm.
 * 
 * @author Allen Lin, 19 June 2016
 */
public class SimCoupleCmsInter extends SimCoupleCms {
	private static final long serialVersionUID = -931580640744057399L;

	/** calculate sim(val1, val2). */
	@Override
	protected double calcValSim(Value val1, Value val2) throws Exception {
		return interSim(val1, val2) / 2;
	}
}