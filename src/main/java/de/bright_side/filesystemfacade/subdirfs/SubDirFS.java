package de.bright_side.filesystemfacade.subdirfs;

import java.util.ArrayList;
import java.util.List;

import de.bright_side.filesystemfacade.facade.FSFEnvironment;
import de.bright_side.filesystemfacade.facade.FSFFile;
import de.bright_side.filesystemfacade.facade.FSFSystem;
import de.bright_side.filesystemfacade.facade.IllegalPathItemNameException;
import de.bright_side.filesystemfacade.util.FSFFileUtil;

/**
 * A file system which resides inside a sub-directory within another file system such as NativeFS ("normal" file system on the disk), 
 * DataBaseFS, on a data base, MemoryFS in Memory only (e.g. for unit tests), etc.
 * 
 * The SubDirFS makes sure all file system actions are "locked in" the provided sub-directory. So that in case of an application error or an attack
 * No harm is done outside the provided sub-directory.  
 * 
 * <code>
 *		FSFSystem fs = SubDirFS(new NativeFS(), "/home/me/my_applicaion_dir");
 *		fs.createByPath("/myFile.txt").writeString("My text");
 *      String readText = fs.createByPath("/myFile.txt").readString();
 * </code>
 * 
 * @author Philip Heyse
 *
 */
public class SubDirFS implements FSFSystem{
	protected static final String SEPARATOR = "/";
	private String basePath;
	private FSFFile baseDir;
	private FSFSystem innerFS;
	private String internalFSSeparator;

	/**
	 * 
	 * @param innerFS the file system on which the SubDirFS is based
	 * @param basePath the path where the temp file system is located. The base path may not exist yet
	 * @throws Exception on general error
	 */
	public SubDirFS(FSFSystem innerFS, String basePath) throws Exception {
		this(innerFS, basePath, FSFFileUtil.createDefaultEnvironment());
	}

	
	/**
	 * 
	 * @param innerFS the file system on which the SubDirFS is based
	 * @param basePath the path where the temp file system is located. The base path may not exist yet
	 * @param environment environment object to e.g. get the current time 
	 * @throws Exception on general error
	 */
	public SubDirFS(FSFSystem innerFS, String basePath, FSFEnvironment environment) throws Exception {
		this.innerFS = innerFS;
		this.basePath = FSFFileUtil.removeIfEndsWith(basePath, innerFS.getSeparator()) + innerFS.getSeparator(); //: let path end with separator (but of course only once)
		baseDir = innerFS.createByPath(this.basePath);
		internalFSSeparator = baseDir.getFSFSystem().getSeparator();
		if (baseDir.getAbsolutePath().replace(innerFS.getSeparator(), "").isEmpty()){
			throw new IllegalPathItemNameException("The base dir '" + this.basePath + "' may not be the root ('/') of the file system");
		}
		if (baseDir.getParentFile() == null) {
			throw new IllegalPathItemNameException("The base dir '" + this.basePath + "' must have a parent directory in the file system");
		}
		
		if (!baseDir.exists()){
			throw new IllegalPathItemNameException("Provided base path '" + basePath + "' does not exist in inner file system");
		} 
	}
	
	@Override
	public List<FSFFile> listRoots() {
		List<FSFFile> result = new ArrayList<>();
		result.add(new SubDirFile(this, baseDir));
		return result;
	}

	@Override
	public FSFFile createByPath(String path) throws Exception {
		if ((path.isEmpty()) || (path.equals(SEPARATOR))) {
			return new SubDirFile(this, baseDir);
		}
		
		if (!path.startsWith(SEPARATOR)) {
			throw new IllegalPathItemNameException("Path must start with '" + SEPARATOR + "', but was '" + path + "'");
		}
		
		if (new String(SEPARATOR + path).contains(SEPARATOR + ".." + SEPARATOR)){
			throw new IllegalPathItemNameException("Path may not contain dir-up-sequence " + SEPARATOR + "'..'" + SEPARATOR);
		}
		
		String internalPath = path.substring(SEPARATOR.length());
		internalPath = FSFFileUtil.removeIfEndsWith(internalPath, SEPARATOR);
		
		if (!internalFSSeparator.equals(SEPARATOR)){
			if (path.contains(internalFSSeparator)) {
				throw new IllegalPathItemNameException("createByPath method uses '" + SEPARATOR + "' as the path separator and not '" + internalFSSeparator);
			}
		}
		
		String[] items = internalPath.split(SEPARATOR);
		FSFFile innerFile = baseDir;
		for (String filename: items) {
			innerFile = innerFile.getChild(filename); 
		}
		
		return new SubDirFile(this, innerFile);
	}

	@Override
	public String getSeparator() {
		return SEPARATOR;
	}
	
	protected boolean isInnerFileInBasePath(FSFFile file) {
		String usePath = file.getAbsolutePath() + innerFS.getSeparator();
		return usePath.startsWith(basePath);
	}
	
	protected String getBasePath() {
		return basePath;
	}

	public FSFSystem getInnerFS() {
		return innerFS;
	}

	protected String getInternalFSSeparator() {
		return internalFSSeparator;
	}
}
