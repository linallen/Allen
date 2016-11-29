package aai.base.dataset;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Missing value handling class: replace missing values with the most frequent
 * values.
 * 
 * @author Allen Lin, 29 June 2016
 */
public class MissingValue_old {
	/** replace missing (Categorical) values with the most frequent values. */
	public static void replaceWithFreq(DataSet dataSet) {
		// 1. build value_count [value, count]
		HashMap<Value, Integer> mapValueCount = new HashMap<Value, Integer>();
		for (Obj obj : dataSet.getObjs()) {
			for (Value value : obj.getValues()) {
				if (value.getFtr().type() == FtrType.CATEGORICAL) {
					Integer count = mapValueCount.get(value);
					mapValueCount.put(value, (count == null) ? 1 : (count + 1));
				}
			}
		}
		// 2. filter out most frequent values from value_count [value, count]
		HashSet<Value> inFreqValues = new HashSet<Value>();
		for (Value val1 : mapValueCount.keySet()) {
			for (Value val2 : mapValueCount.keySet()) {
				if (val1.getFtr() == val2.getFtr()) {
					Integer count1 = mapValueCount.get(val1);
					Integer count2 = mapValueCount.get(val2);
					if (count1 <= count2) {
						inFreqValues.add(val1);
					} else {
						inFreqValues.add(val2);
					}
				}
			}
		}
		// 3. get the most frequent value for each feature
		HashSet<Value> freqValues = new HashSet<Value>(mapValueCount.keySet());
		freqValues.removeAll(inFreqValues);
		HashMap<Feature, Value> mapFtrFreqValue = new HashMap<Feature, Value>();
		for (Value value : freqValues) {
			mapFtrFreqValue.put(value.getFtr(), value);
		}

		// 4. replace missing (Categorical) values with the most frequent values
		for (Obj obj : dataSet.getObjs()) {
			for (Feature ftr : dataSet.getFtrLst(FtrType.CATEGORICAL)) {
				if (Value.isMissing(obj.getValue(ftr))) {
					obj.setValue(ftr, mapFtrFreqValue.get(ftr));
				}
			}
		}
	}
}