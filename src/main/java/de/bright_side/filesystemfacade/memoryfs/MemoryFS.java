package de.bright_side.filesystemfacade.memoryfs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import de.bright_side.filesystemfacade.facade.FSFEnvironment;
import de.bright_side.filesystemfacade.facade.FSFFile;
import de.bright_side.filesystemfacade.facade.FSFSystem;
import de.bright_side.filesystemfacade.util.FSFFileUtil;

/**
 * @author Philip Heyse
 *
 */
public class MemoryFS implements FSFSystem{
	protected static final String SEPARATOR = "/";
	
	/** if an entry is a dir, it at least contains the key with an empty list. The list items are full paths (and not just names)*/
	private Map<String, SortedSet<String>> pathToChildrenMap = new TreeMap<>();
	private Map<String, MemoryFSItem> pathToItemsMap = new TreeMap<>();
	
	private FSFEnvironment environment;
	
	public MemoryFS(FSFEnvironment environment) {
		this.environment = environment;
		pathToItemsMap.put("", new MemoryFSItem(this, true, 0L, 0L));
		pathToChildrenMap.put("", new TreeSet<String>());
	}
	
	public MemoryFS(){
		this(FSFFileUtil.createDefaultEnvironment());
	}
	
	@Override
	public List<FSFFile> listRoots() {
		return Collections.singletonList((FSFFile)pathToItemsMap.get(""));
	}

	@Override
	public FSFFile createByPath(String path) {
		return new MemoryFile(this, MemoryFSUtil.normalize(path));
	}

	
	protected MemoryFSItem getItem(String path){
		return pathToItemsMap.get(path);
	}

	protected List<MemoryFSItem> getChildItemsOrEmpty(String path){
		List<MemoryFSItem> result = new ArrayList<>();
		Set<String> children = pathToChildrenMap.get(path);
		if (children == null){
			return result;
		}
		
		for (String i: children){
			result.add(pathToItemsMap.get(i));
		}
		
		return result;
	}
	
	public void setExistenceInParentDir(String path, boolean exists) {
		String parentPath = MemoryFSUtil.getParentPath(path);
		if (!exists){
			if (pathToChildrenMap.containsKey(parentPath)){
				pathToChildrenMap.get(parentPath).remove(path);
			}
		} else {
			if (!pathToChildrenMap.containsKey(parentPath)){
				pathToChildrenMap.put(parentPath, new TreeSet<String>());
			}
			pathToChildrenMap.get(parentPath).add(path);
		}
	}


	protected List<String> getChildPathsOrEmpty(String path){
		Set<String> children = pathToChildrenMap.get(path);
		if (children == null){
			return new ArrayList<>();
		}
		return new ArrayList<String>(children);
	}

	public void setItem(String path, MemoryFSItem item) {
		pathToItemsMap.put(path, item);
		if (item.isDir()){ 
			if (!pathToChildrenMap.containsKey(path)){
				pathToChildrenMap.put(path, new TreeSet<String>());
			}
		} else {
			pathToChildrenMap.remove(path);
		}
		
	}

	public void removeItem(String path) {
		pathToItemsMap.remove(path);
		pathToChildrenMap.remove(path);
	}

	public void setEnvironment(FSFEnvironment environment) {
		this.environment = environment;
	}

	protected FSFEnvironment getEnvironment() {
		return environment;
	}

	@Override
	public String getSeparator() {
		return SEPARATOR;
	}
	
	
}
