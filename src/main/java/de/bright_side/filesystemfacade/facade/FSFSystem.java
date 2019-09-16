package de.bright_side.filesystemfacade.facade;

import java.util.List;

/**
 * @author Philip Heyse
 *
 */
public interface FSFSystem {
	List<FSFFile> listRoots();
	FSFFile createByPath(String path) throws Exception;
	String getSeparator();
}
