package allen.address;

import java.util.ArrayList;
import java.util.Arrays;

import aai.base.common.AAI_IO;
import aai.base.common.Common;
import aai.base.module.AAI_Module;
import allen.address.fuzzy.FuzzySearch;
import allen.address.keyaddr.Addr;
import allen.address.keyaddr.AddrOrg;
import allen.address.keyaddr.CommFunc;
import allen.address.keyaddr.Key;
import allen.address.keyaddr.KeySet;
import allen.address.keyaddr.Kwd;

public class AppMatch extends AAI_Module {
	private static final long serialVersionUID = 7384874700719711854L;
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
		// Step 1.1: build global kwds[], addrs[] and mutual indexes
		System.out.println("\nStep 1.1: build global kwds[], addrs[] and mutual indexes ...");
		String buf = AAI_IO.readFile(addrsCSV);
		buf = buf.replace("\r", "");
		String lines[] = buf.split("\n");
		for (int i = 1; i < lines.length; i++) {
			progress(i + 1, lines.length);
			AddrOrg addrOrg = new AddrOrg(lines[i]);
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
		System.out.println("Done. " + m_kwdSet.size() + " kwds, " + m_addrSet.size() + " addrs.");
		// debug
		// AAI_IO.saveFile(addrsCSV + ".kwds.txt", m_kwdSet.toString());
		// AAI_IO.saveFile(addrsCSV + ".addrs.txt", m_addrSet.toString());

		// TODO Step 1.2: build <char, pos, kwds[]> for spelling check
		System.out.println("\nStep 1.2: build <char, pos, kwds[]> for spelling check");
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
		System.out.println("Done. " + m_charPosKwds.size() + " entries.");

		System.out.println("\nSaving charPosKwds to file ...");
		AAI_IO.saveFile("c:/temp/charPosKwds.txt", m_charPosKwds.toString());
		System.out.println("done ======\n\n");
	}

	/** Phase 2: search addrs[] with user-input address */
	public void searching(String searchAddr, int topK) throws Exception {
		searchAddr = searchAddr.toLowerCase();
		// TODO Step 1 [exact search]: search in the addrTree

		// Step 2 [fuzzy search]: if can not find in exact search
		FuzzySearch fuzzySearch = new FuzzySearch();
		fuzzySearch.search(searchAddr, m_kwdSet, m_addrSet, topK);
	}

	public static void main(String[] args) throws Exception {
		AppMatch appMatch = new AppMatch();
		appMatch.debug(true);
		appMatch.indexing("C:/Allen/UTS/UTS_SourceCode/Address/_data/address_v2.csv");
		System.out.println("Done.\n");
		// Testing
		int topK = 10;
		byte inputBytes[] = new byte[4096];
		while (true) {
			System.out.println("Waiting for input:");
			// receive user input
			int bytes = System.in.read(inputBytes);
			String searchAddr = new String(inputBytes, 0, bytes);
			searchAddr = CommFunc.retainAlphaNum(searchAddr);
			System.out.println("input [" + searchAddr.length() + "]: " + searchAddr);
			if (searchAddr.length() > 0) {
				appMatch.searching(searchAddr, topK);
				// "28/344 pennant hills nsw 2012 nsw good carlingford 3");
			}
		}
	}
}