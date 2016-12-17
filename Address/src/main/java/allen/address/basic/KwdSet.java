package allen.address.basic;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import aai.base.common.AAI_IO;
import aai.base.common.Common;
import aai.base.common.Timer;
import aai.base.module.AAI_Module;

/** Global Keyword Set: <kwdStr, kwdObj> */
public class KwdSet extends AAI_Module {
	private static final long serialVersionUID = -8763440357696765066L;

	/** global kwds[] */
	public HashMap<String, Kwd> m_kwdSet = new HashMap<String, Kwd>();

	public int size() {
		return m_kwdSet.size();
	}

	public Kwd get(String kwdStr) {
		return m_kwdSet.get(kwdStr);
	}

	public Collection<String> getStrs() {
		return m_kwdSet.keySet();
	}

	public Collection<Kwd> getKwds() {
		return m_kwdSet.values();
	}

	public Kwd add(String kwdStr) {
		try {
			kwdStr = kwdStr.intern();
			Kwd kwd = m_kwdSet.get(kwdStr);
			if (kwd == null) {
				kwd = new Kwd(kwdStr);
				m_kwdSet.put(kwdStr, kwd);
			}
			return kwd;
		} catch (Exception e) {
			return null;
		}
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		for (String kwdStr : m_kwdSet.keySet()) {
			buf.append(get(kwdStr) + "\n");
		}
		return buf.toString();
	}

	/** save [kwd, length, size, ratio%, addrs[]] */
	public void save(String fileName) throws Exception {
		output("Started saving kwds[" + size() + "] to file " + fileName);
		Timer timer = new Timer();
		ArrayList<Kwd> kwds = new ArrayList<Kwd>(m_kwdSet.values());
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < kwds.size(); i++) {
			progress(i + 1, kwds.size());
			Kwd kwd = kwds.get(i);
			// [kwd, length, size, ratio%, addrs[]]
			OrderedLst addrLst = kwd.hostAddrs();
			int length = addrLst.objNum();
			int size = addrLst.intNum();
			int ratio = 100 * length / size;
			sb.append(kwd.str() + "," + length + "," + size + "," + ratio + "%,");
			for (int j = 0; j < addrLst.objNum(); j++) {
				sb.append(j > 0 ? " " : "");
				Object addrId = addrLst.get(j);
				if (addrId instanceof Integer) {
					sb.append((Integer) addrId);
				} else if (addrId instanceof Range) {
					Range range = (Range) addrId;
					sb.append(range.min + "~" + range.max);
				} else {
					throw new Exception("Invalid addrId " + Common.quote(addrId.toString()));
				}
			}
			sb.append("\n");
		}
		AAI_IO.saveFile(fileName, sb.toString());
		output("Finished saving kwds[" + size() + "] to file. " + timer);
	}

	public boolean saveOld(String fileName) {
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(fileName));
			ArrayList<Kwd> kwds = new ArrayList<Kwd>(m_kwdSet.values());
			output("Started saving kwds[" + kwds.size() + "] to file " + fileName);
			Timer timer = new Timer();
			for (int i = 0; i < kwds.size(); i++) {
				progress(i + 1, kwds.size());
				Kwd kwd = kwds.get(i);
				// [kwd, length, size, ratio%, addrs[]]
				// return kwd.m_kwdStr + "," + length + "," + size + "," + ratio
				// + "%," + OrderedLst.toString(kwd.m_addrIds);
				int length = kwd.hostAddrs().objNum();
				int size = kwd.hostAddrs().intNum();
				int ratio = 100 * length / size;
				// bw.write(kwd.str() + "," + length + "," + size + "," + ratio
				// + "%," + OrderedLst.toString(kwd.m_addrIds);
				// bw.write(Kwd.toString(kwd) + "\n");
			}
			bw.close();
			output("Finished saving kwds[" + kwds.size() + "] to file. " + timer);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/** TODO load [kwd, length, size, ratio%, addrs[]] */
	public void load(String kwdsFile) {
		output("Started loading kwds[] from file " + kwdsFile);
		Timer timer = new Timer();
		String buf = AAI_IO.readFile(kwdsFile);
		String lines[] = buf.split("\n");
		for (int i = 0; i < lines.length; i++) {
			progress(i + 1, lines.length);
			String line = lines[i];
			// 1. kwd
			int pos = line.indexOf(',');
			String kwdStr = line.substring(0, pos);
			Kwd kwd = add(kwdStr);
			// 2. add addrIds[]
			pos = line.lastIndexOf(',');
			String addrIdsStr = line.substring(pos + 1, line.length());
			String addrIds[] = addrIdsStr.split(" ");
			for (String addrId : addrIds) {
				if (addrId.contains("~")) {
					String numbers[] = addrId.split("~");
					Common.Assert(numbers.length == 2);
					int min = Integer.parseInt(numbers[0]);
					int max = Integer.parseInt(numbers[1]);
					for (int num = min; num <= max; num++) {
						kwd.addAddr(num);
					}
				} else {
					kwd.addAddr(Integer.parseInt(addrId));
				}
			}
		}
		output("Finished loading kwds[" + m_kwdSet.size() + "] from file. " + timer);
	}
}