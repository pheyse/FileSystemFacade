package de.bright_side.filesystemfacade.historyfs;

import de.bright_side.filesystemfacade.encryptedfs.EncryptedFS;
import de.bright_side.filesystemfacade.facade.FSFEnvironment;
import de.bright_side.filesystemfacade.facade.FSFSystem;
import de.bright_side.filesystemfacade.facade.GeneralFSTest;
import de.bright_side.filesystemfacade.memoryfs.MemoryFS;

public class GeneralHistoryNoVersionsEncryptedMemoryFSTest extends GeneralFSTest {
	private static final int MAX_NUMBER_OF_HISTORY_FILES = 10;
	private static final String BASE_PATH = "/data/encryptedDir";

	@Override
	public FSFSystem createFS(FSFEnvironment environment) throws Exception {
		MemoryFS memoryFS = new MemoryFS(environment);
		
		String password = "This-is-the-password!";
		memoryFS.createByPath(BASE_PATH).mkdirs();
		EncryptedFS encryptedFS = new EncryptedFS(memoryFS, password, BASE_PATH, environment); 
		
		return new HistoryFS(encryptedFS, false, MAX_NUMBER_OF_HISTORY_FILES, HistoryFS.DEFAULT_VERSION_DIR_NAME, HistoryFS.DEFAULT_HISTORY_DIR_NAME, environment);
	}

	@Override
	public String listDir(FSFSystem fs) throws Exception {
		return fs.createByPath("").listDirAsString(LIST_DIR_FORMATTING_SIMPLE);
	}

	@Override
	public void beforeTest() {
	}

	@Override
	public void afterTest() throws Exception {
	}

	@Override
	public void beforeClass() {
	}

	@Override
	public boolean supportsVersioning() throws Exception {
		return false;
	}

	@Override
	public boolean supportsHistory() throws Exception {
		return true;
	}

	@Override
	public boolean supportCopyHistoryFilesTree() {
		return false;
	}

	@Override
	public String listDirInnerFS(FSFSystem fs) throws Exception {
		return ((HistoryFS)fs).getInnerFS().createByPath("").listDirAsString(LIST_DIR_FORMATTING_SIMPLE);
	}

	@Override
	public void logStatus(String location) throws Exception {
	}

	@Override
	protected FSFSystem getInnerFS(FSFSystem fs) {
		return ((HistoryFS)fs).getInnerFS();
	}

	@Override
	public void afterClass() throws Exception {
	}

	@Override
	public boolean hasInnerFS() throws Exception {
		return true;
	}

	@Override
	public boolean isInnerFSEncrypted() throws Exception {
		//: the inner FS doesn't contain encrypted data, but the FS inside the inner FS contains encrypted data (!)
		return false;
	}

	@Override
	public boolean isTimeCreatedSupported() throws Exception {
		return true;
	}

}
