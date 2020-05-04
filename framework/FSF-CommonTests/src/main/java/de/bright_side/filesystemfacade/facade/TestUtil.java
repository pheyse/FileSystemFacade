package de.bright_side.filesystemfacade.facade;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import de.bright_side.filesystemfacade.nativefs.NativeFS;

public class TestUtil {
	private static final int SPACES_PER_LEVEL = 3;
	private static final String TYPE_INDICATOR_START = "<";
	private static final String TYPE_INDICATOR_END = "> ";
	private static final char TYPE_DIRECTORY = 'D';
	private static final char TYPE_FILE = 'F';

	public static FSFFile getConfigDir() throws Exception {
		return new NativeFS().createByPath(new File(System.getProperty("user.home")).getAbsolutePath()).getChild(".FileSystemFacade_test_config");
	}
	
	public static FSFFile getConfigPropertiesFile() throws Exception {
		return getConfigDir().getChild("fsf-test-config.properties");
	}
	
	public static Properties getConfigProperties() throws Exception {
		Properties result = new Properties();
		FSFFile file = getConfigPropertiesFile();
		if (!file.exists()) {
			return null;
		}
		try(InputStream inputStream = file.getInputStream()){
			result.load(inputStream);
		} catch (Exception e) {
			throw e;
		}
		return result;
	}
	
	public static String getConfigPropertyOrFail(String key) throws Exception {
		Properties properties = getConfigProperties();
		if (properties == null) {
			throw new Exception("Please provide FSF testing configuration in the properties file '" + getConfigPropertiesFile().getAbsolutePath() + "'");
		}
		String result = properties.getProperty(key);
		if (result == null) {
			throw new Exception("Could not find property '" + key + "' in properties file '" + getConfigPropertiesFile().getAbsolutePath() + "'");
		}
		
		return result;
	}
	
	public static List<ListDirItem> parseListDirItems(StringBuilder sb) throws Exception {
		List<ListDirItem> result = new ArrayList<ListDirItem>();
		if (sb.length() == 0) {
			return result;
		}
		
		List<String> rows = Arrays.asList(sb.toString().split("\n"));
		Map<Integer, ListDirItem> lastItemOnLevel = new TreeMap<Integer, ListDirItem>();
		for (String row : rows) {
			ListDirItem item = readItemWithoutChildren(row);
			lastItemOnLevel.put(item.getLevel(), item);
			if (item.getLevel() > 0) {
				ListDirItem parentItem = lastItemOnLevel.get(item.getLevel() - 1);
				parentItem.getChildren().add(item);
			} else {
				result.add(item);
			}
		}
		
		return result;
	}
	
	public static String dirListNoNames(StringBuilder sb) throws Exception {
		StringBuilder result = new StringBuilder();
		List<ListDirItem> items = parseListDirItems(sb);

		for (ListDirItem i: items) {
			i.setSortString(toSortString(i));
		}
		Collections.sort(items, new Comparator<ListDirItem>() {
			@Override
			public int compare(ListDirItem o1, ListDirItem o2) {
				return o1.getSortString().compareTo(o2.getSortString());
			}
		});
		for (ListDirItem i: items) {
			sortChildren(i);
		}
		
		for (ListDirItem i: items) {
			result.append(toOutputString(i));
		}
		return result.toString();
	}
	
	private static String toOutputString(ListDirItem item) {
		String result = "";
		String typeString = "<D>";
		if (!item.isDirectory()) {
			typeString = "<F>";
		}
		for (int i = 0; i < item.getLevel(); i++) {
			result += "   ";
		}
		result += typeString + " ???\n";
		
		for (ListDirItem i: item.getChildren()) {
			result += toOutputString(i);
		}
		
		return result;
	}

	private static void sortChildren(ListDirItem item) {
		for (ListDirItem i: item.getChildren()) {
			i.setSortString(toSortString(i));
			sortChildren(i);
		}
		Collections.sort(item.getChildren(), new Comparator<ListDirItem>() {
			@Override
			public int compare(ListDirItem o1, ListDirItem o2) {
				return o1.getSortString().compareTo(o2.getSortString());
			}
		});
	}

	public static String toSortString(ListDirItem item) {
		String typeString = "DIR";
		if (!item.isDirectory()) {
			typeString = "FILE";
		}
		String childrenInfo = "";
		if (!item.getChildren().isEmpty()){
			childrenInfo = ", children: ";
			boolean first = false;
			
			List<String> childrenInfos = new ArrayList<>();
			for (ListDirItem i: item.getChildren()) {
				childrenInfos.add(toSortString(i));
			}
			Collections.sort(childrenInfos);
			for (String i: childrenInfos) {
				if (first) {
					first = false;
				} else {
					childrenInfo += ", "; 
				}
				childrenInfo += i;
			}
		}
		return typeString + "{level=" + item.getLevel() + childrenInfo + "}";
	}
	

	private static ListDirItem readItemWithoutChildren(String row) throws Exception{
		ListDirItem result = new ListDirItem();
		int leadingSpaces = countLeadingSpaces(row); 
		result.setLevel(leadingSpaces / SPACES_PER_LEVEL);
		result.setChildren(new ArrayList<ListDirItem>());
		String restRow = row.substring(leadingSpaces);
		if (!restRow.startsWith(TYPE_INDICATOR_START)) {
			throw new Exception("No type indicator found in row >>" + row + "<<");
		}
		restRow = restRow.substring(TYPE_INDICATOR_START.length());
		if (restRow.charAt(0) == TYPE_DIRECTORY) {
			result.setDirectory(true);
		} else if (restRow.charAt(0) == TYPE_FILE) {
			result.setDirectory(false);
		} else {
			throw new Exception("No type char found in row >>" + row + "<<");
		}
		restRow = restRow.substring(1);
		if (!restRow.startsWith(TYPE_INDICATOR_END)) {
			throw new Exception("No type indicator end found in row >>" + row + "<<");
		}
		restRow = restRow.substring(TYPE_INDICATOR_END.length());
		result.setName(restRow);
		
		return result;
	}

	private static int countLeadingSpaces(String row) {
		int pos = 0;
		while (row.charAt(pos) == ' ') {
			pos ++;
		}
		
		return pos;
	}

	public static String dirListNoNames(String string) throws Exception {
		return dirListNoNames(new StringBuilder(string));
	}
}
