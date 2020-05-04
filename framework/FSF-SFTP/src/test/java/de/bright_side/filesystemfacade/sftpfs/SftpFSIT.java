package de.bright_side.filesystemfacade.sftpfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import de.bright_side.filesystemfacade.facade.FSFEnvironment;
import de.bright_side.filesystemfacade.facade.FSFFile;
import de.bright_side.filesystemfacade.facade.FSFSystem;
import de.bright_side.filesystemfacade.facade.TestUtil;
import de.bright_side.filesystemfacade.util.FSFFileUtil;

public class SftpFSIT {
    private static final String CONFIG_KEY_HOST = "sftp.host";
    private static final String CONFIG_KEY_PORT = "sftp.port";
    private static final String CONFIG_KEY_USER = "sftp.user";
    private static final String CONFIG_KEY_PASSWORD = "sftp.password";
    private static final String CONFIG_KEY_TEST_DIR = "sftp.test-dir-that-may-be-overwritten";

    private static final SftpFSConfig SFTP_CONFIG = createConfig();
    private static final String TESTING_BASE_DIR = readTestBaseDir();

    private static String readTestBaseDir() {
        try{
            return TestUtil.getConfigPropertyOrFail(CONFIG_KEY_TEST_DIR);
        } catch (Exception e){
            throw new RuntimeException("Could not read testing configuration", e);
        }
    }

    private static SftpFSConfig createConfig() {
        SftpFSConfig config = new SftpFSConfig();
        try{
            config.setHost(TestUtil.getConfigPropertyOrFail(CONFIG_KEY_HOST));
            config.setPort(Integer.valueOf(TestUtil.getConfigPropertyOrFail(CONFIG_KEY_PORT)));
            config.setUser(TestUtil.getConfigPropertyOrFail(CONFIG_KEY_USER));
            config.setPassword(TestUtil.getConfigPropertyOrFail(CONFIG_KEY_PASSWORD));
            return config;
        } catch (Exception e){
            throw new RuntimeException("Could not read testing configuration", e);
        }
    }

    protected static FSFFile getTestRootDir(String testName) throws Exception {
        FSFEnvironment environment = FSFFileUtil.createDefaultEnvironment();
        FSFSystem fs = new SftpFS(environment, SftpFS.Mode.VFS, SFTP_CONFIG, TESTING_BASE_DIR);
        FSFFile result = fs.createByPath(TESTING_BASE_DIR).mkdirs().getChild("" + System.currentTimeMillis()).getChild(testName);
        if (result.exists()){
            throw new Exception("Dir '" + result.getAbsolutePath() + "' was not expected to exist");
        }
        return result;
    }

    @Test
    public void simple_writeAndReadFile() throws Exception {
        FSFFile dir = getTestRootDir("simple_writeAndReadFile");
        String data = "this is a test";
        String filename = "Hello.txt";
        dir.getChild(filename).writeString(data);

        String result = dir.getChild(filename).readString();

        assertEquals(data, result);
    }

    @Test
    public void simple_writeAndDelete() throws Exception {
        FSFFile dir = getTestRootDir("simple_writeAndDelete");
        String data = "this is a test";
        String filename = "Hello.txt";

        assertEquals(false, dir.getChild(filename).exists());
        dir.getChild(filename).writeString(data);
        assertEquals(true, dir.getChild(filename).exists());
        dir.getChild(filename).delete();
        assertEquals(false, dir.getChild(filename).exists());
    }

    @Test
    public void simple_writeInDir() throws Exception {
        FSFFile dir = getTestRootDir("simple_writeInDir");
        String data = "this is a test";
        String dirName = "dir1";
        String filename = "Hello.txt";

        //: pre-check
        assertEquals(false, dir.getChild(dirName).exists());
        assertEquals(false, dir.getChild(dirName).getChild(filename).exists());

        //: action
        dir.getChild(dirName).mkdirs().getChild(filename).writeString(data);

        //: check
        assertEquals(true, dir.getChild(dirName).exists());
        assertEquals(true, dir.getChild(dirName).getChild(filename).exists());
        String result = dir.getChild(dirName).getChild(filename).readString();
        assertEquals(data, result);
    }

    @Test
    public void renameDir() throws Exception {
        FSFFile dir = getTestRootDir("renameDir");
        String data = "this is a test";
        String oldDirName = "dir1";
        String newDirName = "newDir1";
        String filename = "Hello.txt";

        dir.getChild(oldDirName).mkdirs().getChild(filename).writeString(data);

        //: pre-check
        assertEquals(true, dir.getChild(oldDirName).exists());
        assertEquals(true, dir.getChild(oldDirName).getChild(filename).exists());
        assertEquals(false, dir.getChild(newDirName).exists());
        assertEquals(false, dir.getChild(newDirName).getChild(filename).exists());

        //: action
        dir.getChild(oldDirName).rename(newDirName);

        //: check
        assertEquals(false, dir.getChild(oldDirName).exists());
        assertEquals(false, dir.getChild(oldDirName).getChild(filename).exists());
        assertEquals(true, dir.getChild(newDirName).exists());
        assertEquals(true, dir.getChild(newDirName).getChild(filename).exists());

        String result = dir.getChild(newDirName).getChild(filename).readString();
        assertEquals(data, result);
    }


}
