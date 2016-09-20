package pkgShellService;

import java.util.concurrent.ConcurrentHashMap;

import allen.base.module.AAI_Module;
import pkgShellService.Return.RetCode;

/**
 * common functions used in Server.
 * 
 * @author Allen Lin, 10 Dec 2014
 */
public class ServerComm {

	/** return view result of AAI_Module objs: users, tasks and modules */
	public static Return viewObjs(ConcurrentHashMap<String, AAI_Module> objSet, String objName, String titleRow) {
		String retMsg = new String();
		if (objName.equalsIgnoreCase("ALL")) {
			if (objSet.isEmpty()) {
				return new Return(RetCode.SUCCESS);
			}
			for (AAI_Module obj : objSet.values()) {
				retMsg += "\n" + obj.description();
			}
			return new Return(RetCode.SUCCESS, titleRow + "\n" + retMsg);
		}
		AAI_Module obj = objSet.get(objName);
		if (obj == null) {
			return new Return(RetCode.WARNING, RetMsg.isExist("user", objName, false));
		}
		return new Return(RetCode.SUCCESS, titleRow + "\n" + obj.description());
	}
}