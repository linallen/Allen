package allen.base.table;

import java.util.ArrayList;
import java.util.Collections;

/**
 * A Key-List Table = [key1, key2, ..., value], where value is indexed by String
 * key=[key1.hashCode_key2.hashCode_...] and keys are ordered by their
 * hashCodes. It can be used for implementing Symmetric Matrixes.
 * 
 * @author Allen Lin, 23 June 2016
 */
public class TableKeyLst extends TableBase {
	private static final long serialVersionUID = -1994758298534323757L;

	/** @return keyStr = _key1_key2..., where keys are ordered by hashCode. */
	protected String getKeyStr(Object... keys) {
		ArrayList<Integer> keyCodes = getKeyCodes(keys);
		Collections.sort(keyCodes);
		return getKeyStr(keyCodes);
	}

	public static void main(String[] args) throws Exception {
		TableKeyLst bt = new TableKeyLst();
		bt.put("Obj1", "this", "is", "Allen");
		bt.put("Obj2", "Hello", "World");
		System.out.println(bt.toString());
	}
}