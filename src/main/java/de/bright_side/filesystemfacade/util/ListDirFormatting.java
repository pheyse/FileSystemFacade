package de.bright_side.filesystemfacade.util;

/**
 * @author Philip Heyse
 *
 */
public class ListDirFormatting {
	public enum Style{TREE, FULL_PATH} 
	
	private boolean includeTime;
	private boolean includeSize;
	private boolean allSubItems;
	private Style style;
	
	public boolean isIncludeTime() {
		return includeTime;
	}
	public ListDirFormatting setIncludeTime(boolean includeTime) {
		this.includeTime = includeTime;
		return this;
	}
	public boolean isIncludeSize() {
		return includeSize;
	}
	public ListDirFormatting setIncludeSize(boolean includeSize) {
		this.includeSize = includeSize;
		return this;
	}
	public boolean isAllSubItems() {
		return allSubItems;
	}
	public ListDirFormatting setAllSubItems(boolean allSubItems) {
		this.allSubItems = allSubItems;
		return this;
	}
	public Style getStyle() {
		return style;
	}
	public ListDirFormatting setStyle(Style style) {
		this.style = style;
		return this;
	}

}
