package allen.clusterer.alg.spectral.matlab;

import allen.base.common.AAI_IO;
import allen.base.common.Common;
import allen.base.common.Timer;
import allen.clusterer.Clusterer;
import allen.matlab.Matlab;
import matlabcontrol.MatlabProxy;

/**
 * Spectral Clustering (Matlab version) by invoking the Matlab function
 * spectralClustering(SimGraph, k, Type) developed by Ingo B��rk for thesis.<br>
 * 
 * @author Allen Lin, 2 Apr 2016
 */
public class SpecClusterMatlab extends Clusterer {
	private static final long serialVersionUID = 3478029931589853988L;

	private static String s_matlabDir = "matlab_src/SpectralClustering/", s_TempDir;

	public SpecClusterMatlab() throws Exception {
		Common.Assert(AAI_IO.dirExist(s_matlabDir));
		AAI_IO.createDir(s_TempDir = s_matlabDir + "temp/");
	}

	@Override
	protected int[] clusteringAlg() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Clustering a similarity graph with Spectral Clustering algorithm.
	 * 
	 * @param simGraphFile
	 *            A similarity graph
	 * @param k
	 *            cluster number
	 * @return clusters[]
	 */
	public int[] clustering(String simGraphFile, int k) throws Exception {
		System.out.println("Spectral Clustering started (Matlab version). " + simGraphFile);
		System.out.println("k = " + k);
		Timer timer = new Timer();
		String simGraphTemp = s_TempDir + AAI_IO.getFileNamePre(simGraphFile).replace(".", "_") + ".m";
		AAI_IO.fileCopy(simGraphFile, simGraphTemp);
		try {
			MatlabProxy proxy = Matlab.getProxy();
			proxy.eval("cd " + s_TempDir);
			proxy.eval(AAI_IO.getFileNamePre(simGraphTemp));
			proxy.eval("cd " + s_matlabDir);
			proxy.eval("[C, L, U] = SpectralClustering(SimGraph, " + k + ", 1);");
			// convert sparse C to full matrix (obj_num X cluster_num)
			proxy.eval("C = full(C)';");
			proxy.eval("obj_num = size(C,2);");
			Object C = proxy.getVariable("C");
			int objNum = (int) ((double[]) proxy.getVariable("obj_num"))[0];
			// System.out.println(C.getClass().getName());
			// System.out.println(C.getClass().getSimpleName());
			double clusterMatrix[] = (double[]) C;
			Common.Assert(clusterMatrix.length == objNum * k);
			m_clusters = new int[objNum];
			for (int i = 0; i < objNum; i++) {
				for (int j = 0; j < k; j++) {
					int idx = i * k + j;
					// System.out.println("clusterMatrix[" + i + ", " + j + "] =
					// " + clusterMatrix[idx]);
					if (clusterMatrix[idx] == 1.) {
						m_clusters[i] = j;
					}
				}
				// System.out.println(); // debug
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Spectral Clustering finished (Matlab version). " + timer);
		return m_clusters;
	}
}