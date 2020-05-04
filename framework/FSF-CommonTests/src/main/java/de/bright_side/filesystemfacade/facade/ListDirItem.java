package de.bright_side.filesystemfacade.facade;

import java.util.List;

public class ListDirItem {
	private String name;
	private int level;
	private boolean directory;
	private List<ListDirItem> children;
	private String sortString;
	
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public boolean isDirectory() {
		return directory;
	}
	public void setDirectory(boolean directory) {
		this.directory = directory;
	}
	public List<ListDirItem> getChildren() {
		return children;
	}
	public void setChildren(List<ListDirItem> children) {
		this.children = children;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSortString() {
		return sortString;
	}
	public void setSortString(String sortString) {
		this.sortString = sortString;
	}

	
	
}
