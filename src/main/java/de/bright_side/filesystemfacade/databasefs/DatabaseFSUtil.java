package de.bright_side.filesystemfacade.databasefs;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.BindException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLNonTransientConnectionException;
import java.util.ArrayList;
import java.util.List;

import de.bright_side.filesystemfacade.util.FSFFileUtil;

/**
 * @author Philip Heyse
 *
 */
public class DatabaseFSUtil {
	private static final String CREATE_TABLE_SQL = getCreateTableSQL();
	private static final String CREATE_INDEX_SQL = getCreateTableIndexSQL();
	private static final int TRIES_TO_GET_A_CONNECTION = 10;
	private static final boolean LOGGING_ENABLED = false;
	
	protected static Connection getConnection(DatabaseFSConfig config) throws Exception {
		if (config.getDbConnection() != null) {
			return config.getDbConnection();
		}
		Exception lastException = null;
		for (int triesLeft = TRIES_TO_GET_A_CONNECTION; triesLeft >= 0; triesLeft --) {
			try{
				Class.forName(config.getDbDriverClassName());
				return DriverManager.getConnection(config.getDbUrl(), config.getDbUserName(), config.getDbPassword());
			} catch (SQLNonTransientConnectionException e) {
				Throwable cause = e.getCause();
				if (cause != null) {
					if (cause instanceof BindException) {
						lastException = e;
						logException("getConnection: port bind exception", triesLeft, e);
						try {
							Thread.sleep(1000);
						} catch (InterruptedException ignored) {
						}
					} else {
						throw e;
					}
				} else {
					throw e;
				}
			} catch (Exception e) {
				throw e;
			}
		}
		throw lastException;
	}
	
	private static boolean isConnectionCreatedForEachAction(DatabaseFSConfig config) {
		return config.getDbConnection() == null;
	}
	
	private static String getCreateTableIndexSQL() {
		return "CREATE INDEX %2_INDEX_APP_TENANT_PARENT_ID ON %1 (APP, TENANT, PARENT_ID)";
	}
	
	protected static long insertEntry(DatabaseFSConfig config, int filetype, String name, long length, long lastModifiedTime, long creationTime, long parentID, byte[] data) throws Exception {
		Connection connection = null;
		try {
			connection = getConnection(config);
			if (isConnectionCreatedForEachAction(config)) {
				connection.setAutoCommit(false);
			}
		
			String sql;
			sql = replaceSchemaAndTable(config, "%1", "SELECT MAX(ID) FROM %1 WHERE APP=? AND TENANT=?");
			Long id = processQueryAsValue(config, true, connection, sql, Long.class, config.getAppName(), config.getTenantName());
			if (id == null) {
				id = 1L;
			} else {
				id++;
			}
			
			sql = replaceSchemaAndTable(config, "%1", "INSERT INTO %1 (APP, TENANT, ID, FILE_TYPE, NAME, FILE_LENGTH, LAST_MODIFICATION_TIME, CREATION_TIME, PARENT_ID) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
			sql = sql.replace("%1", getSchemaAndTable(config));
			Object[] values = new Object[] {config.getAppName(), config.getTenantName(), id, filetype, name, length, lastModifiedTime, creationTime, parentID};
			processUpdate(config, connection, sql, values);
			
			if (data != null) {
				writeData(config, connection, id, data);
			}
			
			if (isConnectionCreatedForEachAction(config)) {
				connection.commit();
			}
			return id;
		} catch (Exception e) {
			throw e;
		} finally {
			if (connection != null) {
				if (isConnectionCreatedForEachAction(config)) {
					connection.close();
				}
			}
		}
	}
	
	protected static void deleteAllItemsInFileTable(DatabaseFSConfig config) throws Exception {
		String sql = replaceSchemaAndTable(config, "%1", "delete from %1");
		processUpdate(config, sql);
	}
	
	protected static void dropFileTable(DatabaseFSConfig config) throws Exception {
		String sql = replaceSchemaAndTable(config, "%1", "drop table %1");
		processUpdate(config, sql);
	}
	
	public static void createFileTable(DatabaseFSConfig config) throws Exception {
		String sql;
		sql = replaceSchemaAndTable(config, "%1", CREATE_TABLE_SQL);
		try {
			processUpdate(config, sql);
		} catch (Exception e) {
			throw new Exception("Could not create table by SQL >>" + sql + "<<", e);
		}

		sql = replaceSchemaAndTable(config, "%1", CREATE_INDEX_SQL);
		sql = sql.replace("%2", config.getFileTableName());
		try {
			processUpdate(config, sql);
		} catch (Exception e) {
			throw new Exception("Could not create table index by SQL >>" + sql + "<<");
		}
	}
	
	private static void logException(String info, int triesLeft, Exception e) {
		System.out.println("DatabseFSUtil exception " + info + " (tries left: " + triesLeft + "):");
		e.printStackTrace();
	}


	protected static Long listOpenConnectionsOfCofingUser(DatabaseFSConfig config) throws Exception {
		return processQueryAsValue(config, true, "SELECT COUNT(*) FROM information_schema.PROCESSLIST where user = ?", Long.class, config.getDbUserName());
	}
	
	protected static void writeData(DatabaseFSConfig config, long id, boolean append, byte[] data, long timeLastModified) throws Exception {
		Connection connection = null;
		try {
			connection = getConnection(config);
			writeData(config, connection, id, append, data, timeLastModified);
		} catch (Exception e) {
			throw e;
		} finally {
			if ((isConnectionCreatedForEachAction(config)) && (connection != null)){
				connection.close();
			}
		}
	}
	
	protected static void writeData(DatabaseFSConfig config, Connection connection, long id, boolean append, byte[] data, long timeLastModified) throws Exception {
		String sql = "UPDATE %1 SET FILE_DATA = ?, LAST_MODIFICATION_TIME = ?, FILE_LENGTH = ? WHERE APP = ? AND TENANT = ? AND ID = ?";
		sql = sql.replace("%1", getSchemaAndTable(config));

		byte[] useData = data;
		
		if (append) {
			byte[] oldData = readData(config, connection, id);
			useData = new byte[oldData.length + data.length];
			System.arraycopy(oldData, 0, useData, 0, oldData.length);
			System.arraycopy(data, 0, useData, oldData.length, data.length);
		}
		
		try (PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setBinaryStream(1, new ByteArrayInputStream(useData));
			statement.setLong(2, timeLastModified);
			statement.setLong(3, useData.length);
			statement.setString(4, config.getAppName());
			statement.setString(5, config.getTenantName());
			statement.setLong(6, id);
			statement.executeUpdate();
		} catch (Exception e) {
			throw e;
		}		
	}

	
	protected static void writeData(DatabaseFSConfig config, Connection connection, long id, byte[] data) throws Exception {
		String sql = "UPDATE %1 SET FILE_DATA = ? WHERE APP = ? AND TENANT = ? AND ID = ?";
		sql = sql.replace("%1", getSchemaAndTable(config));

		try (PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setBinaryStream(1, new ByteArrayInputStream(data));
			statement.setString(2, config.getAppName());
			statement.setString(3, config.getTenantName());
			statement.setLong(4, id);
			statement.executeUpdate();
		} catch (Exception e) {
			throw e;
		}		
	}
	
	protected static byte[] readData(DatabaseFSConfig config, long id) throws Exception {
		Connection connection = null;
		try {
			connection = getConnection(config);
			return readData(config, connection, id);
		} catch (Exception e) {
			throw e;
		} finally {
			if ((isConnectionCreatedForEachAction(config)) && (connection != null)){
				connection.close();
			}
		}
	}
	
	protected static byte[] readData(DatabaseFSConfig config, Connection connection, long id) throws Exception {
		String sql = "SELECT FILE_DATA FROM %1 WHERE APP = ? AND TENANT = ? AND ID = ?";
		sql = sql.replace("%1", getSchemaAndTable(config));
		
		try (PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setString(1, config.getAppName());
			statement.setString(2, config.getTenantName());
			statement.setLong(3, id);
			try (ResultSet resultSet = statement.executeQuery()){
				if (!resultSet.next()) {
					return null;
				}
				InputStream inputStream = resultSet.getBinaryStream(1);
				return FSFFileUtil.readAllBytes(inputStream);
			} catch (Exception e) {
				throw e;
			}
		} catch (Exception e) {
			throw e;
		}		
	}
	
	private static String getCreateTableSQL() {
		StringBuilder sb = new StringBuilder();
		sb.append("CREATE TABLE %1 (\n");
		sb.append("    APP VARCHAR(256) NOT NULL,\n");
		sb.append("    TENANT VARCHAR(256) NOT NULL,\n");
		sb.append("    ID BIGINT(20) NOT NULL,\n");
		sb.append("    FILE_TYPE INT(10) NOT NULL,\n");
		sb.append("    NAME VARCHAR(1024) NOT NULL,\n");
		sb.append("    FILE_LENGTH BIGINT(20) NULL DEFAULT NULL,\n");
		sb.append("    LAST_MODIFICATION_TIME BIGINT(20) NOT NULL,\n");
		sb.append("    CREATION_TIME BIGINT(20) NOT NULL,\n");
		sb.append("    PARENT_ID BIGINT(20) NOT NULL,\n");
		sb.append("    FILE_DATA LONGBLOB,\n");
		sb.append("    PRIMARY KEY (APP, TENANT, ID)\n");
		sb.append(");\n");
		return sb.toString();
	}

	protected static String getSchemaAndTable(DatabaseFSConfig config) {
		if (config.getSchemaName() == null) {
			return config.getFileTableName();
		} else {
			return config.getSchemaName() + "." + config.getFileTableName();
		}
	}
	
	protected static String replaceSchemaAndTable(DatabaseFSConfig config, String placeholder, String sql) {
		return sql.replace(placeholder, getSchemaAndTable(config));
	}
	
	protected static boolean doesFileTableExist(DatabaseFSConfig config) throws Exception{
		try {
			//: no error logging because if this fails it only means that the table doesn't exist
			processQueryAsValue(config, false, "select count(*) from " + getSchemaAndTable(config), Long.class);
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	protected static <K extends Object> K processQueryAsValue(DatabaseFSConfig config, String sql, final Class<K> valueClass, Object ...values) throws Exception {
		return processQueryAsValue(config, true, sql, valueClass, values);
	}
	
	protected static <K extends Object> K processQueryAsValue(DatabaseFSConfig config, boolean logErrors, String sql, final Class<K> valueClass, Object ...values) throws Exception {
		Connection connection = null;
		try {
			connection = getConnection(config);
			return processQueryAsValue(config, logErrors, connection, sql, valueClass, values);
		} catch (Exception e) {
			throw e;
		} finally {
			if ((isConnectionCreatedForEachAction(config)) && (connection != null)){
				connection.close();
			}
		}
	}

	@SuppressWarnings("unchecked")
	protected static <K extends Object> K processQueryAsValue(DatabaseFSConfig config, boolean logErrors, Connection connection, String sql, final Class<K> valueClass, Object ...values) throws Exception {
		final List<Object> result = new ArrayList<>();
		ResultSetProcessor processor = new ResultSetProcessor() {
			@Override
			public void process(ResultSet resultSet) throws Exception {
				if (resultSet.next()) {
					if (valueClass == String.class) {
						result.add(resultSet.getString(1));
					} else if (valueClass == Long.class) {
						result.add(resultSet.getLong(1));
					} else if (valueClass == Integer.class) {
						result.add(resultSet.getInt(1));
					}

					if (resultSet.wasNull()) {
						result.clear();
					}
				}
			}
		};
		processQuery(config, logErrors, connection, sql, processor, values);
		if ((result == null) || (result.isEmpty())){
			return null;
		}
		return (K) result.get(0);
	}

	protected static void processQuery(DatabaseFSConfig config, boolean logErrors, String sql, ResultSetProcessor processor, Object ...values) throws Exception {
		Connection connection = null;
		try {
			connection = getConnection(config);
			processQuery(config, logErrors, connection, sql, processor, values);
		} catch (Exception e) {
			throw e;
		} finally {
			if ((isConnectionCreatedForEachAction(config)) && (connection != null)){
				connection.close();
			}
		}
	}
	
	protected static void processQuery(DatabaseFSConfig config, boolean logErrors, Connection connection, String sql, ResultSetProcessor processor, Object ...values) throws Exception {
		log("processQuery: sql = >>" + sql + "<<, values = " + values);
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		Exception exception = null;
		try {
			log("processQuery: prepate statement");
			statement = connection.prepareStatement(sql);
			int index = 1;
			log("processQuery: setting values");
			for (Object i: values) {
				setValue(statement, index, i);
				index ++;
			}
			log("processQuery: executing query");
			try {
				resultSet = statement.executeQuery();
			} catch (Exception e) {
				if (logErrors) {
					log("processQuery", e);
				}
				throw new Exception("Could not execute SQL >>" + sql + "<<", e);
			}
			log("processQuery: processing result set");
			processor.process(resultSet);
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}
			} catch (Exception e) {
				exception = e;
			}
			try {
				if (statement != null) {
					statement.close();
				}
			} catch (Exception e) {
				if (exception != null) {
					exception = e;
				}
			}
		}
	}
	
	protected static void processUpdate(DatabaseFSConfig config, String sql, Object...values) throws Exception {
		Connection connection = null;
		try {
			connection = getConnection(config);
			processUpdate(config, connection, sql, values);
		} catch (Exception e) {
			throw e;
		} finally {
			if ((isConnectionCreatedForEachAction(config)) && (connection != null)){
				connection.close();
			}
		}
	}

	protected static void processUpdate(DatabaseFSConfig config, Connection connection, String sql, Object...values) throws Exception {
		log("processUpdate: sql = >>" + sql + "<<, values = " + values);
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		Exception exception = null;
		try {
			log("processUpdate: preparing statement");
			statement = connection.prepareStatement(sql);
			log("processUpdate: setting values");
			int index = 1;
			for (Object i: values) {
				setValue(statement, index, i);
				index ++;
			}
			log("processUpdate: executing update");
			statement.executeUpdate();
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}
			} catch (Exception e) {
				exception = e;
			}
			try {
				if (statement != null) {
					statement.close();
				}
			} catch (Exception e) {
				if (exception != null) {
					exception = e;
				}
			}
		}
	}

	private static void setValue(PreparedStatement statement, int index, Object i) throws SQLException, Exception {
		log("setValue: index = " + index + ", value = " + i);
		
		if (i instanceof String) {
			statement.setString(index, (String)i);	
		} else if (i instanceof Long) {
			statement.setLong(index, (Long)i);	
		} else if (i instanceof Integer) {
			statement.setLong(index, (Integer)i);	
		} else {
			String type = "?";
			if (i == null) {
				type = "null";
			} else {
				type = i.getClass().getName();
			}
			throw new Exception("Unexpected type: at position (starting at 1) " + index + ":" + type);
		}
	}

	private static void log(String message) {
		if (LOGGING_ENABLED) {
			System.out.println("DatabaseFSUtil> " + message);
		}
	}

	private static void log(String message, Exception e) {
		if (LOGGING_ENABLED) {
			System.out.println("DatabaseFSUtil> error: ");
			e.printStackTrace();
		}
	}


}
