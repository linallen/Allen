package allen.address.basic;

import java.io.Serializable;
import java.util.HashSet;

import aai.base.common.Common;

/** address object */
public class Addr implements Serializable {
	private static final long serialVersionUID = -1701729465126477598L;

	/** address id */
	public Integer id;

	/** below names will be used to build the address tree */
	public String m_bldgNames, m_flatNum, m_numFirst, m_numLast;
	public String m_stateAbbr, m_postCode, m_streetName, m_streetNo, m_suburb;

	/**
	 * build an addr from
	 * "3b/12a-15b_las vagas road, north epping nsw 2008 | village"
	 */
	public static Addr readAddr(String addrStr, KwdSet kwdSet, Integer addrId) {
		Addr addr = new Addr();
		addr.id = addrId;
		HashSet<Kwd> addrAllKwds = new HashSet<Kwd>();

		addrStr = addrStr.replaceAll(" +", " ").trim();
		// 1. extract building names[]
		int pos = addrStr.indexOf('|');
		if (pos > 0) {
			addr.m_bldgNames = addrStr.substring(pos + 1, addrStr.length()).trim();
			for (String bldgName : addr.m_bldgNames.split(" ")) {
				addrAllKwds.add(kwdSet.add(bldgName));
			}
			addrStr = addrStr.substring(0, pos).trim();
		}
		// 2. extract suburb, state_abbr and post_code "north epping nsw 2110"
		pos = addrStr.indexOf(',');
		if (pos > 0) {
			String items[] = addrStr.substring(pos + 1, addrStr.length()).trim().split(" ");
			if (items.length < 3) {
				return null;
			}
			// TODO
			for (int i = 0; i < items.length - 2; i++) {
				addr.m_suburb = ((i == 0) ? "" : (addr.m_suburb + " ")) + items[i];
				addrAllKwds.add(kwdSet.add(items[i]));
			}
			addr.m_stateAbbr = items[items.length - 2];
			addr.m_postCode = items[items.length - 1];
			addrAllKwds.add(kwdSet.add(addr.m_stateAbbr));
			addrAllKwds.add(kwdSet.add(addr.m_postCode));
			addrStr = addrStr.substring(0, pos).trim();
		}
		// 3. extract flat number
		pos = addrStr.indexOf('/');
		if (pos > 0) {
			addr.m_flatNum = addrStr.substring(0, pos).trim();
			addrAllKwds.add(kwdSet.add(addr.m_flatNum));
			addrStr = addrStr.substring(pos + 1, addrStr.length()).trim();
		}
		// 4. extract streetFirstNum-streetLastNum and street name
		String items[] = addrStr.split("_");
		if (items.length > 2) {
			return null;
		}
		if (items.length == 2) {
			addr.m_streetNo = items[0];
			addr.m_streetName = items[1];
		} else {
			addr.m_streetName = items[0];
		}
		// add street names[] to kwds[]
		for (String streetName : addr.m_streetName.trim().split(" ")) {
			addrAllKwds.add(kwdSet.add(streetName));
		}

		if (addr.m_streetNo != null) {
			// parse & and street numbers "12a-15b" to kwds[]
			// e.g., 12a-15b, add 12a, 13, 14, 15b
			String numbers[] = addr.m_streetNo.split("-");
			if (numbers.length > 2) {
				return null;
			}
			addr.m_numFirst = numbers[0];
			addrAllKwds.add(kwdSet.add(addr.m_numFirst));
			if (numbers.length == 2) {
				addr.m_numLast = numbers[1];
				addrAllKwds.add(kwdSet.add(addr.m_numLast));
				// add street numbers between [numFirst, numLast]
				Integer numFirst, numLast;
				try {
					numFirst = Integer.parseInt(CommFunc.retainDigits(numbers[0]));
					numLast = Integer.parseInt(CommFunc.retainDigits(numbers[1]));
					for (Integer streetNum = numFirst + 1; streetNum < numLast; streetNum++) {
						addrAllKwds.add(kwdSet.add(streetNum.toString()));
					}
				} catch (Exception e) {
					System.out.println("[Warning] discarded invalid street number " + Common.quote(addr.m_streetNo)
							+ " in " + addrStr);
				}
			}
		}

		for (Kwd kwd : addrAllKwds) {
			kwd.addAddr(addrId);
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

			// Step 2. build standard addr
			// "3b/12a-15b_las vagas road, north epping nsw 2008 | village"
			// 2. street numbers
			String streetNumFirst = m_numFirst + m_numFirstSuffix;
			String streetNumLast = m_numLast + m_numLastSuffix;
			String connector = (!streetNumFirst.trim().isEmpty() && !streetNumLast.trim().isEmpty()) ? "-" : "";
			String streetNum = streetNumFirst + connector + streetNumLast;
			String addrStr = m_flatNum + (m_flatNum.isEmpty() ? "" : "/") + streetNum + "_"
					+ Common.strArraytoStr(m_streetNames) + " " + m_streetSuffix + " " + m_streetType + ", "
					+ Common.strArraytoStr(m_localityNames) + " " + m_stateAbbr + " " + m_postCode
					+ (m_bldgNames == null ? "" : (" | " + Common.strArraytoStr(m_bldgNames)));
			return Addr.readAddr(addrStr, kwdSet, addrId);
		} catch (Exception e) {
			return null;
		}
	}
}