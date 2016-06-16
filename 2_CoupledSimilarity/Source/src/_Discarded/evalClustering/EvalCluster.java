package _Discarded.evalClustering;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * (DISCARDED)
 * 
 * Evaluating clusterings (Java version).<br>
 * Input: Clusters obj_name, class_label, cluster_name, [mode_values]<br>
 * Output: AC (Accuracy) and NMI (Normalized Mutual Information)<br>
 * Both AC and NMI are between 0 and 1.
 */
public class EvalCluster {
	/** Class set: <class_label, object_names> */
	private HashMap<String, HashSet<String>> m_classSet = new HashMap<String, HashSet<String>>();

	/** Cluster set: <cluster_name, object_names> */
	private HashMap<String, HashSet<String>> m_clusterSet = new HashMap<String, HashSet<String>>();

	/** Object-Class mapping: <object_name, class_label> */
	private HashMap<String, String> m_mapObjClass = new HashMap<String, String>();

	/** Object-Cluster mapping: <object_name, cluster_name> */
	private HashMap<String, String> m_mapObjCluster = new HashMap<String, String>();

	/** clustering CSV: obj_name, class_label, cluster_name, [mode_values] */
	public void Calc(String clusterCSV) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(clusterCSV));
		for (String line = null; (line = br.readLine()) != null;) {
			String[] items = line.split(",");
			assert (items.length >= 3);
			String objectName = items[0].trim().intern();
			String classLabel = items[1].trim().intern();
			String clusterName = items[2].trim().intern();
			// 1. class set
			addObjToSet(classLabel, objectName, m_classSet);
			// 2. cluster set
			addObjToSet(clusterName, objectName, m_clusterSet);
			// 3. <object, class>
			m_mapObjClass.put(objectName, classLabel);
			// 4. <object, cluster>
			m_mapObjCluster.put(objectName, clusterName);
		}
		br.close();
	}

	/** add object to group set <grpName, objSet[]> */
	private static void addObjToSet(String grpName, String objName, HashMap<String, HashSet<String>> grpSet) {
		HashSet<String> objGrp = grpSet.get(grpName);
		if (objGrp == null) {
			objGrp = new HashSet<String>();
			grpSet.put(grpName, objGrp);
		}
		objGrp.add(objName);
	}

	/** return Accuracy */
	public double getAC() {
		double totalPair = 0, matchPair = 0;
		// check into each cluster
		for (HashSet<String> clusterObjs : m_clusterSet.values()) {
			ArrayList<String> objLst = new ArrayList<String>(clusterObjs);
			for (int i = 0; i < objLst.size(); i++) {
				String obj1 = objLst.get(i);
				String label1 = m_mapObjClass.get(obj1);
				for (int j = i + 1; j < objLst.size(); j++) {
					String obj2 = objLst.get(j);
					String label2 = m_mapObjClass.get(obj2);
					// compare object 1 and 2's class labels
					matchPair += (label1 == label2) ? 1 : 0;
					totalPair++;
				}
			}
		}
		return matchPair / totalPair;
	}

	/** return prob() */
	private double prob(double objNum) {
		return objNum / m_mapObjClass.size();
	}

	/** return NMI (Normalized Mutual Information) */
	public double getNMI() {
		// 1. calc MI(C, C')
		double MI = 0;
		for (HashSet<String> classObjs : m_classSet.values()) {
			for (HashSet<String> clusterObjs : m_clusterSet.values()) {
				HashSet<String> jointObjs = new HashSet<String>(classObjs);
				jointObjs.retainAll(clusterObjs);
				if (!jointObjs.isEmpty()) {
					double jointProb = prob(jointObjs.size());
					MI += jointProb * Math.log(jointProb / (1. * prob(classObjs.size()) * prob(clusterObjs.size())));
				}
			}
		}
		// 2. calc H(C) and H(C')
		double HC1 = H(m_classSet);
		double HC2 = H(m_clusterSet);
		return MI / Math.max(HC1, HC2);
	}

	/** return H(C) or Entropy(C) = -sumP(c)logP(c), c \in C */
	private double H(HashMap<String, HashSet<String>> mapObjGrp) {
		double entropy = 0;
		for (HashSet<String> grp : mapObjGrp.values()) {
			entropy += -prob(grp.size()) * Math.log(prob(grp.size()));
		}
		return entropy;
	}
}