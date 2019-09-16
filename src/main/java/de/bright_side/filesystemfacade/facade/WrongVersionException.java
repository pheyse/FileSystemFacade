package de.bright_side.filesystemfacade.facade;

/**
 * @author Philip Heyse
 *
 */
public class WrongVersionException extends Exception {
	private static final long serialVersionUID = -7290564309049346359L;

	public WrongVersionException(String message) {
		super(message);
	}


}
