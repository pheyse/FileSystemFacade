package de.bright_side.filesystemfacade_it.remotefs;


import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.MethodOrderer.Alphanumeric;
import org.junit.jupiter.api.Test;

import de.bright_side.filesystemfacade.databasefs.DatabaseFS;
import de.bright_side.filesystemfacade.facade.FSFFile;
import de.bright_side.filesystemfacade.facade.FSFSystem;
import de.bright_side.filesystemfacade.facade.TestUtil;
import de.bright_side.filesystemfacade.facade.VersionedData;
import de.bright_side.filesystemfacade.facade.WrongVersionException;
import de.bright_side.filesystemfacade.memoryfs.MemoryFS;
import de.bright_side.filesystemfacade.remotefs.RemoteFS;
import de.bright_side.filesystemfacade.remotefs.RemoteFSAuthenticationException;
import de.bright_side.filesystemfacade.util.ListDirFormatting;
import de.bright_side.filesystemfacade.util.ListDirFormatting.Style;

@TestMethodOrder(Alphanumeric.class)
@Tag("IT")
public class RemoteServletIT {
	private static final String SERVLET_URL = readServletURL();
	private static final String APP = "test_app";
	private static final String TENANT = "test_tenant";
	private static final String USER = "test_user";
	private static final String PASSWORD = readPassword();

	private static ListDirFormatting createListDirFormattingSimple() {
		ListDirFormatting formatting = new ListDirFormatting();
		formatting.setStyle(Style.TREE);
		formatting.setAllSubItems(true);
		return formatting;
	}
	
	protected static String readServletURL() {
		//: this file needs to be created manually before the test. The servlet URL is stored externally so it it not uploaded to GitHub. 
		//: An example for a url is "https://my-server.com/remotefs/RemoteFS"

		try {
			FSFFile configFile = TestUtil.getConfigDir().getChild("RemoteServletURL.txt");
			if (!configFile.exists()) {
				String message = "Please add the file '" + configFile.getAbsolutePath() + "' in your local user directory to "
						+ "specify the location of the servlet. Example: \"https://my-server.com/remotefs/RemoteFS\"";
				System.err.println(message);
				throw new Exception(message);
			}
			return configFile.readString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	protected static String readPassword() {
		//: this file needs to be created manually before the test. The password is stored externally so it it not uploaded to GitHub
		
		try {
			FSFFile configFile = TestUtil.getConfigDir().getChild("RemoteServletPassword.txt");
			if (!configFile.exists()) {
				String message = "Please add the file '" + configFile.getAbsolutePath() + "' in your local user directory to "
						+ "specify the password to access the servlet.";
				System.err.println(message);
				throw new Exception(message);
			}
			
			return configFile.readString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/*
	 * INSERT INTO `ACCESS_RIGHTS`(`APP`, `TENANT`, `USERNAME`, `PASSWORD`) VALUES ('test_app','test_tennant','test_user',<password>)
	 */
	
	protected static FSFSystem createRemoteFS() throws Exception {
		return new RemoteFS(DatabaseFS.SEPARATOR, APP, TENANT, USER, PASSWORD, SERVLET_URL);
	}
	
	private void prepare() throws Exception {
		FSFSystem fs = createRemoteFS();
		fs.createByPath("/test_data").deleteTree();
		fs.createByPath("/test_encrypted").deleteTree();
		FSFFile dir = fs.createByPath("/test_data");
		dir.mkdirs();
//		fs.createByPath("/test.txt").delete();
		fs.createByPath("/test_data/hello.txt").writeString("Hello!").writeString("Hello2!");
		fs.createByPath("/test_data/nice.txt").writeString("Nice").writeString("Nice2");
	}
	
	@Test
	public void readString_normal() throws Exception {
		prepare();
		FSFSystem fs = createRemoteFS();
		
		assertEquals("Hello2!", fs.createByPath("/test_data/hello.txt").readString());
		assertEquals("Nice2", fs.createByPath("/test_data/nice.txt").readString());
	}
	
	@Test
	public void readStringAndVersion_normal() throws Exception {
		prepare();
		FSFSystem fs = createRemoteFS();
		
		VersionedData<String> result = fs.createByPath("/test_data/hello.txt").readStringAndVersion();
		
		assertEquals(2, result.getVersion());
		assertEquals("Hello2!", result.getData());

		assertEquals(2, fs.createByPath("/test_data/hello.txt").getVersion());
	}
	
	@Test
	public void writeStringForVersion_normal() throws Exception {
		prepare();
		FSFSystem fs = createRemoteFS();
		
		FSFFile file = fs.createByPath("/test_data/hello.txt");
		file.writeStringForVersion("last", 3);
		
		VersionedData<String> result = fs.createByPath("/test_data/hello.txt").readStringAndVersion();
		assertEquals(3, result.getVersion());
		assertEquals("last", result.getData());
	}
	
	@Test
	public void writeStringForVersion_failBecauseWrongVersion() throws Exception {
		prepare();
		FSFSystem fs = createRemoteFS();
		
		FSFFile file = fs.createByPath("/test_data/hello.txt");
		Exception exception = null;
		try {
			file.writeStringForVersion("last", 2);
		} catch (Exception e) {
			exception = e;
		}
		assertNotNull(exception, "Exception with wrong version");
		assertEquals(WrongVersionException.class.getName(), exception.getClass().getName());
	}
	
	@Test
	public void writeObjectForVersion_normal() throws Exception {
		prepare();
		FSFSystem fs = createRemoteFS();
		
		FSFFile file = fs.createByPath("/test_data/hello.txt");
		file.writeObjectForVersion("last", 3);
		
		VersionedData<String> result = fs.createByPath("/test_data/hello.txt").readObjectAndVersion(String.class);
		assertEquals(3, result.getVersion());
		assertEquals("last", result.getData());
	}
	
	@Test
	public void writeObjectForVersion_failBecauseWrongVersion() throws Exception {
		prepare();
		FSFSystem fs = createRemoteFS();
		
		FSFFile file = fs.createByPath("/test_data/hello.txt");
		Exception exception = null;
		try {
			file.writeObjectForVersion("last", 2);
		} catch (Exception e) {
			exception = e;
		}
		assertNotNull(exception, "Exception with wrong version");
		assertEquals(WrongVersionException.class.getName(), exception.getClass().getName());
	}
	
	@Test
	public void deltree_normal() throws Exception {
		prepare();
		FSFSystem fs = createRemoteFS();
		fs.createByPath("/test_data").deleteTree();
		assertEquals(false, fs.createByPath("/test_data").exists());
		assertEquals(false, fs.createByPath("/test_data/hello.txt").exists());
	}
	
	@Test
	public void failIfPasswordWrong() throws Exception {
		Exception exception = null;
		try {
			FSFSystem fs = new RemoteFS(DatabaseFS.SEPARATOR, APP, TENANT, USER, PASSWORD + "wrong", SERVLET_URL);
			fs.createByPath("/hello.txt").readString();
		} catch (Exception e) {
			exception = e;
		}
		assertNotNull(exception, "Exception with wrong password was expected");
		assertEquals(RemoteFSAuthenticationException.class.getName(), exception.getClass().getName());
	}
	

	@Test
	public void copyTo_simple() throws Exception {
		prepare();
		FSFSystem fs = createRemoteFS();
		
		StringBuilder sb = new StringBuilder();
		sb.append("<D> test_data\n");
		sb.append("   <F> hello.txt\n");
		sb.append("   <F> nice.txt\n");
		String result = fs.createByPath("/").listDirAsString(createListDirFormattingSimple());
		assertEquals(sb.toString(), result);
		
		
		FSFSystem memoryFS = new MemoryFS();
		FSFFile outputDir = memoryFS.createByPath("/output").mkdirs();
		fs.createByPath("/test_data").copyFilesTree(outputDir);
		
		result = memoryFS.createByPath("/").listDirAsString(createListDirFormattingSimple());
		sb = new StringBuilder();
		sb.append("<D> output\n");
		sb.append("   <F> hello.txt\n");
		sb.append("   <F> nice.txt\n");
		assertEquals(sb.toString(), result);

		assertEquals(memoryFS.createByPath("/output/hello.txt").readString(), "Hello2!");
		assertEquals(memoryFS.createByPath("/output/nice.txt").readString(), "Nice2");
	}
	
	
	
}
