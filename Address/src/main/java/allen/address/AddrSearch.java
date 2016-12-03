package allen.address;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

import aai.base.common.AAI_IO;
import aai.base.common.Common;
import aai.base.common.Timer;
import aai.base.module.AAI_Module;
import allen.address.fuzzy.FuzzySearch;
import allen.address.keyaddr.Addr;
import allen.address.keyaddr.CommFunc;
import allen.address.keyaddr.Kwd;
import allen.address.keyaddr.KwdSet;
import allen.address.simkwd.CharPosKwds;

/**
 * Address Search Program.<br>
 * 
 * <b>Syntax:</b><br>
 * Java -jar addrsearch.jar -i addr_csv [-k topk] [-K topK] [-d idx_dir]<br>
 * 
 * <ul>
 * <li><i>-i addr_csv</i> - [input] the address.csv file: [ADDRESS_DETAIL_PID,
 * BUILDING_NAME, FLAT_NUMBER, NUMBER_FIRST, NUMBER_FIRST_SUFFIX, NUMBER_LAST,
 * NUMBER_LAST_SUFFIX, STREET_NAME, STREET_TYPE_CODE, STREET_SUFFIX_TYPE,
 * LOCALITY_NAME, STATE_ABBREVIATION, POSTCODE]</li>
 * <li><i>-k topk</i> - [para] top k addresses, default 10.</li>
 * <li><i>-K topK</i> - [para] top K fuzzy keywords, in spelling check, the
 * limit on matched kwds[] to a input keyword. default 5.</li>
 * <li><i>-d idx_dir</i> - [para] directory where indexing files locate.</li>
 * </ul>
 *
 * When program starts, it will first try to read indexes from the indexing
 * directory. If the indexing is missing, it will read from the input data, make
 * indexes and provide search UI.
 * 
 * @author Allen Lin, 22 Nov 2016
 */
public class AddrSearch extends AAI_Module {
	private static final long serialVersionUID = 7384874700719711854L;

	/** -i addr_csv: [input] address csv */
	private String m_addrCSV;
	/** -k topk: [para] top k addresses */
	private int m_topk = 10;
	/** -K topK: [para] top K fuzzy keywords */
	private int m_topK = 5;
	/** -d idx_dir: [para] directory where indexing files locate. */
	private String m_idxDir;

	/** 1. global keyword set[] */
	private KwdSet m_kwdSet = new KwdSet();
	/** 2. global address set[] DISCARD */
	private AddrLst m_addrLst = new AddrLst();
	/** 3. <char ch, int pos, kwds[]>, kwds[] contain 'ch' at pos. */
	private CharPosKwds m_charPosKwds = new CharPosKwds();

	private String fileIdxKwds() {
		return m_idxDir + "idx_kwds.txt";
	}

	private String fileIdxAddrs() {
		return m_idxDir + "idx_addrs.txt";
	}

	private String fileIdxChars() {
		return m_idxDir + "idx_chars.txt";
	}

	// TODO REVISE save keyword index [kwd, addr[]] to file
	private void saveKwdIdx(String kwdFile) {
		output("Started saving " + m_kwdSet.size() + " indexed kwds to file " + kwdFile);
		Timer timer = new Timer();
		int total = m_kwdSet.size(), finished = 0;
		for (Kwd kwd : m_kwdSet.getKwds()) {
			progress(++finished, total);
			AAI_IO.saveFile(kwdFile, Kwd.toFile(kwd) + "\n", true);
		}
		output("Finished saving indexed kwds to file. " + timer);
	}

	// TODO REVISE save addrs[] to file
	private void saveAddrIdx(String addrFile) {
		output("Started saving " + m_addrLst.size() + " addrs to file " + addrFile);
		Timer timer = new Timer();
		for (int i = 0; i < m_addrLst.size(); i++) {
			progress(i + 1, m_addrLst.size());
			Addr addr = m_addrLst.get(i);
			AAI_IO.saveFile(addrFile, Addr.toStdAddr(addr) + "\n", true);
		}
		output("Finished saving indexed kwds to file. " + timer);
	}

	// TODO
	private void loadKwdIdx(String kwdFile) {
	}

	// TODO
	private void loadAddrIdx(String addrFile) {
	}

	/**
	 * Phase 1: build Indexes from Address Book input: addrs.csv =
	 * {ADDRESS_DETAIL_PID[0], BUILDING_NAME[1], FLAT_NUMBER[2],
	 * NUMBER_FIRST[3], NUMBER_FIRST_SUFFIX[4], NUMBER_LAST[5],
	 * NUMBER_LAST_SUFFIX[6], STREET_NAME[7], STREET_TYPE_CODE[8],
	 * STREET_SUFFIX_TYPE[9], LOCALITY_NAME[10], STATE_ABBREVIATION[11],
	 * POSTCODE[12]]}
	 */
	public void indexing(String addrCSV, String idxDir) throws Exception {
		if (!AAI_IO.fileExist(addrCSV)) {
			throw new Exception("File not exits. " + addrCSV);
		}
		m_idxDir = AAI_IO.getAbsDir(idxDir);
		if (m_idxDir == null) {
			m_idxDir = this.workDir();
		}
		if (!AAI_IO.createDir(m_idxDir)) {
			throw new Exception("Failed to create indexing directory. " + idxDir);
		}
		this.updateDirs(AAI_IO.getAbsDir(addrCSV)); // TODO discard
		output("Indexing started. " + addrCSV);
		Timer timer = new Timer();

		// Step 1.1: build global kwds[], addrs[] [and save to file]
		output("Step 1: indexing kwds[] ...");
		BufferedReader br = new BufferedReader(new FileReader(addrCSV));
		String line = br.readLine();
		double finished = line.length() + 2;
		double total = AAI_IO.getFileSize(addrCSV);
		// 2. read in address rows[]
		int addr_num = 0, addr_num_total = 0;
		for (; (line = br.readLine()) != null; addr_num_total++) {
			// m_finished += line.length() + 2;
			progress(finished += line.length() + 2, total);
			Addr addr = Addr.newAddr(line, m_kwdSet);
			if (addr != null) {
				addr.id = addr_num++;
				m_addrLst.add(addr);
			}
		}
		br.close();
		int wrong_addr_num = addr_num_total - addr_num;
		output(addr_num + " addrs loaded, " + ((wrong_addr_num == 0) ? "" : (wrong_addr_num + " wrong addrs, "))
				+ m_kwdSet.size() + " kwds. " + timer);
		// save kwds[] indexes to files
		saveKwdIdx(fileIdxKwds());
		// save addrs[] to file
		saveAddrIdx(fileIdxAddrs());

		// Step 1.2: build <char, pos, kwds[]> for spelling check
		output("Step 2: indexing for spelling check");
		total = m_kwdSet.size();
		finished = 0;
		for (String kwdStr : m_kwdSet.getStrs()) {
			progress(++finished, total);
			Kwd kwd = m_kwdSet.get(kwdStr);
			Common.Assert(kwd != null);
			for (int j = 0; j < kwdStr.length(); j++) {
				m_charPosKwds.addKwd(kwdStr.charAt(j), j, kwd);
			}
		}
		output("Done. Produced " + m_charPosKwds.size() + " [ch, pos, kwd] indexes. " + timer);
		// TODO save [ch, pos, kwd] indexes to file
		output("Indexing finished. " + timer);
	}

	/** Phase 2: search addrs[] with user query */
	public String searching(String usrQuery) throws Exception {
		usrQuery = CommFunc.retainAlphaNum(usrQuery.toLowerCase());
		outputDbg("query = " + Common.quote(usrQuery));
		// TODO Step 1 [exact search]: search in the addrTree

		// Step 2 [fuzzy search]: if can not find in exact search
		FuzzySearch fuzzySearch = new FuzzySearch(m_kwdSet, m_charPosKwds, m_topk, m_topK);
		fuzzySearch.owner(this);
		fuzzySearch.debug(this.debug());
		return fuzzySearch.search(usrQuery);
	}

	@Override
	protected void mainProc() throws Exception {
		// Step 1. indexing
		indexing(m_addrCSV, m_idxDir);
		// Step 2. searching
		byte inputBytes[] = new byte[4096];
		while (true) {
			System.out.println("\nInput query keywords: (press \"exit\" to quit)\n");
			// receive user input
			int bytes = System.in.read(inputBytes);
			String usrQuery = new String(inputBytes, 0, bytes);
			if (usrQuery.equalsIgnoreCase("exit")) {
				break;
			}
			// output("input [" + searchAddr.length() + "]: " + searchAddr);
			if (usrQuery.length() > 0) {
				System.out.println(searching(usrQuery));
				// "28/344 pennant hills nsw 2012 nsw good carlingford 3");
			}
		}
		output("Bye!");
	}

	@Override
	public void setOptions(String[] options) throws Exception {
		// -i addr_csv
		m_addrCSV = Common.getOption("i", options);
		// -k topk
		m_topk = Common.getOptionInteger("k", options, m_topk);
		// -K topK
		m_topK = Common.getOptionInteger("K", options, m_topK);
		// -d idx_dir
		m_idxDir = Common.getOption("d", options);
		// debug, daemon, etc
		super.setOptions(options);
	}

	public static String help() {
		return "Address Search Program.\n" + "Syntax: Java -jar addrsearch.jar -i addr_csv [-k topk]\n"
				+ "-i addr_csv - [input] the address.csv file: [ADDRESS_DETAIL_PID, BUILDING_NAME, FLAT_NUMBER, NUMBER_FIRST, NUMBER_FIRST_SUFFIX, NUMBER_LAST, NUMBER_LAST_SUFFIX, STREET_NAME, STREET_TYPE_CODE, STREET_SUFFIX_TYPE, LOCALITY_NAME, STATE_ABBREVIATION, POSTCODE]\n"
				+ "-k topk - [para] top k addresses, default 10.\n"
				+ "-K topK - [para] top K fuzzy keywords, in spelling check, the limit on matched kwds[] to a input keyword. default 5.";
	}

	public static String version() {
		return "v0.0.1.Beta, crteated on 27 Nov 2016, Allen Lin";
	}

	public static void main(String[] args) throws Exception {
		exec(Thread.currentThread().getStackTrace()[1].getClassName(), args);
	}
}