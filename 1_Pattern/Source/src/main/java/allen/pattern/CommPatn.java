package allen.pattern;

import java.util.ArrayList;
import java.util.HashSet;

import allen.arff.Arff;
import allen.base.common.Common;
import allen.base.feature.Feature;
import allen.base.feature.FtrType;

/**
 * Common functions for pattern mining.
 * 
 * @author Allen Lin, 22 Jan 2015
 */

public class CommPatn {
	/** filter out infrequent items whose count &gt;= minSup */
	public static ArrayList<Item> filter(ArrayList<Item> items, int minSup) {
		ArrayList<Item> freqItem = new ArrayList<Item>();
		for (Item item : items) {
			if (item.count() >= minSup) {
				freqItem.add(item);
			}
		}
		return freqItem;
	}

	/** return factor: ftr_name=ftr_value */
	public static String factorStr(String ftrName, String ftrValue) throws Exception {
		if (ftrName.trim().isEmpty() || ftrValue.trim().isEmpty()) {
			throw new Exception("empty feature name or value: " + ftrName + "=" + ftrValue);
		}
		return ftrName + "=" + ftrValue;
	}

	/** factor: ftr_name=ftr_value */
	public static String getFtrName(String factor) throws Exception {
		return factor.split("=")[0];
	}

	/** factor: ftr_name=ftr_value */
	public static String getFtrValue(String factor) throws Exception {
		return factor.split("=")[1];
	}

	/** return factors of a feature (feature name + all values) */
	public static HashSet<String> getFtrFactors(String ftrName, Arff arffHdr) throws Exception {
		HashSet<String> factors = new HashSet<String>();
		Feature ftr = arffHdr.getFeature(ftrName);
		if ((ftr != null) && (ftr.type() == FtrType.CATEGORICAL)) {
			for (int j = 0; j < ftr.ftrValueNum(); j++) {
				String ftrValue = ftr.getFtrValue(j);
				factors.add(factorStr(ftr.name(), ftrValue));
			}
		}
		return factors;
	}

	/** get factors[] from user-specified factors[] and feature ARFF */
	public static String[] getFactors(String usrFactors[], Arff arffHdr) throws Exception {
		HashSet<String> factors = new HashSet<String>();
		for (String usrFactor : usrFactors) {
			String items[] = usrFactor.trim().split("=");
			if (items.length == 1) {
				// single feature name
				factors.addAll(getFtrFactors(usrFactor, arffHdr));
			} else if (items.length == 2) {
				// feature=value
				factors.add(factorStr(items[0], items[1]));
			} else {
				throw new Exception("Wrong factor format: " + Common.quote(usrFactor));
			}

		}
		return factors.toArray(new String[factors.size()]);
	}

	/** get all factors[] from feature ARFF */
	public static String[] getFactorsAll(Arff arffHdr) throws Exception {
		ArrayList<String> allFactors = new ArrayList<String>();
		for (int i = 0; i < arffHdr.ftrNum(); i++) {
			if (i != arffHdr.clsIdx()) {
				Feature ftr = arffHdr.getFeature(i);
				if ((ftr.type() == FtrType.CATEGORICAL)) {
					for (int j = 0; j < ftr.ftrValueNum(); j++) {
						String ftrValue = ftr.getFtrValue(j);
						allFactors.add(factorStr(ftr.name(), ftrValue));
					}
				}
			}
		}
		String factors[] = new String[allFactors.size()];
		factors = allFactors.toArray(factors);
		return factors;
	}
}