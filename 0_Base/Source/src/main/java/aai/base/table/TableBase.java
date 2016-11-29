package aai.base.table;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * A Table = [key1, key2, ..., value], where value is indexed by String
 * key=[key1.hashCode_key2.hashCode_...].
 * <p>
 * Hash Code is supposed to be unique for objects.
 * 
 * @author Allen Lin, 23 June 2016
 */
public abstract class TableBase implements Serializable {
	private static final long serialVersionUID = -6880293150945249464L;

	/** A Table = [Object keys[], Object value] ] */
	private HashMap<String, Object> m_table = new HashMap<String, Object>();

	/** property functions ***************************************/
	public int size() {
		return m_table.size();
	}

	public boolean isEmpty() {
		return m_table.isEmpty();
	}

	public void clear() {
		m_table.clear();
	}

	/** main functions ***************************************/
	/** @return keys' hashCodes[] */
	protected final static ArrayList<Integer> getKeyCodes(Object... keys) {
		ArrayList<Integer> keyCodes = new ArrayList<Integer>();
		for (Object key : keys) {
			keyCodes.add(key.hashCode());
		}
		return keyCodes;
	}

	/** @return key string from keys' hashCodes[] */
	protected final static String getKeyStr(ArrayList<Integer> keyCodes) {
		StringBuffer keyStr = new StringBuffer();
		for (Object keyCode : keyCodes) {
			keyStr.append("_").append(keyCode);
		}
		return keyStr.toString();
	}

	/**
	 * define this function to implement different structures.
	 * 
	 * @return keyStr = _key1_key2...<br>
	 */
	protected abstract String getKeyStr(Object... keys);

	/** put in [keys[], value] */
	public final void put(Object obj, Object... keys) {
		m_table.put(getKeyStr(keys), obj);
	}

	/** remove value by keys[] */
	public final Object remove(Object... keys) {
		return m_table.remove(getKeyStr(keys));
	}

	/** get value by keys[] */
	public final Object get(Object... keys) {
		return m_table.get(getKeyStr(keys));
	}

	/** output functions ***************************************/
	public String toString() {
		StringBuffer buf = new StringBuffer();
		for (String key : m_table.keySet()) {
			buf.append("[").append(key).append(" : ").append(m_table.get(key)).append("]\n");
		}
		return buf.toString();
	}
}