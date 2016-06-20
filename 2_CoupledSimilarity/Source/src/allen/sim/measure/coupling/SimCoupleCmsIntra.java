package allen.sim.measure.coupling;

import allen.sim.dataset.Value;

/**
 * CMS-INTRA algorithm.
 * 
 * @author Allen Lin, 19 June 2016
 */
public class SimCoupleCmsIntra extends SimCoupleCms {
	private static final long serialVersionUID = 8765296586456377630L;

	/** calculate sim(val1, val2). */
	@Override
	protected double calcValSim(Value val1, Value val2) throws Exception {
		return intraSim(val1, val2) / 2;
	}
}