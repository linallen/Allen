package test.matlab;

import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;
import matlabcontrol.MatlabProxyFactory;

public class MatlabTest {

	public static void main(String[] args) throws MatlabInvocationException, MatlabConnectionException {
		MatlabProxyFactory factory = null;
		MatlabProxy proxy = null;
		try {
			// To get a sort of handler to the matlab instance
			factory = new MatlabProxyFactory();
			proxy = factory.getProxy();
			// cd is matlab command to change working directory, change the
			// current
			// working directory to path where your matlab scripts are hosted
			proxy.eval("cd D:/GoogleDrive/UTS/SourceCode/1_CoupledSimilarity/_cluster/_matlab");
			// Setting up matrices in Java, basically its 2D object array
			int[] gnd = new int[] { 2, 5, 5 };
			int[] res = new int[] { 0, 2, 2 };
			double result[];
			// Set up the variables in MATLAB
			// sets up a matrix 'a' which is your 2D array a in Java
			proxy.setVariable("gnd", gnd);
			proxy.setVariable("res", res);
			// foo is your matlab script foo.m, lets say the expected result is
			// a boolean - true or false
			proxy.eval("res = bestMap(gnd,res)");
			// proxy.returningFeval(functionName, nargout, args)
			// By default everything in matlab is a matrix, here you get 1 x 1
			// matrix as a Result variabke from matrix, which you need to
			// convert into java boolean
			Object ret = proxy.getVariable("res");
			System.out.println("ret is " + ret.getClass().getSimpleName());
			result = (double[]) ret;
			System.out.println("Matlab finished." + result);
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