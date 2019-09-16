package de.bright_side.filesystemfacade.memoryfs;

import java.util.Arrays;

import de.bright_side.filesystemfacade.util.FSFFileUtil;

/**
 * @author Philip Heyse
 *
 */
public class MemoryFSUtil {
	public static <K> K readObject(MemoryFSItem item, Class<K> classType) throws Exception{
		if (item.getDataAsBytes() != null){
			return FSFFileUtil.objectFromByteArray(item.getDataAsBytes(), classType);
		}
		throw new Exception("item contains no data");
	}

	public static MemoryFSItem copy(MemoryFSItem item) throws Exception {
		MemoryFSItem result = new MemoryFSItem(item.getMemoryFS(), item.isDir(), item.getTimeLastModified(), item.getTimeCreated());
		if (item.getDataAsBytes() != null){
			result.setDataAsBytes(Arrays.copyOf(item.getDataAsBytes(), item.getDataAsBytes().length));
		}
		return result;
	}

	public static String getParentPath(String path){
		int pos = path.lastIndexOf(MemoryFS.SEPARATOR);
		if (pos < 0){
			return null;
		}
		return path.substring(0, pos);
	}

	public static String normalize(String path) {
		String result = path;
		if (result.endsWith(MemoryFS.SEPARATOR)){
			result = result.substring(0, result.length() - 1);
		}
		result = result.replace("\\", MemoryFS.SEPARATOR);
		return result;
	}


}
