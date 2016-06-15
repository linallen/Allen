package main;

import clusterer.Clusterer;
import clusterer.kmodes.Kmodes;
import clusterer.spectral.matlab.SpectralMatlab;
import clusterer.spectral.weka.SpectralWeka;
import eval.clusterer.EvalClusterMatlab;
import matlabcontrol.MatlabProxy;
import matlabcontrol.MatlabProxyFactory;
import sim.dataset.DataSet;
import sim.measure.SimMeasure;

import common.*;

/**
 * Evaluating a similarity measure's clustering performance for multiple rounds
 * to get the average performance.
 * <p>
 * Input: [sim_alg, cluster_alg, data_set]<br>
 * Output: [ACC(accuracy/precision), NMI(Normalized Mutual Information)]
 */
public class EvaluationClustering {
	private String m_matlabDir;
	private MatlabProxy m_proxy;

	// Output: [ACC(accuracy/precision), NMI(Normalized Mutual Information)]
	public double m_ACC, m_NMI;

	public void useMatlab(String matlabDir, MatlabProxy proxy) throws Exception {
		m_matlabDir = matlabDir;
		m_proxy = (proxy == null) ? new MatlabProxyFactory().getProxy() : proxy;
	}

	public void evaluate(SimMeasure simMeasure, String clusterName, DataSet data, int round) throws Exception {

	}

	public void evaluate(String simName, String clusterName, String dataFile, int round) throws Exception {
		// 1. data set DS
		DataSet data = new DataSet();
		data.loadData(dataFile);
		data.dbgSummary();
		// 2. similarity measure
		SimMeasure simMeasure = SimMeasure.getSimMeasure(simName);
		// 3. clustering evaluation. Input: [sim_alg, cluster_alg, data_set]
		// Run clustering(simMeasure, dataSet)
		// Clustering are KM (k-modes) and SC (spectral clustering)
		// 1) input for KM: similarity measure
		// 2) input for SC: similarity matrix/graph file
		// 3) Output: [ACC, NMI]
		int k = data.clsNum(); // cluster number
		int clusterNum = k;
		String clusterCSV = dataFile + "." + simName + "." + clusterName + "." + round + ".csv";
		if (clusterName.equalsIgnoreCase("KM")) {
			Kmodes kmodes = new Kmodes();
			// kmodes.debug(true); // deubg
			kmodes.kModes(k, data, simMeasure, true); // TODO DEBUG
			kmodes.saveClusters(clusterCSV);
		} else if (clusterName.equalsIgnoreCase("SC")) {
			// 1. get sim-matrix (for Java SC) or sim-graph (for Matlab SC)
			Clusterer sc;
			String simScoresFile;
			boolean m_clusterMatlab = true;
			if (!m_clusterMatlab) {
				simScoresFile = dataFile + "." + simName + ".simMatrix.txt";
				simMeasure.saveSimMatrix(simScoresFile, data);
				sc = new SpectralWeka();
			} else {
				simScoresFile = dataFile + "." + simName + ".simGraph.txt";
				simMeasure.saveSimGraph(simScoresFile, data);
				sc = new SpectralMatlab(m_proxy);
				((SpectralMatlab) sc).setMatlabCodeDir(m_matlabDir + "SpectralClustering/");
			}
			// 2. cluster on similarity matrix (KM) or graph (SC)
			int cluster[] = sc.clustering(simScoresFile, k);
			// 3. save cluster[] to clusterCSV
			if (cluster != null) {
				clusterNum = sc.numberOfClusters();
				System.out.println("SC cluster number = " + sc.numberOfClusters());
				sc.saveClusters(cluster, clusterCSV, data);
			}
		} else {
			throw new Exception("Clustering algorithm is not supported");
		}
		// Save ACC and NMI to evaluation file
		if (AAI_IO.fileExist(clusterCSV)) {
			EvalClusterMatlab evalCluster = new EvalClusterMatlab(m_proxy);
			evalCluster.calcClusterMetrics(clusterCSV, m_matlabDir + "Evaluation/");
			m_ACC = evalCluster.getAC();
			m_NMI = evalCluster.getNMI();
		}
	}
}