package aai.base.module;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.Attributes;

/**
 * Module class who could be loaded from file, both local and remote.
 * 
 * @author Allen Lin, 3 Dec 2014
 */
public class ModuleLoader extends URLClassLoader {
	/** module file */
	public String m_moduleFile;

	/** URL of module */
	// private URL m_moduleURL;

	/**
	 * main class name.<br>
	 * "" means moduleFile is a jar file but has no main class,<br>
	 * null means moduleFile is not a jar file.
	 */
	public String m_mainClassName;

	/** main class */
	public Class<?> m_mainClass;

	/** internal module object */
	public Object m_module;

	/** main method of main class */
	public Method m_mainMethod;

	/** constructor with URL */
	public ModuleLoader(URL moduleURL) throws Throwable {
		super(new URL[] { moduleURL });
		// m_moduleURL = moduleURL;
		load();
	}

	/** constructor with file name */
	public ModuleLoader(String moduleFile) throws Throwable {
		super(new URL[] { new URL("file:" + moduleFile) });
		m_moduleFile = moduleFile;
		// m_moduleURL = new URL("jar", "", new URL("file:" + moduleFile) +
		// "!/");
		load();
	}

	/** load module from file. */
	private void load() {
		try {
			// 1. get main class name
			m_mainClassName = getMainClassName(m_moduleFile);
			// 2. get main class
			m_mainClass = loadClass(m_mainClassName);
			if (m_mainClass != null) {
				m_module = m_mainClass.newInstance();
			}
			// 3. get main() method of main class
			m_mainMethod = m_mainClass.getMethod("main", String[].class);
			m_mainMethod.setAccessible(true);
			int mods = m_mainMethod.getModifiers();
			if ((m_mainMethod.getReturnType() != void.class) || !Modifier.isStatic(mods) || !Modifier.isPublic(mods)) {
				m_mainMethod = null;
			}
		} catch (Exception e) {
			return;
		}
	}

	/**
	 * Returns the name of the jar file main class.
	 * 
	 * @return name of main class, or "" meaning is a jar file but no main
	 *         class, or null meaning not a jar file.
	 */
	public static String getMainClassName(URL moduleURL) {
		try {
			JarURLConnection uc = (JarURLConnection) moduleURL.openConnection();
			Attributes attr = uc.getMainAttributes();
			if (attr != null) {
				String value = attr.getValue(Attributes.Name.MAIN_CLASS);
				if (value != null) {
					return value;
				}
			}
			return new String();
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Returns the name of the jar file main class.
	 * 
	 * @return name of main class, or "" meaning is a jar file but no main
	 *         class, or null meaning not a jar file.
	 */
	public static String getMainClassName(String moduleFile) {
		try {
			URL url = new URL("jar", "", new URL("file:" + moduleFile) + "!/");
			return getMainClassName(url);
		} catch (Exception e) {
			return null;
		}
	}

	/** load module from file. */
	@SuppressWarnings("resource")
	public static Class<?> getMainClass(String moduleFile) {
		try {
			ModuleLoader moduleLoader;
			try {
				moduleLoader = new ModuleLoader(moduleFile);
			} catch (Throwable e) {
				return null;
			}
			return moduleLoader.loadClass(getMainClassName(moduleFile));
		} catch (Exception e) {
			return null;
		}
	}

	/** get main() method of main class */
	public static Method getMainMethod(String moduleFile) {
		try {
			// get main class
			Class<?> mainClass = ModuleLoader.getMainClass(moduleFile);
			// get main() method of main class
			Method mainMethod = mainClass.getMethod("main", String[].class);
			mainMethod.setAccessible(true);
			int mods = mainMethod.getModifiers();
			if ((mainMethod.getReturnType() == void.class) && Modifier.isStatic(mods) && Modifier.isPublic(mods)) {
				return mainMethod;
			}
			return null;
		} catch (Exception e) {
			return null;
		}
	}

	/** get main() method of main class */
	public static Method getMainMethod(Class<?> mainClass) {
		try {
			Method mainMethod = mainClass.getMethod("main", String[].class);
			mainMethod.setAccessible(true);
			int mods = mainMethod.getModifiers();
			if ((mainMethod.getReturnType() == void.class) && Modifier.isStatic(mods) && Modifier.isPublic(mods)) {
				return mainMethod;
			}
			return null;
		} catch (Exception e) {
			return null;
		}
	}

	/** load module from file. */
	public static boolean hasMainMethod(String moduleFile) {
		try {
			// get main class
			Class<?> mainClass = ModuleLoader.getMainClass(moduleFile);
			// get main() method of main class
			Method mainMethod = mainClass.getMethod("main", String[].class);
			mainMethod.setAccessible(true);
			int mods = mainMethod.getModifiers();
			return ((mainMethod.getReturnType() == void.class) && Modifier.isStatic(mods) && Modifier.isPublic(mods));
		} catch (Exception e) {
			return false;
		}
	}

	/** invoke a public static method of a class */
	public static Object invoke(Class<?> cls, String methodName, Object... args) throws Exception {
		// 1. get method object from class
		String methodStr = new String();
		Class<?>[] argTypes = new Class[args.length];
		for (int i = 0; i < args.length; i++) {
			argTypes[i] = args[i].getClass();
			methodStr += ((i == 0) ? "" : ", ") + argTypes[i].getSimpleName();
		}
		methodStr = cls.getName() + "." + methodName + "(" + methodStr + ")";
		Method method = cls.getMethod(methodName, argTypes);
		// 2. make sure method is "public static"
		method.setAccessible(true);
		int mods = method.getModifiers();
		if (!Modifier.isPublic(mods)) {
			throw new Exception(methodStr + " is not public.");
		}
		if (!Modifier.isStatic(mods)) {
			throw new Exception(methodStr + " is not static.");
		}
		// return method.invoke(null, new Object[] { args });
		return method.invoke(null, args);
	}

	public static Class<?> getClass(String clsName) {
		try {
			return Class.forName(clsName);
		} catch (Exception e) {
			return null;
		}
	}

	/** create object from class name */
	public static Object newInstance(String clsName) throws Exception {
		try {
			return newInstance(Class.forName(clsName));
		} catch (Exception e) {
			return null;
		}
	}

	/** create object from class name */
	public static Object newInstance(Class<?> cls) throws Exception {
		try {
			return cls.newInstance();
		} catch (Exception e) {
			return null;
		}
	}
}