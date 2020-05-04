package de.bright_side.filesystemfacade.remotefs;

import de.bright_side.filesystemfacade.facade.FSFSystem;

/**
 * @author Philip Heyse
 *
 */
public interface RemoteFSFSystemProvider {
	FSFSystem getFSFFystem(String app, String tenant, String username, String password) throws RemoteFSAuthenticationException, Exception;

}
