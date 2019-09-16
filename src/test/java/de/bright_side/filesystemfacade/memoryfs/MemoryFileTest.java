package de.bright_side.filesystemfacade.memoryfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.OutputStream;
import java.util.List;

import org.junit.jupiter.api.Test;

import de.bright_side.filesystemfacade.facade.FSFFile;
import de.bright_side.filesystemfacade.util.FSFFileUtil;
import de.bright_side.filesystemfacade.util.ListDirFormatting;
import de.bright_side.filesystemfacade.util.ListDirFormatting.Style;

public class MemoryFileTest{
	private static final String ENCODING = "UTF-8";
	private static final boolean LOGGING_ENABLED = false;
	private static ListDirFormatting LIST_DIR_FORMATTING_SIMPLE = createListDirFormattingSimple();
	
	private static MemoryFS createFS(){
		return new MemoryFS();
	}
	
	private static String listDirFormattingSimple(MemoryFS memoryFS){
		return memoryFS.createByPath("").listDirAsString(LIST_DIR_FORMATTING_SIMPLE);
	}
	
	private String listDirFormattingSimple(MemoryFS memoryFS, String testDir){
		return memoryFS.createByPath("/" + testDir).listDirAsString(LIST_DIR_FORMATTING_SIMPLE);
	}

	
	private static FSFFile getTopDir(MemoryFS memoryFS, String testName) throws Exception{
		FSFFile topDir = memoryFS.createByPath("/" + testName);
		topDir.mkdirs();
		return topDir;
	}
	
	private static ListDirFormatting createListDirFormattingSimple() {
		return new ListDirFormatting().setStyle(Style.TREE).setAllSubItems(true);
	}

	@Test
	public void test_getName_normal(){
		FSFFile file = new MemoryFile(createFS(), "/dir1/dir2/dir3/myFile.txt");
		String expected = "myFile.txt";
		String result = file.getName();
		assertEquals(expected, result);
	}
	
	@Test
	public void test_getParentFile_normal(){
		FSFFile file = new MemoryFile(createFS(), "/dir1/dir2/dir3/myFile.txt");
		FSFFile parentFile = file.getParentFile();
		assertEquals("/dir1/dir2/dir3", parentFile.getAbsolutePath());
	}
	
	@Test
	public void test_getParentFile_onRoot(){
		FSFFile file = new MemoryFile(createFS(), "");
		assertNull(file.getParentFile());
	}

	@Test
	public void test_rename_normal() throws Exception{
		MemoryFS memoryFS = createFS();
		String path = "/dir1/dir2/dir3/";
		String name1 = "myFile.txt";
		String name2 = "newName.txt";
		String text = "Hello!";
		FSFFile file = new MemoryFile(memoryFS, path + name1);
		
		file.getParentFile().mkdirs();
		file.writeObject(text);
		file.rename(name2);
		
		FSFFile oldFile = new MemoryFile(memoryFS, path + name1);
		
		FSFFile file2 = new MemoryFile(memoryFS, path + name2);
		assertFalse(oldFile.exists());
		assertTrue(file2.exists());
		assertEquals(text, file2.readObject(String.class));
		assertEquals(name2, file.getName());
		assertEquals(path + name2, file.getAbsolutePath());
	}

	@Test
	public void test_getLength_afterOnlySettingObject() throws Exception{
		FSFFile file = new MemoryFile(createFS(), "/dir1/dir2/dir3/myFile1.txt");
		String text = "Hello!";
		file.getParentFile().mkdirs();
		file.writeObject(text);
		assertEquals(text, file.readObject(String.class));
		int lengthOfBeginningAndEndQuote = 2;
		assertEquals(text.length() + lengthOfBeginningAndEndQuote, file.getLength());
	}
	
	@Test
	public void test_mkDirs_normelLevel3() throws Exception{
		MemoryFS memoryFS = createFS();
		String path1 = "/dir1";
		String path2 = path1 + "/dir2";
		String path3 = path2 + "/dir2";
		FSFFile file1 = new MemoryFile(memoryFS, path1);
		FSFFile file2 = new MemoryFile(memoryFS, path2);
		FSFFile file3 = new MemoryFile(memoryFS, path3);
		assertFalse(file1.exists());
		assertFalse(file2.exists());
		assertFalse(file3.exists());
		file3.mkdirs();
		assertTrue(file1.exists());
		assertTrue(file2.exists());
		assertTrue(file3.exists());
	}
	

	@Test
	public void test_delete_normal() throws Exception{
		MemoryFS memoryFS = createFS();
		String path = "/dir1/dir2/dir3/";
		String name1 = "myFile.txt";
		String text = "Hello!";
		FSFFile file = new MemoryFile(memoryFS, path + name1);
		file.getParentFile().mkdirs();
		file.writeObject(text);
		assertTrue(file.exists());
		file.delete();
		assertFalse(file.exists());
	}
	

	@Test
	public void test_readWriteObject_normal() throws Exception{
		MemoryFS memoryFS = createFS();
		String path = "/dir1/dir2/dir3/";
		String name1 = "myFile.txt";
		String text = "Hello!";
		FSFFile file = new MemoryFile(memoryFS, path + name1);
		file.getParentFile().mkdirs();
		file.writeObject(text);
		String result = file.readObject(String.class);
		assertEquals(text, result);
	}
	

	@Test
	public void test_copyTo_normal() throws Exception{
		MemoryFS memoryFS = createFS();
		String path1 = "/dir1/dir2/dir3/myFile1.txt";
		String path2 = "/dir1/dir2/new_DIR/myFile2.txt";
		String text = "Hello!";
		FSFFile file1 = new MemoryFile(memoryFS, path1);
		file1.getParentFile().mkdirs();
		file1.writeObject(text);
		FSFFile file2 = new MemoryFile(memoryFS, path2);
		file2.getParentFile().mkdirs();
		file1.copyTo(file2);
		assertTrue(file1.exists());
		assertTrue(file2.exists());
		assertEquals(text, file1.readObject(String.class));
		assertEquals(text, file2.readObject(String.class));
		
//		System.out.println("listDirAsString: \n" + FSFFileUtil.listDirAsString(memoryFS.createByPath("/dir1"), true));
		
		
		
//		System.out.println("exists: " + file1.getParentFile().exists());
//		System.out.println("listDirAsString: \n" + FSFFileUtil.listDirAsString(file1.getParentFile(), true));
	}
	
	@Test
	public void test_listFiles_normal() throws Exception{
		MemoryFS memoryFS = createFS();
		String path1 = "/dir1/dir2/dir3/myFile1.txt";
		String path2 = "/dir1/dir2/dir3/myFile2.txt";
		String path3 = "/dir1/dir2/dir3/myFile3.txt";
		String text = "Hello!";
		FSFFile file1 = new MemoryFile(memoryFS, path1);
		file1.getParentFile().mkdirs();
		file1.writeObject(text);
		file1.copyTo(new MemoryFile(memoryFS, path2));
		file1.copyTo(new MemoryFile(memoryFS, path3));
		
		FSFFile dir3 = file1.getParentFile();
		List<FSFFile> files = dir3.listFiles();
		
		assertEquals(3, files.size());
		assertEquals("myFile1.txt", files.get(0).getName());
		assertEquals("myFile2.txt", files.get(1).getName());
		assertEquals("myFile3.txt", files.get(2).getName());
		
		FSFFile dir2 = dir3.getParentFile();
		files = dir2.listFiles();
		assertEquals(1, files.size());
		assertEquals("dir3", files.get(0).getName());
		assertEquals(true, files.get(0).isDirectory());

		FSFFile dir1 = dir2.getParentFile();
		files = dir1.listFiles();
		assertEquals(1, files.size());
		assertEquals("dir2", files.get(0).getName());
		assertEquals(true, files.get(0).isDirectory());

		FSFFile dirRoot = dir1.getParentFile();
		files = dirRoot.listFiles();
		assertEquals(1, files.size());
		assertEquals("dir1", files.get(0).getName());
		assertEquals(true, files.get(0).isDirectory());

		
		ListDirFormatting formatting = new ListDirFormatting();
		formatting.setAllSubItems(true);
		formatting.setStyle(Style.TREE);
		//		System.out.println("listDirAsString: \n" + FSFFileUtil.listDirAsString(memoryFS.createByPath("/dir1/dir2/dir3"), true));
//		System.out.println("listDirAsString: \n" + FSFFileUtil.listDirAsString(memoryFS.createByPath("/dir1/dir2"), true));
		log("listDirAsString: \n" + FSFFileUtil.listDirAsString(memoryFS.createByPath("/"), formatting));
	}
	
	private void log(String message) {
		if (LOGGING_ENABLED) {
			System.out.println("MemoryFileTest> " + message);
		}
	}

	@Test
	public void test_listFiles_afterDelete() throws Exception{
		MemoryFS memoryFS = createFS();
		String path1 = "/dir1/dir2/dir3/myFile1.txt";
		String path2 = "/dir1/dir2/dir3/myFile2.txt";
		String path3 = "/dir1/dir2/dir3/myFile3.txt";
		String text = "Hello!";
		FSFFile file1 = new MemoryFile(memoryFS, path1);
		file1.getParentFile().mkdirs();
		file1.writeObject(text);
		file1.copyTo(new MemoryFile(memoryFS, path2));
		file1.copyTo(new MemoryFile(memoryFS, path3));
		file1.delete();
		
		
		FSFFile dir3 = file1.getParentFile();
		List<FSFFile> files = dir3.listFiles();
		assertEquals(2, files.size());
		assertEquals("myFile2.txt", files.get(0).getName());
		assertEquals("myFile3.txt", files.get(1).getName());

		FSFFile dir2 = dir3.getParentFile();
		files = dir2.listFiles();
		assertEquals(1, files.size());
		assertEquals("dir3", files.get(0).getName());
		assertEquals(true, files.get(0).isDirectory());

		FSFFile dir1 = dir2.getParentFile();
		files = dir1.listFiles();
		assertEquals(1, files.size());
		assertEquals("dir2", files.get(0).getName());
		assertEquals(true, files.get(0).isDirectory());

		FSFFile dirRoot = dir1.getParentFile();
		files = dirRoot.listFiles();
		assertEquals(1, files.size());
		assertEquals("dir1", files.get(0).getName());
		assertEquals(true, files.get(0).isDirectory());
	}
	

	@Test
	public void test_writeOutputStream_normal() throws Exception{
		MemoryFS memoryFS = createFS();
		String path = "/dir1/dir2/dir3/";
		String name1 = "myFile.txt";
		String text = "Hello!";
		FSFFile file = new MemoryFile(memoryFS, path + name1);
		file.getParentFile().mkdirs();
		OutputStream outputStream = file.getOutputStream(false);
		outputStream.write(text.getBytes(ENCODING));
		outputStream.close();
		assertEquals(1, file.getParentFile().listFiles().size());
		assertEquals(true, file.exists());
		byte[] readBytes = file.readBytes();
		String result = new String(readBytes, ENCODING);
		assertEquals(text, result);
	}
	
	@Test
	public void test_writeOutputStream_appendToBytes() throws Exception{
		MemoryFS memoryFS = createFS();
		String path = "/dir1/dir2/dir3/";
		String name1 = "myFile.txt";
		String text1 = "Hello!";
		String text2 = "Again!";
		FSFFile file = new MemoryFile(memoryFS, path + name1);
		file.getParentFile().mkdirs();
		OutputStream outputStream1 = file.getOutputStream(false);
		outputStream1.write(text1.getBytes(ENCODING));
		outputStream1.close();
		
		OutputStream outputStream2 = file.getOutputStream(true);
		outputStream2.write(text2.getBytes(ENCODING));
		outputStream2.close();
		assertEquals(1, file.getParentFile().listFiles().size());
		assertEquals(true, file.exists());
		byte[] readBytes = file.readBytes();
		String result = new String(readBytes, ENCODING);
		assertEquals(text1 + text2, result);
	}
	
	@Test
	public void test_writeOutputStream_overwriteAfterBytes() throws Exception{
		MemoryFS memoryFS = createFS();
		String path = "/dir1/dir2/dir3/";
		String name1 = "myFile.txt";
		String text1 = "Hello!";
		String text2 = "Again!";
		FSFFile file = new MemoryFile(memoryFS, path + name1);
		file.getParentFile().mkdirs();
		OutputStream outputStream1 = file.getOutputStream(false);
		outputStream1.write(text1.getBytes(ENCODING));
		outputStream1.close();
		
		OutputStream outputStream2 = file.getOutputStream(false);
		outputStream2.write(text2.getBytes(ENCODING));
		outputStream2.close();
		assertEquals(1, file.getParentFile().listFiles().size());
		assertEquals(true, file.exists());
		byte[] readBytes = file.readBytes();
		String result = new String(readBytes, ENCODING);
		assertEquals(text2, result);
	}
	
	@Test
	public void test_writeOutputStream_overwriteAfterObject() throws Exception{
		MemoryFS memoryFS = createFS();
		String path = "/dir1/dir2/dir3/";
		String name1 = "myFile.txt";
		String text1 = "Hello!";
		String text2 = "Again!";
		FSFFile file = new MemoryFile(memoryFS, path + name1);
		file.getParentFile().mkdirs();
		file.writeObject(text1);
		OutputStream outputStream = file.getOutputStream(false);
		outputStream.write(text2.getBytes(ENCODING));
		outputStream.close();
		assertEquals(1, file.getParentFile().listFiles().size());
		assertEquals(true, file.exists());
		byte[] readBytes = file.readBytes();
		String result = new String(readBytes, ENCODING);
		assertEquals(text2, result);
	}

	@Test
	public void test_writeOutputStream_appendAfterObject() throws Exception{
		MemoryFS memoryFS = createFS();
		String path = "/dir1/dir2/dir3/";
		String name1 = "myFile.txt";
		String text1 = "Hello!";
		String text2 = "Again!";
		FSFFile file = new MemoryFile(memoryFS, path + name1);
		file.getParentFile().mkdirs();
		file.writeObject(text1);
		OutputStream outputStream = file.getOutputStream(true);
		outputStream.write(text2.getBytes(ENCODING));
		outputStream.close();
		assertEquals(1, file.getParentFile().listFiles().size());
		assertEquals(true, file.exists());
		byte[] readBytes = file.readBytes();
		String result = new String(readBytes, ENCODING);
		assertEquals("\"" + text1 + "\"" + text2, result);
	}

	private MemoryFS createComplexDirTree() throws Exception {
		MemoryFS memoryFS = createFS();
		FSFFile fileB = new MemoryFile(memoryFS, "/dir1/dir1-1/dir1-1-1/fileB.txt");
		fileB.getParentFile().mkdirs();
		fileB.writeObject("nice");
		FSFFile fileA = new MemoryFile(memoryFS, "/dir1/dir1-1/dir1-1-1/fileA.txt");
		fileA.getParentFile().mkdirs();
		fileA.writeObject("Hello!");
		FSFFile fileC = new MemoryFile(memoryFS, "/dir1/dir1-2/fileC.txt");
		fileC.getParentFile().mkdirs();
		fileC.writeObject("nice2");
		FSFFile fileD = new MemoryFile(memoryFS, "/dir1/fileD.txt");
		fileD.getParentFile().mkdirs();
		fileD.writeObject("dee");
		FSFFile fileE = new MemoryFile(memoryFS, "/fileE.txt");
		fileE.getParentFile().mkdirs();
		fileE.writeObject("eeeeee");
		return memoryFS;
	}

	@Test
	public void test_listFilesTree_normal() throws Exception{
		MemoryFS memoryFS = createComplexDirTree();

		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<D> dir1\n");
		sb.append("   <D> dir1-1\n");
		sb.append("      <D> dir1-1-1\n");
		sb.append("         <F> fileA.txt\n");
		sb.append("         <F> fileB.txt\n");
		sb.append("   <D> dir1-2\n");
		sb.append("      <F> fileC.txt\n");
		sb.append("   <F> fileD.txt\n");
		sb.append("<F> fileE.txt\n");
		assertEquals(sb.toString(), listDirFormattingSimple(memoryFS));
		
		List<FSFFile> rootFilesTree = new MemoryFile(memoryFS, "").listFilesTree();
		List<FSFFile> dir1FilesTree = new MemoryFile(memoryFS, "/dir1").listFilesTree();
		assertEquals(9, rootFilesTree.size());
		assertEquals(7, dir1FilesTree.size());
		
		assertEquals("dir1", rootFilesTree.get(0).getName());
		assertEquals(true, rootFilesTree.get(0).isDirectory());
		assertEquals("dir1-1", rootFilesTree.get(1).getName());
		assertEquals(true, rootFilesTree.get(1).isDirectory());
		assertEquals("dir1-1-1", rootFilesTree.get(2).getName());
		assertEquals(true, rootFilesTree.get(2).isDirectory());
		assertEquals("fileA.txt", rootFilesTree.get(3).getName());
		assertEquals("Hello!", rootFilesTree.get(3).readObject(String.class));
		assertEquals("fileB.txt", rootFilesTree.get(4).getName());
		assertEquals("nice", rootFilesTree.get(4).readObject(String.class));
		assertEquals("dir1-2", rootFilesTree.get(5).getName());
		assertEquals("fileC.txt", rootFilesTree.get(6).getName());
		assertEquals("nice2", rootFilesTree.get(6).readObject(String.class));
		assertEquals("fileD.txt", rootFilesTree.get(7).getName());
		assertEquals("fileE.txt", rootFilesTree.get(8).getName());
		
		assertEquals("dir1-1", dir1FilesTree.get(0).getName());
		assertEquals(true, dir1FilesTree.get(0).isDirectory());
		assertEquals("dir1-1-1", dir1FilesTree.get(1).getName());
		assertEquals(true, dir1FilesTree.get(1).isDirectory());
		assertEquals("fileA.txt", dir1FilesTree.get(2).getName());
		assertEquals("Hello!", dir1FilesTree.get(2).readObject(String.class));
		assertEquals("fileB.txt", dir1FilesTree.get(3).getName());
		assertEquals("nice", dir1FilesTree.get(3).readObject(String.class));
		assertEquals("dir1-2", dir1FilesTree.get(4).getName());
		assertEquals("fileC.txt", dir1FilesTree.get(5).getName());
		assertEquals("nice2", dir1FilesTree.get(5).readObject(String.class));
		assertEquals("fileD.txt", dir1FilesTree.get(6).getName());
		
	}
	
	@Test
	public void test_listFilesTree_empty() throws Exception{
		MemoryFS memoryFS = createComplexDirTree();
		
		FSFFile emptyDir = memoryFS.createByPath("/dir1/mt");
		emptyDir.mkdirs();
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<D> dir1\n");
		sb.append("   <D> dir1-1\n");
		sb.append("      <D> dir1-1-1\n");
		sb.append("         <F> fileA.txt\n");
		sb.append("         <F> fileB.txt\n");
		sb.append("   <D> dir1-2\n");
		sb.append("      <F> fileC.txt\n");
		sb.append("   <D> mt\n");
		sb.append("   <F> fileD.txt\n");
		sb.append("<F> fileE.txt\n");
		assertEquals(sb.toString(), listDirFormattingSimple(memoryFS));
		
		List<FSFFile> emptyFilesTree = new MemoryFile(memoryFS, emptyDir.getAbsolutePath()).listFilesTree();
		assertEquals(0, emptyFilesTree.size());
	}
	
	@Test
	public void test_copyFilesTree_normal() throws Exception{
		MemoryFS memoryFS = createComplexDirTree();
		FSFFile destDir = memoryFS.createByPath("/dir_new/new_sub_dir");
		destDir.mkdirs();
		FSFFile sourceDir = memoryFS.createByPath("/dir1");
		sourceDir.copyFilesTree(destDir);
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<D> dir1\n");
		sb.append("   <D> dir1-1\n");
		sb.append("      <D> dir1-1-1\n");
		sb.append("         <F> fileA.txt\n");
		sb.append("         <F> fileB.txt\n");
		sb.append("   <D> dir1-2\n");
		sb.append("      <F> fileC.txt\n");
		sb.append("   <F> fileD.txt\n");
		sb.append("<D> dir_new\n");
		sb.append("   <D> new_sub_dir\n");
		sb.append("      <D> dir1-1\n");
		sb.append("         <D> dir1-1-1\n");
		sb.append("            <F> fileA.txt\n");
		sb.append("            <F> fileB.txt\n");
		sb.append("      <D> dir1-2\n");
		sb.append("         <F> fileC.txt\n");
		sb.append("      <F> fileD.txt\n");
		sb.append("<F> fileE.txt\n");
		assertEquals(sb.toString(), listDirFormattingSimple(memoryFS));

		List<FSFFile> destDirFilesTree = destDir.listFilesTree();
		List<FSFFile> sourceDirFilesTree = sourceDir.listFilesTree();
		assertEquals(7, sourceDirFilesTree.size());
		assertEquals(7, destDirFilesTree.size());
		assertEquals("dir1-1", destDirFilesTree.get(0).getName());
		assertEquals("dir1-1-1", destDirFilesTree.get(1).getName());
		assertEquals("fileA.txt", destDirFilesTree.get(2).getName());
		assertEquals("Hello!", destDirFilesTree.get(2).readObject(String.class));
		assertEquals("fileB.txt", destDirFilesTree.get(3).getName());
		assertEquals("nice", destDirFilesTree.get(3).readObject(String.class));
		assertEquals("dir1-2", destDirFilesTree.get(4).getName());

		for (int i = 0; i < sourceDirFilesTree.size(); i++){
			FSFFile a = sourceDirFilesTree.get(i);
			FSFFile b = destDirFilesTree.get(i);
			assertEquals(a.getLength(), b.getLength());
			assertEquals(a.getName(), b.getName());
			assertEquals(a.getTimeLastModified(), b.getTimeLastModified());
			assertEquals(a.isDirectory(), b.isDirectory());
			assertEquals(a.isFile(), b.isFile());
			if (a.isFile()){
				assertEquals(a.readObject(String.class), b.readObject(String.class));
			}
		}
		
	}
	
	@Test
	public void test_copyFilesTree_emptyDir() throws Exception{
		MemoryFS memoryFS = createComplexDirTree();
		FSFFile emptyDir = memoryFS.createByPath("/dir1/mt");
		emptyDir.mkdirs();
		FSFFile destDir = memoryFS.createByPath("/dir_new/new_sub_dir");
		destDir.mkdirs();
		FSFFile sourceDir = memoryFS.createByPath(emptyDir.getAbsolutePath());
		sourceDir.copyFilesTree(destDir);

		List<FSFFile> resultFilesTree = new MemoryFile(memoryFS, destDir.getAbsolutePath()).listFilesTree();
		assertEquals(0, resultFilesTree.size());
		assertEquals(true, destDir.exists());
		assertEquals(true, destDir.isDirectory());
	}
	
	@Test
	public void test_copyFilesTree_copyFile() throws Exception{
		MemoryFS memoryFS = createComplexDirTree();
		FSFFile destFile = memoryFS.createByPath("/dir_new/new_item");
		destFile.getParentFile().mkdirs();
		FSFFile sourceFile = memoryFS.createByPath("/dir1/dir1-2/fileC.txt");
		sourceFile.copyFilesTree(destFile);
		
		//: perform check
		StringBuilder sb = new StringBuilder();
		sb.append("<D> dir1\n");
		sb.append("   <D> dir1-1\n");
		sb.append("      <D> dir1-1-1\n");
		sb.append("         <F> fileA.txt\n");
		sb.append("         <F> fileB.txt\n");
		sb.append("   <D> dir1-2\n");
		sb.append("      <F> fileC.txt\n");
		sb.append("   <F> fileD.txt\n");
		sb.append("<D> dir_new\n");
		sb.append("   <F> new_item\n");
		sb.append("<F> fileE.txt\n");
		assertEquals(sb.toString(), listDirFormattingSimple(memoryFS));

		assertEquals(true, destFile.exists());
		assertEquals(true, destFile.isFile());
		assertEquals("nice2", destFile.readObject(String.class));
	}
	
	@Test
	public void test_deleteFilesTree_emptyDir() throws Exception{
		MemoryFS memoryFS = createComplexDirTree();
		FSFFile dirToDelete = memoryFS.createByPath("/dir1/dir1-3");
		dirToDelete.mkdirs();

		StringBuilder sb;
		sb = new StringBuilder();
		sb.append("<D> dir1\n");
		sb.append("   <D> dir1-1\n");
		sb.append("      <D> dir1-1-1\n");
		sb.append("         <F> fileA.txt\n");
		sb.append("         <F> fileB.txt\n");
		sb.append("   <D> dir1-2\n");
		sb.append("      <F> fileC.txt\n");
		sb.append("   <D> dir1-3\n");
		sb.append("   <F> fileD.txt\n");
		sb.append("<F> fileE.txt\n");
		assertEquals(sb.toString(), listDirFormattingSimple(memoryFS));
		
		dirToDelete.deleteTree();
		
		//: perform check
		sb = new StringBuilder();
		sb.append("<D> dir1\n");
		sb.append("   <D> dir1-1\n");
		sb.append("      <D> dir1-1-1\n");
		sb.append("         <F> fileA.txt\n");
		sb.append("         <F> fileB.txt\n");
		sb.append("   <D> dir1-2\n");
		sb.append("      <F> fileC.txt\n");
		sb.append("   <F> fileD.txt\n");
		sb.append("<F> fileE.txt\n");
		assertEquals(sb.toString(), listDirFormattingSimple(memoryFS));

		assertEquals(false, dirToDelete.exists());
	}
	
	@Test
	public void test_deleteFilesTree_deleteFile() throws Exception{
		MemoryFS memoryFS = createComplexDirTree();
		FSFFile fileToDelete = memoryFS.createByPath("/dir1/dir1-1/dir1-1-1/fileA.txt");
		fileToDelete.deleteTree();
		
		StringBuilder sb = new StringBuilder();
		sb.append("<D> dir1\n");
		sb.append("   <D> dir1-1\n");
		sb.append("      <D> dir1-1-1\n");
		sb.append("         <F> fileB.txt\n");
		sb.append("   <D> dir1-2\n");
		sb.append("      <F> fileC.txt\n");
		sb.append("   <F> fileD.txt\n");
		sb.append("<F> fileE.txt\n");
		assertEquals(sb.toString(), listDirFormattingSimple(memoryFS));
		
	}

	@Test
	public void test_deleteFilesTree_normal() throws Exception{
		MemoryFS memoryFS = createComplexDirTree();
		FSFFile dirToDelete = memoryFS.createByPath("/dir1/dir1-1");

		StringBuilder sb;
		sb = new StringBuilder();
		sb.append("<D> dir1\n");
		sb.append("   <D> dir1-1\n");
		sb.append("      <D> dir1-1-1\n");
		sb.append("         <F> fileA.txt\n");
		sb.append("         <F> fileB.txt\n");
		sb.append("   <D> dir1-2\n");
		sb.append("      <F> fileC.txt\n");
		sb.append("   <F> fileD.txt\n");
		sb.append("<F> fileE.txt\n");
		assertEquals(sb.toString(), listDirFormattingSimple(memoryFS));
		
		dirToDelete.deleteTree();
		
		//: perform check
		sb = new StringBuilder();
		sb.append("<D> dir1\n");
		sb.append("   <D> dir1-2\n");
		sb.append("      <F> fileC.txt\n");
		sb.append("   <F> fileD.txt\n");
		sb.append("<F> fileE.txt\n");
		assertEquals(sb.toString(), listDirFormattingSimple(memoryFS));

		assertEquals(false, dirToDelete.exists());
	}
	
	@Test
	public void test_writeBytes() throws Exception{
		byte[] inputBytes = new byte[4];
		inputBytes[0] = 12;
		inputBytes[1] = 8;
		inputBytes[2] = Byte.MIN_VALUE;
		inputBytes[3] = Byte.MAX_VALUE;
		
		FSFFile file = new MemoryFile(createFS(), "/myFile.dat");
		file.writeBytes(false, inputBytes);
		
		byte[] result = file.readBytes();
		
		assertEquals(inputBytes.length, result.length);
		for (int i = 0; i < inputBytes.length; i++) {
			assertEquals(inputBytes[i], result[i]);
		}
	}

	@Test
	public void test_writeString() throws Exception{
		String inputString = "Hellööö!";
		
		FSFFile file = new MemoryFile(createFS(), "/myFile.text");
		file.writeString(inputString);
		
		String result = file.readString();
		
		assertEquals(inputString, result);
	}

	@Test
	public void test_moveTo_normal() throws Exception{
		MemoryFS memoryFS = createFS();
		String path1 = "/dir1/dir2/dir3/myFile1.txt";
		String path2 = "/dir1/dir2/new_DIR/myFile2.txt";
		String text = "Hello!";
		FSFFile file1 = new MemoryFile(memoryFS, path1);
		file1.getParentFile().mkdirs();
		file1.writeObject(text);
		FSFFile file2 = new MemoryFile(memoryFS, path2);
		file2.getParentFile().mkdirs();
		file1.moveTo(file2);
		assertFalse(file1.exists());
		assertTrue(file2.exists());
		assertEquals(text, file2.readObject(String.class));
	}
	
	@Test
	public void test_moveTo_fileOtherDir() throws Exception {
		String testName = "test_moveTo_fileOtherDir";
		MemoryFS memoryFS = createFS();
		String text = "Hello!";
		FSFFile fileA = getTopDir(memoryFS, testName).getChild("dirA").mkdirs().getChild("hello.txt").writeString(text);
		FSFFile fileB = getTopDir(memoryFS, testName).getChild("dirB").getChild("testBBB").mkdirs().getChild("helloB.txt");

		//: perform check before move
 		StringBuilder sb = new StringBuilder();
		sb.append("<D> dirA\n");
		sb.append("   <F> hello.txt\n");
		sb.append("<D> dirB\n");
		sb.append("   <D> testBBB\n");
		assertEquals(sb.toString(), listDirFormattingSimple(memoryFS, testName));

		
		fileA.moveTo(fileB);
		
		//: perform check after move
 		sb = new StringBuilder();
		sb.append("<D> dirA\n");
		sb.append("<D> dirB\n");
		sb.append("   <D> testBBB\n");
		sb.append("      <F> helloB.txt\n");
		assertEquals(sb.toString(), listDirFormattingSimple(memoryFS, testName));
		assertEquals(text, fileB.readString());
		
	}

	@Test
	public void test_moveTo_dirEmpty() throws Exception {
		String testName = "test_moveTo_dirEmpty";
		MemoryFS memoryFS = createFS();
		FSFFile dirA = getTopDir(memoryFS, testName).getChild("dirA").mkdirs();
		FSFFile dirB = getTopDir(memoryFS, testName).getChild("dirB").mkdirs().getChild("testBBB");
		assertEquals(true, dirA.exists());
		
		
		//: perform check before move
 		StringBuilder sb = new StringBuilder();
		sb.append("<D> dirA\n");
		sb.append("<D> dirB\n");
		assertEquals(sb.toString(), listDirFormattingSimple(memoryFS, testName));
		
		dirA.moveTo(dirB);
		
		//: perform check after move
 		sb = new StringBuilder();
		sb.append("<D> dirB\n");
		sb.append("   <D> testBBB\n");
		assertEquals(sb.toString(), listDirFormattingSimple(memoryFS, testName));
	}

	@Test
	public void test_moveTo_dirWithSubDirsAndFiles() throws Exception {
		String testName = "test_moveTo_dirWithSubDirsAndFiles";
		MemoryFS memoryFS = createFS();
		String text1 = "Hello!";
		String text2 = "Hello2!";
		FSFFile dirA = getTopDir(memoryFS, testName).getChild("dirA").mkdirs();
		dirA.getChild("helloOne.txt").writeString(text1);
		FSFFile dirAOne = dirA.getChild("one").mkdirs();
		dirAOne.getChild("first.txt").writeString(text1);
		dirAOne.getChild("second.txt").writeString(text2);
		FSFFile dirB = getTopDir(memoryFS, testName).getChild("dirB").mkdirs().getChild("testBBB");
		
		//: perform check before move
 		StringBuilder sb = new StringBuilder();
		sb.append("<D> dirA\n");
		sb.append("   <D> one\n");
		sb.append("      <F> first.txt\n");
		sb.append("      <F> second.txt\n");
		sb.append("   <F> helloOne.txt\n");
		sb.append("<D> dirB\n");
		assertEquals(sb.toString(), listDirFormattingSimple(memoryFS, testName));
		
		dirA.moveTo(dirB);
		
		//: perform check after move
 		sb = new StringBuilder();
		sb.append("<D> dirB\n");
		sb.append("   <D> testBBB\n");
		sb.append("      <D> one\n");
		sb.append("         <F> first.txt\n");
		sb.append("         <F> second.txt\n");
		sb.append("      <F> helloOne.txt\n");
		assertEquals(sb.toString(), listDirFormattingSimple(memoryFS, testName));
		assertEquals(text1, dirB.getChild("helloOne.txt").readString());
		assertEquals(text1, dirB.getChild("one").getChild("first.txt").readString());
		assertEquals(text2, dirB.getChild("one").getChild("second.txt").readString());
	}
	
	@Test
	public void test_moveTo_dirWithFile() throws Exception {
		String testName = "test_moveTo_dirWithFile";
		MemoryFS memoryFS = createFS();
		String text1 = "Hello!";
		String text2 = "Hello2!";
		FSFFile dirA = getTopDir(memoryFS, testName).getChild("dirA").mkdirs();
		dirA.getChild("helloOne.txt").writeString(text1);
		dirA.getChild("helloTwo.txt").writeString(text2);
		FSFFile dirB = getTopDir(memoryFS, testName).getChild("dirB").mkdirs().getChild("testBBB");
		
		//: perform check before move
 		StringBuilder sb = new StringBuilder();
		sb.append("<D> dirA\n");
		sb.append("   <F> helloOne.txt\n");
		sb.append("   <F> helloTwo.txt\n");
		sb.append("<D> dirB\n");
		assertEquals(sb.toString(), listDirFormattingSimple(memoryFS, testName));
		
		dirA.moveTo(dirB);
		
		//: perform check after move
 		sb = new StringBuilder();
		sb.append("<D> dirB\n");
		sb.append("   <D> testBBB\n");
		sb.append("      <F> helloOne.txt\n");
		sb.append("      <F> helloTwo.txt\n");
		assertEquals(sb.toString(), listDirFormattingSimple(memoryFS, testName));
		assertEquals(text1, dirB.getChild("helloOne.txt").readString());
		assertEquals(text2, dirB.getChild("helloTwo.txt").readString());
	}


	
}
