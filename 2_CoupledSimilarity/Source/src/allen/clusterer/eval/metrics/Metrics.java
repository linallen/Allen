package allen.clusterer.eval.metrics;

import allen.base.common.Common;
import allen.matlab.Matlab;
import matlabcontrol.MatlabProxy;

/**
 * Evaluating Clusterer' Performance with some common metrics including NMI,
 * Precision, Recall, etc. So far this class depends on Matlab functions NIM()
 * and TFPN().
 * 
 * @author Allen Lin, 22 June 2016
 */
public class Metrics {
	private static String s_matlabDir;

	/** metrics */
	public Double m_NMI, m_prec, m_rec;

	public static void setMatlabDir(String matlabDir) {
		s_matlabDir = matlabDir;
	}

	private static String getMatlabArray(int[] array) {
		String buf = "[";
		for (int i = 0; i < array.length; i++) {
			buf += ((i == 0) ? "" : ";") + array[i];
		}
		return buf + "]";
	}

	/**
	 * TODO evaluate the clusterer by metrics: NMI, Precision, and Recall, etc.
	 * 
	 * @param labels
	 *            class labels[]
	 * @param flags
	 *            cluster ids[] produces by the clusterer
	 * @param s_matlabDir
	 *            absolute directory of matlab functions
	 * @return
	 * @return metrics: NMI, Precision, and Recall, etc
	 */
	public static Metrics getMetrics(int[] labels, int[] flags) throws Exception {
		Metrics metric = new Metrics();
		try {
			MatlabProxy proxy = Matlab.getProxy();
			proxy.eval("cd " + s_matlabDir);
			// proxy.setVariable("flag_spec", 5.0);
			// proxy.setVariable("label", 5.0);
			proxy.eval("label=" + getMatlabArray(labels) + ";");
			proxy.eval("flag_spec=" + getMatlabArray(flags) + ";");
			proxy.eval("NMI_spec = NMI(flag_spec',label');");
			proxy.eval("[precision, recall, ri, fscore] = TFPN(flag_spec,label');");
			// get metrics NMI, prec
			metric.m_NMI = ((double[]) proxy.getVariable("NMI_spec"))[0];
			metric.m_prec = ((double[]) proxy.getVariable("precision"))[0];
			metric.m_rec = ((double[]) proxy.getVariable("recall"))[0];
		} catch (Exception e) {
			e.printStackTrace();
		}
		return metric;
	}

	public String toString() {
		String buf = new String();
		try {
			buf += ((m_NMI != null) ? ("NMI=" + Common.decimal(m_NMI)) : "") + " ";
			buf += ((m_prec != null) ? ("Prec=" + Common.decimal(m_prec)) : "") + " ";
			buf += ((m_rec != null) ? ("Recall=" + Common.decimal(m_rec)) : "") + " ";
		} catch (Exception e) {
			e.printStackTrace();
		}
		return buf;
	}
}