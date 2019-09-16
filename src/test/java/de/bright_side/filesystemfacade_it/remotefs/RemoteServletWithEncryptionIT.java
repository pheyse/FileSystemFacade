package de.bright_side.filesystemfacade_it.remotefs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.Alphanumeric;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import de.bright_side.filesystemfacade.encryptedfs.EncryptedFS;
import de.bright_side.filesystemfacade.facade.FSFFile;
import de.bright_side.filesystemfacade.facade.FSFSystem;
import de.bright_side.filesystemfacade.facade.VersionedData;

@TestMethodOrder(Alphanumeric.class)
@Tag("IT")
public class RemoteServletWithEncryptionIT {

	private static final String PASSWORD = "this-is-the-password";
	private static final String BASE_PATH = "/test_encrypted";

	@Test
	public void writeStringForVersion_normal() throws Exception {
		FSFSystem remoteFS = RemoteServletIT.createRemoteFS();
		remoteFS.createByPath(BASE_PATH).deleteTree();
		remoteFS.createByPath(BASE_PATH).mkdirs();
		
		FSFSystem fs = new EncryptedFS(remoteFS, PASSWORD, BASE_PATH);
		
		FSFFile file = fs.createByPath("/hello.txt");
		file.writeStringForVersion("one", 1);
		file.writeStringForVersion("two", 2);
		
		VersionedData<String> result = fs.createByPath("/hello.txt").readStringAndVersion();
		assertEquals(2, result.getVersion());
		assertEquals("two", result.getData());
	}
	
	@Test
	public void listFilesTree_normal() throws Exception {
		FSFSystem remoteFS = RemoteServletIT.createRemoteFS();
		remoteFS.createByPath(BASE_PATH).deleteTree();
		remoteFS.createByPath(BASE_PATH).mkdirs();
		
		FSFSystem fs = new EncryptedFS(remoteFS, PASSWORD, BASE_PATH);
		
		FSFFile dir = fs.createByPath("/dir1").mkdirs();
		dir.getChild("fileA.txt").writeString("hello");
		dir.getChild("fileB.txt").writeString("one").writeString("two");
		dir.getChild("dir2").mkdir().getChild("fileC.txt").writeString("nice");

		
		FSFFile fileResult;
		fileResult = fs.createByPath("/dir1/fileA.txt");
		assertEquals(1, fileResult.getVersion());
		assertEquals("hello", fileResult.readString());
		fileResult = fs.createByPath("/dir1/fileB.txt");
		assertEquals(2, fileResult.getVersion());
		assertEquals("two", fileResult.readString());
		fileResult = fs.createByPath("/dir1/dir2/fileC.txt");
		assertEquals(1, fileResult.getVersion());
		assertEquals("nice", fileResult.readString());
		
		List<FSFFile> result = fs.createByPath("/").listFilesTree();
		assertEquals(5, result.size());
		
		fileResult = result.get(0);
		assertEquals("dir1", fileResult.getName());
		assertEquals("/dir1", fileResult.getAbsolutePath());
		assertEquals(true, fileResult.isDirectory());
		
		fileResult = result.get(1);
		assertEquals("dir2", fileResult.getName());
		assertEquals("/dir1/dir2", fileResult.getAbsolutePath());
		assertEquals(true, fileResult.isDirectory());

		fileResult = result.get(2);
		assertEquals("fileC.txt", fileResult.getName());
		assertEquals("/dir1/dir2/fileC.txt", fileResult.getAbsolutePath());
		assertEquals(1, fileResult.getVersion());
		assertEquals(true, fileResult.isFile());

		fileResult = result.get(3);
		assertEquals("fileA.txt", fileResult.getName());
		assertEquals("/dir1/fileA.txt", fileResult.getAbsolutePath());
		assertEquals(1, fileResult.getVersion());
		assertEquals(true, fileResult.isFile());
		
		fileResult = result.get(4);
		assertEquals("fileB.txt", fileResult.getName());
		assertEquals("/dir1/fileB.txt", fileResult.getAbsolutePath());
		assertEquals(2, fileResult.getVersion());
		assertEquals(true, fileResult.isFile());
		
	}

	@Test
	public void listFiles_normal() throws Exception {
		FSFSystem remoteFS = RemoteServletIT.createRemoteFS();
		remoteFS.createByPath(BASE_PATH).deleteTree();
		remoteFS.createByPath(BASE_PATH).mkdirs();
		
		FSFSystem fs = new EncryptedFS(remoteFS, PASSWORD, BASE_PATH);
		
		FSFFile dir = fs.createByPath("/dir1").mkdirs();
		dir.getChild("fileA.txt").writeString("hello");
		dir.getChild("fileB.txt").writeString("one").writeString("two");
		dir.getChild("dir2").mkdir().getChild("fileC.txt").writeString("nice");
		
		
		FSFFile fileResult;
		
		FSFFile dir1 = fs.createByPath("/dir1");
		log("listFiles_normal: before listFiles ============================");
		List<FSFFile> result = dir1.listFiles();
		log("listFiles_normal: after listFiles =============================");
		assertEquals(3, result.size());
		
		fileResult = result.get(0);
		assertEquals("dir2", fileResult.getName());
		assertEquals("/dir1/dir2", fileResult.getAbsolutePath());
		assertEquals(true, fileResult.isDirectory());
		
		fileResult = result.get(1);
		assertEquals("fileA.txt", fileResult.getName());
		assertEquals("/dir1/fileA.txt", fileResult.getAbsolutePath());
		assertEquals(1, fileResult.getVersion());
		assertEquals(true, fileResult.isFile());
		
		fileResult = result.get(2);
		assertEquals("fileB.txt", fileResult.getName());
		assertEquals("/dir1/fileB.txt", fileResult.getAbsolutePath());
		assertEquals(2, fileResult.getVersion());
		assertEquals(true, fileResult.isFile());

	}

	private void log(String message) {
		System.out.println("RemoteServletWithEncryptionTest> " + message);
	}
	

}
