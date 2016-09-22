package student;

import distribution.Distribution;

/** Generate "libgate" <stuId, gate, enter_time, record> */
public class GenLibgate {

	public String getTitle() {
		return "stuId, gate, enter_time, record";
	}

	/** generate log */
	public String genLog(int stuId, Distribution dist, Object... paras) {
		double p = dist.P(paras);
		assert ((p >= 0) && (p <= 1));

		return null;
	}
}