package allen.address.main;

import java.io.BufferedReader;
import java.io.FileReader;

import aai.base.common.AAI_IO;
import aai.base.common.Common;
import aai.base.common.Timer;
import aai.base.module.AAI_Module;
import allen.address.basic.Addr;
import allen.address.basic.AddrLst;
import allen.address.basic.CommFunc;
import allen.address.basic.Kwd;
import allen.address.basic.KwdSet;
import allen.address.fuzzy.FuzzySearch;
import allen.address.simkwd.CharPosKwds;

/**
 * AddrSearch is an address search program.<br>
 * 
 * <b>Syntax:</b><br>
 * Java -jar addrsearch.jar [-i addr_csv] -k topk [-K topK] [-d dir_idx]<br>
 * 
 * <ul>
 * <li><i>-i addr_csv</i> - [input] the address.csv file: [ADDRESS_DETAIL_PID,
 * BUILDING_NAME, FLAT_NUMBER, NUMBER_FIRST, NUMBER_FIRST_SUFFIX, NUMBER_LAST,
 * NUMBER_LAST_SUFFIX, STREET_NAME, STREET_TYPE_CODE, STREET_SUFFIX_TYPE,
 * LOCALITY_NAME, STATE_ABBREVIATION, POSTCODE].<br>
 * If addr_csv is given, AddrSearch will first build indexes from the data file,
 * save them into file and then start the search loop, otherwise it will load
 * indexes from indexing files in the dir_idx and then start the search loop.
 * </li>
 * <li><i>-k topk</i> - [para] top k addresses.</li>
 * <li><i>-K topK</i> - [para] top K similar keywords. In fuzzy search, it
 * defines the max-size of similar kwds[] to a user keyword that is not in the
 * address keyword list. Default 5.</li>
 * <li><i>-d dir_idx</i> - [para] the indexing directory where indexing files
 * locate. Default is current directory.</li>
 * </ul>
 * 
 * Usage example:<br>
 * Java -jar addrsearch.jar -i addr.csv #building indexes and then search<br>
 * Java -jar addrsearch.jar -k 10 -d c:/idx #load indexes and then search<br>
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
	/** -d dir_idx: [para] directory where indexing files locate. */
	private String m_dirIdx;

	/** 1. global keyword set[] */
	private KwdSet m_kwdSet = new KwdSet();
	/** 2. global address set[] DISCARD */
	private AddrLst m_addrLst = new AddrLst();
	/** 3. <char ch, int pos, kwds[]>, kwds[] contain 'ch' at pos. */
	private CharPosKwds m_chposKwds = new CharPosKwds();

	public AddrSearch() {
		m_kwdSet.owner(this);
		m_addrLst.owner(this);
		m_chposKwds.owner(this);
	}

	private String kwdsFile() {
		return m_dirIdx + "idx_kwds.txt";
	}

	private String addrsFile() {
		return m_dirIdx + "idx_addrs.txt";
	}

	private String chposKwdsFile() {
		return m_dirIdx + "idx_chars.txt";
	}

	public void saveIdx(String idxDir) throws Exception {
		// save kwds[] indexes to files
		m_kwdSet.save(kwdsFile());
		// AAI_IO.saveFile(kwdsFile(), m_kwdSet.toString());
		// save addrs[] to file
		m_addrLst.save(addrsFile());
		// AAI_IO.saveFile(addrsFile(), m_addrLst.toString());
		// save chPosKwds[] to file
		AAI_IO.saveFile(chposKwdsFile(), m_chposKwds.toString());
	}

	public void loadIdx(String idxDir) throws Exception {
		// load kwds[] indexes from files
		m_kwdSet.load(kwdsFile());
		// load addrs[] from file
		m_addrLst.loadAddrs(addrsFile(), m_kwdSet);
		// load chPosKwds[] from file
		m_chposKwds.loadChPosFile(chposKwdsFile(), m_kwdSet);
	}

	/**
	 * Phase 1: build Indexes from Address Book input: addrs.csv =
	 * {ADDRESS_DETAIL_PID[0], BUILDING_NAME[1], FLAT_NUMBER[2],
	 * NUMBER_FIRST[3], NUMBER_FIRST_SUFFIX[4], NUMBER_LAST[5],
	 * NUMBER_LAST_SUFFIX[6], STREET_NAME[7], STREET_TYPE_CODE[8],
	 * STREET_SUFFIX_TYPE[9], LOCALITY_NAME[10], STATE_ABBREVIATION[11],
	 * POSTCODE[12]]}
	 */
	public void indexing(String addrCSV) throws Exception {
		if (!AAI_IO.fileExist(addrCSV)) {
			throw new Exception("File not exits. " + addrCSV);
		}
		this.updateDirs(AAI_IO.getAbsDir(addrCSV)); // TODO discard
		Timer timer = new Timer();

		// Step 1.1: build global kwds[], addrs[] [and save to file]
		output("Started indexing kwds[].");
		BufferedReader br = new BufferedReader(new FileReader(addrCSV));
		String line = br.readLine();
		double finished = line.length() + 2;
		double total = AAI_IO.getFileSize(addrCSV);
		// 2. read in address rows[]
		int addr_num_indexed = 0, addr_num_all = 0;
		for (; (line = br.readLine()) != null; addr_num_all++) {
			// m_finished += line.length() + 2;
			progress(finished += line.length() + 2, total);
			Addr addr = Addr.newAddr(line, m_kwdSet, addr_num_indexed);
			if (addr == null) {
				outputWarning("skip invalid address: " + Common.quote(line));
				continue;
			}
			m_addrLst.add(addr);
			addr_num_indexed++;
		}
		br.close();
		int addr_num_discard = addr_num_all - addr_num_indexed;
		output("Finished indexing kwds[]. " + addr_num_indexed + " addrs indexed, "
				+ ((addr_num_discard == 0) ? "" : (addr_num_discard + " addrs discarded, ")) + m_kwdSet.size()
				+ " indexing kwds[]. " + timer);

		// Step 1.2: build <char, pos, kwds[]> for spelling check
		output("Step 2: indexing for spelling check");
		total = m_kwdSet.size();
		finished = 0;
		for (String kwdStr : m_kwdSet.getStrs()) {
			progress(++finished, total);
			Kwd kwd = m_kwdSet.get(kwdStr);
			Common.Assert(kwd != null);
			for (int j = 0; j < kwdStr.length(); j++) {
				m_chposKwds.addKwd(kwdStr.charAt(j), j, kwd);
			}
		}
		output("Done. Produced " + m_chposKwds.size() + " [ch, pos, kwd] indexes. " + timer);
		// System.gc();
	}

	/** Phase 2: search addrs[] with user query */
	public String searching(String usrQuery) throws Exception {
		usrQuery = CommFunc.retainAlphaNum(usrQuery.toLowerCase());
		outputDbg("query = " + Common.quote(usrQuery));
		// TODO Step 1 [exact search]: search in the addrTree

		// Step 2 [fuzzy search]: if can not find in exact search
		FuzzySearch fuzzySearch = new FuzzySearch(m_kwdSet, m_addrLst, m_chposKwds, m_topk, m_topK);
		fuzzySearch.owner(this);
		fuzzySearch.debug(this.debug());
		return fuzzySearch.search(usrQuery);
	}

	private String createIdxDir(String dirIdx) throws Exception {
		if (dirIdx == null || dirIdx.isEmpty()) {
			dirIdx = this.workDir() + "idx/";
		}
		if (!AAI_IO.createDir(dirIdx)) {
			throw new Exception("Failed to create indexing directory. " + dirIdx);
		}
		return dirIdx;
	}

	@Override
	protected void mainProc() throws Exception {
		// Step 1. indexing
		if (AAI_IO.fileExist(m_addrCSV)) {
			indexing(m_addrCSV);
			saveIdx(m_dirIdx);
		} else {
			loadIdx(m_dirIdx);
			saveIdx(m_dirIdx + "/re-save/");
		}

		// Step 2. searching
		byte inputBytes[] = new byte[4096];
		while (true) {
			System.out.println("\nInput query keywords: (press \"exit\" to quit)\n");
			// receive user input
			int bytes = System.in.read(inputBytes);
			String usrQuery = new String(inputBytes, 0, bytes);
			usrQuery = usrQuery.replaceAll("\r", "").replaceAll("\n", "");
			if (usrQuery.equalsIgnoreCase("exit")) {
				break;
			}
			if (usrQuery.length() > 0) {
				System.out.println(searching(usrQuery));
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
		// -d dir_idx
		m_dirIdx = Common.getOption("d", options);
		m_dirIdx = createIdxDir(m_dirIdx);
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