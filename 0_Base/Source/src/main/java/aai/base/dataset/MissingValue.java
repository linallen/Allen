package aai.base.dataset;

import java.util.HashMap;

/**
 * Missing value handling class: replace missing values with the most frequent
 * values.
 * 
 * @author Allen Lin, 29 June 2016
 */
public class MissingValue {
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
		// 3. get the most frequent value for each feature [ftr, freq_value]
		HashMap<Feature, Value> mapFtrFreqValue = new HashMap<Feature, Value>();
		for (Value val1 : mapValueCount.keySet()) {
			boolean freq = true; // assume val1 is the most frequent
			for (Value val2 : mapValueCount.keySet()) {
				if (val1.getFtr() == val2.getFtr()) {
					Integer count1 = mapValueCount.get(val1);
					Integer count2 = mapValueCount.get(val2);
					if (count1 < count2) {
						freq = false;
						break;
					}
				}
			}
			if (freq && (mapFtrFreqValue.get(val1.getFtr()) == null)) {
				mapFtrFreqValue.put(val1.getFtr(), val1);
			}
		}

		// 4. replace missing (Categorical) values with the most frequent values
		boolean replaced = false;
		for (Obj obj : dataSet.getObjs()) {
			for (Feature ftr : dataSet.getFtrLst(FtrType.CATEGORICAL)) {
				if (Value.isMissing(obj.getValue(ftr))) {
					obj.setValue(ftr, mapFtrFreqValue.get(ftr));
					replaced = true;
				}
			}
		}
		if (replaced) {
			System.out.println(dataSet.dataName() + " : missing values have been replaced!");
		}
	}
}