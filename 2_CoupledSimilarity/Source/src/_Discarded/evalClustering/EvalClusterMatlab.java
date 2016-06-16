package _Discarded.evalClustering;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;
import matlabcontrol.MatlabProxyFactory;

/**
 * Evaluating clusterings (Matlab version).<br>
 * Input: Clusters obj_name, class_label, cluster_name, [mode_values]<br>
 * Output: AC (Accuracy) and NMI (Normalized Mutual Information)<br>
 * Both AC and NMI are between 0 and 1.
 */
public class EvalClusterMatlab {
	private MatlabProxy m_proxy;
	private double m_ACC, m_NMI;

	private static int[] getIds(ArrayList<String> nameLst) {
		HashMap<String, Integer> mapNameId = new HashMap<String, Integer>();
		Integer id = 0;
		int ids[] = new int[nameLst.size()];
		for (int i = 0; i < nameLst.size(); i++) {
			String name = nameLst.get(i);
			Integer nameId = mapNameId.get(name);
			if (nameId == null) {
				nameId = id++;
				mapNameId.put(name, nameId);
			}
			ids[i] = nameId;
		}
		return ids;
	}

	public EvalClusterMatlab(MatlabProxy proxy) throws Exception {
		m_proxy = (proxy == null) ? new MatlabProxyFactory().getProxy() : proxy;
	}

	/** clustering CSV: obj_name, class_label, cluster_name, [mode_values] */
	public void calcClusterMetrics(String clusterCSV, String matlabDir) throws Exception {
		ArrayList<String> labelLst = new ArrayList<String>();
		ArrayList<String> clusterLst = new ArrayList<String>();
		BufferedReader br = new BufferedReader(new FileReader(clusterCSV));
		for (String line = null; (line = br.readLine()) != null;) {
			String[] items = line.split(",");
			assert (items.length >= 3);
			// String objectName = items[0].trim().intern();
			String label = items[1].trim().intern();
			String cluster = items[2].trim().intern();
			assert (!label.isEmpty() && !cluster.isEmpty());
			labelLst.add(label);
			clusterLst.add(cluster);
		}
		br.close();
		// compute AC and NMI by calling matlab code
		int[] gnd = getIds(labelLst);
		int[] res = getIds(clusterLst);
		calcClusterMetrics(gnd, res, matlabDir);
	}

	private void calcClusterMetrics(int[] gnd, int[] res, String matlabDir)
			throws MatlabInvocationException, MatlabConnectionException {
		try {
			m_proxy.eval("cd " + matlabDir);
			m_proxy.setVariable("gnd", gnd);
			m_proxy.setVariable("res", res);
			m_proxy.eval("res = bestMap(gnd,res);");
			m_proxy.eval("gnd = gnd';");
			m_proxy.eval("AC = length(find(gnd == res))/length(gnd);");
			m_proxy.eval("MIhat = MutualInfo(gnd,res);");
			Object ObjAC = m_proxy.getVariable("AC");
			Object ObjMIhat = m_proxy.getVariable("MIhat");
			m_ACC = ((double[]) ObjAC)[0];
			m_NMI = ((double[]) ObjMIhat)[0];
			System.out.println("AC = " + m_ACC + ", NMI = " + m_NMI);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** return Accuracy */
	public double getAC() {
		return m_ACC;
	}

	/** return NMI (Normalized Mutual Information) */
	public double getNMI() {
		return m_NMI;
	}
}