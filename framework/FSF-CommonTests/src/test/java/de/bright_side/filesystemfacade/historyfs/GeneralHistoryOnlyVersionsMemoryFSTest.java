package de.bright_side.filesystemfacade.historyfs;

import de.bright_side.filesystemfacade.facade.FSFEnvironment;
import de.bright_side.filesystemfacade.facade.FSFSystem;
import de.bright_side.filesystemfacade.facade.GeneralFSTest;
import de.bright_side.filesystemfacade.memoryfs.MemoryFS;

public class GeneralHistoryOnlyVersionsMemoryFSTest extends GeneralFSTest {
	@Override
	public FSFSystem createFS(FSFEnvironment environment) {
		return new HistoryFS(new MemoryFS(environment), true, 0, HistoryFS.DEFAULT_VERSION_DIR_NAME, HistoryFS.DEFAULT_HISTORY_DIR_NAME, environment);
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
		return false;
	}

	@Override
	public boolean isTimeCreatedSupported() throws Exception {
		return true;
	}
	
}
