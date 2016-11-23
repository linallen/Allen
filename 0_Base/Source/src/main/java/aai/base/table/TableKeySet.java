package allen.base.table;

import java.util.ArrayList;

/**
 * A Key-Set Table = [key1, key2, ..., value], where value is indexed by String
 * key=[key1.hashCode_key2.hashCode_...].
 * 
 * @author Allen Lin, 23 June 2016
 */
public class TableKeySet extends TableBase {
	private static final long serialVersionUID = 5221118660646828924L;

	/** @return keyStr = _key1_key2... */
	protected String getKeyStr(Object... keys) {
		ArrayList<Integer> keyCodes = getKeyCodes(keys);
		return getKeyStr(keyCodes);
	}

	public static void main(String[] args) throws Exception {
		TableKeySet bt = new TableKeySet();
		bt.put("Obj1", "this", "is", "Allen");
		bt.put("Obj2", "Hello", "World");
		System.out.println(bt.toString());
	}
}