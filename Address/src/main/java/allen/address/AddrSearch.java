package allen.address;

import java.io.BufferedReader;
import java.io.FileReader;

import aai.base.common.AAI_IO;
import aai.base.common.Common;
import aai.base.common.Timer;
import aai.base.module.AAI_Module;
import allen.address.fuzzy.FuzzySearch;
import allen.address.fuzzykwd.CharPosKwds;
import allen.address.keyaddr.Addr;
import allen.address.keyaddr.AddrOrg;
import allen.address.keyaddr.CommFunc;
import allen.address.keyaddr.Key;
import allen.address.keyaddr.KeySet;
import allen.address.keyaddr.Kwd;

/**
 * Address Search Program.<br>
 * 
 * <b>Syntax:</b><br>
 * Java -jar addrsearch.jar -i addr_csv [-k topk]<br>
 * 
 * <ul>
 * <li><i>-i addr_csv</i> - [input] the address.csv file: [ADDRESS_DETAIL_PID,
 * BUILDING_NAME, FLAT_NUMBER, NUMBER_FIRST, NUMBER_FIRST_SUFFIX, NUMBER_LAST,
 * NUMBER_LAST_SUFFIX, STREET_NAME, STREET_TYPE_CODE, STREET_SUFFIX_TYPE,
 * LOCALITY_NAME, STATE_ABBREVIATION, POSTCODE]</li>
 * <li><i>-k topk</i> - [para] top k results, default 10.</li>
 * </ul>
 * 
 * @author Allen Lin, 22 Nov 2016
 */
public class AddrSearch extends AAI_Module {
	private static final long serialVersionUID = 7384874700719711854L;

	/** -i addr_csv: [input] address csv */
	private String m_addrCSV;
	/** -k topk: [para] top K results */
	private int m_topK = 10;

	/** 1. global keyword set[] */
	private KeySet m_kwdSet = new KeySet();
	/** 2. global address set[] */
	private KeySet m_addrSet = new KeySet();
	/** <char ch, int pos, kwds[]>, kwds[] contain 'ch' at pos. */
	private CharPosKwds m_charPosKwds;

	/**
	 * Phase 1: build Indexes from Address Book input: addrs.csv =
	 * {ADDRESS_DETAIL_PID[0], BUILDING_NAME[1], FLAT_NUMBER[2],
	 * NUMBER_FIRST[3], NUMBER_FIRST_SUFFIX[4], NUMBER_LAST[5],
	 * NUMBER_LAST_SUFFIX[6], STREET_NAME[7], STREET_TYPE_CODE[8],
	 * STREET_SUFFIX_TYPE[9], LOCALITY_NAME[10], STATE_ABBREVIATION[11],
	 * POSTCODE[12], [ADDRESS[13]]}
	 */
	public void indexing(String addrsCSV) throws Exception {
		if (!AAI_IO.fileExist(m_addrCSV)) {
			throw new Exception("File not exits. " + m_addrCSV);
		}
		this.dbgDir(AAI_IO.getAbsDir(addrsCSV) + "/debug/");
		this.tempDir(AAI_IO.getAbsDir(addrsCSV) + "/temp/");
		output("Indexing started. " + addrsCSV);
		Timer timer = new Timer();

		// Step 1.1: build global kwds[], addrs[]
		output("Step 1: indexing kwds[], addrs[] ...");
		BufferedReader br = new BufferedReader(new FileReader(m_addrCSV));
		String line = br.readLine();
		m_finished += line.length() + 2;
		m_total = AAI_IO.getFileSize(m_addrCSV) * 2;
		// 2. read in address rows[]
		for (; (line = br.readLine()) != null;) {
			m_finished += line.length() + 2;
			progress(m_finished, m_total);
			AddrOrg addrOrg = new AddrOrg(line);
			// 1. update global addrs[]
			Addr addr = (Addr) m_addrSet.add(addrOrg.stdAddr(), Addr.class);
			addr.length(addrOrg.length());
			// 2. update global kwds[] and indexes <kwd, addr>
			for (String key : addrOrg.keys()) {
				Common.Assert(!key.isEmpty());
				Kwd kwd = (Kwd) m_kwdSet.add(key, Kwd.class);
				kwd.addIndex(addr);
				addr.addIndex(kwd); // for DEBUG only
			}
		}
		br.close();
		output("Done. Indexed " + m_kwdSet.size() + " kwds, " + m_addrSet.size() + " addrs." + timer);
		if (debug()) {
			outputDbg("Saving indexed kwds[] and addrs[] to file ...");
			AAI_IO.saveFile(this.dbgDir() + AAI_IO.getFileName(addrsCSV + ".kwds.txt"), m_kwdSet.toString());
			AAI_IO.saveFile(this.dbgDir() + AAI_IO.getFileName(addrsCSV + ".addrs.txt"), m_addrSet.toString());
			outputDbg("Done" + timer);
		}
		// Step 1.2: build <char, pos, kwds[]> for spelling check
		output("Step 2: indexing for spelling check");
		m_charPosKwds = new CharPosKwds();
		String[] keys = m_kwdSet.getKeys().toArray(new String[m_kwdSet.size()]);
		for (int i = 0; i < keys.length; i++) {
			progress(i + 1, keys.length);
			String key = keys[i];
			Key kwd = m_kwdSet.get(key);
			Common.Assert(kwd != null);
			for (int j = 0; j < key.length(); j++) {
				m_charPosKwds.addKwd(key.charAt(j), j, kwd);
			}
		}
		output("Done. Indexed " + m_charPosKwds.size() + " entries." + timer);
		if (debug()) {
			outputDbg("Saving charPosKwds to file ...");
			AAI_IO.saveFile(this.dbgDir() + "charPosKwds.txt", m_charPosKwds.toString());
			outputDbg("Done" + timer);
		}
		output("Indexing finished. " + timer);
	}

	/** Phase 2: search addrs[] with user-input address */
	public void searching(String searchAddr, int topK) throws Exception {
		searchAddr = searchAddr.toLowerCase();
		// TODO Step 1 [exact search]: search in the addrTree

		// Step 2 [fuzzy search]: if can not find in exact search
		FuzzySearch fuzzySearch = new FuzzySearch(m_kwdSet, m_addrSet, m_charPosKwds, topK);
		fuzzySearch.owner(this);
		fuzzySearch.debug(this.debug());
		fuzzySearch.search(searchAddr);
	}

	@Override
	protected void mainProc() throws Exception {
		// 1. indexing
		indexing(m_addrCSV);
		// 2. searching
		byte inputBytes[] = new byte[4096];
		while (true) {
			System.out.println("Waiting for user input:\n");
			// receive user input
			int bytes = System.in.read(inputBytes);
			String searchAddr = new String(inputBytes, 0, bytes);
			searchAddr = CommFunc.retainAlphaNum(searchAddr);
			if (searchAddr.equals("exit")) {
				break;
			}
			// output("input [" + searchAddr.length() + "]: " + searchAddr);
			if (searchAddr.length() > 0) {
				searching(searchAddr, m_topK);
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
		m_topK = Common.getOptionInteger("k", options, m_topK);
		// debug, daemon, etc
		super.setOptions(options);
	}

	public static String help() {
		return "Address Search Program.\n" + "Syntax: Java -jar addrsearch.jar -i addr_csv [-k topk]\n"
				+ "-i addr_csv - [input] the address.csv file: [ADDRESS_DETAIL_PID, BUILDING_NAME, FLAT_NUMBER, NUMBER_FIRST, NUMBER_FIRST_SUFFIX, NUMBER_LAST, NUMBER_LAST_SUFFIX, STREET_NAME, STREET_TYPE_CODE, STREET_SUFFIX_TYPE, LOCALITY_NAME, STATE_ABBREVIATION, POSTCODE]\n"
				+ "-k topk - [para] top k results, default 10.\n";
	}

	public static String version() {
		return "v0.0.1.Beta, crteated on 27 Nov 2016, Allen Lin";
	}

	public static void main(String[] args) throws Exception {
		exec(Thread.currentThread().getStackTrace()[1].getClassName(), args);
	}
}