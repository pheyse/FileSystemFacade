package de.bright_side.filesystemfacade.nativefs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.bright_side.filesystemfacade.facade.FSFFile;
import de.bright_side.filesystemfacade.facade.FSFSystem;

/**
 * @author Philip Heyse
 *
 */
public class NativeFS implements FSFSystem{
	
	@Override
	public List<FSFFile> listRoots() {
		List<FSFFile> result = new ArrayList<FSFFile>();
		for (File i: File.listRoots()){
			result.add(new NativeFile(this, i));
		}
		return result;
	}

	@Override
	public FSFFile createByPath(String path) {
		return new NativeFile(this, new File(path));
	}

	@Override
	public String getSeparator() {
		return File.separator;
	}

}
