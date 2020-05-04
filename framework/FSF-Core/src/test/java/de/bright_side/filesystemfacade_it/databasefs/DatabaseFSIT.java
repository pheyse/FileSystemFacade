package de.bright_side.filesystemfacade_it.databasefs;

import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.Alphanumeric;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import de.bright_side.filesystemfacade.databasefs.DatabaseFS;
import de.bright_side.filesystemfacade.facade.FSFEnvironment;
import de.bright_side.filesystemfacade.facade.FSFFile;
import de.bright_side.filesystemfacade.util.SimpleFSFEnvironment;


@TestMethodOrder(Alphanumeric.class)
@Tag("IT")
public class DatabaseFSIT {
	
	public DatabaseFS createFS(FSFEnvironment environment) throws Exception {
		return new DatabaseFS(DatabaseFSUtilIT.createConfig(), environment);
	}

	@BeforeEach
	public void beforeEveryTest() throws Exception {
		DatabaseFSUtilIT.dropFileTableIfExists(DatabaseFSUtilIT.createConfig());
	}
	
	@Test
	public void getParent_root() throws Exception{
		SimpleFSFEnvironment env = new SimpleFSFEnvironment(0);
		DatabaseFS fs = createFS(env);
		
		FSFFile root = fs.createByPath("");
		fs.createByPath("/test.txt").writeString("Hello!");
		fs.createByPath("/test2.txt").writeString("Hello 2!");
		fs.createByPath("/dir1").mkdirs().getChild("SubFile.txt").writeString("nice");
		
		assertNull(root.getParentFile(), "Root may not have parent files");
	}
}
