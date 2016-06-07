package clusterer.spectral.matlab;

import clusterer.Clusterer;
import matlabcontrol.MatlabProxy;
import matlabcontrol.MatlabProxyFactory;
import pkgCommon.AAI_IO;
import pkgCommon.Common;
import pkgCommon.Timer;
import pkgDataSet.DataSet;
import similarity.measure.SimMeasure;

/**
 * Spectral Clustering implementation (Matlab version). The class calls Matlab
 * function pectralClustering(SimGraph, k, Type) developed by Ingo B¨¹rk for
 * thesis.<br>
 * 
 * @author Allen Lin, 2 Apr 2016
 */
public class SpectralMatlab implements Clusterer {
	private MatlabProxy m_proxy;
	private String m_matlabCodeDir, m_matlabTempDir;
	private int m_clusters[], m_clusterNum;

	public SpectralMatlab() throws Exception {
		m_proxy = new MatlabProxyFactory().getProxy();
	}

	public SpectralMatlab(MatlabProxy proxy) throws Exception {
		m_proxy = (proxy == null) ? new MatlabProxyFactory().getProxy() : proxy;
	}

	@Override
	public int numberOfClusters() {
		return m_clusterNum;
	}

	public void setMatlabCodeDir(String MatlabCodeDir) throws Exception {
		m_matlabCodeDir = MatlabCodeDir;
		Common.Assert(AAI_IO.dirExist(m_matlabCodeDir));
		m_matlabTempDir = m_matlabCodeDir + "temp/";
		AAI_IO.createDir(m_matlabTempDir);
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
	@Override
	public int[] clustering(String simGraphFile, int k) throws Exception {
		System.out.println("Spectral Clustering started (Matlab version). " + simGraphFile);
		System.out.println("k = " + k);
		Timer timer = new Timer();
		m_clusterNum = k;
		String simGraphTemp = m_matlabTempDir + AAI_IO.getFileNamePre(simGraphFile).replace(".", "_") + ".m";
		AAI_IO.fileCopy(simGraphFile, simGraphTemp);
		try {
			m_proxy.eval("cd " + m_matlabTempDir);
			m_proxy.eval(AAI_IO.getFileNamePre(simGraphTemp));
			m_proxy.eval("cd " + m_matlabCodeDir);
			m_proxy.eval("[C, L, U] = SpectralClustering(SimGraph, " + k + ", 1);");
			// convert sparse C to full matrix (obj_num X cluster_num)
			m_proxy.eval("C = full(C)';");
			m_proxy.eval("obj_num = size(C,2);");
			Object C = m_proxy.getVariable("C");
			int objNum = (int) ((double[]) m_proxy.getVariable("obj_num"))[0];
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

	@Override
	public int[] clustering(SimMeasure simMeasure, DataSet data, int k) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}