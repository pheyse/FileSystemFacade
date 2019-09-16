package de.bright_side.filesystemfacade.remotefs;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Philip Heyse
 *
 */
public interface RemoteFSConnectionProvider {

	InputStream getInputStream() throws Exception;
	OutputStream getOutputStream() throws Exception;
	int compareLocation(RemoteFSConnectionProvider connectionProvider);

	
}
