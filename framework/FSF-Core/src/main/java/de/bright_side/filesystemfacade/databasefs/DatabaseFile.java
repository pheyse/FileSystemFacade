package de.bright_side.filesystemfacade.databasefs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import de.bright_side.filesystemfacade.facade.FSFFile;
import de.bright_side.filesystemfacade.facade.FSFSystem;
import de.bright_side.filesystemfacade.facade.VersionedData;
import de.bright_side.filesystemfacade.facade.WrongVersionException;
import de.bright_side.filesystemfacade.util.FSFFileUtil;
import de.bright_side.filesystemfacade.util.ListDirFormatting;

/**
 * @author Philip Heyse
 *
 */
public class DatabaseFile implements FSFFile{
	private static final boolean LOGGING_ENABLED = false;
	private static final int FILE_TYPE_DIR = 1;
	private static final int FILE_TYPE_FILE = 2;
	private static final long ID_VALUE_ID_ROOT = 0;
	private static final long ID_VALUE_ID_UNKNOWN = -1;
	private static final long ID_VALUE_FILE_DOES_NOT_EXIST = -2;
	private static final long PARENT_ID_VALUE_PARENT_IS_ROOT = 0;
	private static final long PARENT_ID_VALUE_UNKNOWN = -1;
	private DatabaseFS databaseFS;
	private DatabaseFSConfig config;
	private long id = ID_VALUE_ID_UNKNOWN;

	/** If the parent is unknown (PARENT_ID_VALUE_UNKNOWN) or does not exist (PARENT_ID_VALUE_PARENT_DOES_NOT_EXIST) 
	 * the field parent contains the parent DatabaseFile. Another special case is PARENT_ID_VALUE_PARENT_IS_ROOT in which case the parent file may be null.
	 */
	private long parentID;
	/** may be null if read from ResultSet. In this case parentID contains the ID of the parent*/
	private DatabaseFile parent;
	private String name;
//	, long creationTime, long timeLastModified, long size, boolean isDirC

	/** null as long as the value is unknown*/
	private String cachedAbsolutePath = null;
	
	protected static DatabaseFile createDatabaseFile(DatabaseFS databaseFS, long id, DatabaseFile parent, boolean parentIsRoot, String name) {
		DatabaseFile result = new DatabaseFile();
		
		result.databaseFS = databaseFS;
		result.config = databaseFS.getConfig();
		result.id = id;
		result.parentID = PARENT_ID_VALUE_UNKNOWN;
		result.name = name;

		if (parentIsRoot) {
			result.parent = null;
			result.parentID = PARENT_ID_VALUE_PARENT_IS_ROOT;
			result.cachedAbsolutePath = DatabaseFS.SEPARATOR + name;
		} else {
			result.parent = parent;
			if (parent != null) {
				if (parent.cachedAbsolutePath != null) {
					result.cachedAbsolutePath = parent.cachedAbsolutePath + DatabaseFS.SEPARATOR + name;
				}
				if (parent.id > 0){
					result.parentID = parent.id;
				}
			}
		}
		return result;
	}
	
	private void internalError(Exception e) {
		//: at this moment, internal errors are not handled
	}
	
	protected static DatabaseFile createRootDatabaseFile(DatabaseFS databaseFS) {
		DatabaseFile result = new DatabaseFile();
		result.databaseFS = databaseFS;
		result.config = databaseFS.getConfig();
		result.id = ID_VALUE_ID_ROOT;
		result.parentID = PARENT_ID_VALUE_PARENT_IS_ROOT;
		result.name = "";
		result.parent = null;
		result.setCachedAbsolutePath("");
		return result;
	}
	
	private DatabaseFile() {
	}

	private void readIDIfNecessary() throws Exception{
		if (id >= 0) {
			return;
		}
		if (id == ID_VALUE_ID_ROOT) {
			return;
		}
		if (parentID == PARENT_ID_VALUE_UNKNOWN) {
			parent.readIDIfNecessary();
			if (parent.id >= 0) {
				parentID = parent.id; 
			}
		}
		String sql = DatabaseFSUtil.replaceSchemaAndTable(config, "%1", "select ID from %1 where APP = ? AND TENANT = ? AND PARENT_ID = ? AND NAME = ?");
		Long readID;
		readID = DatabaseFSUtil.processQueryAsValue(config, sql, Long.class, config.getAppName(), config.getTenantName(), parentID, name);
		if (readID == null) {
			id = ID_VALUE_FILE_DOES_NOT_EXIST;
		} else {
			id = readID;
		}
	}
	
	private void readParentIfNecessary() throws Exception {
		if (parentID == PARENT_ID_VALUE_PARENT_IS_ROOT) {
			parent = createRootDatabaseFile(databaseFS);
			return;
		}
		if (parent == null){
			if (parentID == PARENT_ID_VALUE_UNKNOWN) {
				throw new Exception("File has no parent ID and no reference to a parent object"); 
			}
			String sql = DatabaseFSUtil.replaceSchemaAndTable(config, "%1", "select * from %1 where APP = ? AND TENANT = ? AND ID = ?");
			final List<DatabaseFile> result = new ArrayList<>();
			ResultSetProcessor processor = new ResultSetProcessor() {
				
				@Override
				public void process(ResultSet resultSet) throws Exception {
					if (resultSet.next()) {
						result.add(createFile(databaseFS, resultSet, null));
					}
				}
			};
			DatabaseFSUtil.processQuery(config, true, sql, processor, config.getAppName(), config.getTenantName(), parentID);
			if (!result.isEmpty()) {
				parent = result.get(0);
			}
		}
		if (parentID == PARENT_ID_VALUE_UNKNOWN) {
			if (parent != null) {
				parent.readIDIfNecessary();
				if ((parent.id != ID_VALUE_ID_UNKNOWN) && (parent.id != ID_VALUE_FILE_DOES_NOT_EXIST)){
					parentID = parent.id; 
				}
			}
		}
	}
	
	@Override
	public int compareTo(FSFFile other) {
		if (other == null){
			return 1;
		}
		if (this == other) {
			return 0;
		}
		int result = (other.getFSFSystem().getClass().getName()).compareTo(databaseFS.getClass().getName());
		if (result != 0){
			return result;
		}
		return getAbsolutePath().compareTo(other.getAbsolutePath());
	}

	protected DatabaseFile createFile(DatabaseFS databaseFS, ResultSet resultSet, DatabaseFile parent) throws Exception{
		DatabaseFile result = new DatabaseFile();
		result.databaseFS = databaseFS;
		result.config = databaseFS.getConfig();
		result.id = resultSet.getLong("ID");
		result.parentID = resultSet.getLong("PARENT_ID");
		result.parent = parent;
		result.name = resultSet.getString("NAME");
		
		if ((parent != null) && (parent.cachedAbsolutePath != null)){
			result.cachedAbsolutePath = parent.cachedAbsolutePath + DatabaseFS.SEPARATOR + result.name;
		}
		
//		result.timeLastModified = resultSet.getLong("LAST_MODIFICATION_TIME");
//		result.creationTime = resultSet.getLong("CREATION_TIME");
//		result.size = resultSet.getLong("FILE_LENGTH");
//		result.isDir = resultSet.getInt("FILE_TYPE") == FILE_TYPE_DIR;
		return result;
	}

	@Override
	public List<FSFFile> listFiles() {
		if ((id < 0) && (id != ID_VALUE_ID_ROOT)){
			if (!exists()) {
				return null;
			}
		}
		final List<FSFFile> result = new ArrayList<>();
		
		String sql = DatabaseFSUtil.replaceSchemaAndTable(config, "%1", "select ID, FILE_TYPE, NAME, FILE_LENGTH, LAST_MODIFICATION_TIME, CREATION_TIME, PARENT_ID from %1 "
				+ "where APP = ? AND TENANT = ? AND PARENT_ID = ? order by NAME, ID");
		
		ResultSetProcessor processor = new ResultSetProcessor() {
			@Override
			public void process(ResultSet resultSet) throws Exception {
				while (resultSet.next()) {
					result.add(createFile(databaseFS, resultSet, DatabaseFile.this));
				}
			}

		};
		try {
			DatabaseFSUtil.processQuery(config, true, sql, processor, config.getAppName(), config.getTenantName(), id);
		} catch (Exception e) {
			throw new RuntimeException("Could not list files of file with id " + id, e);
		}
		return result;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public long getTimeLastModified() throws Exception {
		readParentIfNecessary();
		String sql = DatabaseFSUtil.replaceSchemaAndTable(config, "%1", "select LAST_MODIFICATION_TIME from %1 where APP = ? AND TENANT = ? AND PARENT_ID = ? AND NAME = ?");
		Long result;
		try {
			result = DatabaseFSUtil.processQueryAsValue(config, sql, Long.class, config.getAppName(), config.getTenantName(), parentID, name);
		} catch (Exception e) {
			throw new Exception("Could not read details from table", e);
		}
		if (result == null) {
			return 0;
		}
		
		return result.longValue();
	}

	@Override
	public long getTimeCreated() throws Exception{
		readParentIfNecessary();
		String sql = DatabaseFSUtil.replaceSchemaAndTable(config, "%1", "select CREATION_TIME from %1 where APP = ? AND TENANT = ? AND PARENT_ID = ? AND NAME = ?");
		Long result;
		try {
			result = DatabaseFSUtil.processQueryAsValue(config, sql, Long.class, config.getAppName(), config.getTenantName(), parentID, name);
		} catch (Exception e) {
			throw new Exception("Could not read details from table", e);
		}
		if (result == null) {
			return 0;
		}
		
		return result.longValue();
	}
	
	private Integer getType() throws Exception {
		readParentIfNecessary();
		String sql = DatabaseFSUtil.replaceSchemaAndTable(config, "%1", "select FILE_TYPE from %1 where APP = ? AND TENANT = ? AND PARENT_ID = ? AND NAME = ?");
		Integer result;
		try {
			result = DatabaseFSUtil.processQueryAsValue(config, sql, Integer.class, config.getAppName(), config.getTenantName(), parentID, name);
		} catch (Exception e) {
			throw new RuntimeException("Could not read details from table", e);
		}
		return result;
	}
	
	@Override
	public boolean isFile() {
		try {
			return Integer.valueOf(FILE_TYPE_FILE).equals(getType());
		} catch (Exception e) {
			internalError(e);
			return false;
		}
	}

	@Override
	public boolean isDirectory() {
		if (id == ID_VALUE_ID_ROOT) {
			return true;
		}
		try {
			return Integer.valueOf(FILE_TYPE_DIR).equals(getType());
		} catch (Exception e) {
			internalError(e);
			return false;
		}
	}
	
	@Override
	public boolean exists() {
		try {
			readIDIfNecessary();
		} catch (Exception e) {
			internalError(e);
			return false;
		}
		if (id == ID_VALUE_ID_ROOT) {
			return true;
		}
		return id != ID_VALUE_FILE_DOES_NOT_EXIST;
	}

	@Override
	public FSFFile getParentFile() {
		if (id == ID_VALUE_ID_ROOT) {
			return null;
		}
		
		try {
			readParentIfNecessary();
		} catch (Exception e) {
			internalError(e);
			return null;
		}
		return parent;
	}
	
	@Override
	public void rename(String newName) throws Exception {
		readIDIfNecessary();
		if (id == ID_VALUE_ID_ROOT) {
			throw new Exception("Root cannot be renamed");
		}
		readParentIfNecessary();
		if (!exists()) {
			throw new Exception("The file does not exist");
		}
		cachedAbsolutePath = null;
		String sql = DatabaseFSUtil.replaceSchemaAndTable(config, "%1", "select count(*) from %1 where APP = ? AND TENANT = ? AND PARENT_ID = ? AND NAME = ?");
		long existingNames = DatabaseFSUtil.processQueryAsValue(config, sql, Long.class, config.getAppName(), config.getTenantName(), parentID, newName);
		if (existingNames > 0) {
			throw new Exception("There already is a file with the name '" + newName + "'");
		}
		
		sql = DatabaseFSUtil.replaceSchemaAndTable(config, "%1", "update %1 set name = ? where APP = ? AND TENANT = ? AND ID = ?");
		DatabaseFSUtil.processUpdate(config, sql, newName, config.getAppName(), config.getTenantName(), id);

		if ((parent != null) && (parent.cachedAbsolutePath != null)) {
			cachedAbsolutePath = parent.cachedAbsolutePath + DatabaseFS.SEPARATOR + newName;
		}

		name = newName;
	}

	@Override
	public FSFFile getChild(String name) {
		return createDatabaseFile(databaseFS, ID_VALUE_ID_UNKNOWN, this, id == ID_VALUE_ID_ROOT, name);
	}
	
	@Override
	public FSFFile mkdir() throws Exception{
		if (id == ID_VALUE_ID_ROOT) {
			return this;
		}
		if (parentID != PARENT_ID_VALUE_PARENT_IS_ROOT) {
			if (!parent.exists()) {
				throw new Exception("Parent does not exist");
			}
		}
		readParentIfNecessary();
		if (exists()) {
			return this;
		}
		
		long time = databaseFS.getEnvironment().getCurrentTimeMillis();
		id = DatabaseFSUtil.insertEntry(config, FILE_TYPE_DIR, name, 0, time, time, parentID, null);
		return this;
	}

	@Override
	public FSFFile mkdirs()  throws Exception{
		if (id == ID_VALUE_ID_ROOT) {
			return this;
		}
		if (exists()) {
			return this;
		}
		if (parentID != PARENT_ID_VALUE_PARENT_IS_ROOT) {
			readParentIfNecessary();
			if (parent == null) {
				throw new Exception("File has no parent. " + listDetails());
			}
			parent.mkdirs();
			if (!parent.exists()) {
				throw new Exception("Could not create parent");
			}
		}
		
		mkdir();
		return this;
	}
	
	@Override
	public String getAbsolutePath() {
		if (id == ID_VALUE_ID_ROOT) {
			return "";
		}
		if (cachedAbsolutePath != null) {
			return cachedAbsolutePath;
		}
		try {
			readParentIfNecessary();
			cachedAbsolutePath = parent.getAbsolutePath() + DatabaseFS.SEPARATOR + name; 
			return cachedAbsolutePath;
		} catch (Exception e) {
			internalError(e);
			return "!!!Could not readPath!!!";
		}
	}

	@Override
	public void delete() throws Exception {
		if (id == ID_VALUE_ID_ROOT) {
			throw new Exception("Root cannot be deleted");
		}
		readIDIfNecessary();
		if (!exists()) {
			return;
		}
		String sql = DatabaseFSUtil.replaceSchemaAndTable(config, "%1", "DELETE FROM %1 where APP = ? AND TENANT = ? AND ID = ?");
		try {
			DatabaseFSUtil.processUpdate(config, sql, config.getAppName(), config.getTenantName(), id);
		} catch (Exception e) {
			throw new Exception("Could not delete entry from table", e);
		}
		//: after delete the id still contains a value which looks like the id is known. However there is no entry with this ID anymore
		id = ID_VALUE_FILE_DOES_NOT_EXIST;
	}

	@Override
	public FSFSystem getFSFSystem() {
		return databaseFS;
	}

	@Override
	public void moveTo(FSFFile otherFile) throws Exception {
		if ((otherFile.exists()) && (otherFile.isDirectory()) && (!otherFile.listFiles().isEmpty())){
			throw new Exception("Cannot move to '" + otherFile.getAbsolutePath() + "' because destination is non-empty directory");
		}
		copyFilesTree(otherFile);
		deleteTree();
	}

	@Override
	public void copyTo(FSFFile destFile) throws Exception {
		FSFFileUtil.verifyCopyPossible(this, destFile);
		FSFFileUtil.copyViaStreams(this, destFile);
	}

	@Override
	public long getLength() {
		if (id == ID_VALUE_ID_ROOT) {
			return 0;
		}
		
		try {
			readParentIfNecessary();
			String sql = DatabaseFSUtil.replaceSchemaAndTable(config, "%1", "select FILE_LENGTH from %1 where APP = ? AND TENANT = ? AND PARENT_ID = ? AND NAME = ?");
			Long result;
			try {
				result = DatabaseFSUtil.processQueryAsValue(config, sql, Long.class, config.getAppName(), config.getTenantName(), parentID, name);
			} catch (Exception e) {
				throw new Exception("Could not read details from table", e);
			}
			if (result == null) {
				return 0;
			}
			return result.longValue();
		} catch (Exception e) {
			internalError(e);
			return 0;
		}
	}

	@Override
	public String listDirAsString(ListDirFormatting formatting) {
		return FSFFileUtil.listDirAsString(this, formatting, null);
	}

	@Override
	public void copyFilesTree(FSFFile dest) throws Exception {
		FSFFileUtil.copyFilesTree(this, dest);
	}

	@Override
	public void deleteTree() throws Exception {
		FSFFileUtil.deleteTree(this);
	}

	@Override
	public List<FSFFile> listFilesTree() throws Exception {
		return FSFFileUtil.listFilesTree(this);
	}

	@Override
	public void setTimeLastModified(long timeLastModified) throws Exception {
		if (id == ID_VALUE_ID_ROOT) {
			return;
		}
		readParentIfNecessary();
		String sql = DatabaseFSUtil.replaceSchemaAndTable(config, "%1", "UPDATE %1 SET LAST_MODIFICATION_TIME = ? where APP = ? AND TENANT = ? AND PARENT_ID = ? AND NAME = ?");
		try {
			DatabaseFSUtil.processUpdate(config, sql, timeLastModified, config.getAppName(), config.getTenantName(), parentID, name);
		} catch (Exception e) {
			throw new Exception("Could not write details to table", e);
		}
	}

	@Override
	public boolean setTimeCreated(long timeCreated) throws Exception {
		if (id == ID_VALUE_ID_ROOT) {
			return false;
		}
		readParentIfNecessary();
		String sql = DatabaseFSUtil.replaceSchemaAndTable(config, "%1", "UPDATE %1 SET CREATION_TIME = ? where APP = ? AND TENANT = ? AND PARENT_ID = ? AND NAME = ?");
		try {
			DatabaseFSUtil.processUpdate(config, sql, timeCreated, config.getAppName(), config.getTenantName(), parentID, name);
		} catch (Exception e) {
			throw new Exception("Could not write details to table", e);
		}
		return true;
	}
	
	@Override
	public SortedSet<Long> getHistoryTimes() throws Exception {
		return new TreeSet<Long>();
	}

	@Override
	public void copyHistoryFilesTree(FSFFile dest, long historyTime) throws Exception {
		throw new Exception("Versions are not supported in this file system type");
	}

	@Override
	public InputStream getHistoryInputStream(long historyTime) throws Exception {
		throw new Exception("Versions are not supported in this file system type");
	}

	@Override
	public OutputStream getOutputStream(final boolean append) throws Exception {
		return new ByteArrayOutputStream() {
			@Override
			public void close() throws IOException {
				super.close();
				try {
					DatabaseFile.this.writeBytes(append, toByteArray());
				} catch (Exception e) {
					throw new IOException(e);
				}
			}
		};
	}

	@Override
	public InputStream getInputStream() throws Exception {
		return new ByteArrayInputStream(readBytes());
	}

	@Override
	public <K> K readObject(Class<K> classType) throws Exception {
		return FSFFileUtil.objectFromByteArray(readBytes(), classType);
	}

	@Override
	public <K> FSFFile writeObject(K objectToWrite) throws Exception {
		writeBytes(false, FSFFileUtil.objectToByteArray(objectToWrite));
		return this;
	}

	@Override
	public byte[] readBytes() throws Exception {
		readIDIfNecessary();
		if (!exists()) {
			throw new Exception("File does not exist");
		}
		return DatabaseFSUtil.readData(config, id);
	}

	@Override
	public FSFFile writeBytes(boolean append, byte[] bytes) throws Exception {
		readIDIfNecessary();
		log("writeBytes: " + listDetails());
		long time = databaseFS.getEnvironment().getCurrentTimeMillis();
		log("writeBytes: checkIfExists");
		if (!exists()) {
			log("writeBytes: does not exist. Insert entry");
			id = DatabaseFSUtil.insertEntry(config, FILE_TYPE_FILE, name, bytes.length, time, time, parentID, bytes);
		} else {
			log("writeBytes: exists update");
			DatabaseFSUtil.writeData(config, id, append, bytes, time);
		}
		return this;
	}

	private void log(String message) {
		if (LOGGING_ENABLED) {
			System.out.println("DatabaseFile> " + message);
		}
	}

	@Override
	public String readString() throws Exception {
		return FSFFileUtil.readString(this);
	}

	@Override
	public FSFFile writeString(String string) throws Exception {
		return FSFFileUtil.writeString(this, string);
	}

	@Override
	public VersionedData<InputStream> getInputStreamAndVersion() throws Exception {
		return new VersionedData<InputStream>(0, getInputStream());
	}

	@Override
	public OutputStream getOutputStreamForVersion(boolean append, long newVersion) throws WrongVersionException, Exception {
		return getOutputStream(append);
	}

	@Override
	public <K> VersionedData<K> readObjectAndVersion(Class<K> classType) throws Exception {
		return new VersionedData<K>(0, readObject(classType));
	}

	@Override
	public <K> FSFFile writeObjectForVersion(K objectToWrite, long newVersion) throws WrongVersionException, Exception {
		return writeObject(objectToWrite);
	}

	@Override
	public VersionedData<byte[]> readBytesAndVersion() throws Exception {
		return new VersionedData<byte[]>(0, readBytes());
	}

	@Override
	public FSFFile writeBytesForVersion(boolean append, byte[] bytes, long newVersion) throws WrongVersionException, Exception {
		return writeBytes(append, bytes);
	}

	@Override
	public VersionedData<String> readStringAndVersion() throws Exception {
		return new VersionedData<String>(0, readString());
	}

	@Override
	public FSFFile writeStringForVersion(String string, long newVersion) throws WrongVersionException, Exception {
		return writeString(string);
	}

	@Override
	public long getVersion() throws Exception {
		return 0;
	}
	
	@Override
	public long getVersion(boolean allowCache) throws Exception {
		return 0;
	}
	
	@Override
	public void setVersion(long version) throws Exception {
	}

	@Override
	public String toString() {
		return "DatabaseFile{path='" + getAbsolutePath() + "'}";
	}

	protected void setCachedAbsolutePath(String cachedAbsolutePath) {
		this.cachedAbsolutePath = cachedAbsolutePath;
	}
	
	private String listDetails() {
		StringBuilder result = new StringBuilder();
		result.append("[Details: id = " + id + ", name = '" + name + "', parentID = " + parentID + ", parent = " + parent + ", cachedAbsolutePath = " + cachedAbsolutePath + "]");
		return result.toString();
	}
	
	public long getIdInFileTable() {
		return id;
	}

	public long getParentIdInFileTable() {
		return parentID;
	}

}
