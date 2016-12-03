package allen.address.basic;

import java.io.Serializable;
import java.util.ArrayList;

import aai.base.common.Common;

/** address object */
public class Addr implements Serializable, Comparable<Addr> {
	private static final long serialVersionUID = -1701729465126477598L;

	/** addr id */
	public Integer id;

	/** kwdLst[] */
	private Kwd m_kwdLst[];

	public static int length(Addr addr) {
		return addr.m_kwdLst.length;
	}

	private static void addKwd(String addrStr, KwdSet kwdSet, ArrayList<Kwd> kwdLst, String kwdStr) {
		Kwd kwd = kwdSet.add(kwdStr);
		if (kwd == null) {
			System.out.println("[Warning] discarded invalid kwd " + Common.quote(kwdStr) + " in " + addrStr);
		} else {
			kwdLst.add(kwd);
		}
	}

	/** build an addr from "3b/12a-15b las vagas road | village" */
	public static Addr readAddr(String addrStr, KwdSet kwdSet, Integer addrId) {
		addrStr = addrStr.replaceAll("/", " ");
		addrStr = addrStr.replaceAll("\\|", " ");
		String kwdStrs[] = addrStr.replaceAll(" +", " ").trim().split(" ");

		ArrayList<Kwd> kwdLst = new ArrayList<Kwd>();
		for (String kwdStr : kwdStrs) {
			if (kwdStr.contains("-")) {
				// parse & and street numbers "12a-15b" to kwds[]
				// e.g., 12a-15b, add 12a, 13, 14, 15b
				String numbers[] = kwdStr.split("-");
				Common.Assert(numbers.length == 2);
				addKwd(addrStr, kwdSet, kwdLst, numbers[0]); // first number
				addKwd(addrStr, kwdSet, kwdLst, numbers[1]); // last number
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
					addKwd(addrStr, kwdSet, kwdLst, streetNum.toString());
				}
			} else {
				addKwd(addrStr, kwdSet, kwdLst, kwdStr);
			}
		}

		Addr addr = new Addr();
		addr.id = addrId;
		for (Kwd kwd : kwdLst) {
			kwd.addAddr(addrId);
		}
		addr.m_kwdLst = kwdLst.toArray(new Kwd[0]);
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

			line = line.replaceAll(" +", " ").replaceAll(" ,", ",").replaceAll(", ", ",");
			String items[] = line.toLowerCase().trim().split(",");
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
		for (int i = 0; i < addr.m_kwdLst.length; i++) {
			sb.append(Kwd.str(addr.m_kwdLst[i]) + " ");
		}
		return sb.toString();
	}
}