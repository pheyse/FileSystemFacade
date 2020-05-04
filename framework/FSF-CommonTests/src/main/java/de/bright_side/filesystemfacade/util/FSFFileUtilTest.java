package de.bright_side.filesystemfacade.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class FSFFileUtilTest {
	@Test
	public void removeIfStartsWith_simple() {
		String input = "\\hello";
		String result = FSFFileUtil.removeIfStartsWith(input, "\\");
		assertEquals("hello", result);
	}

	@Test
	public void removeIfStartsWith_differentStart() {
		String input = "!hello";
		String result = FSFFileUtil.removeIfStartsWith(input, "\\");
		assertEquals("!hello", result);
	}

	@Test
	public void removeIfEndsWith_simple() {
		String input = "hello\\";
		String result = FSFFileUtil.removeIfEndsWith(input, "\\");
		assertEquals("hello", result);
	}
	
	@Test
	public void removeIfEndsWith_differentEnd() {
		String input = "hello!";
		String result = FSFFileUtil.removeIfStartsWith(input, "\\");
		assertEquals("hello!", result);
	}
}
