package allen.matlab;

import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;
import matlabcontrol.MatlabProxyFactory;

public class Matlab {
	private static MatlabProxy s_proxy = null;
	private static int s_clientNum = 0;

	public static MatlabProxy getProxy() {
		return getProxy(null);
	}

	public static MatlabProxy getProxy(String scriptPath) {
		try {
			if (s_proxy == null) {
				s_proxy = new MatlabProxyFactory().getProxy();
			}
			if (scriptPath != null) {
				s_proxy.eval("cd " + scriptPath);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		s_clientNum++;
		return s_proxy;
	}

	public static void disconnect() {
		if ((--s_clientNum == 0) && (s_proxy != null)) {
			try {
				s_proxy.exit();
			} catch (MatlabInvocationException e1) {
				// logging, handling
			}
			s_proxy.disconnect();
		}
	}

	public static void main(String[] args) throws Exception {
		MatlabProxy proxy = Matlab.getProxy("C:/Allen");
		System.out.println(proxy.toString());
		Matlab.disconnect();
	}
}