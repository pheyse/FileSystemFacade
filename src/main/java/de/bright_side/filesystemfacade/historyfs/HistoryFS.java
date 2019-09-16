package de.bright_side.filesystemfacade.historyfs;

import java.util.ArrayList;
import java.util.List;

import de.bright_side.filesystemfacade.facade.FSFEnvironment;
import de.bright_side.filesystemfacade.facade.FSFFile;
import de.bright_side.filesystemfacade.facade.FSFSystem;
import de.bright_side.filesystemfacade.util.FSFFileUtil;

/**
 * @author Philip Heyse
 *
 */
public class HistoryFS implements FSFSystem{
	public static final String DEFAULT_HISTORY_DIR_NAME = "~history"; 
	public static final String DEFAULT_VERSION_DIR_NAME = "~version"; 
	public static final int DEFAULT_MAX_NUMBER_OF_HISTORY_FILES = 10; 
	public static final boolean DEFAULT_TRACK_VERSIONS = true; 
	public static final int INFINITE_HISTORY_FILES = -1; 
	public static final int NO_HISTORY_FILES = 0; 

	private FSFSystem innerFS;
	private String historyDirName;
	private String versionDirName;
	private int maxNumberOfHistoryFiles;
	private FSFEnvironment environment;
	private boolean trackVersions;

	public HistoryFS(FSFSystem innerFS, boolean trackVersions) {
		this(innerFS, trackVersions, DEFAULT_MAX_NUMBER_OF_HISTORY_FILES, DEFAULT_VERSION_DIR_NAME, DEFAULT_HISTORY_DIR_NAME, FSFFileUtil.createDefaultEnvironment());
	}
	
	public HistoryFS(FSFSystem innerFS, boolean trackVersions, int numberOfHistoryFiles) {
		this(innerFS, trackVersions, numberOfHistoryFiles, DEFAULT_VERSION_DIR_NAME, DEFAULT_HISTORY_DIR_NAME, FSFFileUtil.createDefaultEnvironment());
	}

	
	/**
	 * 
	 * @param innerFS
	 * @param numberOfHistoryFiles use negative value for infinity, use 0 for no history files
	 * @param trackVersions true creates a version sub-directory which holds the version number in a file of the same name as this file
	 */
	public HistoryFS(FSFSystem innerFS, boolean trackVersions, int numberOfHistoryFiles, String versionDirName, String historyDirName) {
		this(innerFS, trackVersions, numberOfHistoryFiles, versionDirName, historyDirName, FSFFileUtil.createDefaultEnvironment());
	}

	/**
	 * 
	 * @param innerFS
	 * @param numberOfHistoryFiles use negative value for infinity, use 0 for no history files
	 * @param trackVersions true creates a version sub-directory which holds the version number in a file of the same name as this file
	 */
	public HistoryFS(FSFSystem innerFS, boolean trackVersions, int numberOfHistoryFiles, String versionDirName, String historyDirName, FSFEnvironment environment) {
		this.innerFS = innerFS;
		this.maxNumberOfHistoryFiles = numberOfHistoryFiles;
		this.trackVersions = trackVersions;
		this.historyDirName = historyDirName;
		this.versionDirName = versionDirName;
		
		if (this.historyDirName == null) {
			this.historyDirName = DEFAULT_HISTORY_DIR_NAME;
		}
		if (this.versionDirName == null) {
			this.versionDirName = DEFAULT_VERSION_DIR_NAME;
		}
		
		this.environment = environment;
		
		if (maxNumberOfHistoryFiles == 0) {
			this.historyDirName = "";
		}
		if (!trackVersions) {
			this.versionDirName = "";
		}
	}
	
	private boolean historyEnabled() {
		return maxNumberOfHistoryFiles != 0;
	}

	@Override
	public List<FSFFile> listRoots() {
		List<FSFFile> result = new ArrayList<>();
		for (FSFFile i : innerFS.listRoots()) {
			result.add(new HistoryFile(this, i));
		}
		return result;
	}

	@Override
	public FSFFile createByPath(String path) throws Exception {
		String check = "/" + path.replace("\\", "/") + "/";
		if ((historyEnabled()) && (check.contains("/" + historyDirName + "/"))) {
			throw new RuntimeException("cannot create file in HistoryFS which contains a history dir '" + historyDirName + "' as one element. Provided path: '" + path + "'");
		}
		return new HistoryFile(this, innerFS.createByPath(path)); 
	}
	
	public String getHistoryDirName() {
		return historyDirName;
	}
	
	public int getMaxNumberOfHistoryFiles() {
		return maxNumberOfHistoryFiles;
	}
	
	protected FSFEnvironment getEnvironment() {
		return environment;
	}
	
	public FSFSystem getInnerFS() {
		return innerFS;
	}
	
	protected String getVersionDirName() {
		return versionDirName;
	}
	
	protected boolean isTrackVersions() {
		return trackVersions;
	}

	@Override
	public String getSeparator() {
		return innerFS.getSeparator();
	}
}
