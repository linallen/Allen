package pkgTestMatlab;

import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;
import matlabcontrol.MatlabProxyFactory;

public class MatlabTest2 {

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
			int[] gnd = new int[] { 2, 1, 2, 2, 2, 1, 2, 2, 2, 1, 2, 2, 1, 1, 1 };
			int[] res = new int[] { 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };
			double result[];
			// Set up the variables in MATLAB
			// sets up a matrix 'a' which is your 2D array a in Java
			proxy.setVariable("gnd", gnd);
			proxy.setVariable("res", res);
			// foo is your matlab script foo.m, lets say the expected result is
			// a boolean - true or false
			proxy.eval("res = bestMap(gnd,res);");
			proxy.eval("gnd=gnd';");
			proxy.eval("AC = length(find(gnd == res))/length(gnd);");
			proxy.eval("MIhat = MutualInfo(gnd,res);");
			// proxy.returningFeval("evaluation", 2, res, gnd);
			// By default everything in matlab is a matrix, here you get 1 x 1
			// matrix as a Result variabke from matrix, which you need to
			// convert into java boolean
			Object ObjAC = proxy.getVariable("AC");
			Object ObjMIhat = proxy.getVariable("MIhat");
			double AC = ((double[]) ObjAC)[0];
			double MIhat = ((double[]) ObjMIhat)[0];
			System.out.println("AC = " + AC + ", MIhat = " + MIhat);
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