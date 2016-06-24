package allen.clusterer.eval;

import java.util.Collection;
import java.util.HashMap;

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

	/** [metric_name, metric_value] */
	private HashMap<String, Double> m_metrics = new HashMap<String, Double>();

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

	private Collection<String> getMetricNames() {
		return m_metrics.keySet();
	}

	private void setMetric(String metricName, Double metricValue) {
		m_metrics.put(metricName, metricValue);
	}

	private Double getValue(String metricName) {
		return m_metrics.get(metricName);
	}

	private void addMetric(String metricName, Double metricValue) {
		if (metricValue != null) {
			Double orgValue = m_metrics.get(metricName);
			metricValue += ((orgValue != null) ? orgValue : 0);
			m_metrics.put(metricName, metricValue);
		}
	}

	public void addMetrics(Metrics metrics) {
		for (String metricName : metrics.getMetricNames()) {
			addMetric(metricName, metrics.getValue(metricName));
		}
	}

	public void divide(double divisor) {
		for (String metricName : getMetricNames()) {
			setMetric(metricName, getValue(metricName) / divisor);
		}
	}

	/**
	 * evaluate the clusterer by metrics: NMI, Precision, and Recall, etc.
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
			metric.setMetric("NMI_spec", ((double[]) proxy.getVariable("NMI_spec"))[0]);
			metric.setMetric("precision", ((double[]) proxy.getVariable("precision"))[0]);
			metric.setMetric("recall", ((double[]) proxy.getVariable("recall"))[0]);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return metric;
	}

	public String toString() {
		String buf = new String();
		for (String metricName : getMetricNames()) {
			try {
				buf += (metricName + "=" + Common.decimal(getValue(metricName)) + " ");
			} catch (Exception e) {
			}
		}
		return buf;
	}

	public String toCSV() {
		String buf = new String();
		for (String metricName : getMetricNames()) {
			try {
				buf += Common.decimal(getValue(metricName)) + "，";
			} catch (Exception e) {
			}
		}
		return buf;
	}
}