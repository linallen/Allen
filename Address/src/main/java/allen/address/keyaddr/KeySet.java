package allen.address.keyaddr;

import java.util.Collection;
import java.util.HashMap;

/** a general set for storing <key, keyClass> mappings */
public class KeySet {
	public HashMap<String, Key> m_keySet = new HashMap<String, Key>();

	public Key get(String key) {
		return m_keySet.get(key);
	}

	public int size() {
		return m_keySet.size();
	}

	public Collection<String> getKeys() {
		return m_keySet.keySet();
	}

	public Object add(String key, Class<?> keyClass) throws Exception {
		key = key.intern();
		Key obj = get(key);
		if (obj == null) {
			obj = (Key) keyClass.newInstance();
			obj.setKey(key);
			m_keySet.put(key, obj);
		}
		return obj;
	}

	public String toString() {
		String buf = new String();
		for (String key : m_keySet.keySet()) {
			buf += get(key).toString() + "\n";
		}
		return buf;
	}
}