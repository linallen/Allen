package allen.clusterer.alg.spectral.jian;

import allen.base.common.AAI_IO;
import allen.base.common.Common;
import allen.clusterer.Clusterer;
import allen.matlab.Matlab;
import matlabcontrol.MatlabProxy;

/**
 * Spectral Clustering (Matlab Jian version) by invoking the Matlab function
 * SpectralClustering_Normalized(double(matrix),k) developed by Songlei Jian.
 * <br>
 * 
 * @author Allen Lin, 21 June 2016
 */
public class SpecClusterJian extends Clusterer {
	private static final long serialVersionUID = 8051774479887039653L;
	private static String s_matlabDir, s_TempDir;

	public SpecClusterJian() throws Exception {
		if (s_matlabDir == null) {
			s_matlabDir = workDir() + "../Matlab/CMS/functions/";
			Common.Assert(AAI_IO.dirExist(s_matlabDir));
			AAI_IO.createDir(s_TempDir = s_matlabDir + "temp/");
		}
	}

	@Override
	protected int[] clusteringAlg() throws Exception {
		String simGraphFile = tempDir() + dataSet().dataName() + ".sim_graph.txt";
		m_simMeasure.saveSimGraph(simGraphFile);
		return clustering(simGraphFile, m_k);
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
	private int[] clustering(String simGraphFile, int k) throws Exception {
		String simGraphTemp = s_TempDir + AAI_IO.getFileNamePre(simGraphFile).replace(".", "_") + ".m";
		AAI_IO.fileCopy(simGraphFile, simGraphTemp);
		try {
			MatlabProxy proxy = Matlab.getProxy();
			proxy.eval("cd " + s_TempDir);
			proxy.eval(AAI_IO.getFileNamePre(simGraphTemp));
			proxy.eval("cd " + s_matlabDir);
			proxy.eval("flag_spec = SpectralClustering_Normalized(double(SimGraph), " + k + ");");
			double flag_spec[] = ((double[]) proxy.getVariable("flag_spec"));
			m_clusters = new int[m_dataSet.objNum()];
			for (int i = 0; i < m_dataSet.objNum(); i++) {
				m_clusters[i] = (int) flag_spec[i];
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return m_clusters;
	}

	public static String help() {
		return "Spectral Clustering (Matlab Jian version) by invoking the Matlab function SpectralClustering_Normalized(double(matrix),k) developed by Songlei Jian.\n\n"
				+ Clusterer.help();
	}

	public static String version() {
		return "v0.01, Created on 21 June 2016, Allen Lin.";
	}

	public static void main(String[] args) throws Exception {
		exec(Thread.currentThread().getStackTrace()[1].getClassName(), args);
	}
}