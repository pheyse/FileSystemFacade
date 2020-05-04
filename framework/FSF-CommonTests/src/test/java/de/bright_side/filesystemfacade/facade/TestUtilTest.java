package de.bright_side.filesystemfacade.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * tests the class TestUtil
 * @author Philip Heyse
 *
 */
public class TestUtilTest {
	@Test
	public void parseListDirItems_simple() throws Exception{
		StringBuilder sb = new StringBuilder();
		sb.append("<D> ~history\n");
		sb.append("   <F> myFile_123.txt\n");
		sb.append("<D> numberOne\n");
		sb.append("   <D> numberTwo\n");
		sb.append("      <F> myDeepFile.txt\n");
		sb.append("   <F> myFile.txt\n");
		sb.append("   <F> myOtherFile.txt\n");
		sb.append("<F> myRootFile.txt\n");
		
		List<ListDirItem> result = TestUtil.parseListDirItems(sb);
		assertEquals(3, result.size());
		
		ListDirItem item;
		
		item = result.get(0);
		assertEquals("~history", item.getName());
		assertEquals(true, item.isDirectory());
		assertEquals(0, item.getLevel());
		assertEquals(1, item.getChildren().size());
		
		item = result.get(0).getChildren().get(0);
		assertEquals("myFile_123.txt", item.getName());
		assertEquals(false, item.isDirectory());
		assertEquals(1, item.getLevel());
		assertEquals(0, item.getChildren().size());
		
		item = result.get(1);
		assertEquals("numberOne", item.getName());
		assertEquals(true, item.isDirectory());
		assertEquals(0, item.getLevel());
		assertEquals(3, item.getChildren().size());
		
		item = result.get(1).getChildren().get(0);
		assertEquals("numberTwo", item.getName());
		assertEquals(true, item.isDirectory());
		assertEquals(1, item.getLevel());
		assertEquals(1, item.getChildren().size());
		
		item = result.get(1).getChildren().get(0).getChildren().get(0);
		assertEquals("myDeepFile.txt", item.getName());
		assertEquals(false, item.isDirectory());
		assertEquals(2, item.getLevel());
		assertEquals(0, item.getChildren().size());
		
		item = result.get(1).getChildren().get(1);
		assertEquals("myFile.txt", item.getName());
		assertEquals(false, item.isDirectory());
		assertEquals(1, item.getLevel());
		assertEquals(0, item.getChildren().size());

		item = result.get(1).getChildren().get(2);
		assertEquals("myOtherFile.txt", item.getName());
		assertEquals(false, item.isDirectory());
		assertEquals(1, item.getLevel());
		assertEquals(0, item.getChildren().size());
		
		item = result.get(2);
		assertEquals("myRootFile.txt", item.getName());
		assertEquals(false, item.isDirectory());
		assertEquals(0, item.getLevel());
		assertEquals(0, item.getChildren().size());
	}
	
	@Test
	public void dirListNoNames_simple() throws Exception{
		StringBuilder sb = new StringBuilder();
		sb.append("<D> ~history\n");
		sb.append("   <F> myFile_123.txt\n");
		sb.append("<D> numberOne\n");
		sb.append("   <D> numberTwo\n");
		sb.append("      <F> myDeepFile.txt\n");
		sb.append("   <F> myFile.txt\n");
		sb.append("   <F> myOtherFile.txt\n");
		sb.append("<F> myRootFile.txt\n");
		
		String result = TestUtil.dirListNoNames(sb);
		
		StringBuilder expected = new StringBuilder();
		expected.append("<D> ???\n");
		expected.append("   <D> ???\n");
		expected.append("      <F> ???\n");
		expected.append("   <F> ???\n");
		expected.append("   <F> ???\n");
		expected.append("<D> ???\n");
		expected.append("   <F> ???\n");
		expected.append("<F> ???\n");

		assertEquals(expected.toString(), result);
	}
	
	@Test
	public void dirListNoNames_empty() throws Exception{
		StringBuilder sb = new StringBuilder();
		
		String result = TestUtil.dirListNoNames(sb);
		
		StringBuilder expected = new StringBuilder();
		assertEquals(expected.toString(), result);
	}
	
	@Test
	public void dirListNoNames_singleItem() throws Exception{
		StringBuilder sb = new StringBuilder();
		sb.append("<D> ~history\n");
		
		String result = TestUtil.dirListNoNames(sb);
		
		StringBuilder expected = new StringBuilder();
		expected.append("<D> ???\n");

		assertEquals(expected.toString(), result);
	}	
	
}
