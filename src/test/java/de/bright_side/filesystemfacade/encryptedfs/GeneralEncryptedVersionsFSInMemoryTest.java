package de.bright_side.filesystemfacade.encryptedfs;

import de.bright_side.filesystemfacade.facade.FSFEnvironment;
import de.bright_side.filesystemfacade.facade.FSFSystem;
import de.bright_side.filesystemfacade.facade.GeneralFSTest;
import de.bright_side.filesystemfacade.historyfs.HistoryFS;
import de.bright_side.filesystemfacade.memoryfs.MemoryFS;

public class GeneralEncryptedVersionsFSInMemoryTest extends GeneralFSTest {
	private static final String BASE_PATH = "/data/encryptedDir";
	private static final boolean LOGGING_ENABELD = false;
	
	@Override
	public FSFSystem createFS(FSFEnvironment environment) throws Exception {
		FSFSystem innerFS = new HistoryFS(new MemoryFS(environment), true, 0);
		
		String password = "This-is-the-password!";
		innerFS.createByPath(BASE_PATH).mkdirs();
		return new EncryptedFS(innerFS, password, BASE_PATH, environment); 
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
		return true;
	}

	@Override
	public boolean supportsHistory() throws Exception {
		return false;
	}

	@Override
	public boolean supportCopyHistoryFilesTree() {
		return false;
	}

	@Override
	public String listDirInnerFS(FSFSystem fs) throws Exception {
		//: don't return the next inner FS, but one level deeper which will contains version files etc.
		
		
		FSFSystem innerFS = ((EncryptedFS)fs).getInnerFS();
		innerFS = ((HistoryFS)innerFS).getInnerFS();
		return innerFS.createByPath(BASE_PATH).listDirAsString(LIST_DIR_FORMATTING_SIMPLE);
//		return ((EncryptedFS)fs).getInnerFS().createByPath(BASE_PATH).listDirAsString(LIST_DIR_FORMATTING_SIMPLE);
	}

	@Override
	public void logStatus(String status) throws Exception {
		if (LOGGING_ENABELD) {
			System.out.println("==================== " + getClass().getSimpleName() + "-status> " + status + " ========================================");
		}
	}

	@Override
	protected FSFSystem getInnerFS(FSFSystem fs) {
		return ((EncryptedFS)fs).getInnerFS();
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
		return true;
	}

}
