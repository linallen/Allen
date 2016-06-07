package pkgTestSSpace;

import edu.ucla.sspace.clustering.Assignments;
import edu.ucla.sspace.clustering.NormalizedSpectralClustering;
import edu.ucla.sspace.matrix.ArrayMatrix;
//import org.netlib.*;
//import org.netlib.lapack.*;
//import org.netlib.util.intW;

public class TestSSpace {

	// ArrayMatrix, OnDiskMatrix, ScalarMatrix

	public static void main(String[] args) throws Throwable {
		NormalizedSpectralClustering sc = new NormalizedSpectralClustering();
		ArrayMatrix simMatrix = new ArrayMatrix(4, 4);
		simMatrix.set(0, 0, 1);
		simMatrix.set(0, 1, 1);
		simMatrix.set(0, 2, 0);
		simMatrix.set(0, 3, 0);

		simMatrix.set(1, 0, 1);
		simMatrix.set(1, 1, 1);
		simMatrix.set(1, 2, 0);
		simMatrix.set(1, 3, 0);

		simMatrix.set(2, 0, 0);
		simMatrix.set(2, 1, 0);
		simMatrix.set(2, 2, 1);
		simMatrix.set(2, 3, 1);

		simMatrix.set(3, 0, 0);
		simMatrix.set(3, 1, 0);
		simMatrix.set(3, 2, 1);
		simMatrix.set(3, 3, 1);
		// LAPACK lapack = new LAPACK();

		Assignments assign = sc.cluster(simMatrix, 2, null);
		int a[][] = assign.assignments();
		System.out.println("done");
	}
}
