package allen.base.common;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Commonly metrics, such as NMI, Precision, Recall, DBI, etc.
 * <p>
 * Data structure:<br>
 * [metric_name(the key), metric_values[]], where metric_name is usually the
 * class name of a metric/descriptor.
 * 
 * @author Allen Lin, 12 July 2016
 */
public class Metrics {
	/** map [metric, values[]] */
	private HashMap<String, ArrayList<Double>> m_metrics = new HashMap<String, ArrayList<Double>>();

	public void put(String metricName, double metricValue) {
		ArrayList<Double> valueLst = m_metrics.get(metricName);
		if (valueLst == null) {
			valueLst = new ArrayList<Double>();
			m_metrics.put(metricName, valueLst);
		}
		valueLst.add(metricValue);
	}

	/** return average(metric) */
	public Double getAverage(String metricName) {
		ArrayList<Double> valueLst = m_metrics.get(metricName);
		if (valueLst != null) {
			Double sum = 0.0;
			for (Double value : valueLst) {
				sum += value;
			}
			return (valueLst.size() == 0) ? 0 : (sum / valueLst.size());
		}
		return null;
	}

	public String toString() {
		String buf = new String();
		for (String metric : m_metrics.keySet()) {
			buf += metric + ": ";
			for (Double value : m_metrics.get(metric)) {
				buf += value + ", ";
			}
			buf += "\n";
		}
		return buf;
	}
}