package allen.base.common;

import java.util.HashMap;

/**
 * global class manager.
 * 
 * @author Allen Lin, 22 June 2016
 */
public class ClassManager {
	/** global registered objects[] */
	private HashMap<String, Class<?>> s_classes = new HashMap<String, Class<?>>();

	public boolean registered(String clsName) {
		return s_classes.get(clsName) != null;
	}

	public boolean register(String clsName, Class<?> clsClass) {
		if (!registered(clsName)) {
			s_classes.put(clsName, clsClass);
			return true;
		}
		return false;
	}

	public Object getInstance(String clsName) throws Exception {
		Class<?> clsClass = s_classes.get(clsName);
		return clsClass.newInstance();
	}

	public static void main(String[] args) {
	}
}
