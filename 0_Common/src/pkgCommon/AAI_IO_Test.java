package pkgCommon;

import java.io.File;

public class AAI_IO_Test {

	public static void main(String[] args) throws Exception {
		// 1. boolean saveFile(String fileName, String buf, boolean append)
		// a) fileName = null
		// b) fileName = ""
		// c) buf = null
		// d) buf = ""
		String fileName = "F:/3_GoogleDrive";// , dirName;
		// String fileName = "test.txt";
		File file = new File(fileName);
		// boolean ret = file.getAbsoluteFile().getParentFile().mkdirs();
		System.out.println(file.getAbsolutePath());
		System.out.println(file.getCanonicalPath());
		System.out.println(file.getName());
		System.out.println("getParent: " + file.getParent());
		System.out.println(new File(file.getAbsolutePath()).getParent());
		System.out.println(file.getPath());
		// boolean success = AAI_IO.saveFile(fileName, "test", false);
		// System.out.println(success ? "success" : "fail");
		System.out.println(AAI_IO.getAbsDir(fileName));

		// fileName = "c:/test/abc/new.txt";
		// dirName = "c:/dir";
		// fileName = AAI_IO.getFile(fileName, null);
		// System.out.println(fileName);
	}
}