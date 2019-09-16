package de.bright_side.filesystemfacade_it.nativefs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import de.bright_side.filesystemfacade.encryptedfs.EncryptedFS;
import de.bright_side.filesystemfacade.facade.FSFEnvironment;
import de.bright_side.filesystemfacade.facade.FSFFile;
import de.bright_side.filesystemfacade.facade.FSFSystem;
import de.bright_side.filesystemfacade.historyfs.HistoryFS;
import de.bright_side.filesystemfacade.nativefs.NativeFS;
import de.bright_side.filesystemfacade.util.FSFFileUtil;
import de.bright_side.filesystemfacade.util.SimpleFSFEnvironment;

@Tag("IT")
public class NativeFileWithEncryptionAndHistoryIT {
	private static final String PASSWORD = "password";
	private static final String ENCRYPTED_DATA_DIR_NAME = "encryptedData";
	private static final int NUMBER_OF_HISTORY_FILES = 10;
	private static final boolean LOGGING_ENABELD = false;

	private static FSFSystem createNativeFS() {
		return new NativeFS();
	}
	
//	private static NativeFS createFS(Class<?> testClass, String testName){
////		FSFSystem subDirFS = new SubDirFS(innerFS, basePath)
//		
//		FSFFile dataDir = NativeFileTestBase.getTopDir(createNativeFS(), testClass, testName).getChild("encryptedData");
//		dataDir.mkdirs();
//		FSFSystem encryptedFS = new EncryptedFS(createNativeFS(), PASSWORD, dataDir.getAbsolutePath());
//		FSFSystem historizedDir = new HistoryFS(encryptedFS, false);
//		return historizedDir.createByPath("/configCollection.json");
//
//		
//		return new NativeFS();
//	}
	
	private FSFFile getNativeTopDir(String testName) throws Exception {
		return NativeFileTestBase.getTopDir(createNativeFS(), getClass(), testName);
	}

	private FSFSystem getEncryptedFS(String testName) throws Exception {
		FSFFile dataDir = NativeFileTestBase.getTopDir(createNativeFS(), getClass(), testName).getChild(ENCRYPTED_DATA_DIR_NAME);
		dataDir.mkdirs();
		FSFSystem encryptedFS = new EncryptedFS(createNativeFS(), PASSWORD, dataDir.getAbsolutePath());
		return encryptedFS;
	}
	
	private FSFFile getEncryptedFSTopDir(String testName, FSFEnvironment environment) throws Exception{
		return getEncryptedFS(testName).createByPath("");
	}
	
	private FSFFile getTopDir(String testName, FSFEnvironment environment) throws Exception{
		FSFSystem encryptedFS = getEncryptedFS(testName);
		FSFSystem historizedDir = new HistoryFS(encryptedFS, false, NUMBER_OF_HISTORY_FILES, HistoryFS.DEFAULT_VERSION_DIR_NAME, HistoryFS.DEFAULT_HISTORY_DIR_NAME, environment);
		return historizedDir.createByPath("");
	}
	
	private String listDir(String testName, FSFEnvironment environment) throws Exception {
		return getTopDir(testName, environment).listDirAsString(NativeFileTestBase.LIST_DIR_FORMATTING_SIMPLE);
	}
	
	private String listEncryptedFSDir(String testName, FSFEnvironment environment) throws Exception {
		return getEncryptedFSTopDir(testName, environment).listDirAsString(NativeFileTestBase.LIST_DIR_FORMATTING_SIMPLE);
	}
	
	@BeforeAll
	public static void clearTestDir() throws Exception {
		FSFFile testClassDir = new NativeFileWithEncryptionAndHistoryIT().getNativeTopDir("x").getParentFile();
		log("testClassDir = '" + testClassDir.getAbsolutePath() + "'");
		NativeFileTestBase.deleteTree(testClassDir);
	}

	private static void log(String message) {
		if (LOGGING_ENABELD) {
			System.out.println("NativeFileWithEncryptionAndHistoryIT> " + message);
		}
	}

	@Test
	public void listDir_simple() throws Exception{
		String testName = "listDir_simple";
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(NativeFileTestBase.TIME_001);
		FSFFile dir = getTopDir(testName, env);
		dir.listFiles();
	}

	@Test
	public void writeObject_simple() throws Exception{
		String testName = "writeObject_simple";
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(NativeFileTestBase.TIME_001);
		FSFFile dir = getTopDir(testName, env);
		
		String input = "Hello";
		String filename = "testFile.txt";
		
		dir.getChild(filename).writeObject(input);
		String result = dir.getChild(filename).readObject(String.class);
		
		
		assertEquals(input, result);
		
 		StringBuilder sb = new StringBuilder();
		sb.append("<F> testFile.txt\n");
		assertEquals(sb.toString(), listDir(testName, env));
		assertEquals(1, getNativeTopDir(testName).listFiles().size());
		assertEquals(1, getNativeTopDir(testName).getChild(ENCRYPTED_DATA_DIR_NAME).listFiles().size());
	}
	
	@Test
	public void writeObject_twice() throws Exception{
		try {
			
			String testName = "writeObject_twice";
			SimpleFSFEnvironment env = new SimpleFSFEnvironment(NativeFileTestBase.TIME_001);
			
			FSFFile dir = getTopDir(testName, env);
			
			String input1 = "Hello";
			String input2 = "Hello again!";
			String filename = "testFile.txt";
			
			dir.getChild(filename).writeObject(input1);
			env.setCurrentTime(NativeFileTestBase.TIME_002);
			dir.getChild(filename).writeObject(input2); //: write again to create a history entry
			
			String result = dir.getChild(filename).readObject(String.class);
			
			
			
			FSFFile file = dir.getChild(filename);
			
			//: test: content correct
			assertEquals(input2, result);
			
			//: test dir-structure in history FS
			StringBuilder sb = new StringBuilder();
			sb.append("<F> testFile.txt\n");
			assertEquals(sb.toString(), listDir(testName, env));
			
			//: test native top dir only contains the encrypetd dir
			assertEquals(1, getNativeTopDir(testName).listFiles().size());
			
			
			log("Files: " + getNativeTopDir(testName).getChild(ENCRYPTED_DATA_DIR_NAME).listFiles());
			
			assertEquals(2, getNativeTopDir(testName).getChild(ENCRYPTED_DATA_DIR_NAME).listFiles().size());
			
			//: test dir-structure in encrypted FS (on which the history FS is based)
			sb = new StringBuilder();
			sb.append("<D> ~history\n");
			sb.append("   <F> testFile_" + NativeFileTestBase.TIME_002_TEXT + ".txt\n");
			sb.append("<F> testFile.txt\n");
			assertEquals(sb.toString(), listEncryptedFSDir(testName, env));
			
			//: history entry correct
			assertEquals(1, file.getHistoryTimes().size());
			assertEquals(NativeFileTestBase.TIME_002, file.getHistoryTimes().iterator().next().longValue());
			
			
			//: history data correct
			assertEquals("\"" + input1 + "\"", FSFFileUtil.readString(file.getHistoryInputStream(NativeFileTestBase.TIME_002))); 
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}

	}
	
}
