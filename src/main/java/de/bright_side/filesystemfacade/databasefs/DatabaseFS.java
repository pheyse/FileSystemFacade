package de.bright_side.filesystemfacade.databasefs;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import com.google.gson.Gson;

import de.bright_side.filesystemfacade.facade.FSFEnvironment;
import de.bright_side.filesystemfacade.facade.FSFFile;
import de.bright_side.filesystemfacade.facade.FSFSystem;
import de.bright_side.filesystemfacade.util.FSFFileUtil;

/**
 * @author Philip Heyse
 *
 */
public class DatabaseFS implements FSFSystem{
	private static final boolean LOGGING_ENABLED = false;

	public static String SEPARATOR = "/";

	//: https://support.microsoft.com/de-de/help/196271/when-you-try-to-connect-from-tcp-ports-greater-than-5000-you-receive-t
	//: if the test opens and closes a lot of connections, on windows this is a problem for testing
	/*
    1. Start Registry Editor.
    2. Locate the following subkey in the registry, and then click Parameters:
        HKEY_LOCAL_MACHINE\SYSTEM\CurrentControlSet\Services\Tcpip\Parameters
    3. On the Edit menu, click New, and then add the following registry entry:
	    Value Name: MaxUserPort
	    Value Type: DWORD
	    Value data: 65534
	    Valid Range: 5000-65534 (decimal)
	    Default: 0x1388 (5000 decimal)
	    Description: This parameter controls the maximum port number that is used when a program requests any available user port from the system. Typically, ephemeral (short-lived) ports are allocated between the values of 1024 and 5000 inclusive. After the release of security bulletin MS08-037, the behavior of Windows Server 2003 was changed to more closely match that of Windows Server 2008 and Windows Vista. For more information about Microsoft security bulletin MS08-037, click the following article numbers to view the articles in the Microsoft Knowledge Base:
	
	    951746 MS08-037: Description of the security update for DNS in Windows Server 2008, in Windows Server 2003, and in Windows 2000 Server (DNS server-side): July 8, 2008
	
	    951748 MS08-037: Description of the security update for DNS in Windows Server 2003, in Windows XP, and in Windows 2000 Server (client side): July 8, 2008
	
	    953230 MS08-037: Vulnerabilities in DNS could allow spoofing

    4. Exit Registry Editor, and then restart the computer.
	 */
	
	private DatabaseFSConfig config;
	private FSFEnvironment environment;
	
	public DatabaseFS(DatabaseFSConfig config) throws Exception{
		this(config, FSFFileUtil.createDefaultEnvironment());
	}

	public DatabaseFS(DatabaseFSConfig config, FSFEnvironment environment) throws Exception{
		this.config = copyByValue(config);
		this.environment = environment;
		if (config.getDbConnection() == null) {
			List<String> drivers = getAvailableDBDrivers();
			if (!drivers.contains(config.getDbDriverClassName())) {
				String availableDriversInfo;
				if (drivers.isEmpty()) {
					availableDriversInfo = "There are no avaialable drivers.";
				} else {
					availableDriversInfo = "Available drivers: " + drivers;
				}
				throw new Exception("given driver class name is '" + config.getDbDriverClassName() + "' but the driver is not available. " + availableDriversInfo);
			}
		}
		
		boolean tableExists = DatabaseFSUtil.doesFileTableExist(config);
		if (!tableExists) {
			if (!config.isAutoCreateTable()) {
				throw new Exception("The file table does not exist and autoCreateTable is set to false in the DatabaseFSConfig");
			}
			try {
				DatabaseFSUtil.createFileTable(config);
			} catch (Exception e) {
				throw new Exception("Could not create file table", e);
			}
			if (!DatabaseFSUtil.doesFileTableExist(config)) {
				throw new Exception("Table should have been created but doesn't exist");
			}
		}
	}
	
	public static Connection createConnection(DatabaseFSConfig config) throws Exception {
		return DatabaseFSUtil.getConnection(config);
	}

	private void log(String message) {
		if (LOGGING_ENABLED) {
			System.out.println("DatabaseFS> " + message);
		}
	}

	private DatabaseFSConfig copyByValue(DatabaseFSConfig config) {
		DatabaseFSConfig result = null;
		Gson gson = new Gson();
		String json = gson.toJson(config);
		result = gson.fromJson(json, DatabaseFSConfig.class);
		result.setDbConnection(config.getDbConnection());
		return result;
	}

	@Override
	public List<FSFFile> listRoots() {
		List<FSFFile> result = new ArrayList<>();
		result.add(DatabaseFile.createRootDatabaseFile(this));
		return result;
	}

	@Override
	public FSFFile createByPath(String path) throws Exception {
		String[] items = path.split(SEPARATOR);
		
		String currentAbsolutePath = "";

		log("createByPath: path = '" + path + "'");
		
		FSFFile file = DatabaseFile.createRootDatabaseFile(this);
		boolean first = true;
		for (String i: items) {
			log("createByPath: item = '" + i + "'");

			if (first) {
				if (!i.isEmpty()) {
					throw new Exception("absolute path must start with '" + SEPARATOR + "'");
				}
				file = DatabaseFile.createRootDatabaseFile(this);
				first = false;
				log("createByPath: created root file file");
			} else {
				file = file.getChild(i);
				currentAbsolutePath += SEPARATOR + i;
				((DatabaseFile) file).setCachedAbsolutePath(currentAbsolutePath);
				log("createByPath: created file with absolute path '" + currentAbsolutePath + "'");
			}
		}
		return file;
	}
	
	public static List<String> getAvailableDBDrivers() {
		List<String> result = new ArrayList<String>();
		for (Enumeration<?> en = DriverManager.getDrivers(); en.hasMoreElements();){
			result.add(en.nextElement().getClass().getName());
		}
		return result;
	}

	protected DatabaseFSConfig getConfig() {
		return config;
	}

	protected FSFEnvironment getEnvironment() {
		return environment;
	}

	@Override
	public String getSeparator() {
		return SEPARATOR;
	}

}
