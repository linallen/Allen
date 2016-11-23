package allen.address;

import allen.address.keyaddr.Addr;
import allen.address.keyaddr.KeySet;
import allen.address.keyaddr.Kwd;
import allen.base.common.AAI_IO;
import allen.base.common.Common;
import allen.base.module.AAI_Module;
import backup.AddrCSV;

public class AppMatch extends AAI_Module {
	private static final long serialVersionUID = 7384874700719711854L;

	/** 1. global keyword set[] */
	public KwdSet m_kwdSet = new KwdSet();

	/** 2. global address set[] */
	public KeySet m_addrSet = new KeySet();

	/** <char ch, int pos, kwds[]>, kwds[] contain 'ch' at pos. */
	CharPosKwds m_charPosKwds;

	/**
	 * input: addrs.csv = {ADDRESS_DETAIL_PID[0], BUILDING_NAME[1],
	 * FLAT_NUMBER[2], NUMBER_FIRST[3], NUMBER_FIRST_SUFFIX[4], NUMBER_LAST[5],
	 * NUMBER_LAST_SUFFIX[6], STREET_NAME[7], STREET_TYPE_CODE[8],
	 * STREET_SUFFIX_TYPE[9], LOCALITY_NAME[10], STATE_ABBREVIATION[11],
	 * POSTCODE[12], ADDRESS[13]}
	 */
	public void mainProc(String addrsCSV) {
		// Phase 1. Building Indexes from Address Book
		String buf = AAI_IO.readFile(addrsCSV);
		buf = buf.replace("\r", "");
		String lines[] = buf.split("\n");
		for (int i = 1; i < lines.length; i++) {
			progress(i + 1, lines.length);
			AddrOrg addrOrg = new AddrOrg(lines[i]);
			Addr addr = new Addr();
			addr.set(addrOrg.StdAddr);

			// 1. update global addrs[]
			m_addrSet.add(addrOrg.StdAddr, addr);

			// 2. update global kwds[] and indexes <kwd, addr>
			for (String key : addrOrg.keys()) {
				if (!key.isEmpty()) {
					Kwd kwd = m_kwdSet.addKwd(key);
					kwd.addAddr(addr);
					addr.addKwd(kwd);
				}
			}
		}
		// debug
		AAI_IO.saveFile(addrsCSV + ".kwds.txt", m_kwdSet.toString());
		AAI_IO.saveFile(addrsCSV + ".addrs.txt", m_addrSet.toString());
	}

	public static void main(String[] args) throws Exception {
		AppMatch appMatch = new AppMatch();
		appMatch.debug(true);
		appMatch.mainProc("C:/Allen/Dropbox/2016_11_06 Address/address_v2.csv");
		System.out.println("\nAll done!");
	}
}
