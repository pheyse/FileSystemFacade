package de.bright_side.filesystemfacade.facade;

import java.io.File;

import de.bright_side.filesystemfacade.nativefs.NativeFS;

public class TestConfigUtil {
	public static FSFFile getConfigDir() throws Exception {
		return new NativeFS().createByPath(new File(System.getProperty("user.home")).getAbsolutePath()).getChild(".FileSystemFacade_test_config");
	}

}
