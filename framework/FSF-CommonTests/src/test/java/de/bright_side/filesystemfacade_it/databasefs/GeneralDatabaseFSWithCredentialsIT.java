package de.bright_side.filesystemfacade_it.databasefs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Tag;

import de.bright_side.filesystemfacade.databasefs.DatabaseFS;
import de.bright_side.filesystemfacade.databasefs.DatabaseFSConfig;
import de.bright_side.filesystemfacade.databasefs.DatabaseFSTestUtil;
import de.bright_side.filesystemfacade.facade.FSFEnvironment;
import de.bright_side.filesystemfacade.facade.FSFFile;
import de.bright_side.filesystemfacade.facade.FSFSystem;
import de.bright_side.filesystemfacade.facade.GeneralFSTest;
import de.bright_side.filesystemfacade.facade.TestUtil;

@Tag("IT")
public class GeneralDatabaseFSWithCredentialsIT extends GeneralFSTest {
	private long connectionsBeforeTest;
	private long connectionsAfterTest;
	
	public static DatabaseFSConfig createConfig() {
		DatabaseFSConfig result = new DatabaseFSConfig();
		result.setDbUrl(readDbUrl());
		result.setDbDriverClassName("org.mariadb.jdbc.Driver");
		result.setDbUserName(readDbUser());
		result.setDbPassword(readDbPassword());
		result.setAppName("myApp");
		result.setTenantName("myTenant");
		result.setFileTableName("FILESYSTEM");
		result.setAutoCreateTable(true);
		result.setSchemaName("dbfs001");
		return result;
	}
	
	protected static String readDbUrl() {
		try {
			FSFFile configFile = TestUtil.getConfigDir().getChild("DatabaseFSTest-db-url.txt");
			if (!configFile.exists()) {
				String message = "Please add the file '" + configFile.getAbsolutePath() + "' in your local user directory to "
						+ "the database URL for testing. Example: \"jdbc:mariadb://127.0.0.1/dbfs001\"";
				System.err.println(message);
				throw new Exception(message);
			}
			return configFile.readString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	protected static String readDbUser() {
		try {
			FSFFile configFile = TestUtil.getConfigDir().getChild("DatabaseFSTest-db-user.txt");
			if (!configFile.exists()) {
				String message = "Please add the file '" + configFile.getAbsolutePath() + "' in your local user directory to "
						+ "the database username for testing. Example: \"root\"";
				System.err.println(message);
				throw new Exception(message);
			}
			return configFile.readString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	protected static String readDbPassword() {
		try {
			FSFFile configFile = TestUtil.getConfigDir().getChild("DatabaseFSTest-db-password.txt");
			if (!configFile.exists()) {
				String message = "Please add the file '" + configFile.getAbsolutePath() + "' in your local user directory to "
						+ "the database password for testing. Example: \"password\"";
				System.err.println(message);
				throw new Exception(message);
			}
			return configFile.readString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	

	
	public static void dropFileTableIfExists(DatabaseFSConfig config) throws Exception {
		if (DatabaseFSTestUtil.doesFileTableExist(config)) {
			DatabaseFSTestUtil.dropFileTable(config);
		}
	}
	
	@Override
	public FSFSystem createFS(FSFEnvironment environment) throws Exception {
		return new DatabaseFS(createConfig(), environment);
	}

	@Override
	public String listDir(FSFSystem fs) throws Exception {
		return fs.createByPath("").listDirAsString(LIST_DIR_FORMATTING_SIMPLE);
	}

	@Override
	public void beforeClass() throws Exception {
		dropFileTableIfExists(createConfig());
	}

	@Override
	public void beforeTest() throws Exception {
		DatabaseFSConfig config = createConfig();
		connectionsBeforeTest = DatabaseFSTestUtil.listOpenConnectionsOfCofingUser(config);
		dropFileTableIfExists(createConfig());
	}

	@Override
	public void afterTest() throws Exception {
		connectionsAfterTest = DatabaseFSTestUtil.listOpenConnectionsOfCofingUser(createConfig());
		assertEquals(connectionsBeforeTest, connectionsAfterTest, "open connection remained");
	}

	public static Long listOpenConnectionsOfCofingUser(DatabaseFSConfig config) throws Exception {
		return DatabaseFSTestUtil.listOpenConnectionsOfCofingUser(config);
	}

	@Override
	public boolean supportsVersioning() throws Exception {
		return false;
	}

	@Override
	public boolean supportsHistory() throws Exception {
		return false;
	}
	
	@Override
	public boolean supportCopyHistoryFilesTree() {
		return false;
	}

	@Override
	public String listDirInnerFS(FSFSystem fs) throws Exception {
		return null;
	}

	@Override
	public void logStatus(String location) throws Exception {
		log(location + ": open connections: " + DatabaseFSTestUtil.listOpenConnectionsOfCofingUser(createConfig()));
	}

	@Override
	protected FSFSystem getInnerFS(FSFSystem fs) throws Exception{
		throw new Exception("DatabaseFS has no inner FS");
	}

	@Override
	public void afterClass() throws Exception {
	}

	@Override
	public boolean hasInnerFS() throws Exception {
		return false;
	}

	@Override
	public boolean isInnerFSEncrypted() throws Exception {
		return false;
	}

	@Override
	public boolean isTimeCreatedSupported() throws Exception {
		return true;
	}

}
