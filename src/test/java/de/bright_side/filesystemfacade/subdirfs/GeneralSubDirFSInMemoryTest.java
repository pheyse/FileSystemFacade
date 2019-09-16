package de.bright_side.filesystemfacade.subdirfs;

import de.bright_side.filesystemfacade.encryptedfs.EncryptedFS;
import de.bright_side.filesystemfacade.facade.FSFEnvironment;
import de.bright_side.filesystemfacade.facade.FSFSystem;
import de.bright_side.filesystemfacade.facade.GeneralFSTest;
import de.bright_side.filesystemfacade.memoryfs.MemoryFS;

public class GeneralSubDirFSInMemoryTest extends GeneralFSTest {
	private static final String BASE_PATH = "/data/application_dir";
	private static final boolean LOGGING_ENABLED = false;
	
	@Override
	public FSFSystem createFS(FSFEnvironment environment) throws Exception {
		FSFSystem innerFS = new MemoryFS(environment); 
		innerFS.createByPath(BASE_PATH).mkdirs();
		return new SubDirFS(innerFS, BASE_PATH, environment); 
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
		return false;
	}
	
	@Override
	public boolean supportCopyHistoryFilesTree() {
		return false;
	}

	@Override
	public String listDirInnerFS(FSFSystem fs) throws Exception {
		return ((SubDirFS)fs).getInnerFS().createByPath(BASE_PATH).listDirAsString(LIST_DIR_FORMATTING_SIMPLE);
	}

	@Override
	public void logStatus(String status) throws Exception {
		if (LOGGING_ENABLED) {
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
		return false;
	}
	
}
