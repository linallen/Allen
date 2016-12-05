package allen.address.basic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;

import aai.base.common.Common;

/** address object */
public class Addr implements Serializable, Comparable<Addr> {
	private static final long serialVersionUID = -1701729465126477598L;

	/** addr id */
	public Integer id;

	/** kwd list[] (the kwds[] of addr is used for result display ONLY) */
	private Kwd m_bldgNames[], m_flatNum;
	private Kwd m_numFirst, m_numLast;
	/** other kwds[] + middle street numbers besides specific ones above */
	private Kwd m_kwdLst[];

	public static int length(Addr addr) {
		return addr.m_kwdLst.length;
	}

	/** build an addr from "3b/12a-15b las vagas road | village" */
	public static Addr readAddr(String addrStr, KwdSet kwdSet, Integer addrId) {
		Addr addr = new Addr();
		addr.id = addrId;
		HashSet<Kwd> addrAllKwds = new HashSet<Kwd>();

		// addrStr = addrStr.replaceAll(" +", " ").trim();
		// 1. extract building names[]
		int pos = addrStr.indexOf('|');
		if (pos > 0) {
			String bldgNames[] = addrStr.substring(pos + 1, addrStr.length()).trim().split(" ");
			addr.m_bldgNames = new Kwd[bldgNames.length];
			for (int i = 0; i < bldgNames.length; i++) {
				addr.m_bldgNames[i] = kwdSet.add(bldgNames[i]);
				addrAllKwds.add(addr.m_bldgNames[i]);
			}
			addrStr = addrStr.substring(0, pos).trim();
		}
		// 2. extract flat number
		pos = addrStr.indexOf('/');
		if (pos > 0) {
			String flatNum = addrStr.substring(0, pos).trim();
			addr.m_flatNum = kwdSet.add(flatNum);
			addrAllKwds.add(addr.m_flatNum);
			addrStr = addrStr.substring(pos + 1, addrStr.length()).trim();
		}
		// 3. extract streetFirstNum-streetLastNum and other kwds[]
		String kwdStrs[] = addrStr.split(" ");
		addr.m_kwdLst = new Kwd[kwdStrs.length];
		ArrayList<Kwd> addrDispKwds = new ArrayList<Kwd>();
		for (String kwdStr : kwdStrs) {
			if (kwdStr.isEmpty()) {
				continue;
			}
			if (kwdStr.contains("-")) {
				// parse & and street numbers "12a-15b" to kwds[]
				// e.g., 12a-15b, add 12a, 13, 14, 15b
				String numbers[] = kwdStr.split("-");
				Common.Assert(numbers.length == 2);
				addr.m_numFirst = kwdSet.add(numbers[0]);
				addr.m_numLast = kwdSet.add(numbers[1]);
				addrAllKwds.add(addr.m_numFirst);
				addrAllKwds.add(addr.m_numLast);
				Integer numFirst, numLast;
				try {
					numFirst = Integer.parseInt(CommFunc.retainDigits(numbers[0]));
					numLast = Integer.parseInt(CommFunc.retainDigits(numbers[1]));
				} catch (Exception e) {
					System.out.println(
							"[Warning] discarded invalid street number " + Common.quote(kwdStr) + " in " + addrStr);
					continue;
				}
				for (Integer streetNum = numFirst + 1; streetNum < numLast; streetNum++) {
					addrAllKwds.add(kwdSet.add(streetNum.toString()));
				}
			} else {
				Kwd kwd = kwdSet.add(kwdStr);
				addrAllKwds.add(kwd);
				addrDispKwds.add(kwd);
			}
		}

		for (Kwd kwd : addrAllKwds) {
			kwd.addAddr(addrId);
		}
		addr.m_kwdLst = addrDispKwds.toArray(new Kwd[0]);
		return addr;
	}

	/**
	 * parse address line: {ADDRESS_DETAIL_PID[0], BUILDING_NAME[1],
	 * FLAT_NUMBER[2], NUMBER_FIRST[3], NUMBER_FIRST_SUFFIX[4], NUMBER_LAST[5],
	 * NUMBER_LAST_SUFFIX[6], STREET_NAME[7], STREET_TYPE_CODE[8],
	 * STREET_SUFFIX_TYPE[9], LOCALITY_NAME[10], STATE_ABBREVIATION[11],
	 * POSTCODE[12]]}
	 */
	public static Addr newAddr(String line, KwdSet kwdSet, Integer addrId) {
		try {
			String m_bldgNames[], m_flatNum;
			String m_numFirst, m_numFirstSuffix, m_numLast, m_numLastSuffix;
			String m_streetNames[], m_streetType, m_streetSuffix;
			String m_localityNames[], m_stateAbbr, m_postCode;

			// line = line.replaceAll(" +", " ").replaceAll(" ,",
			// ",").replaceAll(", ", ",");
			// line = line.replaceAll(",", " ");
			String items[] = line.toLowerCase().split(",");
			if (items.length < 13) {
				return null; // invalid address line
			}
			// Step 1. parse addr line and generate a standard address string
			// item[1]: building names[] "dolphin square"
			m_bldgNames = items[1].isEmpty() ? null : items[1].split(" ");
			// item[2]: flat number "12"
			m_flatNum = items[2];
			// item[3-6]: street numbers "12a-15b"
			m_numFirst = items[3];
			m_numFirstSuffix = items[4];
			m_numLast = items[5];
			m_numLastSuffix = items[6];
			// item[7]: street names[] "james ruse"
			m_streetNames = items[7].isEmpty() ? null : items[7].split(" ");
			// item[8]: street type "street", "road", "drive", ...
			m_streetType = items[8];
			// item[9]: street suffix "west", "east"
			m_streetSuffix = items[9];
			// item[10]: suburb "north ryde", "sydney"
			m_localityNames = items[10].isEmpty() ? null : items[10].split(" ");
			// item[11]: state abbreviation "nsw", "vic"
			m_stateAbbr = items[11];
			// item[12]: post code "2008"
			m_postCode = items[12];

			// Step 2. build standard addr "3b/12a-15b las vagas road | village"
			// 2. street numbers
			String streetNumFirst = m_numFirst + m_numFirstSuffix;
			String streetNumLast = m_numLast + m_numLastSuffix;
			String connector = (!streetNumFirst.trim().isEmpty() && !streetNumLast.trim().isEmpty()) ? "-" : "";
			String streetNum = streetNumFirst + connector + streetNumLast;
			String addrStr = m_flatNum + (m_flatNum.isEmpty() ? "" : "/") + streetNum + " "
					+ Common.strArraytoStr(m_streetNames) + " " + m_streetSuffix + " " + m_streetType + " "
					+ Common.strArraytoStr(m_localityNames) + " " + m_stateAbbr + " " + m_postCode
					+ (m_bldgNames == null ? "" : (" | " + Common.strArraytoStr(m_bldgNames)));
			return Addr.readAddr(addrStr, kwdSet, addrId);
		} catch (Exception e) {
			return null;
		}
	}

	// TODO Verify
	public int compareTo(Addr addr) {
		return Addr.length(this) - Addr.length(addr);
	}

	public static String toString(Addr addr) {
		// StringBuffer sb = new StringBuffer(addr.id.toString()+" ");
		StringBuffer sb = new StringBuffer();
		// 1. flat number
		if (addr.m_flatNum != null) {
			sb.append(Kwd.str(addr.m_flatNum) + "/");
		}
		// 2. street numbers[]
		if ((addr.m_numFirst != null) && (addr.m_numLast != null)) {
			sb.append(Kwd.str(addr.m_numFirst) + "-" + Kwd.str(addr.m_numLast));
		}
		// 3. other kwds[]
		for (Kwd kwd : addr.m_kwdLst) {
			sb.append(" " + Kwd.str(kwd));
		}
		// 4. building names[]
		if (addr.m_bldgNames != null) {
			sb.append(" |");
			for (Kwd kwd : addr.m_bldgNames) {
				sb.append(" " + Kwd.str(kwd));
			}
		}
		return sb.toString().trim().replaceAll(" +", " ").replaceAll("/ ", "/");
	}
}