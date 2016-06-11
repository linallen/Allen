package common;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * File IO, socket IO, stream IO common functions.
 * 
 * @author Allen Lin, 1 Dec 2014.
 */
public class AAI_IO {
	/** delete file */
	public static boolean deleteFile(String fileName) {
		boolean success = false;
		try {
			File file = new File(fileName);
			success = file.delete();
		} catch (Exception e) {
			// e.printStackTrace();
		}
		return success;
	}

	/** check if two files are the same */
	public static boolean sameFiles(String fileName1, String fileName2) {
		File file1 = new File(fileName1);
		File file2 = new File(fileName2);
		String path1 = file1.getAbsolutePath();
		String path2 = file2.getAbsolutePath();
		return path1.equalsIgnoreCase(path2);
	}

	/** load a Serializable object from file. */
	public static Object loadSerializableObj(String objFile) throws Exception {
		InputStream is = null;
		ObjectInputStream ois = null;
		Object obj = null;
		try {
			is = new FileInputStream(objFile);
			ois = new ObjectInputStream(is);
			obj = ois.readObject();
		} finally {
			AAI_IO.close(ois);
			AAI_IO.close(is);
		}
		return obj;
	}

	/** save a Serializable object to file. */
	public static boolean saveSerializableObj(String objFile, Object obj) throws Exception {
		boolean success = false;
		OutputStream os = null;
		ObjectOutputStream oos = null;
		try {
			os = new FileOutputStream(objFile);
			oos = new ObjectOutputStream(os);
			oos.writeObject(obj);
			success = true;
		} finally {
			AAI_IO.close(oos);
			AAI_IO.close(os);
		}
		return success;
	}

	/**
	 * close a "Closeable" object (Writer, Reader, Socket, InputStream,
	 * OutputStream).
	 */
	public static boolean close(Object obj) {
		// if ((obj != null) &&
		// Closeable.class.isAssignableFrom(obj.getClass())) {
		if ((obj != null) && Common.subInstance(Closeable.class, obj)) {
			try {
				((Closeable) obj).close();
			} catch (Exception e) {
				return false;
			}
		}
		return true;
	}

	/** file exists? */
	public static boolean fileExist(String fileName) {
		File file = null;
		try {
			file = new File(fileName);
		} catch (Exception e) {
			return false;
		}
		return file.isFile() && file.exists();
	}

	/** directory exists? */
	public static boolean dirExist(String dirName) {
		File dir = null;
		try {
			dir = new File(dirName);
		} catch (Exception e) {
			return false;
		}
		return dir.isDirectory() && dir.exists();
	}

	/** file copy */
	public static void fileCopy(String fromFile, String toFile) throws IOException {
		Files.copy(new File(fromFile).toPath(), new File(toFile).toPath(), StandardCopyOption.REPLACE_EXISTING);
	}

	/** add or append text to file (full version) */
	public static void saveFileEx(String fileName, String str, boolean append) throws Exception {
		File file = new File(fileName);
		// 1. make directories
		File dir = file.getAbsoluteFile().getParentFile();
		if (!dirExist(dir.getPath()) && !dir.mkdirs()) {
			throw new Exception("failed to create directory " + Common.quote(dir.getAbsolutePath()));
		}
		// 2. write buf into file
		FileWriter writer = new FileWriter(file, append);
		try {
			writer.write(str);
		} finally {
			close(writer);
		}
	}

	/** add or append text to file (simple version) */
	public static boolean saveFile(String fileName, String buf, boolean append) {
		try {
			saveFileEx(fileName, buf, append);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public static boolean saveFile(String fileName, String buf) {
		return saveFile(fileName, buf, false);
	}

	/** read file to string (full version) */
	public static String readFileEx(String fileName) throws Exception {
		File file = new File(fileName);
		char[] chars = new char[(int) file.length()];
		FileReader reader = new FileReader(file);
		try {
			return (reader.read(chars) != -1) ? (new String(chars)) : null;
		} finally {
			close(reader);
		}
	}

	/** read file to string (full version) */
	public static String readFile(String fileName) {
		try {
			return readFileEx(fileName);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * new file = directory of dirName + file of fileName.<br>
	 * e.g., getFile("c:/txt/all/word.txt","d:/aai") == "c:/txt/all/aai".
	 * 
	 * @return new file name or null if something wrong.
	 */
	public static String getFileEx(String dirName, String fileName) throws Exception {
		File dir = new File(dirName);
		dirName = new File(dir.getAbsolutePath()).getParent();
		File file = new File(fileName);
		fileName = file.getName();
		return dirName + "/" + fileName;
	}

	/** simple version of getFileEx() */
	public static String getFile(String dirName, String fileName) {
		try {
			return getFileEx(dirName, fileName);
		} catch (Exception e) {
			return null;
		}
	}

	/** clear the content of file */
	public static boolean clearFile(String fileName) {
		return saveFile(fileName, new String(), false);
	}

	/** create file */
	public static boolean createFile(String fileName) {
		return saveFile(fileName, new String(), false);
	}

	/** create file */
	public static void createDir(String dirName) throws Exception {
		if (!dirExist(dirName)) {
			new File(dirName).mkdir();
		}
	}

	/** get file size */
	public static long getFileSize(String fileName) {
		try {
			return (new File(fileName)).length();
		} catch (Exception e) {
			return 0;
		}
	}

	/** return directory */
	public static String getDir(String path) {
		if (path == null) {
			return new String();
		}
		return (new File(path)).getAbsolutePath();
	}

	/** return absolute directory */
	public static String getAbsDir(String fileName) {
		if (!Common.isValid(fileName)) {
			return null;
		}
		File file = new File(fileName);
		if (file.isFile()) {
			// String path = file.getAbsolutePath();
			// path = path.substring(0, path.lastIndexOf("/"));
			return (new File(file.getAbsolutePath())).getParent() + "/";
		} else if (file.isDirectory()) {
			return file.getAbsolutePath() + "/";
		}
		return null;
	}

	/** return full file name */
	public static String getFileName(String path) {
		if (path == null) {
			return new String();
		}
		return (new File(path)).getName();
	}

	/** return file name, e.g., getFileNamePre("c:/file.txt") = "file" */
	public static String getFileNamePre(String path) {
		String fileName = getFileName(path);
		int dotIdx = fileName.lastIndexOf('.');
		switch (dotIdx) {
		case -1: // "file" no file extension
			return fileName;
		case 0: // ".txt"
			return "";
		default:
			return fileName.substring(0, dotIdx);
		}
	}

	public static String getFileNamePre(File file) {
		return getFileNamePre(file.getName());
	}

	/** return file extension, e.g., getFileNameExt("c:/file.txt") = "txt" */
	public static String getFileNameExt(String path) {
		String fileName = getFileName(path);
		int dotIdx = fileName.lastIndexOf('.');
		if (dotIdx == -1) {
			return ""; // "file" no file extension
		} else if (fileName.length() > (dotIdx + 1)) {
			// "*.txt"
			return fileName.substring(dotIdx + 1);
		} else {
			return ""; // "abc."
		}
	}

	public static String getCurDir(String path) {
		String curDir = (new File(path)).getParent();
		if (curDir == null) {
			curDir = System.getProperty("user.dir");
		}
		return curDir;
	}

	public static String getCurDir() {
		return System.getProperty("user.dir");
	}

	/** check if a file ends with a specified extension */
	public static boolean endWith(String fileName, String extension) {
		return Common.validString(fileName) && fileName.toLowerCase().endsWith(extension);
	}
}