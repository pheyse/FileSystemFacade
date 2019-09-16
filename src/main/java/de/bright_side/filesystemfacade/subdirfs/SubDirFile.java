package de.bright_side.filesystemfacade.subdirfs;

import de.bright_side.filesystemfacade.facade.FSFFile;
import de.bright_side.filesystemfacade.facade.FSFFileWithInnerFile;
import de.bright_side.filesystemfacade.facade.FSFSystem;
import de.bright_side.filesystemfacade.facade.IllegalPathItemNameException;
import de.bright_side.filesystemfacade.util.FSFFileUtil;

public class SubDirFile extends FSFFileWithInnerFile{
	private SubDirFS fs;

	protected SubDirFile(SubDirFS fs, FSFFile innerFile) {
		super(innerFile);
		this.fs = fs;
		if (!fs.isInnerFileInBasePath(innerFile)) {
			throw new RuntimeException("Inner file '" + innerFile.getAbsolutePath() + "' is outside of the sub directory '" + fs.getBasePath() + "'");
		}
	}
	
	@Override
	public FSFFile getParentFile() {
		FSFFile innerParentFile = getInnerFile().getParentFile();
		if (innerParentFile == null) {
			return null;
		}
		if (!fs.isInnerFileInBasePath(innerParentFile)) {
			return null;
		}
		
		return new SubDirFile(fs, innerParentFile);
	}
	
	@Override
	protected FSFFile wrap(FSFFile innerFile) {
		return new SubDirFile(fs, innerFile);
	}

	@Override
	public String getAbsolutePath() {
		String result = super.getAbsolutePath();
		String innerFSSeparator = getInnerFile().getFSFSystem().getSeparator();
		String basePath = fs.getBasePath();
		result = FSFFileUtil.removeIfEndsWith(result, innerFSSeparator);
		if (result.length() <= basePath.length()) {
			return "";
		}
		result = SubDirFS.SEPARATOR + result.substring(basePath.length());
		return result;
	}

	@Override
	public FSFSystem getFSFSystem() {
		return fs;
	}

	@Override
	public String getName() {
		if (getAbsolutePath().isEmpty()) {
			return "";
		}
		return super.getName();
	}
	
	private boolean isNameOk(String name) {
		String checkName = name.replace(fs.getInternalFSSeparator(), SubDirFS.SEPARATOR);
		
		//: no separator of internal or SubDirFS:
		if (checkName.contains(SubDirFS.SEPARATOR)){
			return false;
		}
		//: no /../
		if ((new String(SubDirFS.SEPARATOR + checkName + SubDirFS.SEPARATOR).contains(SubDirFS.SEPARATOR + ".." + SubDirFS.SEPARATOR))) {
			return false;
		}
		
		return true;
	}
	
	@Override
	public FSFFile getChild(String name) {
		if (!isNameOk(name)) {
			return null;
		}
		
		return super.getChild(name);
	}
	
	@Override
	public String toString() {
		String result = "SubDirFile{name='" + getName() + "', innerFile=";
		if (getInnerFile() != null) {
			result += getInnerFile();
		}
		result += "}";
		return result;
	}

	@Override
	public void rename(String newName) throws Exception {
		if (!isNameOk(newName)) {
			throw new IllegalPathItemNameException("New name '" + newName + "' contains illegal char sequence");
		}
		super.rename(newName);
	}
	
}
