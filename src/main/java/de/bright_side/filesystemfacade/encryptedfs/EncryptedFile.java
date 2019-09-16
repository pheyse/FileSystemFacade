package de.bright_side.filesystemfacade.encryptedfs;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import de.bright_side.filesystemfacade.facade.FSFFile;
import de.bright_side.filesystemfacade.facade.FSFFileWithInnerFile;
import de.bright_side.filesystemfacade.facade.FSFSystem;
import de.bright_side.filesystemfacade.facade.VersionedData;
import de.bright_side.filesystemfacade.facade.WrongVersionException;
import de.bright_side.filesystemfacade.util.FSFFileUtil;
import de.bright_side.filesystemfacade.util.ListDirFormatting;

public class EncryptedFile extends FSFFileWithInnerFile{
	private EncryptedFS fs;
	private String absolutePath = null;

	protected EncryptedFile(EncryptedFS encryptedFS, FSFFile innerFile) {
		super(innerFile);
		this.fs = encryptedFS;
		absolutePath = getAbsolutePath();
	}
	
	protected EncryptedFile(EncryptedFS encryptedFS, FSFFile innerFile, String absolutePath) {
		super(innerFile);
		this.fs = encryptedFS;
		this.absolutePath = absolutePath;
	}

	@Override
	public List<FSFFile> listFiles() {
		List<FSFFile> innerUnsortedResult = getInnerFile().listFiles();
		if (innerUnsortedResult == null) {
			return null;
		}
		
		String parentAbsolutePath = FSFFileUtil.removeIfEndsWith(getAbsolutePath(), EncryptedFS.SEPARATOR);

		//: don't use collections sort, because in the special case of encrypted files with encrypted file names
		//: there may be multiple file names that actually mean the same plain file name
		//: hence getAbsolutePath() must be used which created an uncrypted version of the path
		SortedMap<String, FSFFile> pathToItemMap = new TreeMap<>();
		for (FSFFile i: innerUnsortedResult) {
			String absolutePath = parentAbsolutePath + EncryptedFS.SEPARATOR + fs.readPlainFilename(i.getName());
			EncryptedFile item = new EncryptedFile(fs, i, absolutePath);
			pathToItemMap.put(item.getAbsolutePath(), item);
		}

		return new ArrayList<FSFFile>(pathToItemMap.values());
	}

	@Override
	public String getName() {
		if (absolutePath != null) {
			if (absolutePath.isEmpty()) {
				return "";
			}
			int pos = absolutePath.lastIndexOf(EncryptedFS.SEPARATOR);
			if (pos >= 0) {
				return absolutePath.substring(pos + 1);
			}
		}
		
		if (fs.isRoot(this)) {
			return "";
		}
		return fs.readPlainFilename(getInnerFile().getName());
	}
	
	@Override
	public FSFFile getParentFile() {
		FSFFile innerParentFile = getInnerFile().getParentFile();
		
		if (innerParentFile == null) {
			return null;
		}
		
		if (!fs.isExternalPathInBasePath(innerParentFile.getAbsolutePath())) {
			return null;
		}
		
		innerParentFile = fs.toExistingInnerFileIfPossible(innerParentFile);
		return new EncryptedFile(fs, innerParentFile);
	}
	
	@Override
	public void rename(String newName) throws Exception{
		fs.validateNotRootWithException(this);
		
		fs.validateFilenameWithException(newName);
		setInnerFile(fs.toExistingInnerFileIfPossible(getInnerFile()));
		String encodedFilename = fs.createEncodedFilename(newName, getLength()); 
		getInnerFile().rename(encodedFilename);
		
		absolutePath = EncryptedFS.getParentPath(absolutePath) + EncryptedFS.SEPARATOR + newName;
		
	}
	
	@Override
	public FSFFile getChild(String name) {
		fs.validateFilenameWithRuntimeException(name);
		setInnerFile(fs.toExistingInnerFileIfPossible(getInnerFile()));
		FSFFile innerResult = fs.toExistingInnerFileIfPossibleByParentAndName(getInnerFile(), name, null);
		EncryptedFile result = new EncryptedFile(fs, innerResult);
		return result;
	}
	
	@Override
	public String getAbsolutePath() {
		if (absolutePath != null) {
			return absolutePath;
		}
		
		String innerAbsolutePath = getInnerFile().getAbsolutePath();
		if (!fs.isExternalPathInBasePath(innerAbsolutePath)) {
			throw new RuntimeException("the inner file points to path '" + innerAbsolutePath + "' which is not inside the base path '" + fs.getBasePath() + "'");
		}
		
		FSFFile parent = getParentFile();
		if (parent == null) {
			return getName();
		}
		
		String result = FSFFileUtil.removeIfEndsWith(parent.getAbsolutePath(), EncryptedFS.SEPARATOR) + EncryptedFS.SEPARATOR + getName(); 
		
		if (result.equals(EncryptedFS.SEPARATOR)) {
			return "";
		}
		
		return result;
	}
	
	@Override
	public FSFSystem getFSFSystem() {
		return fs;
	}

	@Override
	public long getLength() {
		if (fs.isRoot(this)) {
			return 0;
		}
		setInnerFile(fs.toExistingInnerFileIfPossible(getInnerFile()));
		if (!getInnerFile().exists()) {
			return 0;
		}
		return fs.readPlainDataLength(getInnerFile().getName());
	}
	
	@Override
	public String listDirAsString(ListDirFormatting formatting) {
		return FSFFileUtil.listDirAsString(this, formatting);
	}

	@Override
	public boolean exists() {
		setInnerFile(fs.toExistingInnerFileIfPossible(getInnerFile()));
		return getInnerFile().exists();
	}
	
	@Override
	public List<FSFFile> listFilesTree() throws Exception{
		return FSFFileUtil.listFilesTree(this);
	}
	
	@Override
	public InputStream getHistoryInputStream(long historyTime) throws Exception{
		throw new Exception("Versions are not supported in this file system type");
	}

	@Override
	public OutputStream getOutputStream(boolean append) throws Exception{
		return fs.createEncryptedOutputStream(getInnerFile().getOutputStream(append), append, this);	
	}

	@Override
	public InputStream getInputStream() throws Exception{
		InputStream inputStream = null;
		setInnerFile(fs.toExistingInnerFileIfPossible(getInnerFile()));
		try {
			inputStream = getInnerFile().getInputStream();
			if (inputStream == null) {
				return null;
			}
			
			return fs.createDecryptedInputStream(inputStream);
		} catch (Exception e) {
			if (inputStream != null) {
				inputStream.close();
			}
			throw e;
		}
	}
	
	@Override
	public byte[] readBytes() throws Exception{
		setInnerFile(fs.toExistingInnerFileIfPossible(getInnerFile()));
		byte[] encryptedBytes = getInnerFile().readBytes();
		return fs.decryptData(encryptedBytes);
	}
	
	@Override
	public FSFFile writeBytes(boolean append, byte[] bytes) throws Exception{
		byte[] encryptedBytes = fs.encryptData(bytes);
		setInnerFile(fs.toExistingInnerFileIfPossible(getInnerFile()));
		getInnerFile().writeBytes(append, encryptedBytes);
		long appendedLength = 0;
		if (append) {
			appendedLength = getLength();
		}
		fs.renameAccordingToLength(this, appendedLength, bytes.length);
		return this;
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
	public String readString() throws Exception {
		return FSFFileUtil.readString(this);
	}

	@Override
	public FSFFile writeString(String string) throws Exception {
		return FSFFileUtil.writeString(this, string);
	}
	
	@Override
	public VersionedData<InputStream> getInputStreamAndVersion() throws Exception{
		InputStream inputStream = null;
		setInnerFile(fs.toExistingInnerFileIfPossible(getInnerFile()));
		VersionedData<InputStream> innerVersionedData;
		try {
			innerVersionedData = getInnerFile().getInputStreamAndVersion(); 
			inputStream = innerVersionedData.getData();
			if (inputStream == null) {
				return null;
			}
			
			return new VersionedData<InputStream>(innerVersionedData.getVersion(), fs.createDecryptedInputStream(inputStream));
		} catch (Exception e) {
			if (inputStream != null) {
				inputStream.close();
			}
			throw e;
		}
	}

	@Override
	public OutputStream getOutputStreamForVersion(boolean append, long newVersion) throws WrongVersionException, Exception{
		return fs.createEncryptedOutputStream(getInnerFile().getOutputStreamForVersion(append, newVersion), append, this);
	}

	@Override
	public <K> VersionedData<K> readObjectAndVersion(Class<K> classType) throws Exception{
		setInnerFile(fs.toExistingInnerFileIfPossible(getInnerFile()));
		VersionedData<byte[]> bytesAndVersion = getInnerFile().readBytesAndVersion();
		byte[] encryptedBytes = bytesAndVersion.getData();
		byte[] decryptedBytes = fs.decryptData(encryptedBytes);

		K resultObject = FSFFileUtil.objectFromByteArray(decryptedBytes, classType); 
		return new VersionedData<K>(bytesAndVersion.getVersion(), resultObject);
	}
	
	@Override
	public <K> FSFFile writeObjectForVersion(K objectToWrite, long newVersion) throws WrongVersionException, Exception{
		return writeBytesForVersion(false, FSFFileUtil.objectToByteArray(objectToWrite), newVersion);
	}

	@Override
	public VersionedData<byte[]> readBytesAndVersion() throws Exception{
		setInnerFile(fs.toExistingInnerFileIfPossible(getInnerFile()));
		VersionedData<byte[]> bytesAndVersion = getInnerFile().readBytesAndVersion();
		byte[] encryptedBytes = bytesAndVersion.getData();
		return new VersionedData<byte[]>(bytesAndVersion.getVersion(), fs.decryptData(encryptedBytes));
	}

	@Override
	public FSFFile writeBytesForVersion(boolean append, byte[] bytes, long newVersion) throws WrongVersionException, Exception{
		byte[] encryptedBytes = fs.encryptData(bytes);
		setInnerFile(fs.toExistingInnerFileIfPossible(getInnerFile()));
		getInnerFile().writeBytesForVersion(append, encryptedBytes, newVersion);
		long appendedLength = 0;
		if (append) {
			appendedLength = getLength();
		}
		fs.renameAccordingToLength(this, appendedLength, bytes.length);
		return this;
	}

	@Override
	public VersionedData<String> readStringAndVersion() throws Exception{
		VersionedData<byte[]> bytesAndVersion = readBytesAndVersion();
		return new VersionedData<String>(bytesAndVersion.getVersion(), FSFFileUtil.readString(bytesAndVersion.getData()));
	}

	@Override
	public FSFFile writeStringForVersion(String string, long newVersion) throws WrongVersionException, Exception{
		byte[] bytes = FSFFileUtil.stringToByteArray(string);
		return writeBytesForVersion(false, bytes, newVersion);
	}
	
	@Override
	public long getVersion() throws Exception {
		return getVersion(true);
	}
	
	@Override
	public long getVersion(boolean allowCache) throws Exception {
		if (allowCache) {
			return getInnerFile().getVersion(allowCache);
		}
		
		setInnerFile(fs.toExistingInnerFileIfPossible(getInnerFile()));
		return getInnerFile().getVersion(allowCache);
	}

	@Override
	public void setVersion(long version) throws Exception {
		setInnerFile(fs.toExistingInnerFileIfPossible(getInnerFile()));
		getInnerFile().setVersion(version);
	}

	@Override
	public void copyHistoryFilesTree(FSFFile dest, long historyTime) throws Exception{
		throw new Exception("This method has not been implemented for EncryptedFile");
	}
	
	@Override
	public String toString() {
		String result = "EncryptedFile{name='" + getName() + "', innerFile=";
		if (getInnerFile() != null) {
			result += getInnerFile();
		}
		result += "}";
		return result;
	}
	
	@Override
	public FSFFile mkdirs() throws Exception {
		setInnerFile(fs.toExistingInnerFileIfPossible(getInnerFile()));
		super.mkdirs();
		setInnerFile(fs.toExistingInnerFileIfPossible(getInnerFile()));
		return this;
	}

	@Override
	public FSFFile mkdir() throws Exception {
		setInnerFile(fs.toExistingInnerFileIfPossible(getInnerFile()));
		super.mkdir();
		setInnerFile(fs.toExistingInnerFileIfPossible(getInnerFile()));
		return this;
	}

	public void copyTo(FSFFile destFile) throws Exception {
		FSFFileUtil.verifyCopyPossible(this, destFile);
		FSFFileUtil.copyViaStreams(this, destFile);
		destFile.setVersion(getVersion(false));
	}

	@Override
	public void moveTo(FSFFile otherFile) throws Exception {
		setInnerFile(fs.toExistingInnerFileIfPossible(getInnerFile()));
		
		if ((otherFile.exists()) && (otherFile.isDirectory()) && (!otherFile.listFiles().isEmpty())){
			throw new Exception("Cannot move to '" + otherFile.getAbsolutePath() + "' because destination is non-empty directory");
		}
		copyFilesTree(otherFile);
		deleteTree();
	}

	@Override
	public int compareTo(FSFFile other) {
		if (other == null){
			return 1;
		}
		if (other instanceof EncryptedFile){
			EncryptedFile otherEncryptedFile = (EncryptedFile)other;
			EncryptedFS otherEncryptedFileFS = (EncryptedFS)other.getFSFSystem();
			
			int result = fs.getBasePath().compareTo(otherEncryptedFileFS.getBasePath());
			if (result != 0) {
				return result;
			}
			result = getAbsolutePath().compareTo(otherEncryptedFile.getAbsolutePath());
			if (result != 0) {
				return result;
			}
			return super.compareTo(other);
		}
		return other.getClass().getName().compareTo(this.getClass().getName());
	}

	@Override
	protected FSFFile wrap(FSFFile innerFile) {
		return new EncryptedFile(fs, innerFile);
	}

}
