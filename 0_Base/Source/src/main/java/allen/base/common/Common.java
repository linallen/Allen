package allen.base.common;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * Common functions.
 * 
 * @author Allen Lin, 11 Sep 2014
 */
public class Common {

	/** string to number (Integer or Double) */
	public static Object toNumber(String valueStr) throws Exception {
		try {
			return Integer.parseInt(valueStr);
		} catch (Exception e1) {
			try {
				return Double.parseDouble(valueStr);
			} catch (Exception e2) {
				throw new Exception("Not a number: " + valueStr);
			}
		}
	}

	/**
	 * added on 20 May 2015.<br>
	 * TODO: need to remove this function.
	 * 
	 * @param str1
	 * @param separator
	 * @param str2
	 * @return str1 + separator + str2.
	 */
	public static String append(String str1, String separator, String str2) {
		str1 = isValid(str1) ? str1 : new String();
		return str1.concat((str1.isEmpty() ? "" : separator) + str2);
	}

	/**
	 * combine two arrays.
	 * 
	 * @param array1[]
	 * @param array2[]
	 * @return array1[] + array2[]
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] ArrayCat(T[] array1, T[] array2) {
		List<T> ret = new ArrayList<T>(array1.length + array2.length);
		Collections.addAll(ret, array1);
		Collections.addAll(ret, array2);
		return (T[]) ret.toArray();
	}

	/**
	 * get intersection of two lists.
	 * 
	 * @param list1[]
	 * @param list2[]
	 * @return list1[] + list2[]
	 */
	public static <T> List<T> intersection(List<T> list1, List<T> list2) {
		List<T> list = new ArrayList<T>();
		for (T t : list1) {
			if (list2.contains(t)) {
				list.add(t);
			}
		}
		return list;
	}

	/**
	 * check if a value is between two values (inclusive).
	 * 
	 * @param value
	 * @param min
	 * @param max
	 * @return list1[] + list2[]
	 */
	public static boolean between(double value, double min, double max) {
		return between(value, min, true, max, true);
	}

	/**
	 * check if a value is between two values.
	 * 
	 * @param value
	 * @param min
	 * @param max
	 * @return list1[] + list2[]
	 */
	public static boolean between(double value, double min, boolean include_min, double max, boolean include_max) {
		boolean above_min = (include_min ? (value >= min) : (value > min));
		boolean below_max = (include_max ? (value <= max) : (value < max));
		return above_min && below_max;
	}

	/**
	 * return percentage format of value.
	 * 
	 * @param value
	 * @param digits
	 * @return value * 100% with digits
	 */
	public static String percent(double value, int digits) {
		return String.format("%." + digits + "f", 100. * value) + "%";
	}

	/**
	 * Standardize string from That's fine.to 'That\'s fine.'
	 * 
	 * @param str
	 * @return standardized string
	 */
	public static String stdString(String str) {
		str = str.replaceAll("''", "\\''");
		if (str.contains(" ")) {
			str = "\'" + str + "\'";
		}
		return str;
	}

	/**
	 * calculate factorial(n).
	 * 
	 * @param n
	 * @return n!
	 */
	public static double factorial(long n) throws Exception {
		if (n < 0) {
			throw new Exception("n = " + n + ": invalid parameter.");
		}
		if (n == 0) {
			return 1;
		}
		double factorial = 1;
		for (int i = 1; i <= n; i++) {
			factorial *= i;
		}
		return factorial;
	}

	/**
	 * return n!/(m!*(n-m)!)
	 * 
	 * @param n
	 * @param m
	 * @return C(n,m)
	 */
	public static double C(long n, long m) throws Exception {
		if ((m > n) || (n <= 0) || (m <= 0)) {
			return 0;
		}
		return factorial(n) / (factorial(m) * factorial(n - m));
	}

	/**
	 * return execution point of thread.
	 * 
	 * @param thread
	 * @return execution point
	 */
	public static String execPoint(Thread thread) {
		try {
			StackTraceElement s = thread.getStackTrace()[1];
			return s.getFileName() + ": " + s.getClassName() + "." + s.getMethodName() + "() line: "
					+ s.getLineNumber();
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * return progress(%).
	 * 
	 * @param finished
	 * @param total
	 * @return progress(%)
	 */
	public static Double progress(double finished, double total) {
		return ((100. * Math.max(1., finished / total)));
	}

	/**
	 * return progress.
	 * 
	 * @param finished
	 * @param total
	 * @return progress
	 */
	public static int progress(long finished, long total) {
		return (int) ((100. * Math.max(1., finished / total)));
	}

	/**
	 * return ip:port.
	 * 
	 * @param socket
	 * @return ip:port.
	 */

	public static String IpAddrPort(Socket socket) {
		try {
			return socket.getInetAddress() + ":" + socket.getPort();
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * return exception description.
	 * 
	 * @param e
	 *            exception
	 * @return exception description
	 */
	public static String exception(Exception e) {
		return "Exception: " + e.getMessage() + ", Cause: " + e.getCause();
	}

	/**
	 * return call stack of an exception.
	 * 
	 * @param e
	 *            exception
	 * @return exception call stack
	 */
	public static String callStack(Exception e) {
		return "Call Stack: " + Arrays.toString(e.getStackTrace());
	}

	/**
	 * deep copy of an object.
	 * 
	 * @param srcObj
	 * @return deep copied object
	 */
	public static Object deepCopy(Object srcObj) throws Exception {
		ObjectOutputStream oos = null;
		ObjectInputStream ois = null;
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream(); // A
			oos = new ObjectOutputStream(bos); // B
			// serialize and pass the object
			oos.writeObject(srcObj); // C
			oos.flush(); // D
			ByteArrayInputStream bin = new ByteArrayInputStream(bos.toByteArray()); // E
			ois = new ObjectInputStream(bin); // F
			// return the new object
			return ois.readObject(); // G
		} catch (Exception e) {
			System.out.println("Exception in ObjectCloner = " + e);
			throw (e);
		} finally {
			oos.close();
			ois.close();
		}
	}

	/**
	 * get string by combining strs[]. TODO discard
	 * 
	 * @param strs[]
	 * @return combined strs[]
	 */
	public static String getString(String[] strs) {
		String buf = new String();
		for (int i = 0; i < strs.length; i++) {
			buf += ((i == 0) ? "" : " ") + strs[i];
		}
		return buf;
	}

	/**
	 * get string by combining strs[].
	 * 
	 * @param strs[]
	 * @param delimiter
	 * @return combined strs[]
	 */
	public static String getString(String[] strs, String delimiter) {
		String buf = new String();
		for (int i = 0; i < strs.length; i++) {
			buf += ((i == 0) ? "" : delimiter) + strs[i];
		}
		return buf;
	}

	/**
	 * execute shell command and return output text.<br>
	 * http://www.mkyong.com/java/how-to-execute-shell-command-from-java/
	 * 
	 * @param command
	 * @return screen output of the command
	 */
	public static String execShellCmd(String command) throws Exception {
		Process p = Runtime.getRuntime().exec(command);
		p.waitFor();
		BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String output = new String(), line;
		while ((line = reader.readLine()) != null) {
			output += line + "\n";
		}
		return output;
	}

	/**
	 * calculate intersection of two ascending-ordered Integer sets A and B.
	 * 
	 * @param setA
	 * @param setB
	 * @return intersection(A,B)
	 */
	public static ArrayList<Integer> intersection(ArrayList<Integer> setA, ArrayList<Integer> setB) {
		ArrayList<Integer> setCommon = new ArrayList<Integer>();
		for (int i = 0, j = 0; (i < setA.size()) && (j < setB.size());) {
			int id_a = setA.get(i);
			int id_b = setB.get(j);
			if (id_a < id_b) {
				i++; // push a forward
			} else if (id_a > id_b) {
				j++; // push b forward
			} else {
				i++;
				j++; // found a common number between A and B
				setCommon.add(id_a);
			}
		}
		return setCommon;
	}

	/**
	 * calculate SST(numbers).
	 * 
	 * @param numbers[]
	 * @return SST(numbers[])
	 */
	public static double calSST(ArrayList<Double> numbers) {
		double sum = 0., mean = 0., sst = 0.;
		for (Double number : numbers) {
			sum += number;
		}
		mean = sum / numbers.size();
		for (Double number : numbers) {
			sst += (number - mean) * (number - mean);
		}
		return Math.sqrt(sst) / numbers.size();
	}

	/**
	 * check if an object is a number.
	 * 
	 * @param obj
	 * @return
	 */
	public static boolean isNumeric(Object obj) {
		return isNumeric(obj.toString());
	}

	/**
	 * check if a string is a number.
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isNumeric(String str) {
		return str.matches("[+-]?\\d*(\\.\\d+)?");
		/*
		 * try { Double.parseDouble(str); } catch (NumberFormatException nfe) {
		 * return false; } return true;
		 */
	}

	/**
	 * check if a string is a integer.
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isInteger(String str) {
		try {
			Integer.parseInt(str);
		} catch (NumberFormatException e) {
			return false;
		}
		// only got here if we didn't return false
		return true;
	}

	/**
	 * check if a string is a double.
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isDouble(String str) {
		try {
			Double.parseDouble(str);
		} catch (NumberFormatException e) {
			return false;
		}
		// only got here if we didn't return false
		return true;
	}

	/**
	 * convert a string to a double.
	 * 
	 * @param str
	 * @return double
	 */
	public static Double getNumber(String str) {
		return Double.parseDouble(str);
	}

	/**
	 * [DEBUG] throw an exception with given text.
	 * 
	 * @param dbgInfo
	 */
	public static void dbgThrowException(String dbgInfo) {
		try {
			throw new Exception("Exception: " + dbgInfo);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * return the text key for a given item set.
	 * 
	 * @param items[]
	 * @return
	 */
	public static String getKey(String[] items) {
		Arrays.sort(items);
		String patnKey = new String();
		for (String item : items) {
			patnKey += item + ";";
		}
		return patnKey;
	}

	/**
	 * @param statement
	 */
	public static void Assert(boolean statement) {
		if (statement == false) {
			try {
				throw new Exception("Assertation!");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @param statement
	 * @param ErrMsg
	 */
	public static void Assert(boolean statement, String ErrMsg) {
		if (statement == false) {
			try {
				throw new Exception(ErrMsg);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static Double Max(ArrayList<Double> numbers) {
		Double max = Double.NEGATIVE_INFINITY;
		for (Double number : numbers) {
			max = Math.max(max, number);
		}
		return max;
	}

	public static Double Min(ArrayList<Double> numbers) {
		Double min = Double.POSITIVE_INFINITY;
		for (Double number : numbers) {
			min = Math.min(min, number);
		}
		return min;
	}

	// public static void exception(Exception e) {
	// System.out.println("Exception throw: " + e);
	// e.printStackTrace();
	// }

	public static boolean validString(String str) {
		return ((str != null) && !str.isEmpty());
	}

	/** return the number of distinct numbers */
	public static int countDist(ArrayList<Double> numbers) {
		HashSet<Double> set = new HashSet<Double>();
		for (Double number : numbers) {
			set.add(number);
		}
		return set.size();
	}

	public static boolean isMissing(String str) {
		return str.equals(".") || str.equals("?");
	}

	/** get the value of a specified option from options[] */
	public static String getOption(String opt, String[] options) {
		opt = !opt.startsWith("-") ? ("-" + opt) : opt;
		for (int i = 0; i < options.length; i++) {
			if (opt.equals(options[i])) {
				options[i] = "";
				if (i < options.length - 1) {
					String value = options[i + 1];
					options[i + 1] = "";
					return value;
				}
				return new String();
			}
		}
		return new String();
	}

	/** get String value of a specified option from options[] */
	public static String getOptionString(String opt, String[] options, String defVal) {
		String val = Common.getOption(opt, options);
		return notNullEmpty(val) ? val : defVal;
	}

	/** TODO: DELETE get integer value of a specified option from options[] */
	public static int getOptionInt(String opt, String[] options, int defVal) {
		String val = Common.getOption(opt, options);
		return notNullEmpty(val) ? Integer.parseInt(val) : defVal;
	}

	/** get integer value of a specified option from options[] */
	public static int getOptionInteger(String opt, String[] options, int defVal) {
		String val = Common.getOption(opt, options);
		// return notNullEmpty(val) ? Integer.parseInt(val) : defVal;
		if (notNullEmpty(val)) {
			return Integer.parseInt(val);
		} else {
			return defVal;
		}
	}

	/** get integer value of a specified option from options[] */
	public static Integer getOptionInteger(String opt, String[] options, Integer defVal) {
		String val = Common.getOption(opt, options);
		// return notNullEmpty(val) ? Integer.parseInt(val) : defVal;
		if (notNullEmpty(val)) {
			return Integer.parseInt(val);
		} else {
			return defVal;
		}
	}

	/** get double value of a specified option from options[] */
	public static double getOptionDouble(String opt, String[] options, double defVal) {
		String val = Common.getOption(opt, options);
		return notNullEmpty(val) ? Double.parseDouble(val) : defVal;
	}

	/** check if an option exists in options[] */
	public static boolean getOptionBool(String opt, String[] options) {
		opt = !opt.startsWith("-") ? ("-" + opt) : opt;
		for (int i = 0; i < options.length; i++) {
			if (opt.equals(options[i])) {
				options[i] = "";
				return true;
			}
		}
		return false;
	}

	// TODO: DELETE
	public static ArrayList<String> removeDups(ArrayList<String> words) {
		HashSet<String> distWords = new HashSet<String>(words);
		return new ArrayList<String>(distWords);
	}

	public static ArrayList<String> distinct(ArrayList<String> words) {
		HashSet<String> distWords = new HashSet<String>(words);
		return new ArrayList<String>(distWords);
	}

	public static String[] distinct(String[] words) {
		HashSet<String> distWords = new HashSet<String>(Arrays.asList(words));
		return distWords.toArray(new String[0]);
	}

	/** return instance number of a CSV or ARFF file */
	public static int getInstNum(String dataFile) throws Exception {
		BufferedReader br = null;
		int instNum = -1;
		try {
			Common.Assert(AAI_IO.fileExist(dataFile));
			br = new BufferedReader(new FileReader(dataFile));
			// 1. skip header lines
			if (dataFile.toLowerCase().endsWith(".csv")) {
				br.readLine();
				instNum = 0;
			}
			if (dataFile.toLowerCase().endsWith(".arff")) {
				for (;;) {
					String line = br.readLine();
					if (line == null) {
						break;
					}
					if (line.toLowerCase().trim().equals("@data")) {
						instNum = 0;
						break;
					}
				}
			}
			if (instNum == -1) {
				return instNum;
			}
			// 2. count data lines
			while (br.readLine() != null) {
				instNum++;
			}
		} finally {
			if (br != null) {
				br.close();
			}
		}
		return instNum;
	}

	/** check if a class with a specified name exists. */
	public static boolean isClass(String className) {
		boolean exist = true;
		try {
			Class.forName(className);
		} catch (ClassNotFoundException e) {
			exist = false;
		}
		return exist;
	}

	/** String[] to String. E.g., ["a b", "c"] to "a b c" */
	public static String strArraytoStr(String[] strArray, String delimiter) {
		String buf = new String();
		for (int i = 0; i < strArray.length; i++) {
			buf += ((i == 0) ? "" : delimiter) + strArray[i];
		}
		return buf;
	}

	/**
	 * TODO: DELETE<br>
	 * Encode/decode strings between quotation marks in inputStr by switching
	 * all spaces in them with user-specified text/character.
	 */
	public static String encodeQuotation(String text, char ch) {
		String outputStr = new String();
		text = text.replace(' ', ch);
		return outputStr;
	}

	public static String quote(String str) {
		return "\"" + ((str == null) ? "" : str) + "\"";
	}

	public static String quote(String str, char ch) {
		return ch + ((str == null) ? "" : str) + ch;
	}

	public static String decodeQuotation(String text, char ch) {
		String outputStr = new String();
		return outputStr;
	}

	/** TODO: delete check if a class "cls" implements the interface "inter". */
	public boolean hasInterface(Class<?> cls, Class<?> inter) {
		return inter.isAssignableFrom(cls);
	}

	/** TODO: DELETE check if obj is a sub-instance of a class */
	public static boolean subInstance(Class<?> cls, Object obj) {
		try {
			return cls.isAssignableFrom(obj.getClass());
		} catch (Exception e) {
			return false;
		}
	}

	/** TODO: DELETE check if a class "sub" is a sub-class of a class "cls" */
	public static boolean subClass(Class<?> cls, Class<?> sub) {
		try {
			return cls.isAssignableFrom(sub);
		} catch (Exception e) {
			return false;
		}
	}

	/** check if a class "sub" is a sub-class of a class "cls" */
	public static boolean inherit(Class<?> cls, Class<?> sub) {
		try {
			return cls.isAssignableFrom(sub);
		} catch (Exception e) {
			return false;
		}
	}

	/** check if obj is a sub-instance of a class */
	public static boolean inherit(Class<?> cls, Object obj) {
		try {
			return cls.isAssignableFrom(obj.getClass());
		} catch (Exception e) {
			return false;
		}
	}

	/** check if a string[] is empty or not */
	public static boolean isEmpty(String[] strings) {
		for (int i = 0; i < strings.length; i++) {
			if (!strings[i].isEmpty()) {
				return false;
			}
		}
		// for (String string : strings) {
		// if ((string != null) && !string.isEmpty()) {
		// return false;
		// }
		// }
		return true;
	}

	/** check if a string is not null or empty. */
	public static boolean notNullEmpty(String str) {
		// boolean debug = (str != null) && !str.isEmpty();
		return (str != null) && !str.isEmpty();
	}

	/** check if a string is valid, i.e., not null or empty. */
	public static boolean isValid(String str) {
		return (str != null) && !str.isEmpty();
	}

	public static boolean isCSV(String fileName) {
		return validString(fileName) && fileName.toLowerCase().endsWith(".csv");
	}

	/** merge newCSV into mainCSV */
	public static void mergeCSV(String mainCSV, String newCSV) {
		String newStrCSV = AAI_IO.readFile(newCSV);
		// if mainCSV already has contents, drop header line of the new CSV
		if (AAI_IO.getFileSize(mainCSV) > 0) {
			newStrCSV = newStrCSV.substring(newStrCSV.indexOf('\n') + 1);
		}
		AAI_IO.saveFile(mainCSV, newStrCSV, true);
	}

	/** merge newARFF into mainARFF */
	public static void mergeARFF(String mainARFF, String newARFF) {
		String newStrARFF = AAI_IO.readFile(newARFF);
		// if mainCSV already has contents, drop header line of the new CSV
		if (AAI_IO.getFileSize(mainARFF) > 0) {
			String dataSection = "@data";
			newStrARFF = newStrARFF.substring(newStrARFF.indexOf(dataSection) + dataSection.length() + 2);
		}
		AAI_IO.saveFile(mainARFF, newStrARFF, true);
	}

	/** convert double to string with given precision */
	public static String decimal(double number, int precision) throws Exception {
		return String.format("%." + precision + "f", number);
	}

	public static String getVersion() {
		return "V1.1.4, add hasInterface() to check if a class \"cls\" implements the interface \"inter\", 22 Oct 2014.\n"
				+ "V1.1.3, add isClass(), strArraytoStr(), clearFile(), 9 Oct 2014\n"
				+ "V1.1.2, add getInstNum(), 3 Oct 2014\n"
				+ "V1.1.1, add removeDups(ArrayList<String> items), 28 Aug 2014\n" + "V1.1.0, add fileExist()\n"
				+ "V1.0, Created on 25 Aug 2014 by Allen Lin.\n";
	}
}