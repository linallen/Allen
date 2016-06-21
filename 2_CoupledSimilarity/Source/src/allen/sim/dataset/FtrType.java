package allen.sim.dataset;

/** feature type: Numeric, Categorical, String or Date */
public enum FtrType {
	NUMERIC("Numeric"), CATEGORICAL("Categorical"), STRING("String"), DATE("Date");
	private String m_type;

	/** constructors *********************************************/
	FtrType(String type) {
		m_type = type;
	}

	/** property functions ***************************************/
	/** get feature type object from String */
	public static FtrType getFtrType(String typeStr) throws Exception {
		for (FtrType ftrType : FtrType.values()) {
			if (ftrType.toString().equalsIgnoreCase(typeStr)) {
				return ftrType;
			}
		}
		throw new Exception("Wrong feature type: " + typeStr);
	}

	/** output functions *****************************************/
	public String toString() {
		return m_type.toLowerCase();
	}
}
