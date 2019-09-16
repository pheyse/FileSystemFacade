package de.bright_side.filesystemfacade.subdirfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.bright_side.filesystemfacade.facade.FSFFile;
import de.bright_side.filesystemfacade.facade.FSFSystem;
import de.bright_side.filesystemfacade.facade.IllegalPathItemNameException;
import de.bright_side.filesystemfacade.memoryfs.MemoryFS;

public class SubDirFSTest {
	private SubDirFS createFS() throws Exception {
		FSFSystem innerFS = new MemoryFS();
		String basePath = "/data/myApp";
		innerFS.createByPath(basePath).mkdirs();
		SubDirFS result = new SubDirFS(innerFS, basePath);
		return result;
	}

	@Test
	public void basicUsage() throws Exception {
		FSFSystem fs = createFS();
		fs.createByPath("/myDir").mkdirs().getChild("myFile.txt").writeString("hello");
		assertEquals("hello", fs.createByPath("/myDir/myFile.txt").readString());
	}

	@Test
	public void createByPath_pathNoStartedWithSlash() throws Exception {
		Assertions.assertThrows(IllegalPathItemNameException.class, () -> {
			createFS().createByPath("test/file.txt");
		});
	}
	
	@Test
	public void createByPath_pathIllegalDirUpChars() throws Exception {
		Assertions.assertThrows(IllegalPathItemNameException.class, () -> {
			createFS().createByPath("/test/../file.txt");
		});
	}
	
	@Test
	public void getParent_parentOfRoot() throws Exception {
		FSFFile parent = createFS().createByPath("").getParentFile();
		assertNull(parent);
	}
	
	@Test
	public void getAbsolutePath_pathRoot() throws Exception {
		String result = createFS().createByPath("").getAbsolutePath();
		assertEquals("", result);
	}
	
	@Test
	public void listRoots_simple() throws Exception {
		List<FSFFile> result = createFS().listRoots();
		assertEquals(1, result.size());
		assertEquals("", result.get(0).getAbsolutePath());
		assertEquals("", result.get(0).getName());
	}
	
	@Test
	public void getName_root() throws Exception {
		String result = createFS().createByPath("").getAbsolutePath();
		assertEquals("", result);
	}
	
	@Test
	public void rename_nameHasWrongDirUpChars() throws Exception {
		Assertions.assertThrows(IllegalPathItemNameException.class, () -> {
			createFS().createByPath("/file.txt").writeString("nice").rename("new/../test");
		});
	}
	
	@Test
	public void rename_nameHasSeparatorOfSubFS() throws Exception {
		Assertions.assertThrows(IllegalPathItemNameException.class, () -> {
			createFS().createByPath("/file.txt").writeString("nice").rename("new/name");
		});
	}
	
}
