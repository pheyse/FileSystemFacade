package de.bright_side.filesystemfacade.remotefs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.SortedSet;

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
public class RemoteFile implements FSFFile{
	private RemoteFS remoteFS;
	private String absolutePath;
	private String separator;
	private Long cachedVersion;
	
	protected RemoteFile(RemoteFS remoteFS, String absolutePath, Long cachedVersion) {
		this.remoteFS = remoteFS;
		this.absolutePath = absolutePath;
		this.cachedVersion = cachedVersion;
		separator = remoteFS.getSeparator();
	}

	private RemoteFSResponse performRemotely(String command, Object ...parameters) throws WrongVersionException, Exception{
		return remoteFS.doRequest(absolutePath, command, null, parameters);
	}

	private RemoteFSResponse performRemotelyWithPayload(String command, InputStream payload, Object ...parameters) throws WrongVersionException, Exception{
		return remoteFS.doRequest(absolutePath, command, payload, parameters);
	}

	private long toLong(RemoteFSResponse response) {
		return response.getNumberResponse();
	}
	
	private boolean toBoolean(RemoteFSResponse response) {
		return response.isBooleanResponse();
	}

	private byte[] toBytes(RemoteFSResponse response) throws Exception {
		return FSFFileUtil.readAllBytes(response.getByteResponseInputStream());
	}

	@Override
	public List<FSFFile> listFiles() {
		try {
			return RemoteFSUtil.toFileList(remoteFS, performRemotely(RemoteFS.COMMAND_LIST_FILES));
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}

	@Override
	public String getName() {
		int pos = absolutePath.lastIndexOf(separator);
		if (pos < 0) {
			return absolutePath;
		}
//		if (pos <= separator.length() - 1) {
//			return "";
//		}
		return absolutePath.substring(pos + 1);
	}

	@Override
	public long getTimeLastModified() throws Exception {
		return toLong(performRemotely(RemoteFS.COMMAND_GET_TIME_LAST_MODIFIED));
	}

	@Override
	public long getTimeCreated() throws Exception {
		return toLong(performRemotely(RemoteFS.COMMAND_GET_TIME_CREATED));
	}

	@Override
	public boolean isFile() {
		try {
			return toBoolean(performRemotely(RemoteFS.COMMAND_IS_FILE));
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}

	@Override
	public boolean isDirectory() {
		try {
			return toBoolean(performRemotely(RemoteFS.COMMAND_IS_DIRECTORY));
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}

	@Override
	public boolean exists() {
		try {
			return toBoolean(performRemotely(RemoteFS.COMMAND_EXISTS));
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}

	@Override
	public FSFFile getParentFile() {
		int pos = absolutePath.lastIndexOf(separator);
		if (pos < 0) {
			return null;
		}
		String parentPath = absolutePath.substring(0, pos);
		return new RemoteFile(remoteFS, parentPath, null);
	}

	@Override
	public void rename(String newName) throws Exception {
		performRemotely(RemoteFS.COMMAND_RENAME, newName);
		absolutePath = getParentFile().getChild(newName).getAbsolutePath();
	}

	@Override
	public FSFFile getChild(String name) {
		return new RemoteFile(remoteFS, absolutePath + remoteFS.getSeparator() + name, null);
	}

	@Override
	public FSFFile mkdirs() throws Exception {
		performRemotely(RemoteFS.COMMAND_MKDIRS);
		return this;
	}

	@Override
	public FSFFile mkdir() throws Exception {
		performRemotely(RemoteFS.COMMAND_MKDIR);
		return this;
	}

	@Override
	public String getAbsolutePath() {
		return absolutePath;
	}

	@Override
	public void delete() throws Exception {
		performRemotely(RemoteFS.COMMAND_DELETE);
	}

	@Override
	public FSFSystem getFSFSystem() {
		return remoteFS;
	}

	@Override
	public void moveTo(FSFFile otherFile) throws Exception {
		if (!otherFile.getFSFSystem().getClass().getName().equals(remoteFS.getClass().getName())){
			throw new Exception("Moving to files of different file systems is not supported for RemoteFS");
		}
		RemoteFS otherRemoteFS = (RemoteFS) otherFile.getFSFSystem();
		if (!remoteFS.isSameLocation(otherRemoteFS)) {
			throw new Exception("Moving to files of different remote file systems is not supported for RemoteFS");
		}
		performRemotely(RemoteFS.COMMAND_MOVE_TO, otherFile.getAbsolutePath());		
	}

	@Override
	public void copyTo(FSFFile destFile) throws Exception {
		FSFFileUtil.verifyCopyPossible(this, destFile);
		FSFFileUtil.copyViaStreams(this, destFile);
//
		destFile.setVersion(getVersion(false));
		
//		
//		if (!destFile.getFSFSystem().getClass().getName().equals(remoteFS.getClass().getName())){
//			throw new Exception("Copying files to a different file system is not supported for RemoteFS");
//		}
//		RemoteFS otherRemoteFS = (RemoteFS) destFile.getFSFSystem();
//		if (!remoteFS.isSameLocation(otherRemoteFS)) {
//			throw new Exception("Copying files to a different remote file systems is not supported for RemoteFS");
//		}
	}

	@Override
	public long getLength() {
		try {
			return toLong(performRemotely(RemoteFS.COMMAND_GET_LENGTH));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String listDirAsString(ListDirFormatting formatting) {
		return FSFFileUtil.listDirAsString(this, formatting, null);
	}

	@Override
	public void copyFilesTree(FSFFile dest) throws Exception {
		FSFFileUtil.copyFilesTree(this, dest, true);
	}

	@Override
	public void deleteTree() throws Exception {
		performRemotely(RemoteFS.COMMAND_DELETE_TREE);		
	}

	@Override
	public List<FSFFile> listFilesTree() throws Exception {
		return FSFFileUtil.listFilesTree(this);
	}

	@Override
	public void setTimeLastModified(long timeLastModified) throws Exception {
		performRemotely(RemoteFS.COMMAND_SET_TIME_LAST_MODIFIED, "" + timeLastModified);		
	}

	@Override
	public boolean setTimeCreated(long timeCreated) throws Exception {
		performRemotely(RemoteFS.COMMAND_SET_TIME_CREATED, "" + timeCreated);
		return true;
	}

	@Override
	public SortedSet<Long> getHistoryTimes() throws Exception {
//		return new TreeSet<>();
		return RemoteFSUtil.toLongSortedSet(performRemotely(RemoteFS.COMMAND_GET_HISTORY_TIMES));
	}

	@Override
	public OutputStream getOutputStream(final boolean append) throws Exception {
		return new ByteArrayOutputStream() {
			@Override
			public void close() throws IOException {
				super.close();
				try {
					writeBytes(append, toByteArray());
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
		byte[] bytes = readBytes();
		return FSFFileUtil.objectFromByteArray(bytes, classType);
	}

	@Override
	public <K> FSFFile writeObject(K objectToWrite) throws Exception {
		byte[] bytes = FSFFileUtil.objectToByteArray(objectToWrite);
		return writeBytes(false, bytes);
	}

	@Override
	public byte[] readBytes() throws Exception {
		return toBytes(performRemotely(RemoteFS.COMMAND_READ_BYTES));
	}

	@Override
	public FSFFile writeBytes(boolean append, byte[] bytes) throws Exception {
		performRemotelyWithPayload(RemoteFS.COMMAND_WRITE_BYTES, new ByteArrayInputStream(bytes), append);
		return this;
	}

	@Override
	public String readString() throws Exception {
		return FSFFileUtil.readString(this);
	}

	@Override
	public FSFFile writeString(String string) throws Exception {
		FSFFileUtil.writeString(this, string);
		return this;
	}

	@Override
	public VersionedData<InputStream> getInputStreamAndVersion() throws Exception {
		RemoteFSResponse result = performRemotely(RemoteFS.COMMAND_READ_BYTES_AND_VERSION);
		return new VersionedData<InputStream>(result.getNumberResponse(), result.getByteResponseInputStream());
	}

	@Override
	public OutputStream getOutputStreamForVersion(final boolean append, final long newVersion) throws WrongVersionException, Exception {
		long currentVersion = getVersion(false);
		if (newVersion != currentVersion + 1) {
			throw new WrongVersionException("Current version is " + currentVersion + ", expected new version is " + (currentVersion + 1) + ", provided new version is " + newVersion);
		}
		
		return new ByteArrayOutputStream() {
			@Override
			public void close() throws IOException {
				super.close();
				try {
					writeBytesForVersion(append, toByteArray(), newVersion);
				} catch (Exception e) {
					throw new IOException(e);
				}
			}
		};
	}
	
	@Override
	public <K> VersionedData<K> readObjectAndVersion(Class<K> classType) throws Exception {
		RemoteFSResponse result = performRemotely(RemoteFS.COMMAND_READ_BYTES_AND_VERSION);
		byte[] bytes = FSFFileUtil.readAllBytes(result.getByteResponseInputStream());
		return new VersionedData<K>(result.getNumberResponse(), FSFFileUtil.objectFromByteArray(bytes, classType));
	}

	@Override
	public <K> FSFFile writeObjectForVersion(K objectToWrite, long newVersion) throws WrongVersionException, Exception {
		return writeBytesForVersion(false, FSFFileUtil.objectToByteArray(objectToWrite), newVersion);
	}

	@Override
	public VersionedData<byte[]> readBytesAndVersion() throws Exception {
		RemoteFSResponse result = performRemotely(RemoteFS.COMMAND_READ_BYTES_AND_VERSION);
		return new VersionedData<byte[]>(result.getNumberResponse(), FSFFileUtil.readAllBytes(result.getByteResponseInputStream()));
	}

	@Override
	public FSFFile writeBytesForVersion(boolean append, byte[] bytes, long newVersion) throws WrongVersionException, Exception {
		performRemotelyWithPayload(RemoteFS.COMMAND_WRITE_BYTES_FOR_VERSION, new ByteArrayInputStream(bytes), append, "" + newVersion);
		return this;
	}

	@Override
	public VersionedData<String> readStringAndVersion() throws Exception {
		RemoteFSResponse result = performRemotely(RemoteFS.COMMAND_READ_BYTES_AND_VERSION);
		return new VersionedData<String>(result.getNumberResponse(), FSFFileUtil.readString(result.getByteResponseInputStream()));
	}

	@Override
	public FSFFile writeStringForVersion(String string, long newVersion) throws WrongVersionException, Exception {
		byte[] bytes = FSFFileUtil.stringToByteArray(string);
		writeBytesForVersion(false, bytes, newVersion);
		return this;
	}

	@Override
	public long getVersion() throws Exception {
		return getVersion(true);
	}

	@Override
	public long getVersion(boolean allowCache) throws Exception {
		if ((cachedVersion == null) || (!allowCache)){
			cachedVersion = toLong(performRemotely(RemoteFS.COMMAND_GET_VERSION)); 
		}
		return cachedVersion;
	}
	
	@Override
	public void setVersion(long version) throws Exception {
		performRemotely(RemoteFS.COMMAND_SET_VERSION, "" + version);
	}
	
	@Override
	public InputStream getHistoryInputStream(long historyTime) throws Exception {
//		throw new Exception("History is not supported in this file system type");
		byte[] bytes = toBytes(performRemotely(RemoteFS.COMMAND_READ_HISTORY_BYTES, "" + historyTime));
		return new ByteArrayInputStream(bytes);
	}

	@Override
	public void copyHistoryFilesTree(FSFFile dest, long historyTime) throws Exception {
		throw new Exception("History is not supported in this file system type");
	}

	@Override
	public int compareTo(FSFFile other) {
		if (other == null){
			return 1;
		}
		int result = (other.getFSFSystem().getClass().getName()).compareTo(remoteFS.getClass().getName());
		if (result != 0){
			return result;
		}
		RemoteFS otherRemoteFS = (RemoteFS) other.getFSFSystem();
		result = remoteFS.compareLocation(otherRemoteFS);
		if (result != 0){
			return result;
		}
		return absolutePath.compareTo(other.getAbsolutePath());
	}

	@Override
	public String toString() {
		return "RemoteFile{path='" + absolutePath + "'}";
	}

}
