package de.bright_side.filesystemfacade.databasefs;

import java.sql.ResultSet;

/**
 * @author Philip Heyse
 *
 */
public interface ResultSetProcessor {
	void process(ResultSet resultSet) throws Exception;
}
