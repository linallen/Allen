package allen.clusterer;

import allen.clusterer.alg.kmodes.Kmodes;

public class ClustererRegister {
	public static void register() {
		// 0. register clusterers[]
		Clusterer.register("KMODES", Kmodes.class);
	}
}
