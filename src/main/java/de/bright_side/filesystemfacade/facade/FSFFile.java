package de.bright_side.filesystemfacade.facade;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.SortedSet;

import de.bright_side.filesystemfacade.util.ListDirFormatting;

/**
 * @author Philip Heyse
 *
 */
public interface FSFFile extends Comparable<FSFFile>{
	/** @return null if the object is not a directory*/
	public List<FSFFile> listFiles();
	public String getName();
	public long getTimeLastModified() throws Exception;
	/** 
	 * @throws Exception on error
	 * @return the time created if this is supported by the Java version (e.g. this will not work on Android before 8.0). If it is not supported, 0 is returned.
	 * */
	public long getTimeCreated() throws Exception;
	public boolean isFile();
	public boolean isDirectory();
	public boolean exists();
	public FSFFile getParentFile();
	
	/** renames the file. Afterwards the file points to the location with the new name
	 * @param newName the new name (no path or location!) of the file
	 * @throws Exception on error*/
	public void rename(String newName) throws Exception;
	public FSFFile getChild(String name);
	public FSFFile mkdirs() throws Exception;
	public FSFFile mkdir() throws Exception;
	public String getAbsolutePath();
	public void delete() throws Exception;
	public FSFSystem getFSFSystem();
	
	/**
	 * moves the file to the location of the other file. E.g. is this file is C:\\dir1\\myfile.txt and the other may be C:\\dir2\\moved.txt. 
	 * Afterwards the FSFFile instance remains pointing to the old(!) location.  This is because the file may have been moved to a different file system.
	 * @param otherFile contains the location and file system where the file should be moved to
	 * @throws Exception on error 
	 */
	public void moveTo(FSFFile otherFile) throws Exception;
	/**
	 * copies the file to the provided location (e.g. /myDir/myfile.txt to /myOtherDir/mycopy.txt. The destination may also be in a different file system
	 *  
	 * @param destFile contains the location and file system where the file should be copied to
	 * @throws Exception on error
	 */
	public void copyTo(FSFFile destFile) throws Exception;
	public long getLength();
	/**
	 * @param formatting null for default formatting 
	 * @return a formatted string with the sub-items of the file (if it is a directory) 
	 */
	public String listDirAsString(ListDirFormatting formatting);
	
	/**
	 * copies all sub-items recursively to the dest directory
	 * The dest directory may also be in a different FileSystem (e.g. you can copy from MemoryFS to NativeFS)
	 * @param dest the path and file system where the directory should be copied to. All sub-items of the source directory will be moved to a directory with the name provided by the dest parameter
	 * @throws Exception on error
	 */
	public void copyFilesTree(FSFFile dest) throws Exception;
	/**
	 * deletes the given item and all sub-items recursively
	 * @throws Exception on error
	 */
	public void deleteTree() throws Exception;
	/**
	 * @return all sub-items recursively and as a list ordered by path
	 * @throws Exception on error
	 */
	public List<FSFFile> listFilesTree() throws Exception;
	public void setTimeLastModified(long timeLastModified) throws Exception;
	
	/** 
	 * sets the time created if this is supported by the Java version (e.g. this will not work on Android before 8.0).
	 * @param timeCreated the new creation time
	 * @return true if the creation time could be set.
	 * @throws Exception on error
	 * */
	public boolean setTimeCreated(long timeCreated) throws Exception;

	/** 
	 * lists the historized versions of a file. Only HistoryFS created versions. If there are no history versions, an empty set is returned
	 * Each long value is the time when the version was created in milliseconds.
	 * @return a sorted set of the history times of the file
	 * @throws Exception on error
	 * */
	public SortedSet<Long> getHistoryTimes() throws Exception;

	/**
	 * copies all sub-items recursively to the dest directory. If the object is a file, that file is copied
	 * @param dest the location and file system where the history file try should be copied to
	 * @param historyTime history time of the archived version to be copied (as provided by "getHistoryTimes()")
	 * @throws Exception on error
	 */
	public void copyHistoryFilesTree(FSFFile dest, long historyTime) throws Exception;

	/**
	 * @param historyTime history time of the archived version (as provided by "getHistoryTimes()")
	 * @throws Exception on error
	 * @return the input stream for the provided history time of the file
	 */
	public InputStream getHistoryInputStream(long historyTime) throws Exception;

	public OutputStream getOutputStream(boolean append) throws Exception;
	public InputStream getInputStream() throws Exception;
	
	public <K> K readObject(Class<K> classType) throws Exception;
	public <K> FSFFile writeObject(K objectToWrite) throws Exception;

	public byte[] readBytes() throws Exception;
	public FSFFile writeBytes(boolean append, byte[] bytes) throws Exception;
	
	public String readString() throws Exception;
	public FSFFile writeString(String string) throws Exception;


	/**
	 * 
	 * @return the version of the file and the input stream to its data. 
	 * If the file system doesn't support versions, the provided version is 0
	 * @throws Exception on error
	 */
	public VersionedData<InputStream> getInputStreamAndVersion() throws Exception;

	/**
	 * gets the output stream to update a file and set the version to newVersion. 
	 * If the file is new, the newVersion must be 0.
	 * If that version already exists a WrongVersionException exception is thrown. This way the file can be read by multiple threads/instances/clients/... 
	 * and then updated and the updated version can be written to the file system while ensuring that no other thread/instance/client/... has made
	 * a change to that file in parallel.
	 * 
	 * If the file system doesn't support versions, the newVersion field is ignored
	 * 
	 * 
	 * @param append if true the data is appended to the file. Otherwise the file is overwritten if it exists.
	 * @param newVersion new version of the file which must be the current version of the file +1 or 0 for new files
	 * @return the output stream for the provided version
	 * @throws WrongVersionException if the version is not the current version + 1 or 0 for new files
	 */
	public OutputStream getOutputStreamForVersion(boolean append, long newVersion) throws WrongVersionException, Exception;
	

	/**
	 * 
	 * @return the version of the file and the object stored in it. 
	 * If the file system doesn't support versions, the provided version is 0
	 * @param <K> This is the type of class
	 * @param classType the class of the object to be read
	 * @throws Exception on error
	 */
	public <K> VersionedData<K> readObjectAndVersion(Class<K> classType) throws Exception;

	/**
	 * writes the object to a file and set the version to newVersion. 
	 * If the file is new, the newVersion must be 0.
	 * If that version already exists a WrongVersionException exception is thrown. This way the file can be read by multiple threads/instances/clients/... 
	 * and then updated and the updated version can be written to the file system while ensuring that no other thread/instance/client/... has made
	 * a change to that file in parallel.
	 * 
	 * If the file system doesn't support versions, the newVersion field is ignored
	 * 
	 * @param <K> type the class of the object to be written
	 * @param objectToWrite the object to be written
	 * @param newVersion new version of the file which must be the current version of the file +1 or 0 for new files
	 * @return the file object that was been written (to allow a cascade of method calls)
	 * @throws WrongVersionException if the version is not the current version + 1 or 0 for new files
	 * @throws Exception on general error
	 */
	public <K> FSFFile writeObjectForVersion(K objectToWrite, long newVersion) throws WrongVersionException, Exception;

	/**
	 * 
	 * @return the file data as a byte array and the current version of the file. 
	 * If the file system doesn't support versions, the provided version is 0
	 * @throws Exception on general error
	 */
	public VersionedData<byte[]> readBytesAndVersion() throws Exception;

	/**
	 * writes the byte array to a file and set the version to newVersion. 
	 * If the file is new, the newVersion must be 0.
	 * If that version already exists a WrongVersionException exception is thrown. This way the file can be read by multiple threads/instances/clients/... 
	 * and then updated and the updated version can be written to the file system while ensuring that no other thread/instance/client/... has made
	 * a change to that file in parallel.
	 * 
	 * If the file system doesn't support versions, the newVersion field is ignored
	 * 
	 * 
	 * @param newVersion new version of the file which must be the current version of the file +1 or 0 for new files
	 * @param bytes bytes to be written
	 * @param append if true the data is appended to the file. Otherwise the file is overwritten if it exists.
	 * @return the file object that was been written (to allow a cascade of method calls)
	 * @throws WrongVersionException if the version is not the current version + 1 or 0 for new files
	 * @throws Exception on general error
	 */
	public FSFFile writeBytesForVersion(boolean append, byte[] bytes, long newVersion) throws WrongVersionException, Exception;

	/**
	 * 
	 * @return the file data as a String and the current version of the file. 
	 * If the file system doesn't support versions, the provided version is 0
	 * @throws Exception on general error
	 */
	public VersionedData<String> readStringAndVersion() throws Exception;
	
	/**
	 * writes the String to a file and set the version to newVersion. 
	 * If the file is new, the newVersion must be 0.
	 * If that version already exists a WrongVersionException exception is thrown. This way the file can be read by multiple threads/instances/clients/... 
	 * and then updated and the updated version can be written to the file system while ensuring that no other thread/instance/client/... has made
	 * a change to that file in parallel.
	 * 
	 * If the file system doesn't support versions, the newVersion field is ignored
	 * 
	 * 
	 * @param string string to be written
	 * @param newVersion new version of the file which must be the current version of the file +1 or 0 for new files
	 * @return the file object that was been written (to allow a cascade of method calls)
	 * @throws WrongVersionException if the version is not the current version + 1 or 0 for new files
	 * @throws Exception on general error
	 */
	public FSFFile writeStringForVersion(String string, long newVersion) throws WrongVersionException, Exception;
	
	/**
	 * 
	 * @return the current version of the file. 
	 * If the file system doesn't support versions, the provided version is 0. If the file doesn't exist the version is also 0.
	 * The version number may be read from the cache which is faster. However the cache could be outdated it the version has changed in the mean time. Use getVersion(false) to 
	 * read the version without using the cache. 
	 * @throws Exception on general error
	 */
	public long getVersion() throws Exception;
	
	/**
	 * @param allowCache if true then the version number may be read from the cache which is faster. However the cache could be outdated it the version has changed in the mean time
	 * @return the current version of the file. 
	 * If the file system doesn't support versions, the provided version is 0. If the file doesn't exist the version is also 0.
	 * @throws Exception on general error
	 */
	public long getVersion(boolean allowCache) throws Exception;
	
	/**
	 * If the file system supports versions, the version is set to the given value. Otherwise the command is ignored.
	 * @param version the new value to be set
	 * @throws Exception on general error
	 */
	void setVersion(long version) throws Exception;
	
}
