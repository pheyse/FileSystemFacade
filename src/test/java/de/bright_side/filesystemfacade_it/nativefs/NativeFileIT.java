package de.bright_side.filesystemfacade_it.nativefs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import de.bright_side.filesystemfacade.facade.FSFFile;
import de.bright_side.filesystemfacade.nativefs.NativeFS;

@Tag("IT")
public class NativeFileIT {
	private static final boolean LOGGING_ENABLED = true;

	private static NativeFS createFS(){
		return new NativeFS();
	}

	private FSFFile getTopDir(String testName) throws Exception {
		return NativeFileTestBase.getTopDir(createFS(), getClass(), testName);
	}

	private String listDir(String testName) throws Exception {
		return NativeFileTestBase.listDir(createFS(), getClass(), testName);
	}
	
	@BeforeAll
	public static void logInfo() throws Exception {
		log("Executing NativeFileIT.");
	}
	
	private static void log(String message) {
		if (LOGGING_ENABLED) {
			System.out.println("NativeFileIT> " + message);
		}
	}

	@BeforeAll
	public static void clearTestDir() throws Exception {
		NativeFileTestBase.clearTestDir(createFS(), new NativeFileIT().getClass());
	}
	
	@Test
	public void test_moveTo_fileOtherDir() throws Exception {
		String testName = "test_moveTo_fileOtherDir";
		assertEquals(0, getTopDir(testName).listFiles().size(), "Test dir must be removed manually before test: " + getTopDir(testName));
		String text = "Hello!";
		FSFFile fileA = getTopDir(testName).getChild("dirA").mkdirs().getChild("hello.txt").writeString(text);
		FSFFile fileB = getTopDir(testName).getChild("dirB").getChild("testBBB").mkdirs().getChild("helloB.txt");

		//: perform check before move
 		StringBuilder sb = new StringBuilder();
		sb.append("<D> dirA\n");
		sb.append("   <F> hello.txt\n");
		sb.append("<D> dirB\n");
		sb.append("   <D> testBBB\n");
		assertEquals(sb.toString(), listDir(testName));

		
		fileA.moveTo(fileB);
		
		//: perform check after move
 		sb = new StringBuilder();
		sb.append("<D> dirA\n");
		sb.append("<D> dirB\n");
		sb.append("   <D> testBBB\n");
		sb.append("      <F> helloB.txt\n");
		assertEquals(sb.toString(), listDir(testName));
		assertEquals(text, fileB.readString());
		
	}

	@Test
	public void test_moveTo_dirEmpty() throws Exception {
		String testName = "test_moveTo_dirEmpty";
		assertEquals(0, getTopDir(testName).listFiles().size(), "Test dir must be removed manually before test: " + getTopDir(testName));
		FSFFile dirA = getTopDir(testName).getChild("dirA").mkdirs();
		FSFFile dirB = getTopDir(testName).getChild("dirB").mkdirs().getChild("testBBB");
		
		//: perform check before move
 		StringBuilder sb = new StringBuilder();
		sb.append("<D> dirA\n");
		sb.append("<D> dirB\n");
		assertEquals(sb.toString(), listDir(testName));
		
		dirA.moveTo(dirB);
		
		//: perform check after move
 		sb = new StringBuilder();
		sb.append("<D> dirB\n");
		sb.append("   <D> testBBB\n");
		assertEquals(sb.toString(), listDir(testName));
	}

	@Test
	public void test_moveTo_dirWithSubDirsAndFiles() throws Exception {
		String testName = "test_moveTo_dirWithSubDirsAndFiles";
		assertEquals(0, getTopDir(testName).listFiles().size(), "Test dir must be removed manually before test: " + getTopDir(testName));
		String text1 = "Hello!";
		String text2 = "Hello2!";
		FSFFile dirA = getTopDir(testName).getChild("dirA").mkdirs();
		dirA.getChild("helloOne.txt").writeString(text1);
		FSFFile dirAOne = dirA.getChild("one").mkdirs();
		dirAOne.getChild("first.txt").writeString(text1);
		dirAOne.getChild("second.txt").writeString(text2);
		FSFFile dirB = getTopDir(testName).getChild("dirB").mkdirs().getChild("testBBB");
		
		//: perform check before move
 		StringBuilder sb = new StringBuilder();
		sb.append("<D> dirA\n");
		sb.append("   <D> one\n");
		sb.append("      <F> first.txt\n");
		sb.append("      <F> second.txt\n");
		sb.append("   <F> helloOne.txt\n");
		sb.append("<D> dirB\n");
		assertEquals(sb.toString(), listDir(testName));
		
		dirA.moveTo(dirB);
		
		//: perform check after move
 		sb = new StringBuilder();
		sb.append("<D> dirB\n");
		sb.append("   <D> testBBB\n");
		sb.append("      <D> one\n");
		sb.append("         <F> first.txt\n");
		sb.append("         <F> second.txt\n");
		sb.append("      <F> helloOne.txt\n");
		assertEquals(sb.toString(), listDir(testName));
		assertEquals(text1, dirB.getChild("helloOne.txt").readString());
		assertEquals(text1, dirB.getChild("one").getChild("first.txt").readString());
		assertEquals(text2, dirB.getChild("one").getChild("second.txt").readString());
	}
	
	@Test
	public void test_moveTo_dirWithFile() throws Exception {
		String testName = "test_moveTo_dirWithFile";
		assertEquals(0, getTopDir(testName).listFiles().size(), "Test dir must be removed manually before test: " + getTopDir(testName));
		String text1 = "Hello!";
		String text2 = "Hello2!";
		FSFFile dirA = getTopDir(testName).getChild("dirA").mkdirs();
		dirA.getChild("helloOne.txt").writeString(text1);
		dirA.getChild("helloTwo.txt").writeString(text2);
		FSFFile dirB = getTopDir(testName).getChild("dirB").mkdirs().getChild("testBBB");
		
		//: perform check before move
 		StringBuilder sb = new StringBuilder();
		sb.append("<D> dirA\n");
		sb.append("   <F> helloOne.txt\n");
		sb.append("   <F> helloTwo.txt\n");
		sb.append("<D> dirB\n");
		assertEquals(sb.toString(), listDir(testName));
		
		dirA.moveTo(dirB);
		
		//: perform check after move
 		sb = new StringBuilder();
		sb.append("<D> dirB\n");
		sb.append("   <D> testBBB\n");
		sb.append("      <F> helloOne.txt\n");
		sb.append("      <F> helloTwo.txt\n");
		assertEquals(sb.toString(), listDir(testName));
		assertEquals(text1, dirB.getChild("helloOne.txt").readString());
		assertEquals(text2, dirB.getChild("helloTwo.txt").readString());
	}
	
	
}
