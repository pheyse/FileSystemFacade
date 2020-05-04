package de.bright_side.filesystemfacade.vfs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.bright_side.filesystemfacade.facade.FSFFile;
import de.bright_side.filesystemfacade.facade.FSFSystem;
import de.bright_side.filesystemfacade.facade.VersionedData;
import de.bright_side.filesystemfacade.facade.WrongVersionException;
import de.bright_side.filesystemfacade.util.FSFFileUtil;
import de.bright_side.filesystemfacade.util.ListDirFormatting;

public class VfsFile implements FSFFile {
    private static final boolean LOGGING_ENABLED = false;
    private static final String ENCODING = "UTF-8";

    private final VfsFileSystem fs;
    private final String path;
    private FileObject fileObject;

    public VfsFile(VfsFileSystem fs, String path) throws FileSystemException {
        this.fs = fs;
        this.path = path;
        fileObject = fs.getFsManager().resolveFile(path);
    }

    public VfsFile(VfsFileSystem fs, FileObject fileObject) throws FileSystemException {
        this.fs = fs;
        this.path = getPath(fileObject);
        this.fileObject = fileObject;
    }

    private String getPath(FileObject i) {
        try {
            return i.getName().getPath();
        } catch (Exception e) {
            throw new RuntimeException("Could not get path of object " + i, e);
        }
    }

    @Override
    public List<FSFFile> listFiles() {
        try {
            if (!fileObject.exists()){
                return null;
            }

            TreeMap<String, FileObject> sortedChildren = new TreeMap<>();
            List<FSFFile> result = new ArrayList<>();
            FileObject[] children = fileObject.getChildren();
            if (children == null){
                return null;
            }
            for (FileObject i: children){
                sortedChildren.put(i.getName().getBaseName(), i);
            }
            for (FileObject i: sortedChildren.values()){
                FSFFile item = new VfsFile(fs, i);
                result.add(item);
            }
            return result;
        } catch (FileSystemException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getName() {
        FileName fileName = fileObject.getName();
        return fileName.getBaseName();
    }

    @Override
    public long getTimeLastModified() throws Exception {
        return fileObject.getContent().getLastModifiedTime();
    }

    @Override
    public long getTimeCreated() throws Exception {
    	//: not provided, so 0 is returned as stated in FSFFile
        return 0;
    }

    @Override
    public boolean isFile() {
        try {
            return fileObject.getType() == FileType.FILE;
        } catch (FileSystemException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isDirectory() {
        try {
            return fileObject.getType() == FileType.FOLDER;
        } catch (FileSystemException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean exists() {
        try {
            return fileObject.exists();
        } catch (FileSystemException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public FSFFile getParentFile() {
        try {
            FileObject parent = null;
            parent = fileObject.getParent();
            if (parent == null){
                return null;
            }
            log("get parent of file '" + path + "'. fileObject = " + fileObject);
            VfsFile result = new VfsFile(fs, parent);
            log("get parent of file '" + path + "'. result = " + result);
            return result;
        } catch (FileSystemException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void rename(String newName) throws Exception {
        FileObject destFileObject = fileObject.getParent().resolveFile(newName);
        fileObjectMoveTo(destFileObject);
        fileObject = destFileObject;
    }

    private void fileObjectMoveTo(FileObject dest) throws Exception {
        if ((fs.hasInternalCommonsVfsBugThatDeletesSubItemsOnMoveOrRename()) && (isDirectory())){
            log("fileObjectMoveTo. it is a dir and the bug is present in the file system");
            VfsFile otherFile = new VfsFile(fs, dest);
            copyFilesTree(otherFile);
            deleteTree();
            return;
        }
        fileObject.moveTo(dest);
    }

    @Override
    public FSFFile getChild(String name) {
        try {
            FileObject child = fileObject.resolveFile(name); //: getChild cannot be used as it returns null if the child doesn't exist
            VfsFile result = new VfsFile(fs, child);
            return result;
        } catch (FileSystemException e) {
            throw new RuntimeException("Could not get child with name '" + name + "'", e);
        }
    }

    @Override
    public FSFFile mkdirs() throws Exception {
        fileObject.createFolder();
        return this;
    }

    @Override
    public FSFFile mkdir() throws Exception {
        fileObject.createFolder();
        setTimeLastModified(getCurrentTime());
        return this;
    }

    @Override
    public String getAbsolutePath() {
        String result =  getPath(fileObject);
        if (result.equals("/")){
            return "";
        }

        return result;
    }

    @Override
    public void delete() throws Exception {
        fileObject.delete();
    }

    @Override
    public FSFSystem getFSFSystem() {
        return fs;
    }

    @Override
    public long getLength() {
        try {
            return fileObject.getContent().getSize();
        } catch (FileSystemException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public OutputStream getOutputStream(boolean append) throws Exception {
        OutputStream os = fileObject.getContent().getOutputStream(append);
        VfsFile file = this;
        return new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                os.write(b);
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                os.write(b, off, len);
            }

            @Override
            public void flush() throws IOException {
                os.flush();
            }

            @Override
            public void close() throws IOException {
                os.close();
                try {
                    file.setTimeLastModified(getCurrentTime());
                } catch (Exception e) {
                    throw new IOException(e);
                }
            }
        };
    }

    @Override
    public InputStream getInputStream() throws Exception {
        return fileObject.getContent().getInputStream();
    }

    @Override
    public <K> K readObject(Class<K> classType) throws Exception {
        BufferedReader reader = null;
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        try{
            reader = new BufferedReader(new InputStreamReader(getInputStream(), ENCODING));
            return gson.fromJson(reader, classType);
        } catch (Exception e){
            throw e;
        } finally {
            if (reader != null){
                reader.close();
            }
        }
    }

    @Override
    public <K> FSFFile writeObject(K objectToWrite) throws Exception {
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        String data = gson.toJson(objectToWrite);
        writeString(data);
        return this;
    }

    @Override
    public byte[] readBytes() throws Exception {
        return FSFFileUtil.readAllBytes(this);
    }

    @Override
    public FSFFile writeBytes(boolean append, byte[] bytes) throws Exception {
        FSFFileUtil.writeBytes(this, append, bytes);
        setTimeLastModified(getCurrentTime());
        return this;
    }

    @Override
    public String readString() throws Exception {
        return FSFFileUtil.readString(this);
    }

    @Override
    public FSFFile writeString(String string) throws Exception {
        FSFFileUtil.writeString(this, string);
        setTimeLastModified(getCurrentTime());
        return this;
    }

    @Override
    public String listDirAsString(ListDirFormatting formatting) {
        return FSFFileUtil.listDirAsString(this, formatting);
    }

    @Override
    public void deleteTree() throws Exception {
        FSFFileUtil.deleteTree(this);
    }

    @Override
    public void copyFilesTree(FSFFile dest) throws Exception {
        FSFFileUtil.copyFilesTree(this, dest);
    }

    @Override
    public List<FSFFile> listFilesTree() throws Exception {
        return FSFFileUtil.listFilesTree(this);
    }

    @Override
    public long getVersion() throws Exception {
        return 0;
    }

    @Override
    public long getVersion(boolean allowCache) throws Exception {
        return 0;
    }

    @Override
    public String toString() {
        return "VfsFile{" +
                "path='" + path + '\'' +
                ", fileObject=" + fileObject +
                '}';
    }

    private void log(String message){
        if (!LOGGING_ENABLED){
            return;
        }
        System.out.println("VfsFile> " + message);
    }

    /**
     * this method can later be exchanged by an external provider class to be called in order to produce exact same results in unit tests
     * @return
     */
    private long getCurrentTime() {
        return fs.getEnvironment().getCurrentTimeMillis();
    }

    @Override
    public void setVersion(long version) throws Exception {
    }


    @Override
    public int compareTo(FSFFile other) {
        if (other == null){
            return 1;
        }
        int result = (other.getFSFSystem().getClass().getName()).compareTo(getFSFSystem().getClass().getName());
        if (result != 0){
            return result;
        }
        return getAbsolutePath().compareTo(other.getAbsolutePath());
    }

    @Override
    public void setTimeLastModified(long timeLastModified) throws Exception {
        fileObject.getContent().setLastModifiedTime(timeLastModified);
    }

    @Override
    public boolean setTimeCreated(long timeCreated) throws Exception {
        return false;
    }

    @Override
    public void copyTo(FSFFile destFile) throws Exception {
        FSFFileUtil.verifyCopyPossible(this, destFile);
        FSFFileUtil.copyViaStreams(this, destFile);
    }

    @Override
    public SortedSet<Long> getHistoryTimes() throws Exception {
        return new TreeSet<Long>();
    }

    @Override
    public void copyHistoryFilesTree(FSFFile dest, long historyTime) throws Exception {
        throw new Exception("History is not supported in this file system type");
    }

    @Override
    public InputStream getHistoryInputStream(long historyTime) throws Exception {
        throw new Exception("History is not supported in this file system type");
    }

    @Override
    public VersionedData<byte[]> readBytesAndVersion() throws Exception {
        return new VersionedData<byte[]>(0, readBytes());
    }

    @Override
    public VersionedData<InputStream> getInputStreamAndVersion() throws Exception {
        return new VersionedData<InputStream>(0, getInputStream());
    }

    @Override
    public OutputStream getOutputStreamForVersion(boolean append, long newVersion) throws WrongVersionException, Exception {
        return getOutputStream(append);
    }

    @Override
    public <K> VersionedData<K> readObjectAndVersion(Class<K> classType) throws Exception {
        return new VersionedData<K>(0, readObject(classType));
    }

    @Override
    public <K> FSFFile writeObjectForVersion(K objectToWrite, long newVersion) throws WrongVersionException, Exception {
        return writeObject(objectToWrite);
    }

    @Override
    public VersionedData<String> readStringAndVersion() throws Exception {
        return new VersionedData<String>(0, readString());
    }

    @Override
    public FSFFile writeStringForVersion(String string, long newVersion) throws WrongVersionException, Exception {
        return writeString(string);
    }

    @Override
    public FSFFile writeBytesForVersion(boolean append, byte[] bytes, long newVersion) throws WrongVersionException, Exception {
        return writeBytes(append, bytes);
    }

    @Override
    public void moveTo(FSFFile otherFile) throws Exception {
        if ((otherFile.exists()) && (otherFile.isDirectory()) && (!otherFile.listFiles().isEmpty())){
            throw new Exception("Cannot move to '" + otherFile.getAbsolutePath() + "' because destination is non-empty directory");
        }

        if (otherFile instanceof VfsFile){
            VfsFile otherVfsFile = (VfsFile)otherFile;
            fileObjectMoveTo(otherVfsFile.fileObject);
            return;
        }

        copyFilesTree(otherFile);
        deleteTree();
    }

}
