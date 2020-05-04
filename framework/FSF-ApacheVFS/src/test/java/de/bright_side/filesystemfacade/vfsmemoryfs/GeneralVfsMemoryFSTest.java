package de.bright_side.filesystemfacade.vfsmemoryfs;

import de.bright_side.filesystemfacade.facade.FSFEnvironment;
import de.bright_side.filesystemfacade.facade.FSFSystem;
import de.bright_side.filesystemfacade.facade.GeneralFSTest;
import de.bright_side.filesystemfacade.vfs.VfsMemoryFS;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;

public class GeneralVfsMemoryFSTest extends GeneralFSTest {

	@Override
	public FSFSystem createFS(FSFEnvironment environment) {
		try {
			return new VfsMemoryFS(environment);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
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
		clearRamFileSystem();
	}

	private void clearRamFileSystem() throws FileSystemException {
		FileSystemManager manager = VFS.getManager();
		FileObject root = manager.resolveFile("ram:/");
		manager.closeFileSystem(root.getFileSystem());
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

	@Override
	public boolean isTimeCreatedSupported() throws Exception {
		return false;
	}

}
