package de.bright_side.filesystemfacade.memoryfs;

import de.bright_side.filesystemfacade.facade.FSFEnvironment;
import de.bright_side.filesystemfacade.facade.FSFSystem;
import de.bright_side.filesystemfacade.facade.GeneralFSTest;

public class GeneralMemoryFSTest extends GeneralFSTest {

	@Override
	public FSFSystem createFS(FSFEnvironment environment) {
		return new MemoryFS(environment);
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
	public boolean hasInnerFS() throws Exception {
		return false;
	}

	@Override
	public String listDirInnerFS(FSFSystem fs) throws Exception {
		return null;
	}

	@Override
	public void logStatus(String location) throws Exception {
	}

	@Override
	protected FSFSystem getInnerFS(FSFSystem fs) throws Exception{
		throw new Exception("MemoryFS has no inner FS");
	}

	@Override
	public void afterClass() throws Exception {
	}

	@Override
	public boolean isInnerFSEncrypted() throws Exception {
		return false;
	}
	
}
