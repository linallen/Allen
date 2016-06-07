package clusterer.spectral.weka;

import java.io.BufferedReader;
import java.io.FileReader;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;
import clusterer.Clusterer;
import pkgCommon.AAI_IO;
import pkgCommon.Timer;
import sim.dataset.DataSet;
import sim.measure.SimMeasure;

/**
 * Spectral Clustering implementation adopted from Luigi Dragone.<br>
 * 
 * @author Allen Lin, 24 Mar 2016
 */
public class SpectralWeka extends SpectralWekaLib implements Clusterer {
	private static final long serialVersionUID = 7903499745283935299L;

	/**
	 * Clustering a similarity matrix with Spectral Clustering algorithm. See
	 * "public void buildClusterer(final Instances data)" in SpectralClusterer.
	 * 
	 * @param simMatrixFile
	 *            A similarity matrix:<br>
	 *            obj_num<br>
	 *            obj_idx1,obj_idx2,sim_score<br>
	 *            ...<br>
	 *            where obj_idx starts from 0 to obj_num - 1
	 * @return clusters[]
	 */
	@Override
	public int[] clustering(String simMatrixFile, int k) throws Exception {
		return clustering(simMatrixFile);
	}

	private int[] clustering(String simMatrixFile) throws Exception {
		System.out.println("Spectral Clustering started" + simMatrixFile);
		Timer timer = new Timer();
		BufferedReader br = new BufferedReader(new FileReader(simMatrixFile));
		try {
			// 1. load similarity matrix from file: n\n 0,0,0.43\n ...
			// 1.1 object number n
			String line = br.readLine().trim();
			final int objNum = Integer.parseInt(line);
			final double sigma_sq = sigma * sigma;
			// useSparseMatrix = true;
			final DoubleMatrix2D simMatrix = useSparseMatrix ? DoubleFactory2D.sparse.make(objNum, objNum)
					: DoubleFactory2D.dense.make(objNum, objNum);
			// 1.2 sim(i,j)
			while ((line = br.readLine()) != null) {
				String items[] = line.split(",");
				int i = Integer.parseInt(items[0].trim());
				int j = Integer.parseInt(items[1].trim());
				double dist = 1. - Double.parseDouble(items[2].trim());
				if ((r <= 0) || (dist < r)) {
					final double simScr = Math.exp(-(dist * dist) / (2 * sigma_sq));
					simMatrix.set(i, j, simScr);
					simMatrix.set(j, i, simScr);
				}
			}
			// 2. compute point partitions
			// debug
			int[][] points = null;
			try {
				points = partition(simMatrix);
			} catch (Exception e) {
				System.out.println("clustering() Exception!");
				return null;
			}
			// debug

			// 3. deploys results
			numOfClusters = points.length;
			cluster = new int[objNum];
			for (int i = 0; i < numOfClusters; i++) {
				for (int j = 0; j < points[i].length; j++) {
					cluster[points[i][j]] = i;
				}
			}
			return cluster;
		} finally {
			AAI_IO.close(br);
			System.out.println("Spectral Clustering finished. " + timer);
		}
	}

	@Override
	public int[] clustering(SimMeasure simMeasure, DataSet data, int k) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}