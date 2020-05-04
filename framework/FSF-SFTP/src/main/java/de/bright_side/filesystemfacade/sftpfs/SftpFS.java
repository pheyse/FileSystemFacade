package de.bright_side.filesystemfacade.sftpfs;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.provider.ftp.FtpFileSystemConfigBuilder;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;

import de.bright_side.filesystemfacade.facade.FSFEnvironment;
import de.bright_side.filesystemfacade.facade.FSFFile;
import de.bright_side.filesystemfacade.facade.FSFSystem;
import de.bright_side.filesystemfacade.util.FSFFileUtil;
import de.bright_side.filesystemfacade.vfs.VfsFile;
import de.bright_side.filesystemfacade.vfs.VfsFileSystem;

public class SftpFS implements FSFSystem {
    private final FileSystemManager fsManager;
    private String host;
    private int port;
    private String user;
    private String password;

    public enum Mode{VFS}

    private VfsFileSystem vfsFileSystem;

    public SftpFS(FSFEnvironment environment, Mode mode, SftpFSConfig config, String startPath) throws Exception {
        this(environment, mode, config.getHost(), config.getPort(), config.getUser(), config.getPassword(), startPath);
    }

    public SftpFS(Mode mode, SftpFSConfig config, String startPath) throws Exception {
        this(FSFFileUtil.createDefaultEnvironment(), mode, config.getHost(), config.getPort(), config.getUser(), config.getPassword(), startPath);
    }

    public SftpFS(Mode mode, String host, int port, String user, String password, String startPath) throws Exception {
        this(FSFFileUtil.createDefaultEnvironment(), mode, host, port, user, password, startPath);
    }

    public SftpFS(FSFEnvironment environment, Mode mode, String host, int port, String user, String password, String startPath) throws Exception {
        if (mode == null){
            throw new Exception("Mode may not be null");
        }

        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
        this.fsManager = VFS.getManager();

        String useStartPath = startPath;
        if (!useStartPath.startsWith("/")){
            useStartPath = "/" + useStartPath;
        }

        FileObject fileObject = getFileObject(useStartPath);

        vfsFileSystem = new VfsFileSystem(environment, fsManager, Arrays.asList(fileObject));
    }

    private FileObject getFileObject(String path) throws FileSystemException {
        FileSystemOptions fsOptions = new FileSystemOptions();
        FtpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(fsOptions, false);
        SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(fsOptions, "no");
        String uri = "sftp://" + this.user + ":" + this.password + "@" + this.host + ":" + this.port + path;
        log("getFileObject: path = >>" + path + "<<. uri = >>" + uri + "<<");
        FileObject result = fsManager.resolveFile(uri, fsOptions);
        return result;
    }

    private void log(String message) {
        System.out.println("SftpFileSystem> " + message);
    }

    @Override
    public List<FSFFile> listRoots() {
        return vfsFileSystem.listRoots();
    }

    @Override
    public FSFFile createByPath(String path) throws Exception {
        FileObject fileObject = getFileObject(path);
        return new VfsFile(vfsFileSystem, fileObject);
    }

    @Override
    public String getSeparator() {
        return vfsFileSystem.getSeparator();
    }
}
