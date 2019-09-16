package de.bright_side.filesystemfacade_it.nativefs;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import de.bright_side.filesystemfacade.facade.FSFFile;
import de.bright_side.filesystemfacade.facade.FSFSystem;
import de.bright_side.filesystemfacade.facade.TestUtil;
import de.bright_side.filesystemfacade.util.ListDirFormatting;
import de.bright_side.filesystemfacade.util.ListDirFormatting.Style;

/**
 * Class with configuration (which dirs in local file system) as well as utility functions
 * @author Philip Heyse
 *
 */
public class NativeFileTestBase {
	private static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss-SSS");
	private static final int MAXIMUM_NUMBER_OF_FILES_TO_DELETE_IN_TREE = 30;

	protected static final String TIME_001_TEXT = "2019-01-01T00-01-01-001";
	protected static final String TIME_002_TEXT = "2019-02-02T00-02-02-002";
	protected static final String TIME_003_TEXT = "2019-03-03T00-03-03-003";

	protected static final long TIME_001 = createTime(TIME_001_TEXT);
	protected static final long TIME_002 = createTime(TIME_002_TEXT);
	protected static final long TIME_003 = createTime(TIME_003_TEXT);
	private static final boolean LOGGING_ENABELD = false;

	public static ListDirFormatting LIST_DIR_FORMATTING_SIMPLE = createListDirFormattingSimple();
	
	private static long createTime(String text) {
		try {
			return TIMESTAMP_FORMAT.parse(text).getTime();
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}
	
	protected static String readTestRootDir() {
		try {
			FSFFile configFile = TestUtil.getConfigDir().getChild("NativeFileTestRootDir.txt");
			if (!configFile.exists()) {
				String message = "Please add the file '" + configFile.getAbsolutePath() + "' in your local user directory to "
						+ "specify the location where files should be written during the test execution. Example: \"C:\\my_fsf_temp_dir\"";
				System.err.println(message);
				throw new Exception(message);
			}
			return configFile.readString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private static ListDirFormatting createListDirFormattingSimple() {
		ListDirFormatting formatting = new ListDirFormatting();
		formatting.setStyle(Style.TREE);
		formatting.setAllSubItems(true);
		return formatting;
	}
	
	public static String listDir(FSFSystem fs, Class<?> testClass, String testName) throws Exception{
		return getTopDir(fs, testClass, testName).listDirAsString(LIST_DIR_FORMATTING_SIMPLE);
	}

	public static void clearTestDir(FSFSystem fs, Class<?> testClass) throws Exception {
		FSFFile testDir = getTopDir(fs, testClass.getSimpleName(), "x").getParentFile();
		log("parent = >>" + testDir + "<<");
		testDir.deleteTree();
	}
	
	public static void clearTestDir(FSFSystem fs, String testClassName) throws Exception {
		FSFFile testDir = getTopDir(fs, testClassName, "x").getParentFile();
		log("parent = >>" + testDir + "<<");
		deleteTree(testDir);
//		testDir.deleteTree();
	}
	
	private static void log(String message) {
		if (LOGGING_ENABELD) {
			System.out.println("NativeFileTestBase> " + message);
		}
	}

	public static FSFFile getTopDir(FSFSystem fs, Class<?> testClass, String testName) throws Exception{
		return getTopDir(fs, testClass.getSimpleName(), testName);
	}
	
	private static FSFFile getTopDir(FSFSystem fs, String testClassName, String testName) throws Exception{
		FSFFile topDir = fs.createByPath(readTestRootDir()).getChild(testClassName).getChild(testName);
		topDir.mkdirs();
		return topDir;
	}
	

	public static void deleteTree(FSFFile dir) throws Exception {
		if (!dir.exists()) {
			return;
		}
		if (dir.listFilesTree().size() > MAXIMUM_NUMBER_OF_FILES_TO_DELETE_IN_TREE) {
			throw new RuntimeException("Found too many files in dir '" + dir.getAbsolutePath() + "'");
		}
		dir.deleteTree();
	}

}
