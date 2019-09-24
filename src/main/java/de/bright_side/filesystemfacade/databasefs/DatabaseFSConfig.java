package de.bright_side.filesystemfacade.databasefs;

import java.sql.Connection;

/**
 * Either dbDriverClassName, dbUrl, dbUserName and dbPassword are needed or a dbConnection object
 * @author Philip Heyse
 *
 */
public class DatabaseFSConfig {
	private String dbDriverClassName;
	private String dbUrl;
	private String dbUserName;
	private String dbPassword;
	private transient Connection dbConnection; //: set to transient so it will not be serialized (e.g. via Gson in the copyByValues method)
	
	
	
	private String appName;
	private String tenantName;
	private boolean autoCreateTable;
	private String fileTableName;
	private String schemaName;
	
	public String getDbDriverClassName() {
		return dbDriverClassName;
	}
	/**
	 * @param dbDriverClassName: class name of the database driver. Example: 'org.mariadb.jdbc.Driver'. The available drivers can be listed by using method getAvailableDBDrivers().
	 * The DB drivers need to be included via separate jar files, such as 'mariadb-java-client-2.4.1.jar'.
	 * Either dbDriverClassName, dbUrl, dbUserName and dbPassword are needed or a dbConnection object
	 */
	public void setDbDriverClassName(String dbDriverClassName) {
		this.dbDriverClassName = dbDriverClassName;
	}
	public String getDbUrl() {
		return dbUrl;
	}
	/**
	 * Either dbDriverClassName, dbUrl, dbUserName and dbPassword are needed or a dbConnection object
	 * @param dbUrl URL of the database. Example: "jdbc:mariadb://192.168.100.174/db"
	 */
	public void setDbUrl(String dbUrl) {
		this.dbUrl = dbUrl;
	}
//	public int getPort() {
//		return port;
//	}
//	public void setPort(int port) {
//		this.port = port;
//	}
	
	public String getDbUserName() {
		return dbUserName;
	}
	/**
	 * Either dbDriverClassName, dbUrl, dbUserName and dbPassword are needed or a dbConnection object
	 * @param dbUserName name of the user to login into the database
	 */
	public void setDbUserName(String dbUserName) {
		this.dbUserName = dbUserName;
	}
	public String getDbPassword() {
		return dbPassword;
	}
	/**
	 * Either dbDriverClassName, dbUrl, dbUserName and dbPassword are needed or a dbConnection object
	 * @param dbPassword password of the user to login into the database
	 */
	public void setDbPassword(String dbPassword) {
		this.dbPassword = dbPassword;
	}
	public String getAppName() {
		return appName;
	}
	/**
	 * @param appName the table which contains the file system data is separated into different apps and those into tenants. Each app-tenant-combination has its own file system
	 */
	public void setAppName(String appName) {
		this.appName = appName;
	}
	public String getTenantName() {
		return tenantName;
	}
	/**
	 * @param tenantName the table which contains the file system data is separated into different apps and those into tenants. Each app-tenant-combination has its own file system
	 */
	public void setTenantName(String tenantName) {
		this.tenantName = tenantName;
	}
	public boolean isAutoCreateTable() {
		return autoCreateTable;
	}
	/**
	 * @param autoCreateTable if true after connecting it is checked whether the file table already exists and if not it is created automatically
	 */
	public void setAutoCreateTable(boolean autoCreateTable) {
		this.autoCreateTable = autoCreateTable;
	}
	public String getFileTableName() {
		return fileTableName;
	}
	/**
	 * @param fileTableName name of the table which stores the file structure and data
	 */
	public void setFileTableName(String fileTableName) {
		this.fileTableName = fileTableName;
	}

	
	public String getSchemaName() {
		return schemaName;
	}
	
	/**
	 * 
	 * @param schemaName name of the schema where the table is located. E.g. "myschema" with table name "mytable" will cause statements like 'select * from myschema.mytable'
	 */
	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}

	@Override
	public String toString() {
		return "DatabaseFSConfig [dbDriverClassName=" + dbDriverClassName + ", dbUrl=" + dbUrl + ", dbUserName="
				+ dbUserName + ", dbPassword=" + dbPassword + ", appName=" + appName + ", tenantName=" + tenantName
				+ ", autoCreateTable=" + autoCreateTable + ", fileTableName=" + fileTableName + ", schemaName="
				+ schemaName + "]";
	}

	public Connection getDbConnection() {
		return dbConnection;
	}
	
	/**
	 * Either dbDriverClassName, dbUrl, dbUserName and dbPassword are needed or a dbConnection object
	 * @param dbConnection a db connection object 
	 */
	public void setDbConnection(Connection dbConnection) {
		this.dbConnection = dbConnection;
	}
}
