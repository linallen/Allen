package allen.csv.converter;

public class Test {
	private static String WORK_DIR = "D:/UTS/SourceCode/2_CsvConverter/_data/";
	private static String m_inputCSV = WORK_DIR + "student_org.csv";
	private static String m_outputFile = WORK_DIR + "student_org.arff";
	private static int m_subNum = -1;
	private static boolean m_shuffle = false;
	private static boolean m_outputCSV = false;

	public static void main(String[] args) throws Throwable {
		// -i input_csv -o output_file [-D id_num] [[-r row_num] | [-s sub_num]]
		// [-v val_num] [-S] [-C]
		String options = "-i " + m_inputCSV + " -o " + m_outputFile
				+ (m_subNum > 0 ? (" -s " + m_subNum) : "")
				+ (m_shuffle ? " -S " : "") + (m_outputCSV ? " -C " : "");
		CsvConverter module = new CsvConverter();
		module.debug(true);
		module.addOptions(options.split(" "));
		module.start();
		module.join();
		System.out.println("\n\n" + module.description());
		String outputFiles[] = module.getSubFiles();
		System.out.println(outputFiles.length + " sub file(s) generated:");
		for (int i = 0; i < outputFiles.length; i++) {
			System.out.println(outputFiles[i]);
		}
	}
}