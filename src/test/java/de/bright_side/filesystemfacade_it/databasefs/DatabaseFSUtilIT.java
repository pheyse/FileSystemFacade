package de.bright_side.filesystemfacade_it.databasefs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Connection;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.MethodOrderer.Alphanumeric;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;


import de.bright_side.filesystemfacade.databasefs.DatabaseFS;
import de.bright_side.filesystemfacade.databasefs.DatabaseFSConfig;
import de.bright_side.filesystemfacade.databasefs.DatabaseFSTestUtil;
import de.bright_side.filesystemfacade.databasefs.DatabaseFSUtil;

@TestMethodOrder(Alphanumeric.class)
@Tag("IT")
public class DatabaseFSUtilIT {
	private static boolean firstCall = true;

	public DatabaseFSConfig createConfig() {
		return GeneralDatabaseFSWithCredentialsIT.createConfig();
	}
	
	@BeforeEach
	public void executeBeforeTest() throws Exception {
		if (firstCall) {
			firstCall = false;
			beforeClass();
		}
		beforeTest();
	}

	
	public void beforeClass() throws Exception {
		DatabaseFSConfig config = createConfig();
		if (DatabaseFSTestUtil.doesFileTableExist(config)) {
			DatabaseFSTestUtil.dropFileTable(createConfig());
		}
	}

	public void beforeTest() throws Exception {
		DatabaseFSConfig config = createConfig();
		if (DatabaseFSTestUtil.doesFileTableExist(config)) {
			DatabaseFSTestUtil.deleteAllItemsInFileTable(config);
		}
		if (!DatabaseFSTestUtil.doesFileTableExist(createConfig())){
			DatabaseFSUtil.createFileTable(config);
		}
	}
	
	@Test
	public void processQuery_simpleAndNoConnection() throws Exception {
		assertEquals(0L, DatabaseFSTestUtil.processQueryAsValue(createConfig(), "select count(*) from dbfs001.FILESYSTEM", Long.class).longValue());
		assertEquals(null, DatabaseFSTestUtil.processQueryAsValue(createConfig(), "select id from dbfs001.FILESYSTEM where app = 'x'", Long.class));
		assertEquals(0L, DatabaseFSTestUtil.processQueryAsValue(createConfig(), "select count(*) from dbfs001.FILESYSTEM where app = 'x'", Long.class).longValue());
	}

	@Test
	public void processQuery_setValues() throws Exception {
		assertEquals(0L, DatabaseFSTestUtil.processQueryAsValue(createConfig(), "select count(*) from dbfs001.FILESYSTEM where app = ?", Long.class, "x").longValue());
	}

	@Test
	public void processUpdate_setValue() throws Exception {
		DatabaseFSTestUtil.processUpdate(createConfig(), "delete from dbfs001.FILESYSTEM where app = ?", "x");
	}
	
	
	@Test
	public void listAllDrivers_normal() {
		List<String> result = DatabaseFS.getAvailableDBDrivers();
		assertEquals(1, result.size());
		assertEquals("org.mariadb.jdbc.Driver", result.get(0));
		log("result = " + result);
	}

	@Test
	public void listOpenConnections_normal() throws Exception {
		long connectionsStart = DatabaseFSTestUtil.listOpenConnectionsOfCofingUser(createConfig());
		log("listOpenConnections_normal: connectionsStart = " + connectionsStart);
		
		long connectionsAfterOpeningOne = 0;
		try (Connection conn = DatabaseFSTestUtil.getConnection(createConfig())){
			connectionsAfterOpeningOne = DatabaseFSTestUtil.listOpenConnectionsOfCofingUser(createConfig());
			log("listOpenConnections_normal: connectionsAfterOpeningOne = " + connectionsAfterOpeningOne);
		} catch (Exception e) {
			throw e;
		}
		long connectionsEnd = DatabaseFSTestUtil.listOpenConnectionsOfCofingUser(createConfig());
		log("listOpenConnections_normal: connectionsEnd = " + connectionsEnd);
		
		assertEquals(connectionsStart + 1, connectionsAfterOpeningOne);
		assertEquals(connectionsStart, connectionsEnd);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
//	@Test
//	public void misc() throws Exception {
//		byte[] data = new byte[] {0, 8, 15};
//		DatabaseFSUtil.testInsert(createConfig(), 1, data);
//		DatabaseFSUtil.testInsert(createConfig(), 2, data);
//		DatabaseFSUtil.testCount(createConfig());
//		byte[] readData = DatabaseFSUtil.readData(createConfig(), 1);
//		log("readData = " + toString(readData));
//	}

	
//	@Test
//	public void testConnection_normal() throws Exception {
//		DatabaseFSUtil.testQuery(createConfig());
//	}
	
//	@Test
//	public void testUpdate_normal() throws Exception {
//		DatabaseFSUtil.testUpdate(createConfig());
//	}
	
	//	private String toString(byte[] byteArray) {
//		if (byteArray == null) {
//			return "byteArray:null";
//		}
//		StringBuilder result = new StringBuilder("byteArray{");
//		boolean first = true;
//		for (byte i: byteArray) {
//			if (first) {
//				first = false;
//			} else {
//				result.append(", ");
//			}
//			result.append(i);
//		}
//		result.append("}");
//		return result.toString();
//	}
//
	public void log(String message) {
		System.out.println("DatabaseFSUtilTest> " + message);
	}
}
