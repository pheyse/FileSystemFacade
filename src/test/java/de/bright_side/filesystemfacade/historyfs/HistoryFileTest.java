package de.bright_side.filesystemfacade.historyfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import de.bright_side.filesystemfacade.facade.FSFEnvironment;
import de.bright_side.filesystemfacade.facade.FSFFile;
import de.bright_side.filesystemfacade.facade.IllegalPathItemNameException;
import de.bright_side.filesystemfacade.facade.VersionedData;
import de.bright_side.filesystemfacade.facade.WrongVersionException;
import de.bright_side.filesystemfacade.memoryfs.MemoryFS;
import de.bright_side.filesystemfacade.util.FSFFileUtil;
import de.bright_side.filesystemfacade.util.ListDirFormatting;
import de.bright_side.filesystemfacade.util.ListDirFormatting.Style;
import de.bright_side.filesystemfacade.util.SimpleFSFEnvironment;

public class HistoryFileTest {
	private static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss-SSS");
	
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
	private static ListDirFormatting LIST_DIR_FORMATTING_SIMPLE = new ListDirFormatting().setStyle(Style.TREE).setAllSubItems(true);

	
	private static long createTime(String text) {
		try {
			return TIMESTAMP_FORMAT.parse(text).getTime();
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	private static String listDirInnerFS(HistoryFS fs) throws Exception{
		return fs.getInnerFS().createByPath("").listDirAsString(LIST_DIR_FORMATTING_SIMPLE);
	}
	
	private static String listDirHistoryFS(HistoryFS fs) throws Exception{
		return fs.createByPath("").listDirAsString(LIST_DIR_FORMATTING_SIMPLE);
	}
	
	private long textToTime(String text) throws ParseException {
		return TIMESTAMP_FORMAT.parse(text).getTime();
	}
	
	@SuppressWarnings("unused")
	private String timeToText(long time) {
		return TIMESTAMP_FORMAT.format(time);
	}
	
	private static HistoryFS createFS(FSFEnvironment environment, boolean trackVersions){
		return new HistoryFS(new MemoryFS(), trackVersions, MAX_NUMBER_OF_HISTORY_FILES, HistoryFS.DEFAULT_VERSION_DIR_NAME, HistoryFS.DEFAULT_HISTORY_DIR_NAME, environment);
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
			file.getOutputStreamForVersion(false, newVersion);
		} catch (WrongVersionException e) {
			exceptionThrown = true;
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
	
	
	/**
	 * see if the file version can be read
	 * @throws Exception 
	 */
	@Test
	public void test_readVerisons_normal() throws Exception {
		long result = HistoryFile.readVersion("hello_2019-04-16T10-08-03-888.txt", 5, 3);
		assertEquals(textToTime("2019-04-16T10-08-03-888"), result);
	}
	
	
	/**
	 * just create a file and see that there is NO history
	 * @throws Exception 
	 */
	@Test
	public void test_writeString_newFile() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		HistoryFS fs = createFS(env, false);
		fs.createByPath("/myFile.txt").writeString("hey!");
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<F> myFile.txt\n");
		assertEquals(sb.toString(), listDirInnerFS(fs));
	}

	/**
	 * create a file and modify it and see that the old version is in the history
	 * @throws Exception 
	 */
	@Test
	public void test_modifyFile_normal() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		HistoryFS fs = createFS(env, false);
		FSFFile file = fs.createByPath("/myFile.txt");
		file.writeString("hey!");
		file.writeString("hello!");
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<D> ~history\n");
		sb.append("   <F> myFile_" + TIME_001_TEXT + ".txt\n");
		sb.append("<F> myFile.txt\n");
		assertEquals(sb.toString(), listDirInnerFS(fs));
	}

	/**
	 * create a file and delete it and see that the old version is in the history
	 * @throws Exception 
	 */
	@Test
	public void test_delete_file() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		HistoryFS fs = createFS(env, false);
		FSFFile file = fs.createByPath("/myFile.txt");
		file.writeString("hey!");
		file.delete();
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<D> ~history\n");
		sb.append("   <F> myFile_" + TIME_001_TEXT + ".txt\n");
		assertEquals(sb.toString(), listDirInnerFS(fs));
	}

	@Test
	public void test_mkDirs_normal() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		HistoryFS fs = createFS(env, false);
		fs.createByPath("/myDir").mkdirs();
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<D> myDir\n");
		assertEquals(sb.toString(), listDirInnerFS(fs));
	}
	
	@Test
	public void test_delete_dirNoFiles() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		HistoryFS fs = createFS(env, false);
		FSFFile dir = fs.createByPath("/myDir").mkdirs();
		dir.delete();
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<D> ~history\n");
		sb.append("   <D> myDir_" + TIME_001_TEXT + "\n");
		assertEquals(sb.toString(), listDirInnerFS(fs));
	}
	
	@Test
	public void test_deleteTree_dirNoModifiedFiles() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		HistoryFS fs = createFS(env, false);
		FSFFile dir = fs.createByPath("/myDir").mkdirs();
		dir.getChild("one.txt").writeString("hey");
		dir.getChild("two.txt").writeString("ho");
		dir.deleteTree();
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<D> ~history\n");
		sb.append("   <D> myDir_" + TIME_001_TEXT + "\n");
		sb.append("      <F> one.txt\n");
		sb.append("      <F> two.txt\n");
		assertEquals(sb.toString(), listDirInnerFS(fs));
	}
	
	@Test
	public void test_delete_dirWithModifiedFiles() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		HistoryFS fs = createFS(env, false);
		FSFFile dir = fs.createByPath("/myDir").mkdirs();
		dir.getChild("one.txt").writeString("hey").writeString("hey2");
		dir.getChild("two.txt").writeString("ho").writeString("ho2");
		env.setCurrentTime(TIME_002);
		dir.deleteTree();
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<D> ~history\n");
		sb.append("   <D> myDir_" + TIME_002_TEXT + "\n");
		sb.append("      <D> ~history\n");
		sb.append("         <F> one_" + TIME_001_TEXT + ".txt\n");
		sb.append("         <F> two_" + TIME_001_TEXT + ".txt\n");
		sb.append("      <F> one.txt\n");
		sb.append("      <F> two.txt\n");
		assertEquals(sb.toString(), listDirInnerFS(fs));
	}
	
	@Test
	public void test_delete_dirTwice() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		HistoryFS fs = createFS(env, false);
		FSFFile dir = fs.createByPath("/myDir").mkdirs();
		dir.getChild("one.txt").writeString("hey").writeString("hey2");
		dir.getChild("two.txt").writeString("ho").writeString("ho2");
		env.setCurrentTime(TIME_002);
		dir.deleteTree();
		
		dir = fs.createByPath("/myDir").mkdirs();
		dir.getChild("first.txt").writeString("the first");
		env.setCurrentTime(TIME_003);
		dir.deleteTree();
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<D> ~history\n");
		sb.append("   <D> myDir_" + TIME_002_TEXT + "\n");
		sb.append("      <D> ~history\n");
		sb.append("         <F> one_" + TIME_001_TEXT + ".txt\n");
		sb.append("         <F> two_" + TIME_001_TEXT + ".txt\n");
		sb.append("      <F> one.txt\n");
		sb.append("      <F> two.txt\n");
		sb.append("   <D> myDir_" + TIME_003_TEXT + "\n");
		sb.append("      <F> first.txt\n");
		assertEquals(sb.toString(), listDirInnerFS(fs));
	}
	
	/**
	 * create a file and rename it and see there is no version in history
	 * @throws Exception 
	 */
	@Test
	public void test_rename_fileWithoutModification() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		HistoryFS fs = createFS(env, false);
		FSFFile file = fs.createByPath("/myFile.txt").writeString("hello");
		file.rename("yourFile.txt");
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<F> yourFile.txt\n");
		assertEquals(sb.toString(), listDirInnerFS(fs));
	}

	/**
	 * create a file, modify it and then rename it and see there is a history version with the old name
	 * @throws Exception 
	 */
	@Test
	public void test_rename_fileAfterModification() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		HistoryFS fs = createFS(env, false);
		FSFFile file = fs.createByPath("/myFile.txt").writeString("hello").writeString("what");
		file.rename("yourFile.txt");
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<D> ~history\n");
		sb.append("   <F> myFile_" + TIME_001_TEXT + ".txt\n");
		sb.append("<F> yourFile.txt\n");
		assertEquals(sb.toString(), listDirInnerFS(fs));
	}
	
	/**
	 * rename an empty dir and see that there is no history file
	 * @throws Exception 
	 */
	@Test
	public void test_rename_dirNoFiles() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		HistoryFS fs = createFS(env, false);
		FSFFile dir = fs.createByPath("/myDir").mkdirs();
		dir.rename("yourDir");
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<D> yourDir\n");
		assertEquals(sb.toString(), listDirInnerFS(fs));
	}

	/**
	 * rename an empty dir and see that the sub-files were moved
	 * @throws Exception 
	 */
	@Test
	public void test_rename_dirWithUnmodifiedFiles() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		HistoryFS fs = createFS(env, false);
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
		assertEquals(sb.toString(), listDirInnerFS(fs));

	}
	
	/**
	 * rename an empty dir and see that the sub-files were moved incl. history
	 * @throws Exception 
	 */
	@Test
	public void test_rename_dirWithModifiedFiles() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		HistoryFS fs = createFS(env, false);
		FSFFile dir = fs.createByPath("/myDir").mkdirs();
		dir.getChild("one.txt").writeString("hey").writeString("hey2");
		dir.getChild("two.txt").writeString("ho").writeString("ho2");
		env.setCurrentTime(TIME_002);
		dir.rename("yourDir");
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<D> yourDir\n");
		sb.append("   <D> ~history\n");
		sb.append("      <F> one_" + TIME_001_TEXT + ".txt\n");
		sb.append("      <F> two_" + TIME_001_TEXT + ".txt\n");
		sb.append("   <F> one.txt\n");
		sb.append("   <F> two.txt\n");
		assertEquals(sb.toString(), listDirInnerFS(fs));
	}
	
	/**
	 * create a file and rename it and see there is no version in history
	 * @throws Exception 
	 */
	@Test
	public void test_moveTo_unmodififedFile() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		HistoryFS fs = createFS(env, false);
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
		assertEquals(sb.toString(), listDirInnerFS(fs));
	}

	/**
	 * create a file, modify it and then rename it and see there is a history version with the old name
	 * @throws Exception 
	 */
	@Test
	public void test_moveTo_modififedFile() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		HistoryFS fs = createFS(env, false);
		FSFFile dir1 = fs.createByPath("/dir1").mkdirs();
		FSFFile dir2 = fs.createByPath("/dirXYZ/dir2").mkdirs();
		FSFFile file = dir1.getChild("myFile.txt").writeString("hi").writeString("there");
		env.setCurrentTime(TIME_002);
		file.moveTo(dir2.getChild("myMovedFile.txt"));
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<D> dir1\n");
		sb.append("   <D> ~history\n");
		sb.append("      <F> myFile_" + TIME_001_TEXT + ".txt\n");
		sb.append("<D> dirXYZ\n");
		sb.append("   <D> dir2\n");
		sb.append("      <F> myMovedFile.txt\n");
		assertEquals(sb.toString(), listDirInnerFS(fs));

		sb = new StringBuilder();
		sb.append("<D> dir1\n");
		sb.append("<D> dirXYZ\n");
		sb.append("   <D> dir2\n");
		sb.append("      <F> myMovedFile.txt\n");
		assertEquals(sb.toString(), listDirHistoryFS(fs));
		
		FSFFile oldFile = fs.createByPath("/dir1/myFile.txt");
		assertEquals(false, oldFile.exists());
		assertEquals(1, oldFile.getHistoryTimes().size());
		assertEquals(TIME_001, oldFile.getHistoryTimes().first().longValue());
		InputStream historyFileInputStream = oldFile.getHistoryInputStream(TIME_001);
		assertEquals("hi", FSFFileUtil.readString(historyFileInputStream));
		assertEquals("there", fs.createByPath("/dirXYZ/dir2/myMovedFile.txt").readString());
	}
	
	@Test
	public void test_moveTo_dirWithoutFiles() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		HistoryFS fs = createFS(env, false);
		FSFFile dir1 = fs.createByPath("/dir1").mkdirs();
		FSFFile dir2 = fs.createByPath("/dirXYZ/dir2").mkdirs();
		env.setCurrentTime(TIME_002);
		dir1.moveTo(dir2.getChild("myMovedDir"));
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<D> dirXYZ\n");
		sb.append("   <D> dir2\n");
		sb.append("      <D> myMovedDir\n");
		assertEquals(sb.toString(), listDirInnerFS(fs));
	}

	@Test
	public void test_move_dirWithUnmodifiedFile() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		HistoryFS fs = createFS(env, false);
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
		assertEquals(sb.toString(), listDirInnerFS(fs));
	}
	
	@Test
	public void test_move_dirWithModififedFiles() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		HistoryFS fs = createFS(env, false);
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
		sb.append("         <D> ~history\n");
		sb.append("            <F> myFile_" + TIME_001_TEXT + ".txt\n");
		sb.append("         <F> myFile.txt\n");
		assertEquals(sb.toString(), listDirInnerFS(fs));
	}
	
	/**
	 * a file is modified so often that the archived versions in the history are being removed
	 * @throws Exception 
	 */
	@Test
	public void test_writeString_reachingArchivedFilesMaximum() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		HistoryFS fs = createFS(env, false);
		FSFFile file = fs.createByPath("/dir1").mkdirs().getChild("myFile.txt").writeString("start");
        for (int i = 0; i < MAX_NUMBER_OF_HISTORY_FILES; i++) {
        	env.setCurrentTime(TIMES.get(i));
        	file.writeString("hello #" + i);
        }
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<D> dir1\n");
		sb.append("   <D> ~history\n");
        for (int i = 0; i < MAX_NUMBER_OF_HISTORY_FILES; i++) {
    		sb.append("      <F> myFile_" + TIME_TEXTS.get(i) + ".txt\n");
        }
        sb.append("   <F> myFile.txt\n");
		assertEquals(sb.toString(), listDirInnerFS(fs));
	}

	/**
	 * a file is modified so often that the archived versions in the history are being removed
	 * @throws Exception 
	 */
	@Test
	public void test_writeString_removeArchivedFilesAfterMaximum() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		HistoryFS fs = createFS(env, false);
		FSFFile file = fs.createByPath("/dir1").mkdirs().getChild("myFile.txt").writeString("start");
        for (int i = 0; i < MAX_NUMBER_OF_HISTORY_FILES + 1; i++) {
        	env.setCurrentTime(TIMES.get(i));
        	file.writeString("hello #" + i);
        }
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<D> dir1\n");
		sb.append("   <D> ~history\n");
        for (int i = 0 + 1; i < MAX_NUMBER_OF_HISTORY_FILES + 1; i++) {
    		sb.append("      <F> myFile_" + TIME_TEXTS.get(i) + ".txt\n");
        }
        sb.append("   <F> myFile.txt\n");
		assertEquals(sb.toString(), listDirInnerFS(fs));
	}

	@Test
	public void test_deleteTree_dirRemoveUntilArchivedDirsReachMaximumAmount() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		HistoryFS fs = createFS(env, false);
        for (int i = 0; i < MAX_NUMBER_OF_HISTORY_FILES; i++) {
        	env.setCurrentTime(TIMES.get(i));
        	FSFFile dir = fs.createByPath("/dir1").mkdirs();
        	dir.getChild("myFile.txt").writeString("hello #" + i);
        	dir.deleteTree();
        }
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<D> ~history\n");
        for (int i = 0; i < MAX_NUMBER_OF_HISTORY_FILES; i++) {
    		sb.append("   <D> dir1_" + TIME_TEXTS.get(i) + "\n");
    		sb.append("      <F> myFile.txt\n");
        }
		assertEquals(sb.toString(), listDirInnerFS(fs));
	}
	
	/**
	 * a dir is created and deleted so often that the archived versions in the history are being removed
	 */
	@Test
	public void test_deleteTree_dirRemoveUntilArchivedDirsExceedMaximumAmount() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		HistoryFS fs = createFS(env, false);
        for (int i = 0; i < MAX_NUMBER_OF_HISTORY_FILES + 1; i++) {
        	env.setCurrentTime(TIMES.get(i));
        	FSFFile dir = fs.createByPath("/dir1").mkdirs();
        	dir.getChild("myFile.txt").writeString("hello #" + i);
        	dir.deleteTree();
        }
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<D> ~history\n");
        for (int i = 0 + 1; i < MAX_NUMBER_OF_HISTORY_FILES + 1; i++) {
    		sb.append("   <D> dir1_" + TIME_TEXTS.get(i) + "\n");
    		sb.append("      <F> myFile.txt\n");
        }
		assertEquals(sb.toString(), listDirInnerFS(fs));
	}

	@Test
	public void test_listFiles_normal() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		HistoryFS fs = createFS(env, false);
		fs.createByPath("/dir1").mkdirs().getChild("file1.txt").writeString("hello!");
		fs.createByPath("/dir1/dir2").mkdirs().getChild("file2.txt").writeString("there");
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<D> dir1\n");
		sb.append("   <D> dir2\n");
		sb.append("      <F> file2.txt\n");
		sb.append("   <F> file1.txt\n");
		assertEquals(sb.toString(), listDirInnerFS(fs));
		assertEquals(sb.toString(), fs.createByPath("/").listDirAsString(LIST_DIR_FORMATTING_SIMPLE));
	}

	@Test
	public void test_listFiles_historyFilesNotShown() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		HistoryFS fs = createFS(env, false);
		fs.createByPath("/dir1").mkdirs().getChild("file1.txt").writeString("hello!").writeString("two");
		fs.createByPath("/dir1/dir2").mkdirs().getChild("file2.txt").writeString("there").writeString("second");
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<D> dir1\n");
		sb.append("   <D> dir2\n");
		sb.append("      <D> ~history\n");
		sb.append("         <F> file2_" + TIME_001_TEXT + ".txt\n");
		sb.append("      <F> file2.txt\n");
		sb.append("   <D> ~history\n");
		sb.append("      <F> file1_" + TIME_001_TEXT + ".txt\n");
		sb.append("   <F> file1.txt\n");
		assertEquals(sb.toString(), listDirInnerFS(fs));

		sb = new StringBuilder();
		sb.append("<D> dir1\n");
		sb.append("   <D> dir2\n");
		sb.append("      <F> file2.txt\n");
		sb.append("   <F> file1.txt\n");
		assertEquals(sb.toString(), fs.createByPath("/").listDirAsString(LIST_DIR_FORMATTING_SIMPLE));
	}

	@Test
	public void test_getHistoryTimesAndReadData_fileWithHistory() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		HistoryFS fs = createFS(env, false);
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
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		HistoryFS fs = createFS(env, false);
		FSFFile dir = fs.createByPath("/myDir").mkdirs();
		dir.getChild("myFile.txt").writeString("hello!");
		dir.getChild("myOtherFile.txt").writeString("test");
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<D> myDir\n");
		sb.append("   <F> myFile.txt\n");
		sb.append("   <F> myOtherFile.txt\n");
		assertEquals(sb.toString(), listDirInnerFS(fs));

		FSFFile testFile = fs.createByPath("/myDir/myFile.txt");
		assertEquals(0, testFile.getHistoryTimes() .size());
		assertEquals("hello!", FSFFileUtil.readString(testFile.getInputStream()));
	}
	
	@Test
	public void test_listVersions_dirWithoutHistory() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		HistoryFS fs = createFS(env, false);
		FSFFile dir = fs.createByPath("/myDir").mkdirs();
		dir.getChild("myFile.txt").writeString("hello!");
		dir.getChild("myOtherFile.txt").writeString("test");
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<D> myDir\n");
		sb.append("   <F> myFile.txt\n");
		sb.append("   <F> myOtherFile.txt\n");
		assertEquals(sb.toString(), listDirInnerFS(fs));

		assertEquals(0, dir.getHistoryTimes().size());
	}
	

	@Test
	public void test_listVersionsOfDir_normal() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		HistoryFS fs = createFS(env, false);
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
        assertEquals(sb.toString(), listDirInnerFS(fs));
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
	public void test_copyVersionOfDir_normal() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		HistoryFS fs = createFS(env, false);
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
        assertEquals(sb.toString(), listDirInnerFS(fs));
        
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
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		HistoryFS fs = createFS(env, false);
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
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		HistoryFS fs = createFS(env, false);
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
		assertEquals(sb.toString(), listDirInnerFS(fs));
		
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
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		HistoryFS fs = createFS(env, false);
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
		assertEquals(sb.toString(), listDirInnerFS(fs));
		
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
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		HistoryFS fs = createFS(env, false);
		FSFFile dir = fs.createByPath("/myDir").mkdirs();

		try {
			dir.getChild(HistoryFS.DEFAULT_HISTORY_DIR_NAME);
			assertTrue(false, "An exception should have been thrown");
		} catch (RuntimeException e) {
			assertTrue(e.getCause() instanceof IllegalPathItemNameException);
		}
	}
	
	/**
	 * a command removed all history versions of one file
	 * @throws Exception 
	 */
	@Test
	public void test_removeFileHistoryByCommand_normal() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		HistoryFS fs = createFS(env, false);
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
        sb.append("   <F> myFile.txt\n");
        sb.append("   <F> myOtherFile.txt\n");
		assertEquals(sb.toString(), listDirInnerFS(fs));
		
		((HistoryFile)file).purgeHistory();
		
		//: perform check
		sb = new StringBuilder();
		sb.append("<D> dir1\n");
        sb.append("   <F> myFile.txt\n");
        sb.append("   <F> myOtherFile.txt\n");
		assertEquals(sb.toString(), listDirInnerFS(fs));
	}

	/**
	 * a command removed all history versions of one dir
	 * @throws Exception 
	 */
	@Test
	public void test_removeDirHistoryByCommand_normal() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		HistoryFS fs = createFS(env, false);
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
		assertEquals(sb.toString(), listDirInnerFS(fs));
		
		((HistoryFile)dir).purgeHistoryTree();
		
		//: perform check
		sb = new StringBuilder();
		sb.append("<D> dir1\n");
        sb.append("   <F> test.txt\n");
		assertEquals(sb.toString(), listDirInnerFS(fs));

	}

/* =================================================================================================
   ==================  V  E  R  S  I  O  N  S  =====================================================
   ================================================================================================= */

	@Test
	public void test_getVersion_notExistingShoulBe0() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		HistoryFS fs = createFS(env, true);
		FSFFile file = fs.createByPath("/myFile.txt");
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		assertEquals(sb.toString(), listDirInnerFS(fs));
		assertEquals(0, file.getVersion());
	}

	
	@Test
	public void test_writeString_firstVersionShouldBe1() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		HistoryFS fs = createFS(env, true);
		FSFFile file = fs.createByPath("/myFile.txt");
		file.writeString("hey!");
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<F> myFile.txt\n");
		assertEquals(sb.toString(), listDirInnerFS(fs));
		assertEquals(1, file.getVersion());
	}

	@Test
	public void test_writeString_secondVersionShouldBe2() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		HistoryFS fs = createFS(env, true);
		FSFFile file = fs.createByPath("/myFile.txt");
		file.writeString("hey!");
		env.setCurrentTime(TIME_002);
		file.writeString("hello!");
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<D> ~history\n");
		sb.append("   <F> myFile_" + TIME_002_TEXT + ".txt\n");
		sb.append("<D> ~version\n");
		sb.append("   <F> myFile.txt\n");
		sb.append("<F> myFile.txt\n");
		assertEquals(sb.toString(), listDirInnerFS(fs));
		assertEquals(2, file.getVersion());
		assertEquals("2", fs.getInnerFS().createByPath("/~version/myFile.txt").readString());
		assertEquals("hello!", file.readString());
	}
	
	@Test
	public void test_writeString_thirdVersionShouldBe3() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		HistoryFS fs = createFS(env, true);
		FSFFile file = fs.createByPath("/myFile.txt");
		file.writeString("hey!");
		env.setCurrentTime(TIME_002);
		file.writeString("hello!");
		env.setCurrentTime(TIME_003);
		file.writeString("there!");
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<D> ~history\n");
		sb.append("   <F> myFile_" + TIME_002_TEXT + ".txt\n");
		sb.append("   <F> myFile_" + TIME_003_TEXT + ".txt\n");
		sb.append("<D> ~version\n");
		sb.append("   <F> myFile.txt\n");
		sb.append("<F> myFile.txt\n");
		assertEquals(sb.toString(), listDirInnerFS(fs));
		assertEquals(3, file.getVersion());
		assertEquals("3", fs.getInnerFS().createByPath("/~version/myFile.txt").readString());
		assertEquals("there!", file.readString());
	}

	@Test
	public void test_writeStringForVersion_onlyNextVersionWorks() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		HistoryFS fs = createFS(env, true);
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
		sb.append("<D> ~history\n");
		sb.append("   <F> myFile_" + TIME_002_TEXT + ".txt\n");
		sb.append("   <F> myFile_" + TIME_003_TEXT + ".txt\n");
		sb.append("<D> ~version\n");
		sb.append("   <F> myFile.txt\n");
		sb.append("<F> myFile.txt\n");
		assertEquals(sb.toString(), listDirInnerFS(fs));
		assertEquals(3, file.getVersion(false));
		assertEquals("3", fs.getInnerFS().createByPath("/~version/myFile.txt").readString());
		assertEquals("there!", file.readString());
	}

	
	@Test
	public void test_writeObject_firstVersionShouldBe1() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		HistoryFS fs = createFS(env, true);
		FSFFile file = fs.createByPath("/myFile.txt");
		file.writeObject("hey!");
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<F> myFile.txt\n");
		assertEquals(sb.toString(), listDirInnerFS(fs));
		assertEquals(1, file.getVersion());
		assertEquals("hey!", file.readObject(String.class));
	}

	@Test
	public void test_writeObject_secondVersionShouldBe2() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		HistoryFS fs = createFS(env, true);
		FSFFile file = fs.createByPath("/myFile.txt");

		file.writeObject("hey!");
		env.setCurrentTime(TIME_002);
		file.writeObject("hello!");
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<D> ~history\n");
		sb.append("   <F> myFile_" + TIME_002_TEXT + ".txt\n");
		sb.append("<D> ~version\n");
		sb.append("   <F> myFile.txt\n");
		sb.append("<F> myFile.txt\n");
		assertEquals(sb.toString(), listDirInnerFS(fs));
		assertEquals(2, file.getVersion());
		assertEquals("2", fs.getInnerFS().createByPath("/~version/myFile.txt").readString());
		assertEquals("hello!", file.readObject(String.class));
	}
	
	@Test
	public void test_writeObject_thirdVersionShouldBe3() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		HistoryFS fs = createFS(env, true);
		FSFFile file = fs.createByPath("/myFile.txt");
		file.writeObject("hey!");
		env.setCurrentTime(TIME_002);
		file.writeObject("hello!");
		env.setCurrentTime(TIME_003);
		file.writeObject("there!");
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<D> ~history\n");
		sb.append("   <F> myFile_" + TIME_002_TEXT + ".txt\n");
		sb.append("   <F> myFile_" + TIME_003_TEXT + ".txt\n");
		sb.append("<D> ~version\n");
		sb.append("   <F> myFile.txt\n");
		sb.append("<F> myFile.txt\n");
		assertEquals(sb.toString(), listDirInnerFS(fs));
		assertEquals(3, file.getVersion());
		assertEquals("3", fs.getInnerFS().createByPath("/~version/myFile.txt").readString());
		assertEquals("there!", file.readObject(String.class));
		
		VersionedData<String> objectAndVersion = file.readObjectAndVersion(String.class);
		assertEquals(3L, objectAndVersion.getVersion());
		assertEquals("there!", objectAndVersion.getData());
	}
	
	@Test
	public void test_writeObjectForVersion_onlyNextVersionWorks() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		HistoryFS fs = createFS(env, true);
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
		sb.append("<D> ~history\n");
		sb.append("   <F> myFile_" + TIME_002_TEXT + ".txt\n");
		sb.append("   <F> myFile_" + TIME_003_TEXT + ".txt\n");
		sb.append("<D> ~version\n");
		sb.append("   <F> myFile.txt\n");
		sb.append("<F> myFile.txt\n");
		assertEquals(sb.toString(), listDirInnerFS(fs));
		assertEquals(3, file.getVersion(false));
		assertEquals("3", fs.getInnerFS().createByPath("/~version/myFile.txt").readString());
		assertEquals("there!", file.readObject(String.class));
		
		VersionedData<String> objectAndVersion = file.readObjectAndVersion(String.class);
		assertEquals(3L, objectAndVersion.getVersion());
		assertEquals("there!", objectAndVersion.getData());
	}
	
	
	@Test
	public void test_writeBytes_firstVersionShouldBe1() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		HistoryFS fs = createFS(env, true);
		FSFFile file = fs.createByPath("/myFile.txt");
		file.writeBytes(false, "hey!".getBytes(CHARSET));
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<F> myFile.txt\n");
		assertEquals(sb.toString(), listDirInnerFS(fs));
		assertEquals(1, file.getVersion());
		assertEquals("hey!", new String(file.readBytes(), CHARSET));
	}
	
	@Test
	public void test_writeBytes_secondVersionShouldBe2() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		HistoryFS fs = createFS(env, true);
		FSFFile file = fs.createByPath("/myFile.txt");
		
		file.writeBytes(false, "hey!".getBytes(CHARSET));
		env.setCurrentTime(TIME_002);
		file.writeBytes(false, "hello!".getBytes(CHARSET));
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<D> ~history\n");
		sb.append("   <F> myFile_" + TIME_002_TEXT + ".txt\n");
		sb.append("<D> ~version\n");
		sb.append("   <F> myFile.txt\n");
		sb.append("<F> myFile.txt\n");
		assertEquals(sb.toString(), listDirInnerFS(fs));
		assertEquals(2, file.getVersion());
		assertEquals("2", fs.getInnerFS().createByPath("/~version/myFile.txt").readString());
		assertEquals("hello!", new String(file.readBytes(), CHARSET));
	}
	
	@Test
	public void test_writeBytes_thirdVersionShouldBe3() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		HistoryFS fs = createFS(env, true);
		FSFFile file = fs.createByPath("/myFile.txt");
		file.writeBytes(false, "hey!".getBytes(CHARSET));
		env.setCurrentTime(TIME_002);
		file.writeBytes(false, "hello!".getBytes(CHARSET));
		env.setCurrentTime(TIME_003);
		file.writeBytes(false, "there!".getBytes(CHARSET));
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<D> ~history\n");
		sb.append("   <F> myFile_" + TIME_002_TEXT + ".txt\n");
		sb.append("   <F> myFile_" + TIME_003_TEXT + ".txt\n");
		sb.append("<D> ~version\n");
		sb.append("   <F> myFile.txt\n");
		sb.append("<F> myFile.txt\n");
		assertEquals(sb.toString(), listDirInnerFS(fs));
		assertEquals(3, file.getVersion());
		assertEquals("3", fs.getInnerFS().createByPath("/~version/myFile.txt").readString());
		assertEquals("there!", new String(file.readBytes(), CHARSET));
		
		VersionedData<byte[]> bytesAndVersion = file.readBytesAndVersion();
		assertEquals(3L, bytesAndVersion.getVersion());
		assertEquals("there!", new String(bytesAndVersion.getData(), CHARSET));
	}
	
	@Test
	public void test_writeBytesForVersion_onlyNextVersionWorks() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		HistoryFS fs = createFS(env, true);
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
		sb.append("<D> ~history\n");
		sb.append("   <F> myFile_" + TIME_002_TEXT + ".txt\n");
		sb.append("   <F> myFile_" + TIME_003_TEXT + ".txt\n");
		sb.append("<D> ~version\n");
		sb.append("   <F> myFile.txt\n");
		sb.append("<F> myFile.txt\n");
		assertEquals(sb.toString(), listDirInnerFS(fs));
		assertEquals(3, file.getVersion(false));
		assertEquals("3", fs.getInnerFS().createByPath("/~version/myFile.txt").readString());
		assertEquals("there!", new String(file.readBytes(), CHARSET));
		
		VersionedData<byte[]> bytesAndVersion = file.readBytesAndVersion();
		assertEquals(3L, bytesAndVersion.getVersion());
		assertEquals("there!", new String(bytesAndVersion.getData(), CHARSET));
	}

	
	@Test
	public void test_getOutputStream_firstVersionShouldBe1() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		HistoryFS fs = createFS(env, true);
		FSFFile file = fs.createByPath("/myFile.txt");
		FSFFileUtil.writeString(file.getOutputStream(false), "hey!");
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<F> myFile.txt\n");
		assertEquals(sb.toString(), listDirInnerFS(fs));
		assertEquals(1, file.getVersion());
		assertEquals("hey!", new String(file.readBytes(), CHARSET));
		assertEquals("hey!", FSFFileUtil.readString(file.getInputStream()));
	}
	
	@Test
	public void test_getOutputStream_secondVersionShouldBe2() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		HistoryFS fs = createFS(env, true);
		FSFFile file = fs.createByPath("/myFile.txt");
		
		FSFFileUtil.writeString(file.getOutputStream(false), "hey!");
		env.setCurrentTime(TIME_002);
		FSFFileUtil.writeString(file.getOutputStream(false), "hello!");
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<D> ~history\n");
		sb.append("   <F> myFile_" + TIME_002_TEXT + ".txt\n");
		sb.append("<D> ~version\n");
		sb.append("   <F> myFile.txt\n");
		sb.append("<F> myFile.txt\n");
		assertEquals(sb.toString(), listDirInnerFS(fs));
		assertEquals(2, file.getVersion());
		assertEquals("2", fs.getInnerFS().createByPath("/~version/myFile.txt").readString());
		assertEquals("hello!", new String(file.readBytes(), CHARSET));
		assertEquals("hello!", FSFFileUtil.readString(file.getInputStream()));
	}
	
	@Test
	public void test_getOutputStream_thirdVersionShouldBe3() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		HistoryFS fs = createFS(env, true);
		FSFFile file = fs.createByPath("/myFile.txt");
		FSFFileUtil.writeString(file.getOutputStream(false), "hey!");
		env.setCurrentTime(TIME_002);
		FSFFileUtil.writeString(file.getOutputStream(false), "hello!");
		env.setCurrentTime(TIME_003);
		FSFFileUtil.writeString(file.getOutputStream(false), "there!");
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<D> ~history\n");
		sb.append("   <F> myFile_" + TIME_002_TEXT + ".txt\n");
		sb.append("   <F> myFile_" + TIME_003_TEXT + ".txt\n");
		sb.append("<D> ~version\n");
		sb.append("   <F> myFile.txt\n");
		sb.append("<F> myFile.txt\n");
		assertEquals(sb.toString(), listDirInnerFS(fs));
		assertEquals(3, file.getVersion());
		assertEquals("3", fs.getInnerFS().createByPath("/~version/myFile.txt").readString());
		assertEquals("there!", new String(file.readBytes(), CHARSET));
		assertEquals("there!", FSFFileUtil.readString(file.getInputStream()));
		
		VersionedData<InputStream> inputStreamAndVersion = file.getInputStreamAndVersion();
		assertEquals(3L, inputStreamAndVersion.getVersion());
		assertEquals("there!", FSFFileUtil.readString(inputStreamAndVersion.getData()));
	}
	
	
	@Test
	public void test_writeOutputStreamForVersion_onlyNextVersionWorks() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		HistoryFS fs = createFS(env, true);
		FSFFile file = fs.createByPath("/myFile.txt");
		getOutputStreamForVersionAndFailWithWrongVersionException(file, "hey!", 0);
		getOutputStreamForVersionAndFailWithWrongVersionException(file, "hey!", 2);
		FSFFileUtil.writeString(file.getOutputStreamForVersion(false, 1), "hey!");
		
		env.setCurrentTime(TIME_002);
		getOutputStreamForVersionAndFailWithWrongVersionException(file, "hello!", 0);
		getOutputStreamForVersionAndFailWithWrongVersionException(file, "hello!", 1);
		getOutputStreamForVersionAndFailWithWrongVersionException(file, "hello!", 3);
		FSFFileUtil.writeString(file.getOutputStreamForVersion(false, 2), "hello!");
		
		env.setCurrentTime(TIME_003);
		getOutputStreamForVersionAndFailWithWrongVersionException(file, "there!", 0);
		getOutputStreamForVersionAndFailWithWrongVersionException(file, "there!", 1);
		getOutputStreamForVersionAndFailWithWrongVersionException(file, "there!", 2);
		getOutputStreamForVersionAndFailWithWrongVersionException(file, "there!", 4);
		FSFFileUtil.writeString(file.getOutputStreamForVersion(false, 3), "there!");

		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<D> ~history\n");
		sb.append("   <F> myFile_" + TIME_002_TEXT + ".txt\n");
		sb.append("   <F> myFile_" + TIME_003_TEXT + ".txt\n");
		sb.append("<D> ~version\n");
		sb.append("   <F> myFile.txt\n");
		sb.append("<F> myFile.txt\n");
		assertEquals(sb.toString(), listDirInnerFS(fs));
		assertEquals(3, file.getVersion(false));
		assertEquals("3", fs.getInnerFS().createByPath("/~version/myFile.txt").readString());
		assertEquals("there!", file.readString());
	}

	
	/**
	 * create a file and delete it and see that the old version is in the history
	 * @throws Exception 
	 */
	@Test
	public void test_delete_fileVersion() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		HistoryFS fs = createFS(env, true);
		FSFFile file = fs.createByPath("/myFile.txt");
		file.writeString("hey!");
		env.setCurrentTime(TIME_002);
		file.writeString("ho!");

		//: perform pre-check
		StringBuilder sb = new StringBuilder();
		sb.append("<D> ~history\n");
		sb.append("   <F> myFile_" + TIME_002_TEXT + ".txt\n");
		sb.append("<D> ~version\n");
		sb.append("   <F> myFile.txt\n");
		sb.append("<F> myFile.txt\n");
		assertEquals(sb.toString(), listDirInnerFS(fs));

		file.delete();
		
		//: perform check
		sb = new StringBuilder();
		sb.append("<D> ~history\n");
		sb.append("   <F> myFile_" + TIME_002_TEXT + ".txt\n");
		assertEquals(sb.toString(), listDirInnerFS(fs));
	}

	@Test
	public void test_delete_dirWithModifiedFilesAndVersionTracking() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		HistoryFS fs = createFS(env, true);
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
		assertEquals(sb.toString(), listDirInnerFS(fs));
	}
	
	/**
	 * create a file, modify it and then rename it and see there is a history version with the old name
	 * @throws Exception 
	 */
	@Test
	public void test_rename_fileAfterModificationWithVersion() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		HistoryFS fs = createFS(env, true);
		FSFFile file = fs.createByPath("/myFile.txt").writeString("hello").writeString("what");
		file.rename("yourFile.txt");
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<D> ~history\n");
		sb.append("   <F> myFile_" + TIME_001_TEXT + ".txt\n");
		sb.append("<D> ~version\n");
		sb.append("   <F> yourFile.txt\n");
		sb.append("<F> yourFile.txt\n");
		assertEquals(sb.toString(), listDirInnerFS(fs));
	}
	
	@Test
	public void test_move_dirWithModififedFilesWithVersionTracking() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		HistoryFS fs = createFS(env, true);
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
		sb.append("         <D> ~history\n");
		sb.append("            <F> myFile_" + TIME_001_TEXT + ".txt\n");
		sb.append("         <D> ~version\n");
		sb.append("            <F> myFile.txt\n");
		sb.append("         <F> myFile.txt\n");
		assertEquals(sb.toString(), listDirInnerFS(fs));
	}
	
	

	@Test
	public void test_listFiles_normalWithVersionTrackingEnabled() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		HistoryFS fs = createFS(env, true);
		fs.createByPath("/dir1").mkdirs().getChild("file1.txt").writeString("hello!");
		fs.createByPath("/dir1/dir2").mkdirs().getChild("file2.txt").writeString("there");
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<D> dir1\n");
		sb.append("   <D> dir2\n");
		sb.append("      <F> file2.txt\n");
		sb.append("   <F> file1.txt\n");
		assertEquals(sb.toString(), listDirInnerFS(fs));
		assertEquals(sb.toString(), fs.createByPath("/").listDirAsString(LIST_DIR_FORMATTING_SIMPLE));
	}

	@Test
	public void test_listFiles_historyAndVersionFilesNotShown() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		HistoryFS fs = createFS(env, true);
		fs.createByPath("/dir1").mkdirs().getChild("file1.txt").writeString("hello!").writeString("two");
		fs.createByPath("/dir1/dir2").mkdirs().getChild("file2.txt").writeString("there").writeString("second");
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<D> dir1\n");
		sb.append("   <D> dir2\n");
		sb.append("      <D> ~history\n");
		sb.append("         <F> file2_" + TIME_001_TEXT + ".txt\n");
		sb.append("      <D> ~version\n");
		sb.append("         <F> file2.txt\n");
		sb.append("      <F> file2.txt\n");
		sb.append("   <D> ~history\n");
		sb.append("      <F> file1_" + TIME_001_TEXT + ".txt\n");
		sb.append("   <D> ~version\n");
		sb.append("      <F> file1.txt\n");
		sb.append("   <F> file1.txt\n");
		assertEquals(sb.toString(), listDirInnerFS(fs));

		sb = new StringBuilder();
		sb.append("<D> dir1\n");
		sb.append("   <D> dir2\n");
		sb.append("      <F> file2.txt\n");
		sb.append("   <F> file1.txt\n");
		assertEquals(sb.toString(), fs.createByPath("/").listDirAsString(LIST_DIR_FORMATTING_SIMPLE));
	}

	@Test
	public void test_createConfictingFileWithVersionName_normal() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		HistoryFS fs = createFS(env, true);
		FSFFile dir = fs.createByPath("/myDir").mkdirs();

		try {
			dir.getChild(HistoryFS.DEFAULT_VERSION_DIR_NAME);
			assertTrue(false, "An exception should have been thrown");
		} catch (RuntimeException e) {
			assertTrue(e.getCause() instanceof IllegalPathItemNameException);
		}
	}
	
	@Test
	public void test_createConfictingFileWithVersionName_butVersionsDisabledSoNoException() throws Exception {
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(TIME_001);
		HistoryFS fs = createFS(env, false);
		FSFFile dir = fs.createByPath("/myDir").mkdirs();
		dir.getChild(HistoryFS.DEFAULT_VERSION_DIR_NAME);
	}
}
