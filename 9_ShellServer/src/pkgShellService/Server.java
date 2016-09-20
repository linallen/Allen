package pkgShellService;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

import allen.base.common.Common;
import allen.base.module.AAI_Module;
import allen.base.module.Module;
import pkgShellService.User.UserType;

/**
 * Server is responsible for<br>
 * 1. listen and accepting client connections,<br>
 * 2. manage services, modules, and users.
 * 
 * Server can run as a System Service.
 * 
 * Syntax & Example:<br>
 * Java -jar Server.jar [-n server_name] [-p port] [-d work_dir] [-debug]<br>
 * Java -jar Server.jar -p 9999 -d "c:/AAI_Server" -n "AAI Server"
 * 
 * -n server_name: server's name.<br>
 * -p port: server's listening port.<br>
 * -d work_dir: server's work directory.<br>
 * -debug: debug version
 * 
 * @author Allen Lin, 25 Nov 2014
 */
public class Server extends AAI_Module {
	private static final long serialVersionUID = -4446808732040585634L;

	private int DFT_PORT = 9999; // default listening port
	private String DFT_WORKDIR = "C:/AAI_Server"; // default work directory
	private String DFT_NAME = "AAI_Server"; // default server name

	/** server listening port */
	private int m_port;

	/** global module set, modules can be AAI_Module or non-AAI_Module */
	public static ConcurrentHashMap<String, Module> m_modules = new ConcurrentHashMap<String, Module>();

	/** System Service start() entry */
	public static void start(String args[]) {
		Server server = null;
		try {
			server = new Server();
			server.setOptions(args);
			server.start();
			server.join();
		} catch (Exception e) {
			server.output("Server start exception: " + e.getMessage());
		}
	}

	/** System Service stop() entry */
	public static void stop(String arg[]) {
		System.out.println("Server stopped successfully.");
	}

	/** [-n server_name] [-p port] [-d work_dir] */
	public void setOptions(String[] options) throws Exception {
		// [-n server_name]
		m_name = Common.getOption("n", options);
		name(!m_name.isEmpty() ? m_name : DFT_NAME);
		// [-p port]
		m_port = Common.getOptionInt("p", options, DFT_PORT);
		// [-d work_dir]
		m_workDir = Common.getOption("d", options);
		updateDirs(!m_workDir.isEmpty() ? m_workDir : DFT_WORKDIR);
		// [-debug, etc]
		super.setOptions(options);
	}

	@Override
	public void run() {
		// Initialize users[] with the "admin" user
		User admin = new User(null, UserType.ADMIN, null);
		admin.updateDirs(this.workDir() + admin.name());
		User.m_users.put(admin.name(), admin);

		// 1. create server socket on port
		ServerSocket svrSocket = null;
		try {
			svrSocket = new ServerSocket(m_port);
		} catch (Exception e) {
			output("Server socket() exception: " + e.getMessage());
			return;
		}
		output("Server started. Listening on port " + m_port + ", work_dir = " + Common.quote(workDir()) + ", debug = "
				+ (Boolean) debug());

		// 2. listen connections and create "service" thread to response clients
		while (true) {
			Socket cltSkt = null;
			try {
				cltSkt = svrSocket.accept();
				// cltSkt.setKeepAlive(true);
				// to avoid send multiple messages together to client
				// cltSkt.setTcpNoDelay(true);
			} catch (Exception e) {
				output("Server accept() exception: " + e.getMessage());
				return;
			}
			String cltAddr = Common.IpAddrPort(cltSkt);
			output("Client connected: " + cltAddr);
			Service service = null;
			try {
				service = new Service(this, cltSkt);
				service.start();
			} catch (Exception e) {
				output("Failed to start service for client " + cltAddr + ": " + e.getMessage());
				continue;
			}
			Service.m_services.add(service);
		}
	}

	public static String version() {
		return "v0.1.5, 21 May 2015, service outstream bug fixed.\n" + "v0.1.4, 1 May 2015, stable version.\n"
				+ "v0.1.3 debugging, client JSON broken up due to small buffer.\n"
				+ "v0.1.2, 11 Jan 2015, changed \"CMDResp\" message to [MsgType:\"CMD\", CmdID, CmdBody[]] to support command batch from client.\n"
				+ "v0.1.2, 9 Jan 2015, revised STATUS response JSON format to [STATUS, TaskName, TaskStatus, Percentage, Detail], "
				+ "where Detail is a new filed. When STATUS = EXCEPTION, Detail = message + stack.\n"
				+ "v0.1.1, 19 Dec 2014, Debug 2\n" + "v0.1, created on 25 Nov 2014, Allen Lin";
	}

	public static String help() {
		return "Usage:\nJava -jar Server.jar [-p port] [-d work_dir] [-n server_name] [-D]\n"
				+ "-n server_name: server's name.\n" + "-p port: server's listening port.\n"
				+ "-d work_dir: server's work directory.\n"
				+ "-D: debug version if set or released version otherwise.\n\n"
				+ "Example: Java -jar Server.jar -p 9999 -d \"c:/AAI_Server\" -n \"AAI Server\"";
	}

	/** demo */
	public static void main(String[] args) {
		System.out.println(version() + "\n");
		if (args.length == 0) {
			System.out.println(help());
		}
		Server.start(args);
	}
}