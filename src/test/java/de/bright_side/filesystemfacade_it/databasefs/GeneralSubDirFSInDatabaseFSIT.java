package de.bright_side.filesystemfacade_it.databasefs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Tag;

import de.bright_side.filesystemfacade.databasefs.DatabaseFS;
import de.bright_side.filesystemfacade.databasefs.DatabaseFSConfig;
import de.bright_side.filesystemfacade.facade.FSFEnvironment;
import de.bright_side.filesystemfacade.facade.FSFSystem;
import de.bright_side.filesystemfacade.facade.GeneralFSTest;
import de.bright_side.filesystemfacade.subdirfs.SubDirFS;

@Tag("IT")
public class GeneralSubDirFSInDatabaseFSIT extends GeneralFSTest {
	private static final String BASE_PATH = "/data/appDir";
	private static final boolean LOGGING_ENABLED = false;
	private long connectionsBeforeTest;
	private long connectionsAfterTest;
	
	@Override
	public FSFSystem createFS(FSFEnvironment environment) throws Exception {
		DatabaseFS innerFS = new DatabaseFS(createConfig(), environment);
		
		innerFS.createByPath(BASE_PATH).mkdirs();
		return new SubDirFS(innerFS, BASE_PATH, environment); 
	}

	@Override
	public String listDir(FSFSystem fs) throws Exception {
		return fs.createByPath("").listDirAsString(LIST_DIR_FORMATTING_SIMPLE);
	}

	private DatabaseFSConfig createConfig() {
		return GeneralDatabaseFSWithCredentialsIT.createConfig();
	}

	@Override
	public void beforeTest() throws Exception {
		connectionsBeforeTest = GeneralDatabaseFSWithCredentialsIT.listOpenConnectionsOfCofingUser(createConfig());
		GeneralDatabaseFSWithCredentialsIT.dropFileTableIfExists(createConfig());
	}

	@Override
	public void afterTest() throws Exception {
		connectionsAfterTest = GeneralDatabaseFSWithCredentialsIT.listOpenConnectionsOfCofingUser(createConfig());
		assertEquals(connectionsBeforeTest, connectionsAfterTest, "open connection remained");
	}

	@Override
	public void beforeClass() throws Exception {
		GeneralDatabaseFSWithCredentialsIT.dropFileTableIfExists(createConfig());
	}

	@Override
	public boolean supportsVersioning() throws Exception {
		return false;
	}

	@Override
	public boolean supportsHistory() throws Exception {
		return false;
	}
	
	@Override
	public boolean supportCopyHistoryFilesTree() {
		return false;
	}

	@Override
	public String listDirInnerFS(FSFSystem fs) throws Exception {
		return ((SubDirFS)fs).getInnerFS().createByPath(BASE_PATH).listDirAsString(LIST_DIR_FORMATTING_SIMPLE);
	}

	@Override
	public void logStatus(String status) throws Exception {
		if (LOGGING_ENABLED) {
			System.out.println("==================== " + getClass().getSimpleName() + "-status> " + status + " ========================================");
		}
	}

	@Override
	protected FSFSystem getInnerFS(FSFSystem fs) {
		return ((SubDirFS)fs).getInnerFS();
	}

	@Override
	public void afterClass() throws Exception {
	}

	@Override
	public boolean hasInnerFS() throws Exception {
		return true;
	}
	
	@Override
	public boolean isInnerFSEncrypted() throws Exception {
		return false;
	}
}
