package backup;

public enum AddrCSV {
	ADDRESS_DETAIL_PID,
	BUILDING_NAME,
	FLAT_NUMBER,
	NUMBER_FIRST,
	NUMBER_FIRST_SUFFIX,
	NUMBER_LAST,
	NUMBER_LAST_SUFFIX,
	STREET_NAME,
	STREET_TYPE_CODE,
	STREET_SUFFIX_TYPE,
	LOCALITY_NAME,
	STATE_ABBREVIATION,
	POSTCODE,
	STD_ADDRESS, // [optional] standard address
	;
	
	public static void main(String[] args) throws Exception {
		System.out.println(AddrCSV.ADDRESS_DETAIL_PID.ordinal());
		System.out.println(AddrCSV.values().length);		
	}
}
