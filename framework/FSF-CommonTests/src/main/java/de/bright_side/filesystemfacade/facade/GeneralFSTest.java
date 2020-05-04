package de.bright_side.filesystemfacade.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.Alphanumeric;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import de.bright_side.filesystemfacade.historyfs.HistoryFS;
import de.bright_side.filesystemfacade.historyfs.HistoryFile;
import de.bright_side.filesystemfacade.memoryfs.MemoryFS;
import de.bright_side.filesystemfacade.remotefs.RemoteFS;
import de.bright_side.filesystemfacade.util.FSFFileUtil;
import de.bright_side.filesystemfacade.util.ListDirFormatting;
import de.bright_side.filesystemfacade.util.ListDirFormatting.Style;
import de.bright_side.filesystemfacade.util.SimpleFSFEnvironment;

/**
 * This is a general class that contains all the test cases and is overwritten by all different tests for the specific file systems (Memory, Native, Database, ...)
 * 
 * @author Philip Heyse
 *
 */

@TestMethodOrder(Alphanumeric.class)
public abstract class GeneralFSTest {
	private static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss-SSS");
	public static ListDirFormatting LIST_DIR_FORMATTING_SIMPLE = new ListDirFormatting().setStyle(Style.TREE).setAllSubItems(true);
	
	private static final String TIME_001_TEXT = "2019-01-01T00-01-01-001";
	private static final String TIME_002_TEXT = "2019-02-02T00-02-02-002";
	private static final String TIME_003_TEXT = "2019-03-03T00-03-03-003";
	private static final String TIME_004_TEXT = "2019-11-11T00-01-01-004";
	private static final String TIME_005_TEXT = "2019-11-11T00-01-01-005";
	private static final String TIME_006_TEXT = "2019-11-11T00-01-01-006";
	private static final String TIME_007_TEXT = "2019-11-11T00-01-01-007";
	private static final String TIME_008_TEXT = "2019-11-11T00-01-01-008";
	private static final String TIME_009_TEXT = "2019-11-11T00-01-01-009";
	private static final String TIME_010_TEXT = "2019-11-11T00-01-01-010";
	private static final String TIME_011_TEXT = "2019-11-11T00-01-01-011";
	
	
	private static final long TIME_001 = createTime(TIME_001_TEXT);
	private static final long TIME_002 = createTime(TIME_002_TEXT);
	private static final long TIME_003 = createTime(TIME_003_TEXT);
	private static final long TIME_004 = createTime(TIME_004_TEXT);
	private static final long TIME_005 = createTime(TIME_005_TEXT);
	private static final long TIME_006 = createTime(TIME_006_TEXT);
	private static final long TIME_007 = createTime(TIME_007_TEXT);
	private static final long TIME_008 = createTime(TIME_008_TEXT);
	private static final long TIME_009 = createTime(TIME_009_TEXT);
	private static final long TIME_010 = createTime(TIME_010_TEXT);
	private static final long TIME_011 = createTime(TIME_011_TEXT);
	
	private static final List<Long> TIMES = Arrays.asList(TIME_001, TIME_002, TIME_003, TIME_004, TIME_005, TIME_006, TIME_007, TIME_008, TIME_009, TIME_010, TIME_011);
	private static final List<String> TIME_TEXTS = Arrays.asList(TIME_001_TEXT, TIME_002_TEXT, TIME_003_TEXT, TIME_004_TEXT, TIME_005_TEXT, TIME_006_TEXT, TIME_007_TEXT, TIME_008_TEXT, TIME_009_TEXT, TIME_010_TEXT, TIME_011_TEXT);
	private static final int MAX_NUMBER_OF_HISTORY_FILES = 10;
	private static final String CHARSET = "UTF-8";
	private static final boolean LOGGING_ENABLED = false;

	public abstract FSFSystem createFS(FSFEnvironment environment) throws Exception;
	public abstract String listDir(FSFSystem fs) throws Exception;
	public abstract void beforeClass() throws Exception;
	public abstract void afterClass() throws Exception;
	public abstract void beforeTest() throws Exception;
	public abstract void afterTest() throws Exception;
	public abstract boolean supportsVersioning() throws Exception;
	public abstract boolean supportsHistory() throws Exception;
	public abstract boolean supportCopyHistoryFilesTree();
	public abstract boolean hasInnerFS() throws Exception;
	public abstract boolean isInnerFSEncrypted() throws Exception;
	public abstract boolean isTimeCreatedSupported() throws Exception;
	public abstract String listDirInnerFS(FSFSystem fs) throws Exception;
	protected abstract FSFSystem getInnerFS(FSFSystem fs) throws Exception;

	private static boolean firstCall = true;
	private static boolean afterClassWasCalled = false;

	public abstract void logStatus(String location) throws Exception;
	
	private byte[] createByteArray(int ...bytes) {
		byte[] result = new byte[bytes.length];
		for (int i = 0; i < bytes.length; i++) {
			result[i] = (byte)bytes[i];
		}
		return result;
	}
	
	protected void log(String message) {
		if (LOGGING_ENABLED) {
			System.out.println("GeneralFSTest: " + getClass().getName() + "> " + message);
		}
	}
	
	private static long createTime(String text) {
		try {
			return TIMESTAMP_FORMAT.parse(text).getTime();
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}
	
	private String toString(byte[] byteArray) {
		if (byteArray == null) {
			return "byteArray:null";
		}
		StringBuilder result = new StringBuilder("byteArray{");
		boolean first = true;
		for (byte i: byteArray) {
			if (first) {
				first = false;
			} else {
				result.append(", ");
			}
			result.append(i);
		}
		result.append("}");
		return result.toString();
	}

	private void writeStringAndFailWithWrongVersionException(FSFFile file, String string, long newVersion) throws Exception{
		boolean exceptionThrown = false;
		try {
			file.writeStringForVersion(string, newVersion);
		} catch (WrongVersionException e) {
			exceptionThrown = true;
		}
		
		if (!exceptionThrown) {
			throw new Exception("Expected exception not thrown");
		}
	}

	private void getOutputStreamForVersionAndFailWithWrongVersionException(FSFFile file, String string, long newVersion) throws Exception{
		boolean exceptionThrown = false;
		try {
			OutputStream outputStream = file.getOutputStreamForVersion(false, newVersion);
			outputStream.close();
		} catch (WrongVersionException e) {
			exceptionThrown = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (!exceptionThrown) {
			throw new Exception("Expected exception not thrown");
		}
	}
	
	private <K> void writeObjectForVersionAndFailWithWrongVersionException(FSFFile file, K objectToWrite, long newVersion) throws Exception{
		boolean exceptionThrown = false;
		try {
			file.writeObjectForVersion(objectToWrite, newVersion);
		} catch (WrongVersionException e) {
			exceptionThrown = true;
		}
		
		if (!exceptionThrown) {
			throw new Exception("Expected exception not thrown");
		}
	}
	
	private void writeBytesForVersionAndFailWithWrongVersionException(FSFFile file, byte[] bytes, long newVersion) throws Exception{
		boolean exceptionThrown = false;
		try {
			file.writeBytesForVersion(false, bytes, newVersion);
		} catch (WrongVersionException e) {
			exceptionThrown = true;
		}
		
		if (!exceptionThrown) {
			throw new Exception("Expected exception not thrown");
		}
	}

	private void assertListDirInnerFSEquals(StringBuilder sb, FSFSystem fs) throws Exception {
		if (hasInnerFS()) {
			if (isInnerFSEncrypted()) {
				assertEquals(TestUtil.dirListNoNames(sb), TestUtil.dirListNoNames(listDirInnerFS(fs)));
			} else {
				assertEquals(sb.toString(), listDirInnerFS(fs));
			}
		}
	}
	
	@BeforeEach
	public void executeBeforeTest() throws Exception {
		if (firstCall) {
			firstCall = false;
			beforeClass();
		}
		beforeTest();
	}
	
	@Test
	public void zzzzzzzzzzzzzzzzzzzzz_alphabeticallyLastMethodToCallAfterClass() throws Exception{
		afterClass();
		afterClassWasCalled = true;
	}
	
	@AfterEach
	public void executeAfterTest() throws Exception {
		if (!afterClassWasCalled) { //: w/o the check the method "zzzzzzzzzzzzzzzzzzzzz_alphabeticallyLastMethodToCallAfterClass" would also call afterTest which is not correct
			afterTest();
		}
	}
	
	@Test
	public void test_writeString_newFile() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		logStatus("1");
		FSFSystem fs = createFS(env);
		logStatus("2a");
		FSFFile file = fs.createByPath("/myFile.txt");
		logStatus("2b");
		file.writeString("hey!");
		logStatus("3");
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<F> myFile.txt\n");
		logStatus("4");
		assertEquals(sb.toString(), listDir(fs));
		logStatus("5");
		assertEquals("hey!", file.readString());
		logStatus("6");
		
		if (supportsHistory()) {
			sb = new StringBuilder();
			sb.append("<F> myFile.txt\n");
			assertListDirInnerFSEquals(sb, fs);
		}
	}
	
	/**
	 * create a file and modify it and see that the old version is in the history
	 * @throws Exception 
	 */
	@Test
	public void test_modifyFile_normal() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
		FSFFile file = fs.createByPath("/myFile.txt");
		file.writeString("hey!");
		env.setCurrentTime(TIME_002);
		file.writeString("hello!");
		
		//: perform check
		file = fs.createByPath("/myFile.txt");
		StringBuilder sb = new StringBuilder();
		sb.append("<F> myFile.txt\n");
		assertEquals(sb.toString(), listDir(fs));

		log("inner FS:\n" + listDirInnerFS(fs));
		
		
		assertEquals("hello!", file.readString());
		log("TIME_001 = " + TIME_001);
		log("TIME_002 = " + TIME_002);
		log("file.getTimeLastModified() = " + TIMESTAMP_FORMAT.format(file.getTimeLastModified()));
		assertEquals(TIME_002, file.getTimeLastModified());
		
		sb = new StringBuilder();
		if (supportsHistory()) {
			sb.append("<D> ~history\n");
			sb.append("   <F> myFile_" + TIME_002_TEXT + ".txt\n");
		}
		if (supportsVersioning()) {
			sb.append("<D> ~version\n");
			sb.append("   <F> myFile.txt\n");
		}
		sb.append("<F> myFile.txt\n");
		
		assertListDirInnerFSEquals(sb, fs);
	}
	
	/**
	 * create a file and delete it and see that the old version is in the history
	 * @throws Exception 
	 */
	@Test
	public void test_delete_file() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
		FSFFile file = fs.createByPath("/myFile.txt");
		file.writeString("hey!");
		assertEquals(true, file.exists());
		file.delete();
		
		//: perform check
		file = fs.createByPath("/myFile.txt");
		StringBuilder sb = new StringBuilder();
		assertEquals(sb.toString(), listDir(fs));
		assertEquals(false, file.exists());
		
		if (supportsHistory()) {
			sb = new StringBuilder();
			sb.append("<D> ~history\n");
			sb.append("   <F> myFile_" + TIME_001_TEXT + ".txt\n");
			assertListDirInnerFSEquals(sb, fs);
		}
	}
	
	@Test
	public void test_getName_cerateByCreateByPath() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
		FSFFile file = fs.createByPath("/myDir/myOtherDir/myFile.txt");
		
		//: perform check
		String result = file.getName();
		assertEquals("myFile.txt", result);
	}

	@Test
	public void test_getName_cerateByCreateByPathWithRoot() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
		FSFFile file = fs.createByPath("/");
		
		//: perform check
		String result = file.getName();
		assertEquals("", result);
	}
	
	@Test
	public void test_getParentFile_cerateByCreateByPath() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
		FSFFile file = fs.createByPath("/myDir/myOtherDir/myFile.txt");
		
		//: perform check
		assertEquals("/myDir/myOtherDir", file.getParentFile().getAbsolutePath());
		FSFFile parent1 = file.getParentFile();
		assertEquals("myOtherDir", parent1.getName());
		FSFFile parent2 = parent1.getParentFile();
		assertEquals("myDir", parent2.getName());
		assertEquals("", file.getParentFile().getParentFile().getParentFile().getName());
	}
	

	
	@Test
	public void test_mkDirs_normal() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
		fs.createByPath("/myDir").mkdirs();
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<D> myDir\n");
		assertEquals(sb.toString(), listDir(fs));
		
		if (supportsHistory()) {
			sb = new StringBuilder();
			sb.append("<D> myDir\n");
			assertListDirInnerFSEquals(sb, fs);
		}
	}
	
	@Test
	public void test_mkDirs_deep() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
		fs.createByPath("/myDir/dir2/dir3/dir4").mkdirs();
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<D> myDir\n");
		sb.append("   <D> dir2\n");
		sb.append("      <D> dir3\n");
		sb.append("         <D> dir4\n");
		assertEquals(sb.toString(), listDir(fs));
	}
	
	@Test
	public void test_delete_dirNoFiles() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
		FSFFile dir = fs.createByPath("/myDir").mkdirs();
		dir.delete();
		
		//: perform check
		dir = fs.createByPath("/myDir");
		StringBuilder sb = new StringBuilder();
		assertEquals(sb.toString(), listDir(fs));
		assertEquals(false, dir.exists());
		
		if (supportsHistory()) {
			sb = new StringBuilder();
			sb.append("<D> ~history\n");
			sb.append("   <D> myDir_" + TIME_001_TEXT + "\n");
			assertListDirInnerFSEquals(sb, fs);
		}
	}
	
	@Test
	public void test_deleteTree_dirNoModifiedFiles() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
		FSFFile dir = fs.createByPath("/myDir").mkdirs();
		dir.getChild("one.txt").writeString("hey");
		dir.getChild("two.txt").writeString("ho");

		//: perform pre-check
		dir = fs.createByPath("/myDir");
		StringBuilder sb = new StringBuilder();
		if (supportsHistory()) {
			sb = new StringBuilder();
			sb.append("<D> myDir\n");
			sb.append("   <F> one.txt\n");
			sb.append("   <F> two.txt\n");
			assertEquals(sb.toString(), listDirInnerFS(fs));
		}
		sb = new StringBuilder();
		sb.append("<D> myDir\n");
		sb.append("   <F> one.txt\n");
		sb.append("   <F> two.txt\n");
		assertEquals(sb.toString(), listDir(fs));
		assertEquals(true, dir.exists());
		
		dir.deleteTree();
		
		//: perform check
		dir = fs.createByPath("/myDir");
		sb = new StringBuilder();
		assertEquals(sb.toString(), listDir(fs));
		assertEquals(false, dir.exists());
		
		if (supportsHistory()) {
			sb = new StringBuilder();
			sb.append("<D> ~history\n");
			sb.append("   <D> myDir_" + TIME_001_TEXT + "\n");
			sb.append("      <F> one.txt\n");
			sb.append("      <F> two.txt\n");
			assertListDirInnerFSEquals(sb, fs);
		}
	}

	@Test
	public void test_delete_dirWithModifiedFiles() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
		FSFFile dir = fs.createByPath("/myDir").mkdirs();
		dir.getChild("one.txt").writeString("hey").writeString("hey2");
		dir.getChild("two.txt").writeString("ho").writeString("ho2");
		env.setCurrentTime(TIME_002);
		dir.deleteTree();
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		assertEquals(sb.toString(), listDir(fs));
		
		if (supportsHistory()) {
			sb = new StringBuilder();
			sb.append("<D> ~history\n");
			sb.append("   <D> myDir_" + TIME_002_TEXT + "\n");
			sb.append("      <D> ~history\n");
			sb.append("         <F> one_" + TIME_001_TEXT + ".txt\n");
			sb.append("         <F> two_" + TIME_001_TEXT + ".txt\n");
			if (supportsVersioning()) {
				sb.append("      <D> ~version\n");
				sb.append("         <F> one.txt\n");
				sb.append("         <F> two.txt\n");
			}
			sb.append("      <F> one.txt\n");
			sb.append("      <F> two.txt\n");
			assertEquals(sb.toString(), listDirInnerFS(fs));
		}
	}

	@Test
	public void test_writeString_twice_this_willProbablyFailForHistWithDB() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
		FSFFile dir = fs.createByPath("/myDir").mkdirs();
		FSFFile file = dir.getChild("one.txt").writeString("hey");
		file.writeString("hey2");
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<D> myDir\n");
		sb.append("   <F> one.txt\n");
		assertEquals(sb.toString(), listDir(fs));
		sb = new StringBuilder();
		sb.append("<D> myDir\n");
		if (supportsHistory()) {
			sb.append("   <D> ~history\n");
			sb.append("      <F> one_" + TIME_001_TEXT + ".txt\n");
		}
		if (supportsVersioning()) {
			sb.append("   <D> ~version\n");
			sb.append("      <F> one.txt\n");
		}
		sb.append("   <F> one.txt\n");
		
		log("test_writeString_twice_this_willProbablyFailForHistWithDB: listDirInnerFS: \n" + listDirInnerFS(fs));
		
		assertListDirInnerFSEquals(sb, fs);
	}
	
	@Test
	public void test_delete_dirTwice_this_failedForHistWithDB() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
		FSFFile dir = fs.createByPath("/myDir").mkdirs();
		dir.getChild("one.txt").writeString("hey").writeString("hey2");
		dir.getChild("two.txt").writeString("ho").writeString("ho2");
		env.setCurrentTime(TIME_002);
		
		//: perform pre-check
		StringBuilder sb = new StringBuilder();
		sb.append("<D> myDir\n");
		sb.append("   <F> one.txt\n");
		sb.append("   <F> two.txt\n");
		assertEquals(sb.toString(), listDir(fs));
		sb = new StringBuilder();
		sb.append("<D> myDir\n");
		if (supportsHistory()) {
			sb.append("   <D> ~history\n");
			sb.append("      <F> one_" + TIME_001_TEXT + ".txt\n");
			sb.append("      <F> two_" + TIME_001_TEXT + ".txt\n");
		}
		if (supportsVersioning()) {
			sb.append("   <D> ~version\n");
			sb.append("      <F> one.txt\n");
			sb.append("      <F> two.txt\n");
		}
		sb.append("   <F> one.txt\n");
		sb.append("   <F> two.txt\n");
		assertListDirInnerFSEquals(sb, fs);
		
		dir.deleteTree();

		dir = fs.createByPath("/myDir").mkdirs();
		dir.getChild("first.txt").writeString("the first");
		env.setCurrentTime(TIME_003);
		dir.deleteTree();
		
		//: perform check
		sb = new StringBuilder();
		assertEquals(sb.toString(), listDir(fs));
		sb = new StringBuilder();
		if (supportsHistory()) {
			sb.append("<D> ~history\n");
			sb.append("   <D> myDir_" + TIME_002_TEXT + "\n");
			sb.append("      <D> ~history\n");
			sb.append("         <F> one_" + TIME_001_TEXT + ".txt\n");
			sb.append("         <F> two_" + TIME_001_TEXT + ".txt\n");
			if (supportsVersioning()) {
				sb.append("      <D> ~version\n");
				sb.append("         <F> one.txt\n");
				sb.append("         <F> two.txt\n");
			}
			sb.append("      <F> one.txt\n");
			sb.append("      <F> two.txt\n");
			sb.append("   <D> myDir_" + TIME_003_TEXT + "\n");
			sb.append("      <F> first.txt\n");
			assertListDirInnerFSEquals(sb, fs);
		}
	}

	
	/**
	 * create a file and rename it and see there is no version in history
	 * @throws Exception 
	 */
	@Test
	public void test_rename_fileWithoutModification() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
		FSFFile file = fs.createByPath("/myFile.txt").writeString("hello");
		file.rename("yourFile.txt");
		
		//: perform check
		file = fs.createByPath("/yourFile.txt");
		StringBuilder sb = new StringBuilder();
		sb.append("<F> yourFile.txt\n");
		assertEquals(sb.toString(), listDir(fs));
		assertEquals("hello", file.readString());
		
		if (supportsHistory()) {
			sb = new StringBuilder();
			sb.append("<F> yourFile.txt\n");
			assertListDirInnerFSEquals(sb, fs);
		}
	}
	
	/**
	 * create a file, modify it and then rename it and see there is a history version with the old name
	 * @throws Exception 
	 */
	@Test
	public void test_rename_fileAfterModification() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
		FSFFile file = fs.createByPath("/myFile.txt").writeString("hello").writeString("what");
		file.rename("yourFile.txt");
		
		//: perform check
		file = fs.createByPath("/yourFile.txt");
		StringBuilder sb = new StringBuilder();
		sb.append("<F> yourFile.txt\n");
		assertEquals(sb.toString(), listDir(fs));
		assertEquals("what", file.readString());
		
		sb = new StringBuilder();
		if (supportsHistory()) {
			sb.append("<D> ~history\n");
			sb.append("   <F> myFile_" + TIME_001_TEXT + ".txt\n");
		}
		if (supportsVersioning()) {
			sb.append("<D> ~version\n");
			sb.append("   <F> yourFile.txt\n");
		}
		sb.append("<F> yourFile.txt\n");
		assertListDirInnerFSEquals(sb, fs);
	}
	

	/**
	 * rename an empty dir and see that there is no history file
	 * @throws Exception 
	 */
	@Test
	public void test_rename_dirNoFiles() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
		FSFFile dir = fs.createByPath("/myDir").mkdirs();
		dir.rename("yourDir");
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<D> yourDir\n");
		assertEquals(sb.toString(), listDir(fs));
		
		if (supportsHistory()) {
			sb = new StringBuilder();
			sb.append("<D> yourDir\n");
			assertListDirInnerFSEquals(sb, fs);
		}
	}
	
	@Test
	public void test_rename_dirWithUnmodifiedFiles() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
		FSFFile dir = fs.createByPath("/myDir").mkdirs();
		dir.getChild("one.txt").writeString("hey");
		dir.getChild("two.txt").writeString("ho");
		env.setCurrentTime(TIME_002);
		dir.rename("yourDir");
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<D> yourDir\n");
		sb.append("   <F> one.txt\n");
		sb.append("   <F> two.txt\n");
		assertEquals(sb.toString(), listDir(fs));
		if (supportsHistory()) {
			sb = new StringBuilder();
			sb.append("<D> yourDir\n");
			sb.append("   <F> one.txt\n");
			sb.append("   <F> two.txt\n");
			assertListDirInnerFSEquals(sb, fs);
		}
	}

	
	/**
	 * rename an empty dir and see that the sub-files were moved incl. history
	 * @throws Exception 
	 */
	@Test
	public void test_rename_dirWithModifiedFiles() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
		FSFFile dir = fs.createByPath("/myDir").mkdirs();
		dir.getChild("one.txt").writeString("hey").writeString("hey2");
		dir.getChild("two.txt").writeString("ho").writeString("ho2");
		env.setCurrentTime(TIME_002);
		dir.rename("yourDir");
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<D> yourDir\n");
		sb.append("   <F> one.txt\n");
		sb.append("   <F> two.txt\n");
		assertEquals(sb.toString(), listDir(fs));
		assertEquals("hey2", fs.createByPath("/yourDir/one.txt").readString());
		assertEquals("ho2", fs.createByPath("/yourDir/two.txt").readString());
		
		sb = new StringBuilder();
		sb.append("<D> yourDir\n");
		if (supportsHistory()) {
			sb.append("   <D> ~history\n");
			sb.append("      <F> one_" + TIME_001_TEXT + ".txt\n");
			sb.append("      <F> two_" + TIME_001_TEXT + ".txt\n");
		}
		if (supportsVersioning()) {
			sb.append("   <D> ~version\n");
			sb.append("      <F> one.txt\n");
			sb.append("      <F> two.txt\n");
		}
		sb.append("   <F> one.txt\n");
		sb.append("   <F> two.txt\n");
		assertListDirInnerFSEquals(sb, fs);
	}
	
	/**
	 * create a file and rename it and see there is no version in history
	 * @throws Exception 
	 */
	@Test
	public void test_moveTo_unmodififedFile() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
		FSFFile dir1 = fs.createByPath("/dir1").mkdirs();
		FSFFile dir2 = fs.createByPath("/dirXYZ/dir2").mkdirs();
		FSFFile file = dir1.getChild("myFile.txt").writeString("hi");
		env.setCurrentTime(TIME_002);
		file.moveTo(dir2.getChild("myMovedFile.txt"));
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<D> dir1\n");
		sb.append("<D> dirXYZ\n");
		sb.append("   <D> dir2\n");
		sb.append("      <F> myMovedFile.txt\n");
		assertEquals(sb.toString(), listDir(fs));

		if (supportsHistory()) {
			sb = new StringBuilder();
			sb.append("<D> dir1\n");
			sb.append("<D> dirXYZ\n");
			sb.append("   <D> dir2\n");
			sb.append("      <F> myMovedFile.txt\n");
			assertListDirInnerFSEquals(sb, fs);
		}
	}


	/**
	 * create a file, modify it and then rename it and see there is a history version with the old name
	 * @throws Exception 
	 */
	@Test
	public void test_moveTo_modififedFile() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
		FSFFile dir1 = fs.createByPath("/dir1").mkdirs();
		FSFFile dir2 = fs.createByPath("/dirXYZ/dir2").mkdirs();
		FSFFile file = dir1.getChild("myFile.txt").writeString("hi").writeString("there");
		env.setCurrentTime(TIME_002);
		file.moveTo(dir2.getChild("myMovedFile.txt"));
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<D> dir1\n");
		sb.append("<D> dirXYZ\n");
		sb.append("   <D> dir2\n");
		sb.append("      <F> myMovedFile.txt\n");
		assertEquals(sb.toString(), listDir(fs));

		FSFFile oldFile = fs.createByPath("/dir1/myFile.txt");
		assertEquals(false, oldFile.exists());
		assertEquals("there", fs.createByPath("/dirXYZ/dir2/myMovedFile.txt").readString());
		
		sb = new StringBuilder();
		sb.append("<D> dir1\n");
		if (supportsHistory()) {
			sb.append("   <D> ~history\n");
			sb.append("      <F> myFile_" + TIME_001_TEXT + ".txt\n");
		}
//		if (supportsVersioning()) {
//			sb.append("   <D> ~version\n");
//		}
		sb.append("<D> dirXYZ\n");
		sb.append("   <D> dir2\n");
		if (supportsVersioning()) {
			sb.append("      <D> ~version\n");
			sb.append("         <F> myMovedFile.txt\n");
		}
		sb.append("      <F> myMovedFile.txt\n");
		
		log("test_moveTo_modififedFile: expected inner fs with names:\n" + sb);
		log("test_moveTo_modififedFile: actual inner fs with names:\n" + listDirInnerFS(fs));

		assertListDirInnerFSEquals(sb, fs);

		oldFile = fs.createByPath("/dir1/myFile.txt");
		assertEquals(false, oldFile.exists());
		if (supportsHistory()) {
			assertEquals(1, oldFile.getHistoryTimes().size());
			assertEquals(TIME_001, oldFile.getHistoryTimes().first().longValue());
			InputStream historyFileInputStream = oldFile.getHistoryInputStream(TIME_001);
			assertEquals("hi", FSFFileUtil.readString(historyFileInputStream));
		}
		assertEquals("there", fs.createByPath("/dirXYZ/dir2/myMovedFile.txt").readString());
	}
	
	@Test
	public void test_moveTo_dirWithoutFiles() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
		FSFFile dir1 = fs.createByPath("/dir1").mkdirs();
		FSFFile dir2 = fs.createByPath("/dirXYZ/dir2").mkdirs();
		env.setCurrentTime(TIME_002);
		dir1.moveTo(dir2.getChild("myMovedDir"));
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<D> dirXYZ\n");
		sb.append("   <D> dir2\n");
		sb.append("      <D> myMovedDir\n");
		assertEquals(sb.toString(), listDir(fs));
		
		if (supportsHistory()) {
			sb = new StringBuilder();
			sb.append("<D> dirXYZ\n");
			sb.append("   <D> dir2\n");
			sb.append("      <D> myMovedDir\n");
			assertListDirInnerFSEquals(sb, fs);
		}
	}
	
	@Test
	public void test_move_dirWithUnmodifiedFile() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
		FSFFile dir1 = fs.createByPath("/dir1").mkdirs();
		FSFFile dir2 = fs.createByPath("/dirXYZ/dir2").mkdirs();
		dir1.getChild("myFile.txt").writeString("hi");
		env.setCurrentTime(TIME_002);
		dir1.moveTo(dir2.getChild("myMovedDir"));
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<D> dirXYZ\n");
		sb.append("   <D> dir2\n");
		sb.append("      <D> myMovedDir\n");
		sb.append("         <F> myFile.txt\n");
		assertEquals(sb.toString(), listDir(fs));
		if (supportsHistory()) {
			sb = new StringBuilder();
			sb.append("<D> dirXYZ\n");
			sb.append("   <D> dir2\n");
			sb.append("      <D> myMovedDir\n");
			sb.append("         <F> myFile.txt\n");
			assertListDirInnerFSEquals(sb, fs);
		}
	}
	
	@Test
	public void test_copyFilesTree_withVersions() throws Exception {
		if (!supportsVersioning()) {
			return;
		}
		
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
		FSFFile dir1 = fs.createByPath("/dir1");
		dir1.mkdirs();
		FSFFile dir2 = fs.createByPath("/dirXYZ/dir2").mkdirs();
		FSFFile file = dir1.getChild("myFile.txt").writeString("hi");
		file.writeString("there");
		env.setCurrentTime(TIME_002);
		
		//: pre-check
		assertEquals(2, file.getVersion(false));
		assertEquals(2, fs.createByPath("/dir1/myFile.txt").getVersion(false));
		
		
		//: action
		log("test_copyFilesTree_withVersions: before dir1.copyFilesTree");
		dir1.copyFilesTree(dir2.getChild("myCopiedDir"));
		log("test_copyFilesTree_withVersions: after dir1.copyFilesTree");
		

		//: perform check
		assertEquals("there", fs.createByPath("/dirXYZ/dir2/myCopiedDir/myFile.txt").readString());
		assertEquals(2, fs.createByPath("/dirXYZ/dir2/myCopiedDir/myFile.txt").getVersion(false));
		
		StringBuilder sb = new StringBuilder();
		sb.append("<D> dir1\n");
		sb.append("   <F> myFile.txt\n");
		sb.append("<D> dirXYZ\n");
		sb.append("   <D> dir2\n");
		sb.append("      <D> myCopiedDir\n");
		sb.append("         <F> myFile.txt\n");
		assertEquals(sb.toString(), listDir(fs));

		sb = new StringBuilder();
		sb.append("<D> dir1\n");
		if (supportsHistory()) {
			sb.append("   <D> ~history\n");
			sb.append("      <F> myFile_" + TIME_001_TEXT + ".txt\n");
		}
		sb.append("   <D> ~version\n");
		sb.append("      <F> myFile.txt\n");
		sb.append("   <F> myFile.txt\n");
		sb.append("<D> dirXYZ\n");
		sb.append("   <D> dir2\n");
		sb.append("      <D> myCopiedDir\n");
		sb.append("         <D> ~version\n");
		sb.append("            <F> myFile.txt\n");
		sb.append("         <F> myFile.txt\n");
		
		log("test_copyFilesTree_withVersions: expected inner fs with names:\n" + sb);
		log("test_copyFilesTree_withVersions: actual inner fs with names:\n" + listDirInnerFS(fs));

		assertListDirInnerFSEquals(sb, fs);
	}
	
	@Test
	public void test_move_dirWithModififedFiles() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
		FSFFile dir1 = fs.createByPath("/dir1").mkdirs();
		FSFFile dir2 = fs.createByPath("/dirXYZ/dir2").mkdirs();
		dir1.getChild("myFile.txt").writeString("hi").writeString("there");
		env.setCurrentTime(TIME_002);
		dir1.moveTo(dir2.getChild("myMovedDir"));
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<D> dirXYZ\n");
		sb.append("   <D> dir2\n");
		sb.append("      <D> myMovedDir\n");
		sb.append("         <F> myFile.txt\n");
		assertEquals(sb.toString(), listDir(fs));
		assertEquals("there", fs.createByPath("/dirXYZ/dir2/myMovedDir/myFile.txt").readString());
		
		sb = new StringBuilder();
		sb.append("<D> dirXYZ\n");
		sb.append("   <D> dir2\n");
		sb.append("      <D> myMovedDir\n");
		if (supportsHistory()) {
			sb.append("         <D> ~history\n");
			sb.append("            <F> myFile_" + TIME_001_TEXT + ".txt\n");
		}
		if (supportsVersioning()) {
			sb.append("         <D> ~version\n");
			sb.append("            <F> myFile.txt\n");
		}
		sb.append("         <F> myFile.txt\n");
		
		log("test_move_dirWithModififedFiles: expected inner fs with names:\n" + sb);
		log("test_move_dirWithModififedFiles: actual inner fs with names:\n" + listDirInnerFS(fs));

		assertListDirInnerFSEquals(sb, fs);
	}
	
	/**
	 * a file is modified so often that the archived versions in the history are being removed
	 * @throws Exception 
	 */
	@Test
	public void test_writeString_reachingArchivedFilesMaximum() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
		FSFFile file = fs.createByPath("/dir1").mkdirs().getChild("myFile.txt").writeString("start");
        for (int i = 0; i < MAX_NUMBER_OF_HISTORY_FILES; i++) {
        	env.setCurrentTime(TIMES.get(i));
        	file.writeString("hello #" + i);
        }
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<D> dir1\n");
		sb.append("   <F> myFile.txt\n");
		assertEquals(sb.toString(), listDir(fs));
		sb = new StringBuilder();
		sb.append("<D> dir1\n");
		if (supportsHistory()) {
			sb.append("   <D> ~history\n");
			for (int i = 0; i < MAX_NUMBER_OF_HISTORY_FILES; i++) {
				sb.append("      <F> myFile_" + TIME_TEXTS.get(i) + ".txt\n");
			}
		}
		if (supportsVersioning()) {
			sb.append("   <D> ~version\n");
			sb.append("      <F> myFile.txt\n");
		}
		sb.append("   <F> myFile.txt\n");
		assertListDirInnerFSEquals(sb, fs);
	}

	/**
	 * a file is modified so often that the archived versions in the history are being removed
	 * @throws Exception 
	 */
	@Test
	public void test_writeString_removeArchivedFilesAfterMaximum() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
		FSFFile file = fs.createByPath("/dir1").mkdirs().getChild("myFile.txt").writeString("start");
        for (int i = 0; i < MAX_NUMBER_OF_HISTORY_FILES + 1; i++) {
        	env.setCurrentTime(TIMES.get(i));
        	file.writeString("hello #" + i);
        }
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<D> dir1\n");
		sb.append("   <F> myFile.txt\n");
		assertEquals(sb.toString(), listDir(fs));
		sb = new StringBuilder();
		sb.append("<D> dir1\n");
		if (supportsHistory()) {
			sb.append("   <D> ~history\n");
			for (int i = 0 + 1; i < MAX_NUMBER_OF_HISTORY_FILES + 1; i++) {
				sb.append("      <F> myFile_" + TIME_TEXTS.get(i) + ".txt\n");
			}
		}
		if (supportsVersioning()) {
			sb.append("   <D> ~version\n");
			sb.append("      <F> myFile.txt\n");
		}
		sb.append("   <F> myFile.txt\n");
		assertListDirInnerFSEquals(sb, fs);
	}
	
	@Test
	public void test_deleteTree_dirRemoveUntilArchivedDirsReachMaximumAmount() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
        for (int i = 0; i < MAX_NUMBER_OF_HISTORY_FILES; i++) {
        	env.setCurrentTime(TIMES.get(i));
        	FSFFile dir = fs.createByPath("/dir1").mkdirs();
        	dir.getChild("myFile.txt").writeString("hello #" + i);
        	dir.deleteTree();
        }
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		assertEquals(sb.toString(), listDir(fs));
		if (supportsHistory()) {
			sb = new StringBuilder();
			sb.append("<D> ~history\n");
			for (int i = 0; i < MAX_NUMBER_OF_HISTORY_FILES; i++) {
				sb.append("   <D> dir1_" + TIME_TEXTS.get(i) + "\n");
				sb.append("      <F> myFile.txt\n");
			}
			assertListDirInnerFSEquals(sb, fs);
		}
	}
	
	/**
	 * a dir is created and deleted so often that the archived versions in the history are being removed
	 */
	@Test
	public void test_deleteTree_dirRemoveUntilArchivedDirsExceedMaximumAmount() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
        for (int i = 0; i < MAX_NUMBER_OF_HISTORY_FILES + 1; i++) {
        	env.setCurrentTime(TIMES.get(i));
        	FSFFile dir = fs.createByPath("/dir1").mkdirs();
        	dir.getChild("myFile.txt").writeString("hello #" + i);
        	dir.deleteTree();
        }
		
		//: perform check
        if (supportsHistory()) {
        	StringBuilder sb = new StringBuilder();
        	sb.append("<D> ~history\n");
        	for (int i = 0 + 1; i < MAX_NUMBER_OF_HISTORY_FILES + 1; i++) {
        		sb.append("   <D> dir1_" + TIME_TEXTS.get(i) + "\n");
        		sb.append("      <F> myFile.txt\n");
        	}
			assertListDirInnerFSEquals(sb, fs);
        }
	}
	
	@Test
	public void test_listDirAsString_normal() throws Exception {
		if (!supportsHistory()) {
			return;
		}
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
		fs.createByPath("/dir1").mkdirs().getChild("file1.txt").writeString("hello!");
		fs.createByPath("/dir1/dir2").mkdirs().getChild("file2.txt").writeString("there");
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<D> dir1\n");
		sb.append("   <D> dir2\n");
		sb.append("      <F> file2.txt\n");
		sb.append("   <F> file1.txt\n");
		assertListDirInnerFSEquals(sb, fs);
		assertEquals(sb.toString(), fs.createByPath("/").listDirAsString(LIST_DIR_FORMATTING_SIMPLE));
	}
	
	@Test
	public void test_listFiles_historyFilesNotShown() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
		fs.createByPath("/dir1").mkdirs().getChild("file1.txt").writeString("hello!").writeString("two");
		fs.createByPath("/dir1/dir2").mkdirs().getChild("file2.txt").writeString("there").writeString("second");
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		if (supportsHistory()) {
			sb = new StringBuilder();
			sb.append("<D> dir1\n");
			sb.append("   <D> dir2\n");
			sb.append("      <D> ~history\n");
			sb.append("         <F> file2_" + TIME_001_TEXT + ".txt\n");
			if (supportsVersioning()) {
				sb.append("      <D> ~version\n");
				sb.append("         <F> file2.txt\n");
			}
			sb.append("      <F> file2.txt\n");
			sb.append("   <D> ~history\n");
			sb.append("      <F> file1_" + TIME_001_TEXT + ".txt\n");
			if (supportsVersioning()) {
				sb.append("   <D> ~version\n");
				sb.append("      <F> file1.txt\n");
			}
			sb.append("   <F> file1.txt\n");
			assertEquals(sb.toString(), listDirInnerFS(fs));
		}

		sb = new StringBuilder();
		sb.append("<D> dir1\n");
		sb.append("   <D> dir2\n");
		sb.append("      <F> file2.txt\n");
		sb.append("   <F> file1.txt\n");
		assertEquals(sb.toString(), fs.createByPath("/").listDirAsString(LIST_DIR_FORMATTING_SIMPLE));
	}
	
	@Test
	public void test_getHistoryTimesAndReadData_fileWithHistory() throws Exception {
		if (!supportsHistory()) {
			return;
		}
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
		FSFFile dir = fs.createByPath("/myDir").mkdirs();
		FSFFile file = dir.getChild("myFile.txt").writeString("hello!");
		dir.getChild("myOtherFile.txt").writeString("test");
		file.writeString("second");
		env.setCurrentTime(TIME_002);
		file.writeString("third");
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<D> myDir\n");
		sb.append("   <D> ~history\n");
		sb.append("      <F> myFile_" + TIME_001_TEXT + ".txt\n");
		sb.append("      <F> myFile_" + TIME_002_TEXT + ".txt\n");
		if (supportsVersioning()) {
			sb.append("   <D> ~version\n");
			sb.append("      <F> myFile.txt\n");
		}
		sb.append("   <F> myFile.txt\n");
		sb.append("   <F> myOtherFile.txt\n");
		assertEquals(sb.toString(), listDirInnerFS(fs));

		FSFFile testFile = fs.createByPath("/myDir/myFile.txt");
		
		assertEquals(2, testFile.getHistoryTimes() .size());
		assertEquals(TIME_001, new ArrayList<Long>(testFile.getHistoryTimes()).get(0).longValue());
		assertEquals(TIME_002, new ArrayList<Long>(testFile.getHistoryTimes()).get(1).longValue());
		
		assertEquals("hello!", FSFFileUtil.readString(testFile.getHistoryInputStream(TIME_001)));
		assertEquals("second", FSFFileUtil.readString(testFile.getHistoryInputStream(TIME_002)));
		assertEquals("third", FSFFileUtil.readString(testFile.getInputStream()));
	}
	
	@Test
	public void test_getHistory_fileWithoutHistory() throws Exception {
		if (!supportsHistory()) {
			return;
		}
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
		FSFFile dir = fs.createByPath("/myDir").mkdirs();
		dir.getChild("myFile.txt").writeString("hello!");
		dir.getChild("myOtherFile.txt").writeString("test");
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<D> myDir\n");
		sb.append("   <F> myFile.txt\n");
		sb.append("   <F> myOtherFile.txt\n");
		assertListDirInnerFSEquals(sb, fs);

		FSFFile testFile = fs.createByPath("/myDir/myFile.txt");
		assertEquals(0, testFile.getHistoryTimes() .size());
		assertEquals("hello!", FSFFileUtil.readString(testFile.getInputStream()));
	}

	@Test
	public void test_listVersions_dirWithoutHistory() throws Exception {
		if (!supportsHistory()) {
			return;
		}
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
		FSFFile dir = fs.createByPath("/myDir").mkdirs();
		dir.getChild("myFile.txt").writeString("hello!");
		dir.getChild("myOtherFile.txt").writeString("test");
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<D> myDir\n");
		sb.append("   <F> myFile.txt\n");
		sb.append("   <F> myOtherFile.txt\n");
		assertListDirInnerFSEquals(sb, fs);

		assertEquals(0, dir.getHistoryTimes().size());
	}
	
	@Test
	public void test_listVersionsOfDir_normal() throws Exception {
		if (!supportsHistory()) {
			return;
		}
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
        for (int i = 0; i < 5; i++) {
        	env.setCurrentTime(TIMES.get(i));
        	FSFFile dir = fs.createByPath("/dir1").mkdirs();
        	dir.getChild("myFile.txt").writeString("hello #" + i);
        	dir.deleteTree();
        }
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<D> ~history\n");
        for (int i = 0; i < 5; i++) {
    		sb.append("   <D> dir1_" + TIME_TEXTS.get(i) + "\n");
    		sb.append("      <F> myFile.txt\n");
        }
		assertListDirInnerFSEquals(sb, fs);
        FSFFile checkDir = fs.createByPath("/dir1");
		assertEquals(false, checkDir.exists());
		assertEquals(5, checkDir.getHistoryTimes().size());
		assertEquals(TIME_001, new ArrayList<Long>(checkDir.getHistoryTimes()).get(0).longValue());
		assertEquals(TIME_002, new ArrayList<Long>(checkDir.getHistoryTimes()).get(1).longValue());
		assertEquals(TIME_003, new ArrayList<Long>(checkDir.getHistoryTimes()).get(2).longValue());
		assertEquals(TIME_004, new ArrayList<Long>(checkDir.getHistoryTimes()).get(3).longValue());
		assertEquals(TIME_005, new ArrayList<Long>(checkDir.getHistoryTimes()).get(4).longValue());
	}
	
	/**
	 * copy a history version of a dir to another dir
	 * @throws Exception 
	 */
	@Test
	public void test_copyHistoryFilesTree_normal() throws Exception {
		if (!supportsHistory()) {
			return;
		}
		if (!supportCopyHistoryFilesTree()) {
			return;
		}
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
        for (int i = 0; i < 5; i++) {
        	env.setCurrentTime(TIMES.get(i));
        	FSFFile dir = fs.createByPath("/dir1").mkdirs();
        	dir.getChild("myFileOne.txt").writeString("hello #" + i);
        	dir.getChild("myFileTwo.txt").writeString("there #" + i);
        	dir.deleteTree();
        }
        
        MemoryFS memoryFS = new MemoryFS();
        FSFFile exportDir = memoryFS.createByPath("/exports/dir1_export").mkdirs();
        FSFFile dir = fs.createByPath("/dir1");
        dir.copyHistoryFilesTree(exportDir, TIME_003);
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<D> ~history\n");
        for (int i = 0; i < 5; i++) {
    		sb.append("   <D> dir1_" + TIME_TEXTS.get(i) + "\n");
    		sb.append("      <F> myFileOne.txt\n");
    		sb.append("      <F> myFileTwo.txt\n");
        }
		assertListDirInnerFSEquals(sb, fs);
        
        sb = new StringBuilder();
        sb.append("<D> exports\n");
        sb.append("   <D> dir1_export\n");
		sb.append("      <F> myFileOne.txt\n");
		sb.append("      <F> myFileTwo.txt\n");
        assertEquals(sb.toString(), memoryFS.createByPath("").listDirAsString(LIST_DIR_FORMATTING_SIMPLE));
        assertEquals("hello #2", memoryFS.createByPath("/exports/dir1_export/myFileOne.txt").readString());
	}

	
	@Test
	public void test_getHistoryTimesAndData_createModifyAndRename() throws Exception {
		if (!supportsHistory()) {
			return;
		}
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
		FSFFile dir = fs.createByPath("/myDir").mkdirs();
		FSFFile file = dir.getChild("myFile.txt").writeString("hello!");
		file.writeString("second");
		env.setCurrentTime(TIME_002);
		file.rename("newName.txt");
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<D> myDir\n");
		sb.append("   <D> ~history\n");
		sb.append("      <F> myFile_" + TIME_001_TEXT + ".txt\n");
		if (supportsVersioning()) {
			sb.append("   <D> ~version\n");
			sb.append("      <F> newName.txt\n");
		}
		sb.append("   <F> newName.txt\n");
		assertEquals(sb.toString(), listDirInnerFS(fs));

		FSFFile testFile = fs.createByPath("/myDir/myFile.txt");
		
		assertEquals(1, testFile.getHistoryTimes() .size());
		assertEquals(TIME_001, new ArrayList<Long>(testFile.getHistoryTimes()).get(0).longValue());
		
		assertEquals("hello!", FSFFileUtil.readString(testFile.getHistoryInputStream(TIME_001)));
		assertEquals("second", fs.createByPath("/myDir/newName.txt").readString());
	}

	@Test
	public void test_getHistoryTimesAndData_createModifyRenameAndDelete() throws Exception {
		if (!supportsHistory()) {
			return;
		}
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
		FSFFile dir = fs.createByPath("/myDir").mkdirs();
		FSFFile file = dir.getChild("myFile.txt").writeString("hello!");
		file.writeString("second");
		env.setCurrentTime(TIME_002);
		file.rename("newName.txt");
		file.delete();
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<D> myDir\n");
		sb.append("   <D> ~history\n");
		sb.append("      <F> myFile_" + TIME_001_TEXT + ".txt\n");
		sb.append("      <F> newName_" + TIME_002_TEXT + ".txt\n");
		assertListDirInnerFSEquals(sb, fs);
		
		FSFFile testFileA = fs.createByPath("/myDir/myFile.txt");
		FSFFile testFileB = fs.createByPath("/myDir/newName.txt");
		
		assertEquals(false, testFileA.exists());
		assertEquals(false, testFileB.exists());
		assertEquals(1, testFileA.getHistoryTimes().size());
		assertEquals(1, testFileB.getHistoryTimes().size());
		assertEquals(TIME_001, new ArrayList<Long>(testFileA.getHistoryTimes()).get(0).longValue());
		assertEquals(TIME_002, new ArrayList<Long>(testFileB.getHistoryTimes()).get(0).longValue());
		assertEquals("hello!", FSFFileUtil.readString(testFileA.getHistoryInputStream(TIME_001)));
		assertEquals("second", FSFFileUtil.readString(testFileB.getHistoryInputStream(TIME_002)));
	}

	@Test
	public void test_getHistoryTimesAndData_createModifyAndDelete() throws Exception {
		if (!supportsHistory()) {
			return;
		}
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
		FSFFile dir = fs.createByPath("/myDir").mkdirs();
		FSFFile file = dir.getChild("myFile.txt").writeString("hello!");
		file.writeString("second");
		env.setCurrentTime(TIME_002);
		file.delete();
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<D> myDir\n");
		sb.append("   <D> ~history\n");
		sb.append("      <F> myFile_" + TIME_001_TEXT + ".txt\n");
		sb.append("      <F> myFile_" + TIME_002_TEXT + ".txt\n");
		assertListDirInnerFSEquals(sb, fs);
		
		FSFFile testFile = fs.createByPath("/myDir/myFile.txt");
		
		assertEquals(false, testFile.exists());
		assertEquals(2, testFile.getHistoryTimes().size());
		assertEquals(TIME_001, new ArrayList<Long>(testFile.getHistoryTimes()).get(0).longValue());
		assertEquals(TIME_002, new ArrayList<Long>(testFile.getHistoryTimes()).get(1).longValue());
		assertEquals("hello!", FSFFileUtil.readString(testFile.getHistoryInputStream(TIME_001)));
		assertEquals("second", FSFFileUtil.readString(testFile.getHistoryInputStream(TIME_002)));
	}

	/**
	 * normal list files but there are history items and those are not shown
	 * @throws Exception 
	 */
	@Test
	public void test_listCreateConfictingFileWithHistoryName_normal() throws Exception {
		if (!supportsHistory()) {
			return;
		}
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
		if (!(fs instanceof RemoteFS)) {
			FSFFile dir = fs.createByPath("/myDir").mkdirs();
			
			try {
				dir.getChild(HistoryFS.DEFAULT_HISTORY_DIR_NAME);
				assertTrue(false, "An exception should have been thrown");
			} catch (RuntimeException e) {
				assertTrue(e.getCause() instanceof IllegalPathItemNameException);
			}
		}
	}
	
	/**
	 * a command removed all history versions of one file
	 * @throws Exception 
	 */
	@Test
	public void test_removeFileHistoryByCommand_normal() throws Exception {
		if (!supportsHistory()) {
			return;
		}
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
		FSFFile file = fs.createByPath("/dir1").mkdirs().getChild("myFile.txt").writeString("start");
		fs.createByPath("/dir1").mkdirs().getChild("myOtherFile.txt").writeString("other");
        for (int i = 0; i < MAX_NUMBER_OF_HISTORY_FILES; i++) {
        	env.setCurrentTime(TIMES.get(i));
        	file.writeString("hello #" + i);
        }
		
		//: perform pre-check
		StringBuilder sb = new StringBuilder();
		sb.append("<D> dir1\n");
		sb.append("   <D> ~history\n");
        for (int i = 0; i < MAX_NUMBER_OF_HISTORY_FILES; i++) {
    		sb.append("      <F> myFile_" + TIME_TEXTS.get(i) + ".txt\n");
        }
		if (supportsVersioning()) {
			sb.append("   <D> ~version\n");
			sb.append("      <F> myFile.txt\n");
		}
        sb.append("   <F> myFile.txt\n");
        sb.append("   <F> myOtherFile.txt\n");
		assertEquals(sb.toString(), listDirInnerFS(fs));
		
		if (file instanceof HistoryFile) {
			((HistoryFile)file).purgeHistory();
			
			//: perform check
			sb = new StringBuilder();
			sb.append("<D> dir1\n");
			if (supportsVersioning()) {
				sb.append("   <D> ~version\n");
				sb.append("      <F> myFile.txt\n");
			}
	        sb.append("   <F> myFile.txt\n");
	        sb.append("   <F> myOtherFile.txt\n");
			assertEquals(sb.toString(), listDirInnerFS(fs));
		}
	}

	/**
	 * a command removed all history versions of one dir
	 * @throws Exception 
	 */
	@Test
	public void test_removeDirHistoryByCommand_normal() throws Exception {
		if (!supportsHistory()) {
			return;
		}
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
        for (int i = 0; i < MAX_NUMBER_OF_HISTORY_FILES; i++) {
        	env.setCurrentTime(TIMES.get(i));
        	FSFFile dir = fs.createByPath("/dir1").mkdirs();
        	dir.getChild("myFile.txt").writeString("hello #" + i);
        	dir.deleteTree();
        }
        FSFFile dir = fs.createByPath("/dir1").mkdirs();
        dir.getChild("test.txt").writeString("yes");
		
		//: perform pre-check
		StringBuilder sb = new StringBuilder();
        sb.append("<D> dir1\n");
        sb.append("   <F> test.txt\n");
		sb.append("<D> ~history\n");
        for (int i = 0; i < MAX_NUMBER_OF_HISTORY_FILES; i++) {
    		sb.append("   <D> dir1_" + TIME_TEXTS.get(i) + "\n");
    		sb.append("      <F> myFile.txt\n");
        }
		assertListDirInnerFSEquals(sb, fs);
		
		if (dir instanceof HistoryFile) {
			((HistoryFile)dir).purgeHistoryTree();
			
			//: perform check
			sb = new StringBuilder();
			sb.append("<D> dir1\n");
	        sb.append("   <F> test.txt\n");
			assertListDirInnerFSEquals(sb, fs);
		}

	}



	@Test
	public void test_writeBytesAndReadBytes_normal() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
		FSFFile file = fs.createByPath("/myFile.txt");
		byte[] testBytes = createByteArray(0, 8, 15, -99);
		file.writeBytes(false, testBytes);
		
		//: perform check
		file = fs.createByPath("/myFile.txt");
		StringBuilder sb = new StringBuilder();
		sb.append("<F> myFile.txt\n");
		assertEquals(sb.toString(), listDir(fs));
		testBytes = createByteArray(0, 8, 15, -99);
		assertEquals(toString(testBytes), toString(file.readBytes()));
		assertEquals(4, file.getLength());
	}
	
	@Test
	public void test_writeBytesAndReadBytes_append() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
		FSFFile file = fs.createByPath("/myFile.txt");
		byte[] testBytes1 = createByteArray(0, 8, 15, -99);
		byte[] testBytes2 = createByteArray(17, 18, 19);
		file.writeBytes(false, testBytes1);
		file.writeBytes(true, testBytes2);
		
		//: perform check
		file = fs.createByPath("/myFile.txt");
		StringBuilder sb = new StringBuilder();
		sb.append("<F> myFile.txt\n");
		assertEquals(sb.toString(), listDir(fs));
		byte[] testBytesAll = createByteArray(0, 8, 15, -99, 17, 18, 19);
		assertEquals(toString(testBytesAll), toString(file.readBytes()));
		assertEquals(7, file.getLength());
	}
	
	@Test
	public void test_getOutputStream_append() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
		FSFFile file = fs.createByPath("/myFile.txt");
		byte[] testBytes1 = createByteArray(0, 8, 15, -99);
		byte[] testBytes2 = createByteArray(17, 18, 19);
		file.writeBytes(false, testBytes1);
		OutputStream outputStream = file.getOutputStream(true);
		outputStream.write(testBytes2);
		outputStream.close();
		
		//: perform check
		file = fs.createByPath("/myFile.txt");
		StringBuilder sb = new StringBuilder();
		sb.append("<F> myFile.txt\n");
		assertEquals(sb.toString(), listDir(fs));
		byte[] testBytesAll = createByteArray(0, 8, 15, -99, 17, 18, 19);
		assertEquals(toString(testBytesAll), toString(file.readBytes()));
	}
	
	@Test
	public void test_getOutputStream_noAppend() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
		FSFFile file = fs.createByPath("/myFile.txt");
		byte[] testBytes1 = createByteArray(0, 8, 15, -99);
		byte[] testBytes2 = createByteArray(17, 18, 19);
		file.writeBytes(false, testBytes1);
		OutputStream outputStream = file.getOutputStream(false);
		outputStream.write(testBytes2);
		outputStream.close();

		
		//: perform check
		file = fs.createByPath("/myFile.txt");
		StringBuilder sb = new StringBuilder();
		sb.append("<F> myFile.txt\n");
		assertEquals(sb.toString(), listDir(fs));
		assertEquals(toString(testBytes2), toString(file.readBytes()));
	}
	
	@Test
	public void test_writeObjectReadObject_normal() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
		FSFFile file = fs.createByPath("/myFile.txt").writeObject("Test");
		
		//: perform check
		file = fs.createByPath("/myFile.txt");
		StringBuilder sb = new StringBuilder();
		sb.append("<F> myFile.txt\n");
		assertEquals(sb.toString(), listDir(fs));

		String readObject = file.readObject(String.class);
		assertEquals("Test", readObject);
	}
	
	@Test
	public void test_getLength_normal() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
		FSFFile file = fs.createByPath("/myFile.txt");
		byte[] testBytes = createByteArray(0, 8, 15, -99);
		file.writeBytes(false, testBytes);
		
		//: perform check
		file = fs.createByPath("/myFile.txt");
		StringBuilder sb = new StringBuilder();
		sb.append("<F> myFile.txt\n");
		assertEquals(sb.toString(), listDir(fs));
		assertEquals(4, file.getLength());
	}
	
	@Test
	public void test_getTimeCreatedAndSetTimeCreated_normal() throws Exception {
		if (!isTimeCreatedSupported()) {
			return;
		}
		
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
		FSFFile file = fs.createByPath("/myFile.txt").writeString("hi");

		//: perform pre-check
		assertEquals(TIME_001, file.getTimeCreated());
		assertEquals(TIME_001, file.getTimeLastModified());

		//: action 1
		env.setCurrentTime(TIME_002);
		file.writeString("ho");

		//: perform check 1
		assertEquals(TIME_001, file.getTimeCreated());
		assertEquals(TIME_002, file.getTimeLastModified());

		//: action 2
		file.setTimeLastModified(TIME_003);
		
		//: perform check 2
		assertEquals(TIME_001, file.getTimeCreated());
		assertEquals(TIME_003, file.getTimeLastModified());

		//: action 3
		file.setTimeCreated(TIME_004);
		
		//: perform check 2
		assertEquals(TIME_004, file.getTimeCreated());
		assertEquals(TIME_003, file.getTimeLastModified());
	}

	@Test
	public void test_listFilesTree_normal() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
		FSFFile dir1 = fs.createByPath("/dir1").mkdirs();
		dir1.getChild("one.txt").writeString("hey").writeString("hey2");
		dir1.getChild("two.txt").writeString("ho").writeString("ho2");
		FSFFile dir2 = dir1.getChild("dir2").mkdirs();
		dir2.getChild("three.txt").writeString("third");
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<D> dir1\n");
		sb.append("   <D> dir2\n");
		sb.append("      <F> three.txt\n");
		sb.append("   <F> one.txt\n");
		sb.append("   <F> two.txt\n");
		assertEquals(sb.toString(), listDir(fs));
		
		assertEquals("hey2", fs.createByPath("/dir1/one.txt").readString());
		assertEquals("ho2", fs.createByPath("/dir1/two.txt").readString());
		assertEquals("third", fs.createByPath("/dir1/dir2/three.txt").readString());
		
		List<FSFFile> result = fs.createByPath("").listFilesTree();
		assertEquals(5, result.size());
		FSFFile firstResultItem = result.get(0);
		assertEquals("/dir1", firstResultItem.getAbsolutePath());
		assertEquals("/dir1/dir2", result.get(1).getAbsolutePath());
		assertEquals("/dir1/dir2/three.txt", result.get(2).getAbsolutePath());
		assertEquals("/dir1/one.txt", result.get(3).getAbsolutePath());
		assertEquals("/dir1/two.txt", result.get(4).getAbsolutePath());
		
		result = fs.createByPath("/").listFilesTree();
		assertEquals(5, result.size());
		assertEquals("/dir1", result.get(0).getAbsolutePath());
		assertEquals("/dir1/dir2", result.get(1).getAbsolutePath());
		assertEquals("/dir1/dir2/three.txt", result.get(2).getAbsolutePath());
		assertEquals("/dir1/one.txt", result.get(3).getAbsolutePath());
		assertEquals("/dir1/two.txt", result.get(4).getAbsolutePath());
		
	}

	@Test
	public void test_getAbsolutePath_createByPath() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
		FSFFile file = fs.createByPath("/dir1/dir2/dir3").mkdirs().getChild("file.txt").writeString("hey");
		logStatus("test_getAbsolutePath_createByPath: created file: " + file);
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<D> dir1\n");
		sb.append("   <D> dir2\n");
		sb.append("      <D> dir3\n");
		sb.append("         <F> file.txt\n");
		String listDirString = listDir(fs);
		assertEquals(sb.toString(), listDirString);
		
		assertEquals("hey", fs.createByPath("/dir1/dir2/dir3/file.txt").readString());

		assertEquals("/dir1/dir2/dir3/file.txt", file.getAbsolutePath());
		assertEquals("/dir1/dir2/dir3", file.getParentFile().getAbsolutePath());
		assertEquals("/dir1/dir2", file.getParentFile().getParentFile().getAbsolutePath());
		assertEquals("/dir1", file.getParentFile().getParentFile().getParentFile().getAbsolutePath());
		FSFFile parent = file.getParentFile().getParentFile().getParentFile().getParentFile();
		assertNotNull(parent);
		assertEquals("", parent.getAbsolutePath());
		
		file = fs.createByPath("/dir1/dir2/dir3/file.txt"); //: create instance again
		assertEquals("/dir1/dir2/dir3/file.txt", file.getAbsolutePath());
		assertEquals("/dir1/dir2/dir3", file.getParentFile().getAbsolutePath());
		assertEquals("/dir1/dir2", file.getParentFile().getParentFile().getAbsolutePath());
		assertEquals("/dir1", file.getParentFile().getParentFile().getParentFile().getAbsolutePath());
		assertEquals("", file.getParentFile().getParentFile().getParentFile().getParentFile().getAbsolutePath());

		file = fs.createByPath("/").getChild("dir1").getChild("dir2").getChild("dir3").getChild("file.txt"); //: create instance again by getChild
		assertEquals("/dir1/dir2/dir3/file.txt", file.getAbsolutePath());
		assertEquals("/dir1/dir2/dir3", file.getParentFile().getAbsolutePath());
		assertEquals("/dir1/dir2", file.getParentFile().getParentFile().getAbsolutePath());
		assertEquals("/dir1", file.getParentFile().getParentFile().getParentFile().getAbsolutePath());
		assertEquals("", file.getParentFile().getParentFile().getParentFile().getParentFile().getAbsolutePath());
	}
	
	@Test
	public void test_getAbsolutePath_afterRename() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
		FSFFile dir2 = fs.createByPath("/dir1/dir2").mkdirs();
		FSFFile file = dir2.getChild("dir3").mkdirs().getChild("file.txt").writeString("hey");
		
		//: perform pre-check
		assertEquals("/dir1/dir2", dir2.getAbsolutePath());
		assertEquals("/dir1/dir2/dir3/file.txt", file.getAbsolutePath());
		
		//: action
		dir2.rename("newDir2");
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<D> dir1\n");
		sb.append("   <D> newDir2\n");
		sb.append("      <D> dir3\n");
		sb.append("         <F> file.txt\n");
		assertEquals(sb.toString(), listDir(fs));

		assertEquals("/dir1/newDir2", dir2.getAbsolutePath());
		assertEquals("/dir1/newDir2/dir3/file.txt", dir2.getChild("dir3").getChild("file.txt").getAbsolutePath()); 
		assertEquals("/dir1/dir2/dir3/file.txt", file.getAbsolutePath()); //: this one is still old
	}
	
	@Test
	public void test_getAbsolutePath_afterMove() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		logStatus("test_getAbsolutePath_afterMove: 1");
		FSFSystem fs = createFS(env);
		FSFFile dir2 = fs.createByPath("/dir1/dir2").mkdirs();
		FSFFile dirNew = fs.createByPath("/dir1/dirNew").mkdirs();
		logStatus("test_getAbsolutePath_afterMove: 2");
		FSFFile file = dir2.getChild("dir3").mkdirs().getChild("file.txt").writeString("hey");
		
		
		//: perform pre-check
		logStatus("test_getAbsolutePath_afterMove: 3");
		assertEquals("/dir1/dir2", dir2.getAbsolutePath());
		assertEquals("/dir1/dir2/dir3/file.txt", file.getAbsolutePath());
		logStatus("test_getAbsolutePath_afterMove: 4");

		logStatus("test_getAbsolutePath_afterMove: inner FS: \n" + listDirInnerFS(fs));
		
		
		//: action
		dir2.moveTo(dirNew.getChild("dirNew2"));
		logStatus("test_getAbsolutePath_afterMove: 5");
		
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<D> dir1\n");
		sb.append("   <D> dirNew\n");
		sb.append("      <D> dirNew2\n");
		sb.append("         <D> dir3\n");
		sb.append("            <F> file.txt\n");
		assertEquals(sb.toString(), listDir(fs));
		
		assertEquals("/dir1/dir2", dir2.getAbsolutePath()); //: this one is still old
		assertEquals("/dir1/dir2/dir3/file.txt", file.getAbsolutePath()); //: this one is still old
		assertEquals("/dir1/dir2/dir3/file.txt", dir2.getChild("dir3").getChild("file.txt").getAbsolutePath()); 
	}
	
	@Test
	public void test_listFiles_normal() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
		FSFFile dir2 = fs.createByPath("/dir1/dir2").mkdirs();
		dir2.getChild("file.txt").writeString("hey");
		dir2.getChild("file2.txt").writeString("ho");
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<D> dir1\n");
		sb.append("   <D> dir2\n");
		sb.append("      <F> file.txt\n");
		sb.append("      <F> file2.txt\n");
		assertEquals(sb.toString(), listDir(fs));

		List<FSFFile> files = dir2.listFiles();
		
		assertEquals(2, files.size()); 
		assertEquals("file.txt", files.get(0).getName()); 
		assertEquals("file2.txt", files.get(1).getName()); 
	}
	
	@Test
	public void test_listFiles_dirDoesNotExist() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
		FSFFile dir2 = fs.createByPath("/dir1/dir2").mkdirs();
		dir2.getChild("file.txt").writeString("hey");
		dir2.getChild("file2.txt").writeString("ho");
		FSFFile dir3 = dir2.getChild("dir3");
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<D> dir1\n");
		sb.append("   <D> dir2\n");
		sb.append("      <F> file.txt\n");
		sb.append("      <F> file2.txt\n");
		assertEquals(sb.toString(), listDir(fs));
		
		List<FSFFile> files = dir3.listFiles();
		
		assertEquals(null, files); 
	}
	
	//   ===================================================================================================
	//   ==================  V  E  R  S  I  O  N  S  =====================================================
	//   ================================================================================================= 

	@Test
	public void test_getVersion_notExistingShoulBe0() throws Exception {
		if (!supportsVersioning()) {
			return;
		}
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
		FSFFile file = fs.createByPath("/myFile.txt");
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		assertEquals(sb.toString(), listDirInnerFS(fs));
		assertEquals(0, file.getVersion());
	}

	
	@Test
	public void test_writeString_firstVersionShouldBe1() throws Exception {
		if (!supportsVersioning()) {
			return;
		}
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
		FSFFile file = fs.createByPath("/myFile.txt");
		file.writeString("hey!");
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<F> myFile.txt\n");
		assertListDirInnerFSEquals(sb, fs);
		assertEquals(1, file.getVersion());
	}

	@Test
	public void test_writeString_secondVersionShouldBe2() throws Exception {
		if (!supportsVersioning()) {
			return;
		}
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
		FSFFile file = fs.createByPath("/myFile.txt");
		file.writeString("hey!");
		env.setCurrentTime(TIME_002);
		file.writeString("hello!");
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		if (supportsHistory()) {
			sb.append("<D> ~history\n");
			sb.append("   <F> myFile_" + TIME_002_TEXT + ".txt\n");
		}
		sb.append("<D> ~version\n");
		sb.append("   <F> myFile.txt\n");
		sb.append("<F> myFile.txt\n");
		assertListDirInnerFSEquals(sb, fs);
		if (hasInnerFS() && !isInnerFSEncrypted()) {
			assertEquals("2", getInnerFS(fs).createByPath("/~version/myFile.txt").readString());
		}
		assertEquals(2, file.getVersion());
		assertEquals("hello!", file.readString());
	}
	
	@Test
	public void test_writeString_thirdVersionShouldBe3() throws Exception {
		if (!supportsVersioning()) {
			return;
		}
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
		FSFFile file = fs.createByPath("/myFile.txt");
		file.writeString("hey!");
		env.setCurrentTime(TIME_002);
		file.writeString("hello!");
		env.setCurrentTime(TIME_003);
		file.writeString("there!");
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		if (supportsHistory()) {
			sb.append("<D> ~history\n");
			sb.append("   <F> myFile_" + TIME_002_TEXT + ".txt\n");
			sb.append("   <F> myFile_" + TIME_003_TEXT + ".txt\n");
		}
		sb.append("<D> ~version\n");
		sb.append("   <F> myFile.txt\n");
		sb.append("<F> myFile.txt\n");
		assertListDirInnerFSEquals(sb, fs);
		if (hasInnerFS() && !isInnerFSEncrypted()) {
			assertEquals("3", getInnerFS(fs).createByPath("/~version/myFile.txt").readString());
		}
		assertEquals(3, file.getVersion());
		assertEquals("there!", file.readString());
	}

	@Test
	public void test_writeStringForVersion_onlyNextVersionWorks() throws Exception {
		if (!supportsVersioning()) {
			return;
		}
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
		FSFFile file = fs.createByPath("/myFile.txt");
		writeStringAndFailWithWrongVersionException(file, "hey!", 0);
		writeStringAndFailWithWrongVersionException(file, "hey!", 2);
		file.writeStringForVersion("hey!", 1);
		env.setCurrentTime(TIME_002);
		writeStringAndFailWithWrongVersionException(file, "hello!", 0);
		writeStringAndFailWithWrongVersionException(file, "hello!", 1);
		writeStringAndFailWithWrongVersionException(file, "hello!", 3);
		file.writeStringForVersion("hello!", 2);
		env.setCurrentTime(TIME_003);
		writeStringAndFailWithWrongVersionException(file, "there!", 0);
		writeStringAndFailWithWrongVersionException(file, "there!", 1);
		writeStringAndFailWithWrongVersionException(file, "there!", 2);
		writeStringAndFailWithWrongVersionException(file, "there!", 4);
		file.writeStringForVersion("there!", 3);
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		if (supportsHistory()) {
			sb.append("<D> ~history\n");
			sb.append("   <F> myFile_" + TIME_002_TEXT + ".txt\n");
			sb.append("   <F> myFile_" + TIME_003_TEXT + ".txt\n");
		}
		sb.append("<D> ~version\n");
		sb.append("   <F> myFile.txt\n");
		sb.append("<F> myFile.txt\n");
		assertListDirInnerFSEquals(sb, fs);
		if (hasInnerFS() && !isInnerFSEncrypted()) {
			assertEquals("3", getInnerFS(fs).createByPath("/~version/myFile.txt").readString());
		}
		assertEquals(3, file.getVersion(false));
		assertEquals("there!", file.readString());
	}

	
	@Test
	public void test_writeObject_firstVersionShouldBe1() throws Exception {
		if (!supportsVersioning()) {
			return;
		}
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
		FSFFile file = fs.createByPath("/myFile.txt");
		file.writeObject("hey!");
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<F> myFile.txt\n");
		assertListDirInnerFSEquals(sb, fs);
		assertEquals(1, file.getVersion());
		assertEquals("hey!", file.readObject(String.class));
	}

	@Test
	public void test_writeObject_secondVersionShouldBe2() throws Exception {
		if (!supportsVersioning()) {
			return;
		}
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
		FSFFile file = fs.createByPath("/myFile.txt");

		file.writeObject("hey!");
		env.setCurrentTime(TIME_002);
		file.writeObject("hello!");
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		if (supportsHistory()) {
			sb.append("<D> ~history\n");
			sb.append("   <F> myFile_" + TIME_002_TEXT + ".txt\n");
		}
		sb.append("<D> ~version\n");
		sb.append("   <F> myFile.txt\n");
		sb.append("<F> myFile.txt\n");
		assertListDirInnerFSEquals(sb, fs);
		if (hasInnerFS() && !isInnerFSEncrypted()) {
			assertEquals("2", getInnerFS(fs).createByPath("/~version/myFile.txt").readString());
		}
		assertEquals(2, file.getVersion());
		assertEquals("hello!", file.readObject(String.class));
	}
	
	@Test
	public void test_writeObject_thirdVersionShouldBe3() throws Exception {
		if (!supportsVersioning()) {
			return;
		}
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
		FSFFile file = fs.createByPath("/myFile.txt");
		file.writeObject("hey!");
		env.setCurrentTime(TIME_002);
		file.writeObject("hello!");
		env.setCurrentTime(TIME_003);
		file.writeObject("there!");
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		if (supportsHistory()) {
			sb.append("<D> ~history\n");
			sb.append("   <F> myFile_" + TIME_002_TEXT + ".txt\n");
			sb.append("   <F> myFile_" + TIME_003_TEXT + ".txt\n");
		}
		sb.append("<D> ~version\n");
		sb.append("   <F> myFile.txt\n");
		sb.append("<F> myFile.txt\n");
		assertListDirInnerFSEquals(sb, fs);
		if (hasInnerFS() && !isInnerFSEncrypted()) {
			assertEquals("3", getInnerFS(fs).createByPath("/~version/myFile.txt").readString());
		}
		assertEquals(3, file.getVersion());
		assertEquals("there!", file.readObject(String.class));
		
		VersionedData<String> objectAndVersion = file.readObjectAndVersion(String.class);
		assertEquals(3L, objectAndVersion.getVersion());
		assertEquals("there!", objectAndVersion.getData());
	}
	
	@Test
	public void test_writeObjectForVersion_onlyNextVersionWorks() throws Exception {
		if (!supportsVersioning()) {
			return;
		}
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
		FSFFile file = fs.createByPath("/myFile.txt");
		writeObjectForVersionAndFailWithWrongVersionException(file, "hey!", 0);
		writeObjectForVersionAndFailWithWrongVersionException(file, "hey!", 2);
		file.writeObjectForVersion("hey!", 1);
		
		env.setCurrentTime(TIME_002);
		writeObjectForVersionAndFailWithWrongVersionException(file, "hello!", 0);
		writeObjectForVersionAndFailWithWrongVersionException(file, "hello!", 1);
		writeObjectForVersionAndFailWithWrongVersionException(file, "hello!", 3);
		file.writeObjectForVersion("hello!", 2);

		env.setCurrentTime(TIME_003);
		writeObjectForVersionAndFailWithWrongVersionException(file, "there!", 0);
		writeObjectForVersionAndFailWithWrongVersionException(file, "there!", 1);
		writeObjectForVersionAndFailWithWrongVersionException(file, "there!", 2);
		writeObjectForVersionAndFailWithWrongVersionException(file, "there!", 4);
		file.writeObjectForVersion("there!", 3);

		
		//: perform check
		StringBuilder sb = new StringBuilder();
		if (supportsHistory()) {
			sb.append("<D> ~history\n");
			sb.append("   <F> myFile_" + TIME_002_TEXT + ".txt\n");
			sb.append("   <F> myFile_" + TIME_003_TEXT + ".txt\n");
		}
		sb.append("<D> ~version\n");
		sb.append("   <F> myFile.txt\n");
		sb.append("<F> myFile.txt\n");
		assertListDirInnerFSEquals(sb, fs);
		if (hasInnerFS() && !isInnerFSEncrypted()) {
			assertEquals("3", getInnerFS(fs).createByPath("/~version/myFile.txt").readString());
		}
		assertEquals(3, file.getVersion(false));
		assertEquals("there!", file.readObject(String.class));
		
		VersionedData<String> objectAndVersion = file.readObjectAndVersion(String.class);
		assertEquals(3L, objectAndVersion.getVersion());
		assertEquals("there!", objectAndVersion.getData());
	}
	
	
	@Test
	public void test_writeBytes_firstVersionShouldBe1() throws Exception {
		if (!supportsVersioning()) {
			return;
		}
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
		FSFFile file = fs.createByPath("/myFile.txt");
		file.writeBytes(false, "hey!".getBytes(CHARSET));
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<F> myFile.txt\n");
		assertListDirInnerFSEquals(sb, fs);
		assertEquals(1, file.getVersion());
		assertEquals("hey!", new String(file.readBytes(), CHARSET));
	}
	
	@Test
	public void test_writeBytes_secondVersionShouldBe2() throws Exception {
		if (!supportsVersioning()) {
			return;
		}
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
		FSFFile file = fs.createByPath("/myFile.txt");
		
		file.writeBytes(false, "hey!".getBytes(CHARSET));
		env.setCurrentTime(TIME_002);
		file.writeBytes(false, "hello!".getBytes(CHARSET));
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		if (supportsHistory()) {
			sb.append("<D> ~history\n");
			sb.append("   <F> myFile_" + TIME_002_TEXT + ".txt\n");
		}
		sb.append("<D> ~version\n");
		sb.append("   <F> myFile.txt\n");
		sb.append("<F> myFile.txt\n");
		assertListDirInnerFSEquals(sb, fs);
		if (hasInnerFS() && !isInnerFSEncrypted()) {
			assertEquals("2", getInnerFS(fs).createByPath("/~version/myFile.txt").readString());
		}
		assertEquals(2, file.getVersion());
		assertEquals("hello!", new String(file.readBytes(), CHARSET));
	}
	
	@Test
	public void test_writeBytes_thirdVersionShouldBe3() throws Exception {
		if (!supportsVersioning()) {
			return;
		}
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
		FSFFile file = fs.createByPath("/myFile.txt");
		file.writeBytes(false, "hey!".getBytes(CHARSET));
		env.setCurrentTime(TIME_002);
		file.writeBytes(false, "hello!".getBytes(CHARSET));
		env.setCurrentTime(TIME_003);
		file.writeBytes(false, "there!".getBytes(CHARSET));
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		if (supportsHistory()) {
			sb.append("<D> ~history\n");
			sb.append("   <F> myFile_" + TIME_002_TEXT + ".txt\n");
			sb.append("   <F> myFile_" + TIME_003_TEXT + ".txt\n");
		}
		sb.append("<D> ~version\n");
		sb.append("   <F> myFile.txt\n");
		sb.append("<F> myFile.txt\n");
		assertListDirInnerFSEquals(sb, fs);

		if (hasInnerFS() && !isInnerFSEncrypted()) {
			assertEquals("3", getInnerFS(fs).createByPath("/~version/myFile.txt").readString());
		}
		assertEquals(3, file.getVersion());
		assertEquals("there!", new String(file.readBytes(), CHARSET));
		
		VersionedData<byte[]> bytesAndVersion = file.readBytesAndVersion();
		assertEquals(3L, bytesAndVersion.getVersion());
		assertEquals("there!", new String(bytesAndVersion.getData(), CHARSET));
	}
	
	@Test
	public void test_writeBytesForVersion_onlyNextVersionWorks() throws Exception {
		if (!supportsVersioning()) {
			return;
		}
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
		FSFFile file = fs.createByPath("/myFile.txt");
		writeBytesForVersionAndFailWithWrongVersionException(file, "hey!".getBytes(CHARSET), 0);
		writeBytesForVersionAndFailWithWrongVersionException(file, "hey!".getBytes(CHARSET), 2);
		file.writeBytesForVersion(false, "hey!".getBytes(CHARSET), 1);
		
		env.setCurrentTime(TIME_002);
		writeBytesForVersionAndFailWithWrongVersionException(file, "hello!".getBytes(CHARSET), 0);
		writeBytesForVersionAndFailWithWrongVersionException(file, "hello!".getBytes(CHARSET), 1);
		writeBytesForVersionAndFailWithWrongVersionException(file, "hello!".getBytes(CHARSET), 3);
		file.writeBytesForVersion(false, "hello!".getBytes(CHARSET), 2);

		
		env.setCurrentTime(TIME_003);
		writeBytesForVersionAndFailWithWrongVersionException(file, "there!".getBytes(CHARSET), 0);
		writeBytesForVersionAndFailWithWrongVersionException(file, "there!".getBytes(CHARSET), 1);
		writeBytesForVersionAndFailWithWrongVersionException(file, "there!".getBytes(CHARSET), 2);
		writeBytesForVersionAndFailWithWrongVersionException(file, "there!".getBytes(CHARSET), 4);
		file.writeBytesForVersion(false, "there!".getBytes(CHARSET), 3);

		//: perform check
		StringBuilder sb = new StringBuilder();
		if (supportsHistory()) {
			sb.append("<D> ~history\n");
			sb.append("   <F> myFile_" + TIME_002_TEXT + ".txt\n");
			sb.append("   <F> myFile_" + TIME_003_TEXT + ".txt\n");
		}
		sb.append("<D> ~version\n");
		sb.append("   <F> myFile.txt\n");
		sb.append("<F> myFile.txt\n");
		assertListDirInnerFSEquals(sb, fs);

		if (hasInnerFS() && !isInnerFSEncrypted()) {
			assertEquals("3", getInnerFS(fs).createByPath("/~version/myFile.txt").readString());
		}
		assertEquals(3, file.getVersion(false));
		assertEquals("there!", new String(file.readBytes(), CHARSET));
		
		VersionedData<byte[]> bytesAndVersion = file.readBytesAndVersion();
		assertEquals(3L, bytesAndVersion.getVersion());
		assertEquals("there!", new String(bytesAndVersion.getData(), CHARSET));
	}

	
	@Test
	public void test_getOutputStream_firstVersionShouldBe1() throws Exception {
		if (!supportsVersioning()) {
			return;
		}
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
		FSFFile file = fs.createByPath("/myFile.txt");
		FSFFileUtil.writeString(file.getOutputStream(false), "hey!");
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<F> myFile.txt\n");
		assertListDirInnerFSEquals(sb, fs);
		
		assertEquals(1, file.getVersion());
		assertEquals("hey!", new String(file.readBytes(), CHARSET));
		assertEquals("hey!", FSFFileUtil.readString(file.getInputStream()));
	}
	
	@Test
	public void test_getOutputStream_secondVersionShouldBe2() throws Exception {
		if (!supportsVersioning()) {
			return;
		}
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
		FSFFile file = fs.createByPath("/myFile.txt");
		
		FSFFileUtil.writeString(file.getOutputStream(false), "hey!");
		env.setCurrentTime(TIME_002);
		FSFFileUtil.writeString(file.getOutputStream(false), "hello!");
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		if (supportsHistory()) {
			sb.append("<D> ~history\n");
			sb.append("   <F> myFile_" + TIME_002_TEXT + ".txt\n");
		}
		sb.append("<D> ~version\n");
		sb.append("   <F> myFile.txt\n");
		sb.append("<F> myFile.txt\n");
		assertListDirInnerFSEquals(sb, fs);
		assertEquals(2, file.getVersion());
		if (!isInnerFSEncrypted()) {
			assertEquals("2", getInnerFS(fs).createByPath("/~version/myFile.txt").readString());
		}
		assertEquals("hello!", new String(file.readBytes(), CHARSET));
		assertEquals("hello!", FSFFileUtil.readString(file.getInputStream()));
	}
	
	@Test
	public void test_getOutputStream_thirdVersionShouldBe3() throws Exception {
		if (!supportsVersioning()) {
			return;
		}
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
		FSFFile file = fs.createByPath("/myFile.txt");
		FSFFileUtil.writeString(file.getOutputStream(false), "hey!");
		env.setCurrentTime(TIME_002);
		FSFFileUtil.writeString(file.getOutputStream(false), "hello!");
		env.setCurrentTime(TIME_003);
		FSFFileUtil.writeString(file.getOutputStream(false), "there!");
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		if (supportsHistory()) {
			sb.append("<D> ~history\n");
			sb.append("   <F> myFile_" + TIME_002_TEXT + ".txt\n");
			sb.append("   <F> myFile_" + TIME_003_TEXT + ".txt\n");
		}
		sb.append("<D> ~version\n");
		sb.append("   <F> myFile.txt\n");
		sb.append("<F> myFile.txt\n");
		assertListDirInnerFSEquals(sb, fs);
		assertEquals(3, file.getVersion());
		if (!isInnerFSEncrypted()) {
			assertEquals("3", getInnerFS(fs).createByPath("/~version/myFile.txt").readString());
		}
		assertEquals("there!", new String(file.readBytes(), CHARSET));
		assertEquals("there!", FSFFileUtil.readString(file.getInputStream()));
		
		VersionedData<InputStream> inputStreamAndVersion = file.getInputStreamAndVersion();
		assertEquals(3L, inputStreamAndVersion.getVersion());
		assertEquals("there!", FSFFileUtil.readString(inputStreamAndVersion.getData()));
	}
	
	
	@Test
	public void test_writeOutputStreamForVersion_onlyNextVersionWorks() throws Exception {
		if (!supportsVersioning()) {
			return;
		}
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
		FSFFile file = fs.createByPath("/myFile.txt");
		getOutputStreamForVersionAndFailWithWrongVersionException(file, "hey!", 0);
		getOutputStreamForVersionAndFailWithWrongVersionException(file, "hey!", 2);
		FSFFileUtil.writeString(file.getOutputStreamForVersion(false, 1), "hey!");
		
		env.setCurrentTime(TIME_002);
		getOutputStreamForVersionAndFailWithWrongVersionException(file, "hello!", 0);
		getOutputStreamForVersionAndFailWithWrongVersionException(file, "hello!", 1);
		getOutputStreamForVersionAndFailWithWrongVersionException(file, "hello!", 3);
//		assertEquals(1, file.getVersion(false));
		FSFFileUtil.writeString(file.getOutputStreamForVersion(false, 2), "hello!");
		
		env.setCurrentTime(TIME_003);
		getOutputStreamForVersionAndFailWithWrongVersionException(file, "there!", 0);
		getOutputStreamForVersionAndFailWithWrongVersionException(file, "there!", 1);
		getOutputStreamForVersionAndFailWithWrongVersionException(file, "there!", 2);
		getOutputStreamForVersionAndFailWithWrongVersionException(file, "there!", 4);
//		assertEquals(2, file.getVersion(false));
		FSFFileUtil.writeString(file.getOutputStreamForVersion(false, 3), "there!");

		
		//: perform check
		StringBuilder sb = new StringBuilder();
		if (supportsHistory()) {
			sb.append("<D> ~history\n");
			sb.append("   <F> myFile_" + TIME_002_TEXT + ".txt\n");
			sb.append("   <F> myFile_" + TIME_003_TEXT + ".txt\n");
		}
		sb.append("<D> ~version\n");
		sb.append("   <F> myFile.txt\n");
		sb.append("<F> myFile.txt\n");
		assertListDirInnerFSEquals(sb, fs);

		if (hasInnerFS() && !isInnerFSEncrypted()) {
			assertEquals("3", getInnerFS(fs).createByPath("/~version/myFile.txt").readString());
		}
		assertEquals(3, file.getVersion(false));
		assertEquals("there!", file.readString());
	}

	
	/**
	 * create a file and delete it and see that the old version is in the history
	 * @throws Exception 
	 */
	@Test
	public void test_delete_fileVersion() throws Exception {
		if (!supportsVersioning()) {
			return;
		}
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
		FSFFile file = fs.createByPath("/myFile.txt");
		file.writeString("hey!");
		env.setCurrentTime(TIME_002);
		file.writeString("ho!");

		//: perform pre-check
		StringBuilder sb = new StringBuilder();
		if (supportsHistory()) {
			sb.append("<D> ~history\n");
			sb.append("   <F> myFile_" + TIME_002_TEXT + ".txt\n");
		}
		sb.append("<D> ~version\n");
		sb.append("   <F> myFile.txt\n");
		sb.append("<F> myFile.txt\n");
		assertListDirInnerFSEquals(sb, fs);

		file.delete();
		
		//: perform check
		sb = new StringBuilder();
		if (supportsHistory()) {
			sb.append("<D> ~history\n");
			sb.append("   <F> myFile_" + TIME_002_TEXT + ".txt\n");
		}
		assertEquals(sb.toString(), listDirInnerFS(fs));
	}

	@Test
	public void test_delete_dirWithModifiedFilesAndVersionTracking() throws Exception {
		if (!supportsVersioning()) {
			return;
		}
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
		FSFFile dir = fs.createByPath("/myDir").mkdirs();
		FSFFile file1 = dir.getChild("one.txt").writeString("hey").writeString("hey2");
		FSFFile file2 = dir.getChild("two.txt").writeString("ho").writeString("ho2");
		env.setCurrentTime(TIME_002);
		file1.writeString("hey3");
		file2.writeString("ho3");
		
		env.setCurrentTime(TIME_003);
		dir.deleteTree();
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		if (supportsHistory()) {
			sb.append("<D> ~history\n");
			sb.append("   <D> myDir_" + TIME_003_TEXT + "\n");
			sb.append("      <D> ~history\n");
			sb.append("         <F> one_" + TIME_001_TEXT + ".txt\n");
			sb.append("         <F> one_" + TIME_002_TEXT + ".txt\n");
			sb.append("         <F> two_" + TIME_001_TEXT + ".txt\n");
			sb.append("         <F> two_" + TIME_002_TEXT + ".txt\n");
			sb.append("      <D> ~version\n");
			sb.append("         <F> one.txt\n");
			sb.append("         <F> two.txt\n");
			sb.append("      <F> one.txt\n");
			sb.append("      <F> two.txt\n");
		}
		assertEquals(sb.toString(), listDirInnerFS(fs));
	}
	
	/**
	 * create a file, modify it and then rename it and see there is a history version with the old name
	 * @throws Exception 
	 */
	@Test
	public void test_rename_fileAfterModificationWithVersion() throws Exception {
		if (!supportsVersioning()) {
			return;
		}
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
		FSFFile file = fs.createByPath("/myFile.txt").writeString("hello").writeString("what");
		file.rename("yourFile.txt");
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		if (supportsHistory()) {
			sb.append("<D> ~history\n");
			sb.append("   <F> myFile_" + TIME_001_TEXT + ".txt\n");
		}
		sb.append("<D> ~version\n");
		sb.append("   <F> yourFile.txt\n");
		sb.append("<F> yourFile.txt\n");
		assertListDirInnerFSEquals(sb, fs);
	}
	
	@Test
	public void test_move_dirWithModififedFilesWithVersionTracking() throws Exception {
		if (!supportsVersioning()) {
			return;
		}
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
		FSFFile dir1 = fs.createByPath("/dir1").mkdirs();
		FSFFile dir2 = fs.createByPath("/dirXYZ/dir2").mkdirs();
		dir1.getChild("myFile.txt").writeString("hi").writeString("there");
		env.setCurrentTime(TIME_002);
		dir1.moveTo(dir2.getChild("myMovedDir"));
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<D> dirXYZ\n");
		sb.append("   <D> dir2\n");
		sb.append("      <D> myMovedDir\n");
		if (supportsHistory()) {
			sb.append("         <D> ~history\n");
			sb.append("            <F> myFile_" + TIME_001_TEXT + ".txt\n");
		}
		sb.append("         <D> ~version\n");
		sb.append("            <F> myFile.txt\n");
		sb.append("         <F> myFile.txt\n");
		assertListDirInnerFSEquals(sb, fs);
	}
	
	

	@Test
	public void test_listFiles_normalWithVersionTrackingEnabled() throws Exception {
		if (!supportsVersioning()) {
			return;
		}
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
		fs.createByPath("/dir1").mkdirs().getChild("file1.txt").writeString("hello!");
		fs.createByPath("/dir1/dir2").mkdirs().getChild("file2.txt").writeString("there");
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<D> dir1\n");
		sb.append("   <D> dir2\n");
		sb.append("      <F> file2.txt\n");
		sb.append("   <F> file1.txt\n");
		assertListDirInnerFSEquals(sb, fs);
		assertEquals(sb.toString(), fs.createByPath("/").listDirAsString(LIST_DIR_FORMATTING_SIMPLE));
	}

	@Test
	public void test_listFiles_historyAndVersionFilesNotShown() throws Exception {
		if (!supportsVersioning()) {
			return;
		}
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
		fs.createByPath("/dir1").mkdirs().getChild("file1.txt").writeString("hello!").writeString("two");
		fs.createByPath("/dir1/dir2").mkdirs().getChild("file2.txt").writeString("there").writeString("second");
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<D> dir1\n");
		sb.append("   <D> dir2\n");
		if (supportsHistory()) {
			sb.append("      <D> ~history\n");
			sb.append("         <F> file2_" + TIME_001_TEXT + ".txt\n");
		}
		sb.append("      <D> ~version\n");
		sb.append("         <F> file2.txt\n");
		sb.append("      <F> file2.txt\n");
		if (supportsHistory()) {
			sb.append("   <D> ~history\n");
			sb.append("      <F> file1_" + TIME_001_TEXT + ".txt\n");
		}
		sb.append("   <D> ~version\n");
		sb.append("      <F> file1.txt\n");
		sb.append("   <F> file1.txt\n");
		assertListDirInnerFSEquals(sb, fs);

		sb = new StringBuilder();
		sb.append("<D> dir1\n");
		sb.append("   <D> dir2\n");
		sb.append("      <F> file2.txt\n");
		sb.append("   <F> file1.txt\n");
		assertEquals(sb.toString(), fs.createByPath("/").listDirAsString(LIST_DIR_FORMATTING_SIMPLE));
	}

	@Test
	public void test_createConfictingFileWithVersionName_normal() throws Exception {
		if ((!supportsVersioning()) || (isInnerFSEncrypted())) {
			return;
		}
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
		if (!(fs instanceof RemoteFS)) {
			FSFFile dir = fs.createByPath("/myDir").mkdirs();
			
			try {
				dir.getChild(HistoryFS.DEFAULT_VERSION_DIR_NAME);
				assertTrue(false, "An exception should have been thrown");
			} catch (RuntimeException e) {
				assertTrue(e.getCause() instanceof IllegalPathItemNameException);
			}
		}
	}
	
	@Test
	public void test_createConfictingFileWithVersionName_butVersionsDisabledSoNoException() throws Exception {
		if (!supportsVersioning()) {
			SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
			FSFSystem fs = createFS(env);
			FSFFile dir = fs.createByPath("/myDir").mkdirs();
			dir.getChild(HistoryFS.DEFAULT_VERSION_DIR_NAME);
		}
	}

	
	@Test
	public void test_getVersionFromCache_oldAndNew() throws Exception {
		if (!supportsVersioning()) {
			return;
		}
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
		FSFFile file1 = fs.createByPath("/myFile.txt").writeString("test1");
		long oldVersion = file1.getVersion();
		fs.createByPath("/myFile.txt").writeString("test2");
		long newVersionNewFile = fs.createByPath("/myFile.txt").getVersion();
		long newVersionFile1FromCache = file1.getVersion(true);
		long newVersionFile1FromCache2 = file1.getVersion();
		long newVersionFile1WithoutCache = file1.getVersion(false);
		
		//: perform check
		if (hasInnerFS() && !isInnerFSEncrypted()) {
			assertEquals("2", getInnerFS(fs).createByPath("/~version/myFile.txt").readString());
		}
		assertEquals(1, oldVersion);                       // 1
		assertEquals(2, newVersionNewFile);                // 2
		assertEquals(2, newVersionFile1WithoutCache);      // 0
		assertTrue((newVersionFile1FromCache == 1) || (newVersionFile1FromCache == 2)); //: with cached data: 1. Up-to-date value: 2
		assertTrue((newVersionFile1FromCache2 == 1) || (newVersionFile1FromCache2 == 2)); //: with cached data: 1. Up-to-date value: 2
	}

	@Test
	public void listFilesTree_withVersions() throws Exception {
		if (!supportsVersioning()) {
			return;
		}
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
		
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

		FSFFile root = fs.createByPath("/");
		log("listFilesTree_withVersions: before listFilesTree ============================");
		List<FSFFile> result = root.listFilesTree();
		log("listFilesTree_withVersions: after listFilesTree =============================");
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
	public void listFiles_withVersions() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		FSFSystem fs = createFS(env);
		
		FSFFile dir = fs.createByPath("/dir1").mkdirs();
		dir.getChild("fileA.txt").writeString("hello");
		dir.getChild("fileB.txt").writeString("one").writeString("two");
		dir.getChild("dir2").mkdir().getChild("fileC.txt").writeString("nice");
		
		
		FSFFile fileResult;
		
		FSFFile dir1 = fs.createByPath("/dir1");
		log("listFiles_withVersions: before listFiles ============================");
		List<FSFFile> result = dir1.listFiles();
		log("listFiles_withVersions: after listFiles =============================");
		assertEquals(3, result.size());
		
		fileResult = result.get(0);
		assertEquals("dir2", fileResult.getName());
		assertEquals("/dir1/dir2", fileResult.getAbsolutePath());
		assertEquals(true, fileResult.isDirectory());
		
		fileResult = result.get(1);
		assertEquals("fileA.txt", fileResult.getName());
		assertEquals("/dir1/fileA.txt", fileResult.getAbsolutePath());
		if (supportsVersioning()) {
			assertEquals(1, fileResult.getVersion());
		}
		assertEquals(true, fileResult.isFile());
		
		fileResult = result.get(2);
		assertEquals("fileB.txt", fileResult.getName());
		assertEquals("/dir1/fileB.txt", fileResult.getAbsolutePath());
		if (supportsVersioning()) {
			assertEquals(2, fileResult.getVersion());
		}
		assertEquals(true, fileResult.isFile());
	}
	
}
