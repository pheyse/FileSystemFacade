package de.bright_side.filesystemfacade.util;

import de.bright_side.filesystemfacade.facade.FSFEnvironment;

/**
 * @author Philip Heyse
 *
 */
public class SimpleFSFEnvironment implements FSFEnvironment{
	private long currentTime;

	public SimpleFSFEnvironment(long currentTime) {
		this.currentTime = currentTime;
	}
	
	@Override
	public long getCurrentTimeMillis() {
		return currentTime;
	}
	
	public void setCurrentTime(long currentTime) {
		this.currentTime = currentTime;
	}

}
