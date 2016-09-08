package allen.sim.measure;

import allen.sim.measure.coupling.SimCoupleCms;
import allen.sim.measure.coupling.SimCoupleCmsInter;
import allen.sim.measure.coupling.SimCoupleCmsIntra;
import allen.sim.measure.coupling.SimCoupleCos;
import allen.sim.measure.coupling.SimCoupleCosInter;
import allen.sim.measure.coupling.SimCoupleCosIntra;

/** @author Allen Lin, 8 Sep 2016 */
public class SimRegister {

	public static void register() {
		// 0. register sim_measures[]
		SimMeasure.register("CMS", SimCoupleCms.class);
		SimMeasure.register("CMS_INTRA", SimCoupleCmsIntra.class);
		SimMeasure.register("CMS_INTER", SimCoupleCmsInter.class);
		SimMeasure.register("COS", SimCoupleCos.class);
		SimMeasure.register("COS_INTRA", SimCoupleCosIntra.class);
		SimMeasure.register("COS_INTER", SimCoupleCosInter.class);
		SimMeasure.register("SMD", SimSMD.class);
		SimMeasure.register("OFD", SimOFD.class);
	}
}