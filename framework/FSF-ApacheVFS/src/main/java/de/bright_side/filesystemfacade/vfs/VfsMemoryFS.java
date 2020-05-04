package de.bright_side.filesystemfacade.vfs;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;

import de.bright_side.filesystemfacade.facade.FSFEnvironment;
import de.bright_side.filesystemfacade.facade.FSFFile;
import de.bright_side.filesystemfacade.facade.FSFSystem;
import de.bright_side.filesystemfacade.util.FSFFileUtil;

public class VfsMemoryFS implements FSFSystem {
    private final FileSystemManager fsManager;

    private VfsFileSystem vfsFileSystem;

    public VfsMemoryFS() throws Exception {
        this(FSFFileUtil.createDefaultEnvironment());
    }

    public VfsMemoryFS(FSFEnvironment environment) throws Exception {
        this.fsManager = VFS.getManager();
        FileObject fileObject = getFileObject("");
        vfsFileSystem = new VfsFileSystem(environment, fsManager, Arrays.asList(fileObject));
        vfsFileSystem.setHasInternalCommonsVfsBugThatDeletesSubItemsOnMoveOrRename(true);
    }

    private FileObject getFileObject(String path) throws FileSystemException {
        String usePath = path;
        if (!usePath.startsWith("/")){
            usePath = "/" + usePath;
        }
        String uri = "ram:" + usePath;
        log("getFileObject: path = >>" + path + "<<. uri = >>" + uri + "<<");
        FileObject result = fsManager.resolveFile(uri);
        return result;
    }

    private void log(String message) {
        System.out.println("VfsMemoryFS> " + message);
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
