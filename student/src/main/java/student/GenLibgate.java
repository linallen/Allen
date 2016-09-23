package student;

import java.util.List;

import org.joda.time.DateTime;

import distribution.Distribution;

/** Generate "libgate" <stuId, enter_time, gate, record> */
public class GenLibgate {

	public static String getTitle() {
		return "stuId, enter_time, gate, record";
	}

	/** generate "libgate" logs <stuId, enter_time, gate, record> */
	public static String genCSV(List<DateTime> between, String stuId, Distribution dist, Object... paras) {
		StringBuffer sb = new StringBuffer().append(getTitle() + "\n");
		for (DateTime d : between) {
			d = DTime.randomTime(d);
			if (dist.hit(paras)) {
				sb.append(stuId + DTime.text(d) + ",Gate 1,Record\n");
			}
		}
		return sb.toString();
	}
}