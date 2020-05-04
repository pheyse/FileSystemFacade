package de.bright_side.filesystemfacade.memoryfs;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
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
public class MemoryFile implements FSFFile{
	private static final boolean LOGGING_ENABLED = false;
	private MemoryFS memoryFS;
	private String path;

	public MemoryFile(MemoryFS memoryFS, String path){
		this.memoryFS = memoryFS;
		this.path = MemoryFSUtil.normalize(path);
	}

	private void log(String message) {
		if (LOGGING_ENABLED) {
			System.out.println("MemoryFile> " + message);
		}
	}
	
	@Override
	public int compareTo(FSFFile other) {
		if (other == null){
			return 1;
		}
		int result = (other.getFSFSystem().getClass().getName()).compareTo(memoryFS.getClass().getName());
		if (result != 0){
			return result;
		}
		return getAbsolutePath().compareTo(other.getAbsolutePath());
	}

	@Override
	public List<FSFFile> listFiles() {
		List<FSFFile> result = new ArrayList<FSFFile>();
		MemoryFSItem item = memoryFS.getItem(path);
		if ((item == null) || (!item.isDir())){
			return null;
		}
		for (String i: memoryFS.getChildPathsOrEmpty(path)){
			result.add(new MemoryFile(memoryFS, i));
		}
		return result;
	}

	@Override
	public String getName() {
		int pos = path.lastIndexOf(MemoryFS.SEPARATOR);
		if (pos < 0){
			return "";
		}
		return path.substring(pos + 1);
	}

	@Override
	public boolean isFile() {
		MemoryFSItem item = memoryFS.getItem(path);
		if (item == null){
			return false;
		}
		return !item.isDir();
	}

	@Override
	public boolean isDirectory() {
		MemoryFSItem item = memoryFS.getItem(path);
		if (item == null){
			return false;
		}
		return item.isDir();
	}

	@Override
	public boolean exists() {
		return memoryFS.getItem(path) != null;
	}
	
	@Override
	public FSFFile getParentFile() {
		String parentPath = MemoryFSUtil.getParentPath(path);
		if (parentPath == null){
			return null;
		}
		return new MemoryFile(memoryFS, parentPath);
	}

	@Override
	public OutputStream getOutputStream(boolean append) throws Exception {
		MemoryFSItem item = memoryFS.getItem(path);
		if ((item != null) && (item.isDir())){
			throw new Exception("path '" + path + "' is a directory");
		}
		
		if (getParentFile() == null) {
			throw new Exception("file's parent directory does not exist. File path = '" + getAbsolutePath() + "'"); 
		}
		
		long time = getCurrentTime();
		if (item == null){
			item = new MemoryFSItem(memoryFS, false, time, time);
		}
		if (append){
			if (item.getDataAsBytes() == null){
				item.setDataAsBytes(new byte[0]);
			}
		} else {
			item.setDataAsBytes(new byte[0]);
		}
		memoryFS.setItem(path, item);
		memoryFS.setExistenceInParentDir(path, true);
		item.setTimeLastModified(time);
		
		return new MemoryFileOutputStream(item, item.getDataAsBytes());
	}

	@Override
	public InputStream getInputStream() throws Exception {
		MemoryFSItem item = memoryFS.getItem(path);
		if (item == null){
			throw new Exception("There is no file at path '" + path + "'");
		}
		if (item.isDir()){
			throw new Exception("Path '" + path + "' points to a directory");
		}
		if (item.getDataAsBytes() == null){
			return new ByteArrayInputStream(new byte[]{});
		}
		return new ByteArrayInputStream(item.getDataAsBytes());
	}

	@Override
	public void rename(String newName) throws Exception {
		MemoryFSItem item = memoryFS.getItem(path);
		if (item == null){
			throw new Exception("There is no file at path '" + path + "'");
		}
		
		FSFFile parent = getParentFile();
		if ((parent == null) || (!parent.exists())){
			throw new Exception("The parent path does not exist: '" + parent.getAbsolutePath() + "'");
		}
		
		moveTo(parent.getChild(newName));
		
		path = getParentFile().getChild(newName).getAbsolutePath();
	}

	@Override
	public FSFFile getChild(String name) {
		return new MemoryFile(memoryFS, path + MemoryFS.SEPARATOR + name);
	}

	@Override
	public FSFFile mkdirs() {
		MemoryFSItem item = memoryFS.getItem(path);
		if (item == null){
			MemoryFile parentItem = (MemoryFile) getParentFile();
			if (parentItem != null){
				if (!parentItem.exists()){
					parentItem.mkdirs();
				} else {
					if (parentItem.isFile()){
						//: cannot make any more dirs if a file is in the middle of the path
						return this;
					}
				}
			} else {
				new MemoryFile(memoryFS, MemoryFSUtil.getParentPath(path)).mkdirs();
			}
			mkdir();
		}
		return this;
	}

	@Override
	public FSFFile mkdir() {
		MemoryFSItem item = memoryFS.getItem(path);
		if (item == null){
			long time = getCurrentTime();
			item = new MemoryFSItem(memoryFS, true, time, time);
			memoryFS.setItem(path, item);
			memoryFS.setExistenceInParentDir(path, true);
		}
		return this;
	}

	@Override
	public String getAbsolutePath() {
		return path;
	}

	@Override
	public void delete() throws Exception {
		MemoryFSItem item = memoryFS.getItem(path);
		if (item == null){
			return;
		}
		if ((exists()) && (isDirectory()) && (!listFiles().isEmpty())){
			throw new Exception("Cannot delete '" + getAbsolutePath() + "' because it is a non-empty directory");
		}
		memoryFS.removeItem(path);
		memoryFS.setExistenceInParentDir(path, false);
	}

	@Override
	public FSFSystem getFSFSystem() {
		return memoryFS;
	}

	@Override
	public <K> K readObject(Class<K> classType) throws Exception {
		MemoryFSItem item = memoryFS.getItem(path);
		if (item == null){
			throw new Exception("No file at path '" + path + "'");
		}
		if (item.isDir()){
			throw new Exception("Cannot read object data from directory. Path: '" + path + "'");
		}
		try{
			return MemoryFSUtil.readObject(item, classType);
		} catch (Exception e){
			throw new Exception("Could not read object of type '" + classType.getName() + "' in file '" + path + "'", e);
		}
	}

	@Override
	public <K> FSFFile writeObject(K objectToWrite) throws Exception {
		FSFFile parentFile = getParentFile();
		if (!parentFile.exists()){
			throw new Exception("Parent directory '" + parentFile.getAbsolutePath() + "' does not exist");
		}
		
		MemoryFSItem item = memoryFS.getItem(path);
		long time = getCurrentTime();
		if (item == null){
			item = new MemoryFSItem(memoryFS, false, time, time);
		}
		item.setDataAsBytes(FSFFileUtil.objectToByteArray(objectToWrite));
		memoryFS.setItem(path, item);
		memoryFS.setExistenceInParentDir(path, true);
		item.setTimeLastModified(time);
		
		return this;
	}

	@Override
	public void moveTo(FSFFile otherFile) throws Exception {
		if ((otherFile.exists()) && (otherFile.isDirectory()) && (!otherFile.listFiles().isEmpty())){
			throw new Exception("Cannot move to '" + otherFile.getAbsolutePath() + "' because destination is non-empty directory");
		}
		MemoryFSItem item = memoryFS.getItem(path);
		if (item == null){
			throw new Exception("The file to be moved does not exist: '" + path + "'");
		}
		
		copyFilesTree(otherFile);
		deleteTree();
//		path = otherFile.getAbsolutePath();
	}

	@Override
	public long getLength() {
		MemoryFSItem item = memoryFS.getItem(path);
		if ((item == null) || (item.isDir())){
			return 0;
		}
		if (item.getDataAsBytes() != null){
			return item.getDataAsBytes().length;
		}
		
		return 0;
	}

	/**
	 * this method can later be exchanged by an external provider class to be called in order to produce exact same results in unit tests
	 * @return
	 */
	private long getCurrentTime() {
		return memoryFS.getEnvironment().getCurrentTimeMillis();
	}

	@Override
	public String listDirAsString(ListDirFormatting formatting) {
		return FSFFileUtil.listDirAsString(this, formatting, null);
	}

	@Override
	public byte[] readBytes() throws Exception {
		MemoryFSItem item = memoryFS.getItem(path);
		if (item == null){
			throw new Exception("path '" + path + "' does not exist");
		}
		if (item.isDir()){
			throw new Exception("path '" + path + "' is a directory");
		}
		return Arrays.copyOf(item.getDataAsBytes(), item.getDataAsBytes().length);
	}

	@Override
	public void copyTo(FSFFile destFile) throws Exception {
		log("copyTo: from '" + getAbsolutePath() + "' to '" + destFile.getAbsolutePath() + "'");
		FSFFileUtil.verifyCopyPossible(this, destFile);
		if (getFSFSystem() == destFile.getFSFSystem()){
			MemoryFSItem item = memoryFS.getItem(path);
			if (item == null){
				throw new Exception("The file to be copied does not exist: '" + path + "'");
			}
			memoryFS.setItem(destFile.getAbsolutePath(), MemoryFSUtil.copy(item));
			memoryFS.setExistenceInParentDir(destFile.getAbsolutePath(), true);
		} else {
			FSFFileUtil.copyViaStreams(this, destFile);
		}
		
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
	public long getTimeLastModified() {
		MemoryFSItem item = memoryFS.getItem(path);
		if (item == null){
			return 0;
		}
		return item.getTimeLastModified();
	}

	@Override
	public long getTimeCreated() {
		MemoryFSItem item = memoryFS.getItem(path);
		if (item == null){
			return 0;
		}
		return item.getTimeCreated();
	}
	
	@Override
	public void setTimeLastModified(long timeLastModified) {
		MemoryFSItem item = memoryFS.getItem(path);
		if (item == null){
			return;
		}
		item.setTimeLastModified(timeLastModified);
	}
	
	@Override
	public boolean setTimeCreated(long timeCreated) throws Exception {
		MemoryFSItem item = memoryFS.getItem(path);
		if (item == null){
			return false;
		}
		item.setTimeCreated(timeCreated);
		return true;
	}

	@Override
	public FSFFile writeBytes(boolean append, byte[] bytes) throws Exception {
		FSFFileUtil.writeBytes(this, append, bytes);
		setTimeLastModified(getCurrentTime());
		return this;
	}

	@Override
	public FSFFile writeString(String string) throws Exception {
		FSFFileUtil.writeString(this, string);
		setTimeLastModified(getCurrentTime());
		return this;
	}

	@Override
	public String readString() throws Exception {
		return FSFFileUtil.readString(this);
	}

	@Override
	public SortedSet<Long> getHistoryTimes() {
		return new TreeSet<Long>();
	}

	@Override
	public void copyHistoryFilesTree(FSFFile dest, long version) throws Exception {
		throw new Exception("History is not supported in this file system type");
	}

	@Override
	public InputStream getHistoryInputStream(long version) throws Exception {
		throw new Exception("History is not supported in this file system type");
	}

	@Override
	public String toString() {
		return "MemoryFile{path='" + getAbsolutePath() + "'}";
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
	public long getVersion() {
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
	public VersionedData<String> readStringAndVersion() throws Exception {
		return new VersionedData<String>(0, readString());
	}

	@Override
	public FSFFile writeStringForVersion(String string, long newVersion) throws WrongVersionException, Exception {
		return writeString(string);
	}


}
