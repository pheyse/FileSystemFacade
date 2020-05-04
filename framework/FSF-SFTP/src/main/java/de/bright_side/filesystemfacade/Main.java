//package de.bright_side.filesystemfacade;
//
//import de.bright_side.filesystemfacade.facade.FSFFile;
//import de.bright_side.filesystemfacade.facade.FSFSystem;
//import de.bright_side.filesystemfacade.sftpfs.SftpFS;
//import de.bright_side.filesystemfacade.vfs.SftpFileSystemConfig;
//import de.bright_side.filesystemfacade.vfs.VfsMemoryFS;
//import org.apache.commons.io.IOUtils;
//import org.apache.commons.vfs2.*;
//import org.apache.commons.vfs2.provider.ftp.FtpFileSystemConfigBuilder;
//import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;
//
//import java.io.File;
//import java.io.IOException;
//import java.io.OutputStream;
//import java.util.TreeMap;
//
//public class Main {
//    private static final String LOCAL_FILE_PATH = "test-data/file1.txt";
//    private static final String SFTP_HOST = "192.168.178.33";
//    private static final int SFTP_PORT = 22;
//    private static final String SFTP_USER = "dev_test_a";
//    private static final String SFTP_PASSWORD = "j4D5HltSD7chLJbyDAz2";
//    private static final String SFTP_PATH_READ = "/dev_test_a/hello.txt";
//    private static final String SFTP_PATH_WRITE = "/dev_test_a/message.txt";
//    private static final String CHARSET = "UTF-8";
//    private static final String NATIVE_FS_BASE_DIR_PATH = "C:/DA1D/VFSTest";
//
//    private static SftpFileSystemConfig SFTP_CONFIG = createConfig();
//
//    private static SftpFileSystemConfig createConfig() {
//        SftpFileSystemConfig config = new SftpFileSystemConfig();
//        config.setHost(SFTP_HOST);
//        config.setPort(SFTP_PORT);
//        config.setUser(SFTP_USER);
//        config.setPassword(SFTP_PASSWORD);
//        return config;
//    }
//
//    public static void main(String[] args) throws Exception {
//        System.out.println("Performing task...");
////        new Main().performReadLocal(LOCAL_FILE_PATH);
////        new Main().performReadSftp(SFTP_HOST, SFTP_PORT, SFTP_USER, SFTP_PASSWORD, SFTP_PATH_READ);
////        new Main().performWriteSftp(SFTP_HOST, SFTP_PORT, SFTP_USER, SFTP_PASSWORD, SFTP_PATH_WRITE, "nice!");
////        new Main().performReadFsfSftp(SFTP_CONFIG, SFTP_PATH_READ);
//        new Main().performWriteFsfSftp(SFTP_CONFIG, SFTP_PATH_WRITE, "cool");
////        new Main().performReadWriteVFSMemory();
//        new Main().performVFSRenameTestRam();
//        new Main().performVFSRenameTestSftp(SFTP_CONFIG);
//        new Main().performVFSRenameTestNative(NATIVE_FS_BASE_DIR_PATH);
////        new Main().performDeleteTestVFSMemory();
//        System.out.println("done.");
//    }
//
//    private String listTree(FileObject dir, int level) throws FileSystemException {
//        StringBuilder result = new StringBuilder();
//        FileObject[] children = dir.getChildren();
//        StringBuilder indent = new StringBuilder();
//        for (int i = 0; i < level; i++){
//            indent.append("   ");
//        }
//
//        if (children == null){
//            return null;
//        }
//        TreeMap<String, FileObject> sortedChildren = new TreeMap<>();
//        for (FileObject i: children){
//            sortedChildren.put(i.getName().getBaseName(), i);
//        }
//        for (FileObject i: sortedChildren.values()){
//            result.append(indent);
//            result.append("<" + i.getType() + "> ");
//            result.append(i.getName().getBaseName());
//            result.append("\n");
//            if (i.getType() == FileType.FOLDER){
//                result.append(listTree(i, level + 1));
//            }
//        }
//
//        return result.toString();
//    }
//
//    private void performReadLocal(String localFilePath) throws Exception{
//        FileSystemManager fsManager = VFS.getManager();
//        File file = new File(localFilePath);
//        String absolutePath = file.getAbsolutePath();
//        FileObject file1 = fsManager.resolveFile( "file:" + absolutePath);
//        System.out.println("Path = >>" + absolutePath + "<<");
//        FileContent content = file1.getContent();
//        String text = IOUtils.toString(content.getInputStream(), "UTF-8");
//        System.out.println("Text = >>" + text + "<<");
//    }
//
//    private void performReadSftp(String host, int port, String user, String password, String absolutePath) throws Exception{
//        if (!absolutePath.startsWith("/")){
//            throw new Exception("Absolute path must start with '/'");
//        }
//
//        FileSystemOptions fsOptions = new FileSystemOptions();
//        FtpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(fsOptions, false);
//        SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(fsOptions, "no");
//        FileSystemManager fsManager = VFS.getManager();
//        String uri = "sftp://" + user + ":" + password + "@" + host + ":" + port + absolutePath;
//        FileObject file1 = fsManager.resolveFile(uri, fsOptions);
//
//
//        System.out.println("SFTP URI = >>" + uri + "<<");
//        FileContent content = file1.getContent();
//
//        String text = IOUtils.toString(content.getInputStream(), CHARSET);
//        System.out.println("Text = >>" + text + "<<");
//    }
//
//    private void performReadFsfSftp(SftpFileSystemConfig config, String absolutePath) throws Exception{
//        FSFSystem fs = new SftpFS(SftpFS.Mode.VFS, config, absolutePath);
//        FSFFile file = fs.createByPath(absolutePath);
//        String text = file.readString();
//        System.out.println("FSF SFTP: Text = >>" + text + "<<");
//    }
//
//    private void performWriteFsfSftp(SftpFileSystemConfig config, String absolutePath, String text) throws Exception{
//        FSFSystem fs = new SftpFS(SftpFS.Mode.VFS, config, absolutePath);
//        FSFFile file = fs.createByPath(absolutePath);
//        file.writeString(text);
//    }
//
//    private void performDeleteTestVFSMemory() throws Exception{
//        FSFSystem fs = new VfsMemoryFS();
//
//        String baseFilePath = "/hello.txt";
//
//        FSFFile fileWrite = fs.createByPath(baseFilePath);
//        System.out.println("Vfs Memory delete test: exists before write: " + fileWrite.exists());
//        fileWrite.writeString("Nice");
//        System.out.println("Vfs Memory delete test> list root content after write \n" + new VfsMemoryFS().createByPath("/").listDirAsString(null));
//        System.out.println("Vfs Memory delete test: exists after write: " + fileWrite.exists());
//
//        fileWrite.delete();
//        System.out.println("Vfs Memory delete test: exists after delete: " + fileWrite.exists());
//
//        String baseDirPath = "/myDir";
//
//        FSFFile dirWrite = fs.createByPath(baseFilePath);
//        System.out.println("Vfs Memory delete test: DIR exists before write: " + dirWrite.exists());
//        dirWrite.mkdirs();
//        System.out.println("Vfs Memory delete test: DIR exists after write: " + dirWrite.exists());
//
//        dirWrite.delete();
//        System.out.println("Vfs Memory delete test: DIR exists after delete: " + dirWrite.exists());
//
//        System.out.println("Vfs Memory delete test> list root content \n" + new VfsMemoryFS().createByPath("/").listDirAsString(null));
//
//    }
//
//    private void performReadWriteVFSMemory() throws Exception{
//        FSFSystem fs = new VfsMemoryFS();
//        String baseFilePath = "/hello.txt";
//        FSFFile fileWrite = fs.createByPath(baseFilePath);
//        fileWrite.writeString("Nice");
//
//        FSFFile fileRead = fs.createByPath(baseFilePath);
//        String result = fileRead.readString();
//        System.out.println("Vfs Memory: Result = >>" + result + "<<");
//
//        String dirPath = "/myDirOne/myDirTwo";
//        FSFFile dirWrite = fs.createByPath(dirPath);
//
//        System.out.println("Vfs Memory: dir exists before create = >>" + dirWrite.exists() + "<<");
//        dirWrite.mkdirs();
//        System.out.println("Vfs Memory: dir exists after create = >>" + dirWrite.exists() + "<<");
//
//        System.out.println("Vfs Memory: list dirs:\n" + fs.createByPath("/").listDirAsString(null));
//
//
//        String deepFilePath = dirPath + "/myFile.txt";
//        FSFFile deepFileWrite = fs.createByPath(deepFilePath);
//        deepFileWrite.writeString("deep file");
//
//        FSFFile deepFileRead = fs.createByPath(deepFilePath);
//        String deepFileReadString = deepFileRead.readString();
//        System.out.println("Vfs Memory: deepFileReadString = >>" + deepFileReadString + "<<");
//    }
//
//    private void performWriteSftp(String host, int port, String user, String password, String absolutePath, String text) throws Exception {
//        if (!absolutePath.startsWith("/")){
//            throw new Exception("Absolute path must start with '/'");
//        }
//
//        FileSystemOptions fsOptions = new FileSystemOptions();
//        FtpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(fsOptions, false);
//        SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(fsOptions, "no");
//        FileSystemManager fsManager = VFS.getManager();
//        String uri = "sftp://" + user + ":" + password + "@" + host + ":" + port + absolutePath;
//        FileObject file1 = fsManager.resolveFile(uri, fsOptions);
//
//        FileContent content = file1.getContent();
//        OutputStream outputStream = content.getOutputStream();
//        IOUtils.write(text, outputStream, "UTF-8");
//        outputStream.close();
//    }
//
//    private void performVFSRenameTest(String testName, FileObject baseDir) throws FileSystemException {
//        baseDir.createFolder();
//        FileObject dir2 = baseDir.resolveFile("dir2");
//        dir2.createFolder();
//        FileObject testFile = dir2.resolveFile("test.txt");
//        try (OutputStream os = testFile.getContent().getOutputStream()){
//            IOUtils.write("Hello", os, "UTF-8");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        System.out.println(testName + "> before test:>>\n" + listTree(baseDir, 0) + "<<");
//
//        //: action
//        FileObject dir2NewName = baseDir.resolveFile("dir2new");
//        dir2.moveTo(dir2NewName);
//
//        //: check
//        System.out.println(testName + "> after test:>>\n" + listTree(baseDir, 0) + "<<");
//    }
//
//    private void performVFSRenameTestRam() throws FileSystemException {
//        FileSystemManager fsManager = VFS.getManager();
//        FileObject dir1 = fsManager.resolveFile("ram:///dir1");
//        performVFSRenameTest("performVFSRamTest", dir1);
//    }
//
//    private void performVFSRenameTestSftp(SftpFileSystemConfig config) throws FileSystemException {
//        FileSystemOptions fsOptions = new FileSystemOptions();
//        FtpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(fsOptions, false);
//        SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(fsOptions, "no");
//        FileSystemManager fsManager = VFS.getManager();
//        String absolutePath = "/dev_test_a";
//        String uri = "sftp://" + config.getUser() + ":" + config.getPassword() + "@" + config.getHost() + ":" + config.getPort() + absolutePath;
//        FileObject dir1 = fsManager.resolveFile(uri, fsOptions);
//
//        performVFSRenameTest("performVFSSftpTest", dir1);
//    }
//
//    private void performVFSRenameTestNative(String baseDirPath) throws FileSystemException {
//        FileSystemManager fsManager = VFS.getManager();
//        String uri = "file://" + baseDirPath;
//        FileObject dir1 = fsManager.resolveFile(uri);
//
//        performVFSRenameTest("performVFSRenameTestNative", dir1);
//    }
//
//
//
//}
