package aai.matlab;

import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;
import matlabcontrol.MatlabProxyFactory;

public class Test1 {

	public static void main(String[] args) throws MatlabInvocationException, MatlabConnectionException {
		MatlabProxyFactory factory = null;
		MatlabProxy proxy = null;
		try {
			// To get a sort of handler to the matlab instance
			factory = new MatlabProxyFactory();
			proxy = factory.getProxy();
			// proxy.eval("cd
			// C:/Allen/UTS/UTS_SourceCode/2_CoupledSimilarity/_experiments/matlab_src/Evaluation/");
			proxy.eval("cd data/matlab/");
			// Setting up matrices in Java, basically its 2D object array
			int[] gnd = new int[] { 2, 5, 5 };
			int[] res = new int[] { 0, 2, 2 };
			// Set up the variables in MATLAB
			// sets up a matrix 'a' which is your 2D array a in Java
			proxy.setVariable("gnd", gnd);
			proxy.setVariable("res", res);
			// foo is your matlab script foo.m, lets say the expected result is
			// a boolean - true or false
			proxy.eval("map = bestMap(gnd,res)");
			// proxy.returningFeval(functionName, nargout, args)
			// By default everything in matlab is a matrix, here you get 1 x 1
			// matrix as a Result variabke from matrix, which you need to
			// convert into java boolean
			Object map = proxy.getVariable("map");
			System.out.println("map is " + map.getClass().getSimpleName());
			double[] result = (double[]) map;
			for (int i = 0; i < result.length; i++) {
				System.out.println("result[" + i + "] = " + result[i]);
			}
			System.out.println("Matlab finished.");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				proxy.exit();
			} catch (MatlabInvocationException e1) {
				// logging, handling
			}
			proxy.disconnect();
			System.exit(-1);
		}
	}
}