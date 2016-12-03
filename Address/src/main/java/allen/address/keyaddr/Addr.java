package allen.address.keyaddr;

import java.io.Serializable;

import aai.base.common.Common;

/** address object */
public class Addr implements Serializable, Comparable<Addr> {
	private static final long serialVersionUID = -1701729465126477598L;

	public int id;

	private Kwd m_kwdLst[];

	/** address's real length, e.g., "3/2a-6 cleveland st" = 4 */
	private int m_length;
	private Kwd m_bldgNames[];
	private Kwd m_flatNum;
	private Integer m_numFirst;
	private String m_numFirstSuffix;
	private Integer m_numLast;
	private String m_numLastSuffix;
	private Kwd m_streetNames[];
	private Kwd m_streetType;
	private Kwd m_streetSuffix;
	private Kwd m_localityNames[];
	private Kwd m_stateAbbr;
	private Kwd m_postCode;

	/** create an addr object from a standard address string */
	public static Addr readAddr(String addrStr, KwdSet kwdSet) {
		Addr addr = new Addr();
		String kwdStrs[] = CommFunc.retainAlphaNum(addrStr).split(" ");
		if (kwdStrs.length == 0) {
			return null;
		}
		addr.m_kwdLst = new Kwd[kwdStrs.length];
		addr.m_length = kwdStrs.length;
		for (int i = 0; i < kwdStrs.length; i++) {
			Kwd kwd = kwdSet.get(kwdStrs[i]);
			if (kwd != null) {
				addr.m_kwdLst[i] = kwd;
			} else {
				System.out.println("[Warning] invalid kwd " + Common.quote(kwdStrs[i]) + " in " + addrStr);
			}
		}
		return addr;
	}

	/**
	 * parse address line: {ADDRESS_DETAIL_PID[0], BUILDING_NAME[1],
	 * FLAT_NUMBER[2], NUMBER_FIRST[3], NUMBER_FIRST_SUFFIX[4], NUMBER_LAST[5],
	 * NUMBER_LAST_SUFFIX[6], STREET_NAME[7], STREET_TYPE_CODE[8],
	 * STREET_SUFFIX_TYPE[9], LOCALITY_NAME[10], STATE_ABBREVIATION[11],
	 * POSTCODE[12]]}
	 */
	public static Addr newAddr(String line, KwdSet kwdSet) {
		Addr addr = new Addr();
		try {
			line = line.replaceAll(" +", " ").replaceAll(" ,", ",").replaceAll(", ", ",");
			String addrItems[] = line.toLowerCase().trim().split(",");
			if (addrItems.length < 13) {
				return null; // invalid address line
			}
			// 1. building name[]: "dolphin square"
			if (!addrItems[1].isEmpty()) {
				String bldgNames[] = addrItems[1].split(" ");
				addr.m_bldgNames = new Kwd[bldgNames.length];
				for (int i = 0; i < bldgNames.length; i++) {
					String bldgName = bldgNames[i];
					addr.m_bldgNames[i] = kwdSet.add(bldgName);
					addr.m_bldgNames[i].addAddr(addr);
				}
			}
			// 2. flat number: "12"
			String flatNum = addrItems[2];
			if (!flatNum.isEmpty()) {
				if (flatNum.contains(" ")) {
					System.out.println("[Warning] invalid FLAT_NUMBER " + Common.quote(flatNum) + ". " + line);
				} else {
					addr.m_flatNum = kwdSet.add(flatNum);
					addr.m_flatNum.addAddr(addr);
				}
			}
			// 3-6. street numbers: "12a-13b"
			String numFirst = addrItems[3];
			if (!numFirst.isEmpty()) {
				if (!Common.isInteger(numFirst)) {
					System.out.println("[Warning] invalid NUMBER_FIRST " + Common.quote(numFirst) + ". " + line);
				} else {
					addr.m_numFirst = Integer.parseInt(numFirst);
				}
			}
			addr.m_numFirstSuffix = addrItems[4].intern();
			String numLast = addrItems[5];
			if (!numLast.isEmpty()) {
				if (!Common.isInteger(numLast)) {
					System.out.println("[Warning] invalid NUMBER_LAST " + Common.quote(numLast) + ". " + line);
				} else {
					addr.m_numLast = Integer.parseInt(numLast);
				}
			}
			addr.m_numLastSuffix = addrItems[6].intern();
			// add street numbers[] to kwds[], e.g., 11-15, add 12, 13, 14
			if ((addr.m_numFirst != null) && (addr.m_numLast != null)) {
				for (Integer streetNum = addr.m_numFirst + 1; streetNum < addr.m_numLast; streetNum++) {
					kwdSet.add(streetNum.toString()).addAddr(addr);
				}
			}
			if (addr.m_numFirst != null) {
				kwdSet.add((addr.m_numFirst + addr.m_numFirstSuffix).toString()).addAddr(addr);
			}
			if (addr.m_numLast != null) {
				kwdSet.add((addr.m_numLast + addr.m_numLastSuffix).toString()).addAddr(addr);
			}
			// 7. street name[]: "james ruse"
			if (!addrItems[7].isEmpty()) {
				String streetNames[] = addrItems[7].split(" ");
				addr.m_streetNames = new Kwd[streetNames.length];
				for (int i = 0; i < streetNames.length; i++) {
					addr.m_streetNames[i] = kwdSet.add(streetNames[i]);
					addr.m_streetNames[i].addAddr(addr);
				}
			}
			// 8. street type: street, road, drive
			String streetType = addrItems[8];
			if (!streetType.isEmpty()) {
				if (streetType.contains(" ")) {
					System.out.println("[Warning] invalid STREET_TYPE_CODE " + Common.quote(streetType) + ". " + line);
				} else {
					addr.m_streetType = kwdSet.add(streetType);
					addr.m_streetType.addAddr(addr);
				}
			}
			// 9. street suffix: west, east
			String streetSuffix = addrItems[9];
			if (!streetSuffix.isEmpty()) {
				if (streetSuffix.contains(" ")) {
					System.out.println(
							"[Warning] invalid STREET_SUFFIX_TYPE " + Common.quote(streetSuffix) + ". " + line);
				} else {
					addr.m_streetSuffix = kwdSet.add(streetSuffix);
					addr.m_streetSuffix.addAddr(addr);
				}
			}
			// 10. suburb: north ryde, sydney
			if (!addrItems[10].isEmpty()) {
				String localityNames[] = addrItems[10].split(" ");
				addr.m_localityNames = new Kwd[localityNames.length];
				for (int i = 0; i < localityNames.length; i++) {
					addr.m_localityNames[i] = kwdSet.add(localityNames[i]);
					addr.m_localityNames[i].addAddr(addr);
				}
			}
			// 11. state abbreviation: nsw, vic
			String stateAbbr = addrItems[11];
			if (!stateAbbr.isEmpty()) {
				if (stateAbbr.contains(" ")) {
					System.out.println("[Warning] invalid STATE_ABBREVIATION " + Common.quote(stateAbbr) + ". " + line);
				} else {
					addr.m_stateAbbr = kwdSet.add(stateAbbr);
					addr.m_stateAbbr.addAddr(addr);
				}
			}
			// 12. post code: 2118
			String postCode = addrItems[12];
			if (!postCode.isEmpty()) {
				if (postCode.contains(" ")) {
					System.out.println("[Warning] invalid POSTCODE " + Common.quote(postCode) + ". " + line);
				} else {
					addr.m_postCode = kwdSet.add(postCode);
					addr.m_postCode.addAddr(addr);
				}
			}
			addr.m_length = getLen(addr);
			return addr;
		} catch (Exception e) {
			return null;
		}
	}

	public static int getLen(Addr addr) {
		int len = 0;
		len += (addr.m_flatNum == null ? 0 : 1);
		len += (addr.m_numFirst == null ? 0 : 1);
		len += (addr.m_numLast == null ? 0 : 1);
		len += addr.m_streetNames.length;
		len += (addr.m_streetType == null ? 0 : 1);
		len += (addr.m_streetSuffix == null ? 0 : 1);
		len += addr.m_localityNames.length;
		return len;
	}

	private static String kwdsStr(Kwd kwds[]) {
		if (kwds == null || kwds.length == 0) {
			return new String();
		}
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < kwds.length; i++) {
			sb.append((i == 0) ? "" : " ").append(kwds[i].toString());
		}
		return sb.toString();
	}

	/** generate standard address string */
	// CommFunc.getString()
	public static String toStdAddr(Addr addr) {
		// 1. flat number
		String flatNum = CommFunc.getString(addr.m_flatNum);
		// 2. street numbers
		String streetNum = CommFunc.getString(addr.m_numFirst) + CommFunc.getString(addr.m_numFirstSuffix);
		String streetNumLast = CommFunc.getString(addr.m_numLast) + CommFunc.getString(addr.m_numLastSuffix);
		if (!streetNumLast.isEmpty()) {
			streetNum += "-" + streetNumLast;
		}
		// 3. building names
		String bldgNames = kwdsStr(addr.m_bldgNames);
		// standard address
		String addrStr = flatNum + (flatNum.isEmpty() ? "" : "/") + streetNum + " " + kwdsStr(addr.m_streetNames) + " "
				+ CommFunc.getString(addr.m_streetSuffix) + " " + CommFunc.getString(addr.m_streetType) + " "
				+ kwdsStr(addr.m_localityNames) + " " + CommFunc.getString(addr.m_stateAbbr) + " "
				+ CommFunc.getString(addr.m_postCode) + (bldgNames.isEmpty() ? "" : " (" + bldgNames + ")");
		return addrStr.replaceAll(" +", " ").trim();
	}

	// TODO Verify
	public int compareTo(Addr o) {
		return this.m_length - o.m_length;
	}
}