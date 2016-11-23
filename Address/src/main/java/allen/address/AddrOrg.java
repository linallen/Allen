package allen.address;

import java.util.Arrays;
import java.util.HashSet;

import allen.base.common.Common;

public class AddrOrg {
	String ADDRESS_DETAIL_PID;
	String BldgName;
	String FlatNum;
	String NumFirst;
	String NumFirstSuffix;
	String NumLast;
	String NumLastSuffix;
	String StreetName;
	String StreetType;
	String StreetSuffix;
	String LocalityName;
	String StateAbbr;
	String PostCode;
	String StdAddr; // [optional] standard address
	HashSet<String> m_keySet = new HashSet<String>();

	public HashSet<String> keys() {
		return m_keySet;
	}

	private void addKwds(String stdAddr) {
		stdAddr = stdAddr.replaceAll("/", " ");
		stdAddr = stdAddr.replaceAll("-", " ");
		stdAddr = stdAddr.replaceAll("\\(", " ");
		stdAddr = stdAddr.replaceAll("\\)", " ");
		String keys[] = stdAddr.trim().replaceAll(" +", " ").split(" ");
		for (String key : keys) {
			m_keySet.add(key.intern());
		}
	}

	public AddrOrg(String line) {
		String addrItems[] = line.toLowerCase().trim().replaceAll(" +", " ").split(",");
		for (int i = 0; i < addrItems.length; i++) {
			addrItems[i] = addrItems[i].trim();
		}
		BldgName = addrItems[1];
		String bldgNameItems[] = BldgName.split(" ");
		m_keySet.addAll(Arrays.asList(bldgNameItems));
		FlatNum = addrItems[2];
		NumFirst = addrItems[3];
		Common.Assert(!NumFirst.isEmpty()); // ?
		NumFirstSuffix = addrItems[4];
		NumLast = addrItems[5];
		NumLastSuffix = addrItems[6];
		// add street numbers[] too
		StreetName = addrItems[7];
		StreetType = addrItems[8];
		StreetSuffix = addrItems[9];
		LocalityName = addrItems[10];
		StateAbbr = addrItems[11];
		PostCode = addrItems[12];
		// generate standard address
		String StreetNum = (NumFirst + NumFirstSuffix) + (NumLast.isEmpty() ? "" : "-" + NumLast + NumLastSuffix);
		StreetName += StreetSuffix.isEmpty() ? "" : (" " + StreetSuffix);
		StdAddr = FlatNum + (FlatNum.isEmpty() ? "" : "/") + StreetNum + " " + StreetName + " " + StreetType + " "
				+ LocalityName + " " + StateAbbr + " " + PostCode + (BldgName.isEmpty() ? "" : " (" + BldgName + ")");
		addKwds(StdAddr);
	}
}