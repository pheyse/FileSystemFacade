package de.bright_side.filesystemfacade_it.databasefs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Connection;

import org.junit.jupiter.api.Tag;

import de.bright_side.filesystemfacade.databasefs.DatabaseFS;
import de.bright_side.filesystemfacade.databasefs.DatabaseFSConfig;
import de.bright_side.filesystemfacade.databasefs.DatabaseFSTestUtil;
import de.bright_side.filesystemfacade.facade.FSFEnvironment;
import de.bright_side.filesystemfacade.facade.FSFSystem;
import de.bright_side.filesystemfacade.facade.GeneralFSTest;

@Tag("IT")
public class GeneralDatabaseFSWithConnectionIT extends GeneralFSTest {
	private long connectionsBeforeTest;
	private long connectionsAfterTest;
	private static Connection connection;
	
	public static DatabaseFSConfig createConfig() throws Exception {
		DatabaseFSConfig result = GeneralDatabaseFSWithCredentialsIT.createConfig();
		if (connection == null) {
			connection = DatabaseFS.createConnection(result);
		}
		result.setDbConnection(connection);
		result.setDbUrl(null);
		result.setDbDriverClassName(null);
		result.setDbPassword(null);
		//: Db user name is kept because it is needed to get open connection statistics
		return result;
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
	public void afterClass() throws Exception {
		log("after class: closing connection");
		connection.close();
	}
	
	@Override
	public void beforeTest() throws Exception {
//		log("before test");
		DatabaseFSConfig config = createConfig();
		connectionsBeforeTest = DatabaseFSTestUtil.listOpenConnectionsOfCofingUser(config);
		dropFileTableIfExists(createConfig());
	}

	@Override
	public void afterTest() throws Exception {
//		log("after test");
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
