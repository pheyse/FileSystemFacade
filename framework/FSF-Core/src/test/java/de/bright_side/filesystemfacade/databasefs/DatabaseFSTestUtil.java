package de.bright_side.filesystemfacade.databasefs;

import java.sql.Connection;


/**
 * purpose: making some of the protected methods of DatabaseFSUtil public so they 
 * can be accessed by the integration tests in Maven
 *
 */
public class DatabaseFSTestUtil {
	public static boolean doesFileTableExist(DatabaseFSConfig config) throws Exception{
		return DatabaseFSUtil.doesFileTableExist(config);
	}
	
	public static void dropFileTable(DatabaseFSConfig config) throws Exception{
		DatabaseFSUtil.dropFileTable(config);
	}

	public static void deleteAllItemsInFileTable(DatabaseFSConfig config) throws Exception{
		DatabaseFSUtil.deleteAllItemsInFileTable(config);
	}
	
	public static <K extends Object> K processQueryAsValue(DatabaseFSConfig config, boolean logErrors, String sql, final Class<K> valueClass, Object ...values) throws Exception {
		return DatabaseFSUtil.processQueryAsValue(config, logErrors, sql, valueClass, values);
	}

	public static <K extends Object> K processQueryAsValue(DatabaseFSConfig config, String sql, final Class<K> valueClass, Object ...values) throws Exception {
		return DatabaseFSUtil.processQueryAsValue(config, sql, valueClass, values);
	}
	
	public static void processUpdate(DatabaseFSConfig config, String sql, Object...values) throws Exception {
		DatabaseFSUtil.processUpdate(config, sql, values);
	}
	
	public static Long listOpenConnectionsOfCofingUser(DatabaseFSConfig config) throws Exception {
		return DatabaseFSUtil.listOpenConnectionsOfCofingUser(config);
	}
	
	public static Connection getConnection(DatabaseFSConfig config) throws Exception {
		return DatabaseFSUtil.getConnection(config);
	}
	

}
