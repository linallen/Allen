package allen.address.keyaddr;

import java.util.HashSet;

import aai.base.common.Common;

public class AddrOrg {
	// private String ADDRESS_DETAIL_PID;
	private String BldgName;
	private String FlatNum;
	private String NumFirst;
	private String NumFirstSuffix;
	private String NumLast;
	private String NumLastSuffix;
	private String StreetName;
	private String StreetType;
	private String StreetSuffix;
	private String LocalityName;
	private String StateAbbr;
	private String PostCode;

	private String m_stdAddr; // standard address

	private HashSet<String> m_keySet = new HashSet<String>();
	/** address's real length, e.g., "3/2a-6 cleveland st" = 4 */
	private int m_length;

	public HashSet<String> keys() {
		return m_keySet;
	}

	public String stdAddr() {
		return m_stdAddr;
	}

	public int length() {
		return m_length;
	}

	// str must be lowercase
	private void addKwds(String str) {
		// replace all non-numeric-alphabet characters to spaces
		String keys[] = CommFunc.retainAlphaNum(str);
		for (String key : keys) {
			if (!key.isEmpty()) {
				m_keySet.add(key.intern());
			}
		}
	}

	public AddrOrg(String line) {
		String addrItems[] = line.toLowerCase().trim().replaceAll(" +", " ").split(",");
		for (int i = 0; i < addrItems.length; i++) {
			addrItems[i] = addrItems[i].trim().replace(" +", " ");
		}
		BldgName = addrItems[1];
		addKwds(BldgName);
		FlatNum = addrItems[2];
		NumFirst = addrItems[3];
		Common.Assert(!NumFirst.isEmpty()); // ?
		NumFirstSuffix = addrItems[4];
		NumLast = addrItems[5];
		NumLastSuffix = addrItems[6];
		// add street numbers[] to kwds[], e.g., 11-15, add 12, 13, 14
		if (!NumLast.isEmpty()) {
			int numFirstInt = Integer.parseInt(NumFirst);
			int numLastInt = Integer.parseInt(NumLast);
			Common.Assert(numFirstInt <= numLastInt);
			for (int streetNum = numFirstInt + 1; streetNum < numLastInt; streetNum++) {
				addKwds(streetNum + "");
			}
		}
		//
		StreetName = addrItems[7];
		StreetType = addrItems[8];
		StreetSuffix = addrItems[9];
		LocalityName = addrItems[10];
		StateAbbr = addrItems[11];
		PostCode = addrItems[12];
		// generate standard address
		String StreetNum = (NumFirst + NumFirstSuffix) + (NumLast.isEmpty() ? "" : "-" + NumLast + NumLastSuffix);
		StreetName += StreetSuffix.isEmpty() ? "" : (" " + StreetSuffix);
		m_stdAddr = FlatNum + (FlatNum.isEmpty() ? "" : "/") + StreetNum + " " + StreetName + " " + StreetType + " "
				+ LocalityName + " " + StateAbbr + " " + PostCode + (BldgName.isEmpty() ? "" : " (" + BldgName + ")");
		addKwds(m_stdAddr);
		// calculate real length of address
		String str = FlatNum + " " + NumFirst + " " + StreetName + " " + StreetType + " " + StreetSuffix + " "
				+ LocalityName + " " + StateAbbr + " " + PostCode;
		m_length = str.trim().replaceAll(" +", " ").split(" ").length;
	}
}