package allen.address;

import java.util.HashMap;

/** a general set for storing <key, object> mappings */
public class KeySet {
	public HashMap<String, Object> m_keySet = new HashMap<String, Object>();

	/** add an object to the set */
	public void add(String key, Object obj) {
		m_keySet.put(key, obj);
	}

	public boolean exist(String key) {
		return m_keySet.containsKey(key);
	}

	public int size() {
		return m_keySet.size();
	}

	public Object get(String key) {
		return m_keySet.get(key);
	}

	public String toString() {
		String buf = new String();
		for (String key : m_keySet.keySet()) {
			buf += key + ", " + get(key).toString() + "\n";
		}
		return buf;
	}
}