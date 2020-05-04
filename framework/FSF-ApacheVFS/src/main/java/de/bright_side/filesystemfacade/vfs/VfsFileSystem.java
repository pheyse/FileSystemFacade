package de.bright_side.filesystemfacade.vfs;

import de.bright_side.filesystemfacade.facade.FSFEnvironment;
import de.bright_side.filesystemfacade.facade.FSFFile;
import de.bright_side.filesystemfacade.facade.FSFSystem;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;

import java.util.ArrayList;
import java.util.List;

public class VfsFileSystem implements FSFSystem {
    private FSFEnvironment environment;
    private final FileSystemManager fsManager;
//    private final FileObject fileObject;
    private List<FileObject> roots;
    private boolean hasInternalCommonsVfsBugThatDeletesSubItemsOnMoveOrRename = false;

//    public VfsFileSystem(String startPath, List<FileObject> roots) throws FileSystemException {
//        this.roots = roots;
//        fsManager = VFS.getManager();
////        fileObject = fsManager.resolveFile(startPath);
//    }

//    public VfsFileSystem(FileSystemManager fsManager, FileObject fileObject) throws FileSystemException {
//        this.fsManager = fsManager;
////        this.fileObject = fileObject;
//    }

    public VfsFileSystem(FSFEnvironment environment, FileSystemManager fsManager, List<FileObject> roots) throws FileSystemException {
        this.environment = environment;
        this.fsManager = fsManager;
        this.roots = roots;
    }

    protected FileSystemManager getFsManager(){
        return fsManager;
    }

    protected static final RuntimeException throwNotImplementedException(){
        throw new RuntimeException("This VFS FileSystem method is not implemented, yet");
    }

    @Override
    public List<FSFFile> listRoots() {
        List<FSFFile> result = new ArrayList<>();

        try {
            for (FileObject i: roots){
                result.add(new VfsFile(this, i));
            }
        } catch (FileSystemException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    @Override
    public FSFFile createByPath(String path) throws Exception {
        return new VfsFile(this, path);
    }

    @Override
    public String getSeparator() {
        return "/";
    }

    public FSFEnvironment getEnvironment() {
        return environment;
    }

    public void setHasInternalCommonsVfsBugThatDeletesSubItemsOnMoveOrRename(boolean hasInternalCommonsVfsBugThatDeletesSubItemsOnMoveOrRename) {
        this.hasInternalCommonsVfsBugThatDeletesSubItemsOnMoveOrRename = hasInternalCommonsVfsBugThatDeletesSubItemsOnMoveOrRename;
    }

    public boolean hasInternalCommonsVfsBugThatDeletesSubItemsOnMoveOrRename() {
        return this.hasInternalCommonsVfsBugThatDeletesSubItemsOnMoveOrRename;
    }

//    public FileObject getFileObject() {
//        return fileObject;
//    }


}
