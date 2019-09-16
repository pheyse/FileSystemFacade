package de.bright_side.filesystemfacade.facade;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import de.bright_side.filesystemfacade.util.FSFFileUtil;
import de.bright_side.filesystemfacade.util.ListDirFormatting;

/**
 * @author Philip Heyse
 *
 */
abstract public class FSFFileWithInnerFile implements FSFFile{
	private FSFFile innerFile;

	public FSFFileWithInnerFile(FSFFile innerFile) {
		this.innerFile = innerFile;
	}

	@Override
	public int compareTo(FSFFile o) {
		return innerFile.compareTo(o);
	}

	@Override
	public List<FSFFile> listFiles() {
		List<FSFFile> innerResult = innerFile.listFiles();
		if (innerResult == null) {
			return null;
		}
		List<FSFFile> result = new ArrayList<FSFFile>();
		for (FSFFile i: innerResult) {
			result.add(wrap(i));
		}
		return result;
	}

	@Override
	public String getName() {
		return innerFile.getName();
	}

	@Override
	public long getTimeLastModified() throws Exception{
		return innerFile.getTimeLastModified();
	}
	
	@Override
	public long getTimeCreated() throws Exception {
		return innerFile.getTimeCreated();
	}

	@Override
	public boolean isFile() {
		return innerFile.isFile();
	}

	@Override
	public boolean isDirectory() {
		return innerFile.isDirectory();
	}

	@Override
	public boolean exists() {
		return innerFile.exists();
	}

	@Override
	public FSFFile getParentFile() {
		return wrap(innerFile.getParentFile());
	}

	@Override
	public OutputStream getOutputStream(boolean append) throws Exception {
		return innerFile.getOutputStream(append);
	}

	@Override
	public InputStream getInputStream() throws Exception {
		return innerFile.getInputStream();
	}

	@Override
	public void rename(String newName) throws Exception {
		innerFile.rename(newName);
	}

	@Override
	public FSFFile getChild(String name) {
		return wrap(innerFile.getChild(name));
	}

	@Override
	public FSFFile mkdirs() throws Exception {
		return wrap(innerFile.mkdirs());
	}

	@Override
	public FSFFile mkdir() throws Exception {
		return wrap(innerFile.mkdir());
	}

	@Override
	public String getAbsolutePath() {
		return innerFile.getAbsolutePath();
	}

	@Override
	public void delete() throws Exception {
		innerFile.delete();
	}

	@Override
	abstract public FSFSystem getFSFSystem() ;
//		return wrap(innerFile).getFSFSystem();
//	}

	@Override
	public <K> K readObject(Class<K> classType) throws Exception {
		return innerFile.readObject(classType);
	}

	@Override
	public <K> FSFFile writeObject(K objectToWrite) throws Exception {
		return wrap(innerFile.writeObject(objectToWrite));
	}

	@Override
	public void moveTo(FSFFile otherFile) throws Exception {
		innerFile.moveTo(otherFile);
	}

	@Override
	public void copyTo(FSFFile destFile) throws Exception {
		innerFile.copyTo(destFile);
		destFile.setVersion(getVersion(false));
	}

	@Override
	public long getLength() {
		return innerFile.getLength();
	}

	@Override
	public String listDirAsString(ListDirFormatting formatting) {
		return innerFile.listDirAsString(formatting);
	}

	@Override
	public byte[] readBytes() throws Exception {
		return innerFile.readBytes();
	}

	@Override
	public void copyFilesTree(FSFFile dest) throws Exception {
		FSFFileUtil.copyFilesTree(this, dest, true);
	}

	@Override
	public void deleteTree() throws Exception {
		innerFile.deleteTree();
	}

	@Override
	public List<FSFFile> listFilesTree() throws Exception {
		List<FSFFile> innerResult = innerFile.listFilesTree();
		if (innerResult == null) {
			return null;
		}
		List<FSFFile> result = new ArrayList<FSFFile>();
		for (FSFFile i: innerResult) {
			result.add(wrap(i));
		}
		return result;
	}

	@Override
	public void setTimeLastModified(long timeLastModified) throws Exception {
		innerFile.setTimeLastModified(timeLastModified);
	}

	@Override
	public FSFFile writeBytes(boolean append, byte[] bytes) throws Exception {
		return wrap(innerFile.writeBytes(append, bytes));
	}

	@Override
	public FSFFile writeString(String string) throws Exception {
		return wrap(innerFile.writeString(string));
	}

	@Override
	public String readString() throws Exception {
		return innerFile.readString();
	}

	@Override
	public SortedSet<Long> getHistoryTimes() throws Exception{
		return innerFile.getHistoryTimes();
	}

	@Override
	public void copyHistoryFilesTree(FSFFile dest, long version) throws Exception {
		innerFile.copyHistoryFilesTree(dest, version);		
	}

	@Override
	public InputStream getHistoryInputStream(long version) throws Exception {
		return innerFile.getHistoryInputStream(version);
	}

	@Override
	public VersionedData<InputStream> getInputStreamAndVersion() throws Exception {
		return innerFile.getInputStreamAndVersion();
	}

	@Override
	public OutputStream getOutputStreamForVersion(boolean append, long newVersion) throws WrongVersionException, Exception {
		return innerFile.getOutputStreamForVersion(append, newVersion);
	}

	@Override
	public <K> VersionedData<K> readObjectAndVersion(Class<K> classType) throws Exception {
		return innerFile.readObjectAndVersion(classType);
	}

	@Override
	public <K> FSFFile writeObjectForVersion(K objectToWrite, long newVersion) throws WrongVersionException, Exception {
		return wrap(innerFile.writeObjectForVersion(objectToWrite, newVersion));
	}

	@Override
	public VersionedData<byte[]> readBytesAndVersion() throws Exception {
		return innerFile.readBytesAndVersion();
	}

	@Override
	public FSFFile writeBytesForVersion(boolean append, byte[] bytes, long newVersion) throws WrongVersionException, Exception {
		return wrap(innerFile.writeBytesForVersion(append, bytes, newVersion));
	}

	@Override
	public long getVersion() throws Exception {
		return innerFile.getVersion();
	}

	@Override
	public long getVersion(boolean allowCache) throws Exception {
		return innerFile.getVersion(allowCache);
	}
	
	@Override
	public void setVersion(long version) throws Exception {
		innerFile.setVersion(version);
	}
	
	@Override
	public VersionedData<String> readStringAndVersion() throws Exception {
		return innerFile.readStringAndVersion();
	}

	@Override
	public FSFFile writeStringForVersion(String string, long newVersion) throws WrongVersionException, Exception {
		return wrap(innerFile.writeStringForVersion(string, newVersion));
	}

	@Override
	public boolean setTimeCreated(long timeCreated) throws Exception {
		return innerFile.setTimeCreated(timeCreated);
	}

	public FSFFile getInnerFile() {
		return innerFile;
	}
	
	public void setInnerFile(FSFFile innerFile) {
		this.innerFile = innerFile;
	}

	abstract protected FSFFile wrap(FSFFile innerFile);
}
