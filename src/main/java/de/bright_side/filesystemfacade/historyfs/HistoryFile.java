package de.bright_side.filesystemfacade.historyfs;

import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import de.bright_side.filesystemfacade.databasefs.DatabaseFile;
import de.bright_side.filesystemfacade.facade.FSFFile;
import de.bright_side.filesystemfacade.facade.FSFFileWithInnerFile;
import de.bright_side.filesystemfacade.facade.FSFSystem;
import de.bright_side.filesystemfacade.facade.IllegalPathItemNameException;
import de.bright_side.filesystemfacade.facade.VersionedData;
import de.bright_side.filesystemfacade.facade.WrongVersionException;
import de.bright_side.filesystemfacade.util.FSFFileUtil;
import de.bright_side.filesystemfacade.util.ListDirFormatting;
import de.bright_side.filesystemfacade.util.ListDirFormatting.Style;

/**
 * @author Philip Heyse
 *
 */
public class HistoryFile extends FSFFileWithInnerFile{
	private static final boolean LOGGING_ENABLED = true;

	private static final String TIMESTAMP_STRING = "yyyy-MM-dd'T'HH-mm-ss-SSS";
	private static final int TIMESTAMP_LENGTH = TIMESTAMP_STRING.replace("'", "").length();
	private static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat(TIMESTAMP_STRING);
	private static final String BACKUP_FILE_NAME_PART = "_";
	private static final int BACKUP_FILE_NAME_PART_LENGTH = BACKUP_FILE_NAME_PART.length();
	
	private FSFFile innerFile;
	private HistoryFS historyFS;
	private Long cachedVersion = null;


	protected HistoryFile(HistoryFS historyFS, FSFFile innerFile) {
		super(innerFile);
		this.historyFS = historyFS;
		this.innerFile = innerFile;
	}
	
	private Set<String> getTechnicalDirNames(){
		Set<String> result = new HashSet<>();
		result.add(historyFS.getHistoryDirName());
		result.add(historyFS.getVersionDirName());
		return result;
	}

	@Override
	public List<FSFFile> listFiles() {
		Set<String> namesToSkip = getTechnicalDirNames();
		List<FSFFile> files = getInnerFile().listFiles();
		if (files == null) {
			return null;
		}
		List<FSFFile> result = new ArrayList<>();
		for (FSFFile i : files) {
			if (!namesToSkip.contains(i.getName())) {
				result.add(wrap(i));
			}
		}
		return result;
	}
	
	private void assertNameAllowed(String name) throws IllegalPathItemNameException {
		if (name == null) {
			throw new IllegalPathItemNameException("The name may not be null");
		}
		if (name.isEmpty()) {
			throw new IllegalPathItemNameException("The name may not be ''");
		}
		if (historyFS.getHistoryDirName().equals(name)) {
			throw new IllegalPathItemNameException("The name of the file may not be equal to the history dir name '" + historyFS.getHistoryDirName() + "'");
		}
		if (historyFS.getVersionDirName().equals(name)) {
			throw new IllegalPathItemNameException("The name of the file may not be equal to the version dir name '" + historyFS.getVersionDirName() + "'");
		}
	}

	@Override
	public void rename(String newName) throws Exception {
		assertNameAllowed(newName);
		FSFFile versionFile = null;
		if (historyFS.isTrackVersions()) {
			versionFile = getVersionFile();
			if (!versionFile.exists()) {
				versionFile = null;
			}
		}
		super.rename(newName);
		if (historyFS.isTrackVersions()) {
			if (versionFile != null) {
				versionFile.rename(newName);
			}
		}
		
//		innerFile = ((HistoryFile)(getParentFile().getChild(newName))).innerFile;
	}

	@Override
	public FSFFile getChild(String name) {
		try {
			assertNameAllowed(name);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return super.getChild(name);
	}

	@Override
	public FSFSystem getFSFSystem() {
		return historyFS;
	}
	
	private void updateHistory() throws Exception {
		updateHistory(false);
	}
	
	@SuppressWarnings("unused")
	private void logFS(String message) throws Exception {
		if (LOGGING_ENABLED) {
			ListDirFormatting formatting = new ListDirFormatting().setStyle(Style.TREE).setAllSubItems(true);
			System.out.println(message + "\n" + innerFile.getFSFSystem().createByPath("").listDirAsString(formatting) + "\n---------------------\n");
		}
	}

	private void updateVersion(boolean fileExists) throws Exception{
		if (!historyFS.isTrackVersions()) {
			return;
		}
		if (fileExists) {
			long currentVersion = getVersionInternally(fileExists);
			setVersion(currentVersion + 1);
		}
	}
	
	@Override
	public void setVersion(long version) throws Exception {
    	FSFFile versionFile = getVersionFile();
    	if (version == 0) {
    		//: ignore this command
    		return;
    	}
    	if (version < 0) {
    		throw new Exception("Existing files may not have versions < 0");
    	}

    	if (version == 1) {
    		//: special case: if the file exists and there is no version file, then the version is 1
        	if (versionFile.exists()) {
        		versionFile.delete();
        	}
        	return;
    	}
    	
    	FSFFile versionDir = versionFile.getParentFile(); 
    	if (!versionDir.exists()) {
    		versionDir.mkdirs();
    	}
    	versionFile.writeString("" + version);
    	cachedVersion = version;
	}

	private void updateHistory(boolean appendToFile) throws Exception {
		if (!exists()){
			return;
		}
		if (historyFS.getMaxNumberOfHistoryFiles() == 0) {
			return;
		}
		String name = innerFile.getName();
		String ending = getFileEnding(this);
		String nameWithoutEnding = getNameWithoutEnding(ending, name);

		FSFFile historyDir = getHistoryDir(innerFile);
		if (!historyDir.exists()) {
			historyDir.mkdir();
		}
//		logFS("updateHistory: make hist dir");
		
		FSFFile historyFile = createHistoryFile(ending, nameWithoutEnding, historyDir);
//		logFS("updateHistory: history file: '" + innerFile.getAbsolutePath() + "'");
		if (appendToFile) {
			innerFile.copyTo(historyFile);
		} else {
			innerFile.moveTo(historyFile);
		}
//		logFS("updateHistory: moved history file: '" + innerFile.getAbsolutePath() + "' to '" + historyFile.getAbsolutePath() + "'");
		removeOldHistory(historyDir, nameWithoutEnding, ending, historyFS.getMaxNumberOfHistoryFiles());
//		logFS("after removing history items");
	}
	
	private FSFFile createHistoryFile(String ending, String nameWithoutEnding, FSFFile historyDir) {
		return createHistoryFile(ending, nameWithoutEnding, historyDir, historyFS.getEnvironment().getCurrentTimeMillis());
	}

	private FSFFile createHistoryFile(String ending, String nameWithoutEnding, FSFFile historyDir, long time) {
		return historyDir.getChild(nameWithoutEnding + BACKUP_FILE_NAME_PART + TIMESTAMP_FORMAT.format(time) + ending);
	}

	private String getNameWithoutEnding(String ending, String name) {
		return name.substring(0, name.length() - ending.length());
	}

	private FSFFile getHistoryDir(FSFFile innerFile) {
		FSFFile parentFile = innerFile.getParentFile();
		return parentFile.getChild(historyFS.getHistoryDirName());
	}

	private FSFFile getVersionDir(FSFFile innerFile) {
		FSFFile parentFile = innerFile.getParentFile();
		return parentFile.getChild(historyFS.getVersionDirName());
	}
	
	private FSFFile getVersionFile(FSFFile innerFile) {
		return getVersionDir(innerFile).getChild(innerFile.getName());
	}

	private FSFFile getVersionFile() {
		return getVersionFile(innerFile);
	}
	
	private void removeOldHistory(FSFFile historyDir, String nameWithoutEnding, String ending, int maximumAmountOfOldFiles) throws Exception {
		if (maximumAmountOfOldFiles < 0) {
			return;
		}
		List<FSFFile> historyFiles = readHistory(historyDir, nameWithoutEnding, ending);
		Collections.sort(historyFiles);
		
//		log("removeOldHistory: historyFiles = " + historyFiles);
		
		while (historyFiles.size() > maximumAmountOfOldFiles){
			FSFFile itemToRemove = historyFiles.get(0);
			if (itemToRemove.isDirectory()) {
				itemToRemove.deleteTree();
			} else {
				itemToRemove.delete();
			}
			historyFiles.remove(0);
		}
	}

	private static String getFileEnding(FSFFile file) {
		if (file.isDirectory()) {
			return "";
		}
		int index = file.getName().lastIndexOf(".");
		if (index < 0) {
			return "";
		}
		return file.getName().substring(index);
	}

	private List<FSFFile> readHistory(FSFFile historyDir, String nameWithoutEnding, String ending) throws Exception {
		String prefix = nameWithoutEnding + BACKUP_FILE_NAME_PART;
		List<FSFFile> result = new ArrayList<FSFFile>();
		int expectedLength = prefix.length() + TIMESTAMP_LENGTH + ending.length();
		List<FSFFile> files = historyDir.listFiles();
		if (files == null){
			return result;
		}
		for (FSFFile i: files){
			String name = i.getName();
			if ((name.startsWith(prefix)) && (name.endsWith(ending)) && (name.length() == expectedLength)){
				result.add(i);
			}
		}
		return result;
	}
	
	private Long getTimeCreatedIfExists(boolean exists) throws Exception {
		if (exists) {
			return innerFile.getTimeCreated();
		}
		return null;
	}

	private void setTimeCreatedIfAvailable(Long timeCreated) throws Exception {
		if (timeCreated != null) {
			innerFile.setTimeCreated(timeCreated);
		}
	}
	
	@Override
	public <K> FSFFile writeObject(K objectToWrite) throws Exception {
		if (isDirectory()) {
			throw new Exception("Objects can be written into files but not into directories");
		}
		boolean exists = innerFile.exists();
		Long timeCreated = getTimeCreatedIfExists(exists); 
		
		updateHistory();
		FSFFile result = super.writeObject(objectToWrite);
		setTimeCreatedIfAvailable(timeCreated);
		updateVersion(exists);
		return result;
	}
	
	@Override
	public OutputStream getOutputStream(boolean append) throws Exception {
		if (isDirectory()) {
			throw new Exception("Output streams can be obtained from files but not from directories");
		}
		boolean exists = innerFile.exists();
		Long timeCreated = getTimeCreatedIfExists(exists);
		updateHistory(append);
		
		boolean appendNeeded = append;
		if ((!append) && (exists)){
			//: the file exists already and is updated without appending to it: create empty file so that the creation time can be "kept"
			appendNeeded = true;
			innerFile.writeBytes(false, new byte[] {});
			innerFile.setTimeCreated(timeCreated);
		}
		
		updateVersion(exists);
		return innerFile.getOutputStream(appendNeeded);
	}

	
	@Override
	public FSFFile writeBytes(boolean append, byte[] bytes) throws Exception {
		if (isDirectory()) {
			throw new Exception("Byte arrays can be written into files but not into directories");
		}
		boolean exists = innerFile.exists();
		Long timeCreated = getTimeCreatedIfExists(exists);
		updateHistory(append);
		updateVersion(exists);
		innerFile.writeBytes(append, bytes);
		setTimeCreatedIfAvailable(timeCreated);
		return this;
	}

	@Override
	public FSFFile writeString(String string) throws Exception {
//		logFS("writeString before writing string '" + string + "'");
//		logFS("writeString: inner file info 1: " + getInnerFileInfo());
		
		if (isDirectory()) {
			throw new Exception("Strings can be written into files but not into directories");
		}

		boolean exists = innerFile.exists();
		Long timeCreated = getTimeCreatedIfExists(exists);
//		logFS("writeString before update history");
//		logFS("writeString: inner file info 2 (before updateHistory): " + getInnerFileInfo());
		updateHistory();
		
//		logFS("writeString after update history");
//		logFS("writeString: inner file info 3 (after updateHistory): " + getInnerFileInfo());
//		log("innerFile = " + innerFile + ", exists = " + innerFile.exists());
		
		updateVersion(exists);
//		logFS("writeString: inner file info 4: " + getInnerFileInfo());
		
		innerFile.writeString(string);
		
//		logFS("writeString: inner file info 5: " + getInnerFileInfo());
		setTimeCreatedIfAvailable(timeCreated);
//		logFS("writeString: inner file info 6: " + getInnerFileInfo());
//		logFS("writeString writing string '" + string + "'");
		return this;
	}

	@SuppressWarnings("unused")
	private String getInnerFileInfo() throws Exception {
		StringBuilder sb = new StringBuilder("Inner file info: [");
		sb.append(innerFile.getAbsolutePath() + " creation time: " + innerFile.getTimeCreated() + " modification time: " + innerFile.getTimeLastModified() + ", name: '" + innerFile.getName() + "'");
		if (innerFile instanceof DatabaseFile) {
			DatabaseFile databaseFile = (DatabaseFile)innerFile;
			sb.append(", ID in table: " + databaseFile.getIdInFileTable());
			sb.append(", parent ID in table: " + databaseFile.getParentIdInFileTable());
		}
		sb.append("]");
		
		return sb.toString();
	}

	private void deleteVersionData() throws Exception {
		if (!historyFS.isTrackVersions()) {
			return;
		}
		FSFFile versionDir = getVersionDir(innerFile);
		
		FSFFile versionFile = getVersionFile();
		if (versionFile.exists()) {
			versionFile.delete();
		}
		List<FSFFile> remainingVersionFiles = null;
		if (versionDir.exists()) {
			remainingVersionFiles = versionDir.listFiles();
			if ((remainingVersionFiles == null) || (remainingVersionFiles.isEmpty())) {
				versionDir.delete();
			}
		}
	}

	
	@Override
	public void deleteTree() throws Exception {
		if (!exists()) {
			return;
		}
		updateHistory();
		innerFile.deleteTree();
		deleteVersionData();
	}

	@Override
	public String listDirAsString(ListDirFormatting formatting) {
		//: don't show history dir names
		return FSFFileUtil.listDirAsString(this, formatting, getTechnicalDirNames());
	}
	
	@Override
	public List<FSFFile> listFilesTree() throws Exception {
		return FSFFileUtil.listFilesTree(this, getTechnicalDirNames());
	}

	@Override
	public void delete() throws Exception {
		if (!exists()) {
			throw new Exception("File/directory cannot be deleted because it doesn't exist");
		}
		if (((isDirectory()) && (listFiles() != null) && (!listFiles().isEmpty()))) {
			throw new Exception("The directory cannot be deleted because it is not empty");
		}
		
		updateHistory();
		innerFile.delete();
		deleteVersionData();
	}

	@Override
	public SortedSet<Long> getHistoryTimes() throws Exception{
		SortedSet<Long> result = new TreeSet<>();
		
		String name = innerFile.getName();
		String ending = getFileEnding(this);
		String nameWithoutEnding = getNameWithoutEnding(ending, name);

		List<FSFFile> historyFiles = readHistory(getHistoryDir(innerFile), nameWithoutEnding, ending);
		
		int nameWithoutEndingLength = nameWithoutEnding.length();
		int endingLength = ending.length();
		
		for (FSFFile i: historyFiles) {
			result.add(readVersion(i.getName(), nameWithoutEndingLength, endingLength));
		}
		
		return result;
	}

	protected static long readVersion(String name, int nameWithoutEndingLength, int endingLength) throws Exception{
		String timeStamp = name.substring(nameWithoutEndingLength + BACKUP_FILE_NAME_PART_LENGTH, name.length() - endingLength);
		return TIMESTAMP_FORMAT.parse(timeStamp).getTime();
	}

	private FSFFile getHistoryFile(long historyTime) {
		String name = innerFile.getName();
		String ending = getFileEnding(this);
		String nameWithoutEnding = getNameWithoutEnding(ending, name);
		FSFFile historyDir = getHistoryDir(innerFile);
		return createHistoryFile(ending, nameWithoutEnding, historyDir, historyTime);
	}
	
	@Override
	public void copyHistoryFilesTree(FSFFile dest, long historyTime) throws Exception {
		FSFFile historyFile = getHistoryFile(historyTime);
		if (!historyFile.exists()) {
			throw new Exception("There is no history entry for time " + historyTime);
		}
		historyFile.copyFilesTree(dest);
	}

	@Override
	public InputStream getHistoryInputStream(long historyTime) throws Exception {
		FSFFile historyFile = getHistoryFile(historyTime);
		if (!historyFile.exists()) {
			throw new Exception("There is no history entry " + historyTime + " (time: " + TIMESTAMP_FORMAT.format(historyTime) + ", expected location: " + historyFile + ")");
		}
		return historyFile.getInputStream();
	}

	@Override
	public String toString() {
		return "HistoryFile{path='" + innerFile.getAbsolutePath() + "', innerFile = " + innerFile + "}";
	}
	
	@Override
	public int compareTo(FSFFile other) {
		if (other == null){
			return 1;
		}
		int result = (other.getFSFSystem().getClass().getName()).compareTo(historyFS.getClass().getName());
		if (result != 0){
			return result;
		}
		return getAbsolutePath().compareTo(other.getAbsolutePath());
	}
	
	@Override
	public void moveTo(FSFFile otherFile) throws Exception {
		FSFFile oldVersionFile = null;
		FSFFile versionsDir = null;
		if (historyFS.isTrackVersions()) {
			oldVersionFile = getVersionFile();
			if (!oldVersionFile.exists()) {
				oldVersionFile = null;
			} else {
				versionsDir = oldVersionFile.getParentFile();
			}
		}
		
		if (otherFile instanceof HistoryFile) {
			HistoryFile otherHistoryFile = (HistoryFile)otherFile;
			innerFile.moveTo(otherHistoryFile.innerFile);
			if (oldVersionFile != null) {
				FSFFile newVersionFile = getVersionFile(otherHistoryFile.innerFile);
//				log("moveTo: moving old version file from '" + oldVersionFile.getAbsolutePath() + "' to '" + newVersionFile.getAbsolutePath() + "'");
				newVersionFile.getParentFile().mkdirs();
				oldVersionFile.moveTo(newVersionFile);
			}
		} else {
			super.moveTo(otherFile);
		}
		
		if ((versionsDir != null) && (versionsDir.exists()) && (versionsDir.listFiles() != null) && (versionsDir.listFiles().isEmpty())) {
			versionsDir.delete();
		}
	}

	@SuppressWarnings("unused")
	private void log(String message) {
		if (LOGGING_ENABLED) {
			System.out.println("HistoryFile >" + message);
		}
	}
	
    @Override
	public VersionedData<InputStream> getInputStreamAndVersion() throws Exception{
    	return new VersionedData<InputStream>(getVersion(false), getInputStream());
    }
    
    private void assertIsRightVersion(long newVersion) throws WrongVersionException, Exception{
    	if (!historyFS.isTrackVersions()) {
    		return;
    	}
		long currentVersion = getVersion(false);
		if (newVersion != currentVersion + 1) {
			throw new WrongVersionException("Current version is " + currentVersion + ", expected new version is " + (currentVersion + 1) + ", provided new version is " + newVersion);
		}
    }
    
    @Override
	public OutputStream getOutputStreamForVersion(boolean append, long newVersion) throws WrongVersionException, Exception{
		assertIsRightVersion(newVersion);
    	return getOutputStream(append);
    }
    
	@Override
	public <K> VersionedData<K> readObjectAndVersion(Class<K> classType) throws Exception{
    	return new VersionedData<K>(getVersion(false), readObject(classType));
    }
    
    @Override
	public <K> FSFFile writeObjectForVersion(K objectToWrite, long newVersion) throws WrongVersionException, Exception{
		assertIsRightVersion(newVersion);
		return writeObject(objectToWrite);
    }
    
    @Override
    public FSFFile writeStringForVersion(String string, long newVersion) throws WrongVersionException, Exception {
    	assertIsRightVersion(newVersion);
    	return writeString(string);
    }
    
    @Override
	public VersionedData<byte[]> readBytesAndVersion() throws Exception{
		return new VersionedData<byte[]>(getVersion(false), readBytes());
	}
	
    @Override
	public FSFFile writeBytesForVersion(boolean append, byte[] bytes, long newVersion) throws WrongVersionException, Exception{
		assertIsRightVersion(newVersion);
		return writeBytes(append, bytes);
    }
    
    @Override
	public long getVersion() throws Exception{
    	return getVersion(true);
    }
    
    @Override
    public long getVersion(boolean allowCache) throws Exception{
    	if (!historyFS.isTrackVersions()) {
    		return 0;
    	}
    	if ((!allowCache) || (cachedVersion == null)) {
    		cachedVersion = getVersionInternally(innerFile.exists()); 
    	}
    	
    	return cachedVersion;
    }

    private long getVersionInternally(boolean exists) throws Exception{
    	if (!historyFS.isTrackVersions()) {
    		return 0;
    	}
    	FSFFile versionFile = getVersionFile();
    	if (!versionFile.exists()) {
    		if (exists) {
    			return 1;
    		} else {
    			return 0;
    		}
    	}
    	return Long.parseLong(versionFile.readString());
    }
    
    /**
     * removed all history entries
     * @throws Exception 
     */
	public void purgeHistory() throws Exception {
		purgeHistory(this);
	}

	/**
	 * removed all history entries
	 * @throws Exception 
	 */
	private static void purgeHistory(HistoryFile file) throws Exception {
		String name = file.innerFile.getName();
		String ending = getFileEnding(file);
		String nameWithoutEnding = file.getNameWithoutEnding(ending, name);
		FSFFile historyDir = file.getHistoryDir(file.innerFile);
		file.removeOldHistory(historyDir, nameWithoutEnding, ending, 0);
		List<FSFFile> filesInHistoryDir = historyDir.listFiles();
		if ((filesInHistoryDir != null) && (filesInHistoryDir.isEmpty())) {
			historyDir.delete();
		}
	}
	
	public void purgeHistoryTree() throws Exception {
		purgeHistory();
		String historyDirName = historyFS.getHistoryDirName();
		
		if (innerFile.isDirectory()) {
			List<FSFFile> allSubItems = innerFile.listFilesTree();
			Collections.sort(allSubItems);
			Collections.reverse(allSubItems);
			for (FSFFile i: allSubItems) {
				if (i.getName().equals(historyDirName)) {
					i.deleteTree();
				}
			}
		}
	}

	@Override
	protected HistoryFile wrap(FSFFile innerFile) {
		if (innerFile == null) {
			return null;
		}
		return new HistoryFile(historyFS, innerFile);
	}
	
	@Override
	public void copyFilesTree(FSFFile dest) throws Exception {
		if (historyFS.isTrackVersions()) {
			FSFFileUtil.copyFilesTree(this, dest, true);
		} else {
			FSFFileUtil.copyFilesTree(this, dest);
		}
	}
	
}
