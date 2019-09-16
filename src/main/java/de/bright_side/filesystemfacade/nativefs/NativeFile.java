package de.bright_side.filesystemfacade.nativefs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.bright_side.filesystemfacade.facade.FSFFile;
import de.bright_side.filesystemfacade.facade.FSFSystem;
import de.bright_side.filesystemfacade.facade.VersionedData;
import de.bright_side.filesystemfacade.facade.WrongVersionException;
import de.bright_side.filesystemfacade.util.FSFFileUtil;
import de.bright_side.filesystemfacade.util.ListDirFormatting;

/**
 * @author Philip Heyse
 *
 */
public class NativeFile implements FSFFile{
	private static final boolean LOGGING_ENABLED = false;
	private File file;
	private FSFSystem fsfSystem;
	private static final String ENCODING = "UTF-8";
	
	protected NativeFile(FSFSystem fsfSystem, File file) {
		this.fsfSystem = fsfSystem;
		this.file = file;
	}

	@Override
	public int compareTo(FSFFile other) {
		if (other == null){
			return 1;
		}
		if (other instanceof NativeFile){
			return file.compareTo(((NativeFile) other).file);
		}
		return other.getClass().getName().compareTo(this.getClass().getName());
	}

	@Override
	public List<FSFFile> listFiles() {
		if (!file.isDirectory()){
			return null;
		}
		List<FSFFile> result = new ArrayList<FSFFile>();
		for (File i: file.listFiles()){
			result.add(new NativeFile(fsfSystem, i));
		}
		return result;
	}

	@Override
	public String getName() {
		return file.getName();
	}

	@Override
	public long getTimeLastModified() {
		return file.lastModified();
	}
	
	@Override
	public long getTimeCreated() throws Exception {
		try{
			BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
			FileTime attributeTime = attr.creationTime();
			if (attributeTime == null) {
				return 0;
			}
			return attributeTime.toMillis();
		} catch (NoSuchMethodError e) { 
			//: File.toPath is not supported in all Java versions, e.g. not in Android before 8.0
			//: In this case just return 0 for "unknown"
			return 0;
		}
	}

	@Override
	public boolean setTimeCreated(long timeCreated) throws Exception {
		try {
			BasicFileAttributeView attributes = Files.getFileAttributeView(file.toPath(), BasicFileAttributeView.class);
			FileTime time = FileTime.fromMillis(timeCreated);
			attributes.setTimes(FileTime.fromMillis(file.lastModified()), time, time);
			return true;
		} catch (NoSuchMethodError e) {
			//: File.toPath is not supported in all Java versions, e.g. not in Android before 8.0
			//: In this case ignore the action because it is not possible to set the time
			return false;
		}
	}

	@Override
	public boolean isFile() {
		return file.isFile();
	}

	@Override
	public boolean isDirectory() {
		return file.isDirectory();
	}

	@Override
	public boolean exists() {
		return file.exists();
	}

	@Override
	public FSFFile getParentFile() {
		File parent = file.getParentFile();
		if (parent == null){
			return null;
		}
		return new NativeFile(fsfSystem, parent);
	}

	@Override
	public OutputStream getOutputStream(boolean append) throws Exception {
		return new FileOutputStream(file, append);
	}

	@Override
	public InputStream getInputStream() throws Exception {
		return new FileInputStream(file);
	}

	@Override
	public void rename(String newName) throws Exception {
		file.renameTo(new File(file.getParentFile(), newName));
	}

	@Override
	public FSFFile getChild(String name) {
		return new NativeFile(fsfSystem, new File(file, name));
	}

	@Override
	public FSFFile mkdirs() {
		file.mkdirs();
		return this;
	}

	@Override
	public FSFFile mkdir() {
		file.mkdir();
		return this;
	}

	@Override
	public String getAbsolutePath() {
		return file.getAbsolutePath();
	}

	@Override
	public void delete() throws Exception{
		if (!file.delete()){
			throw new Exception("Could not delete file '" + file.getAbsolutePath() + "'");
		}
	}

	@Override
	public String toString() {
		return "NativeFile{'" + file + "'}";
	}

	@Override
	public FSFSystem getFSFSystem() {
		return fsfSystem;
	}
	
	@Override
	public <K> K readObject(Class<K> classType) throws Exception {
		BufferedReader reader = null;
		GsonBuilder gsonBuilder = new GsonBuilder();
		Gson gson = gsonBuilder.create();
		try{
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), ENCODING));
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
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String json = gson.toJson(objectToWrite);
		BufferedWriter writer = null;
		try{
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), ENCODING));
			writer.append(json);
		}catch (IOException e){
			throw new Exception("Could not save file '" + file.getAbsolutePath() + "'", e);
		} finally {
			if (writer != null){
				writer.close();
			}
		}
		return this;
	}
	
	private boolean moveViaJavaNIO(NativeFile otherNativeFile) throws IOException{
		try {
			Files.move(file.toPath(), otherNativeFile.file.toPath());
			return true;
		} catch (NoSuchMethodError e) {
			return false;
		}
	}

	/**
	 * This method is not supported in all Java versions, e.g. not in the Java on Android before 8.0 
	 * @param otherNativeFile
	 * @return
	 * @throws IOException
	 */
	private boolean moveViaRenameTo(NativeFile otherNativeFile) throws IOException{
		try {
			return file.renameTo(otherNativeFile.file.getAbsoluteFile());
		} catch (NoSuchMethodError e) {
			return false;
		}
	}

	private void moveViaCopyAndDelete(FSFFile otherFile) throws Exception {
		copyFilesTree(otherFile);
		deleteTree();
	}
	
	@Override
	public void moveTo(FSFFile otherFile) throws Exception {
		if (!(otherFile instanceof NativeFile)) {
			moveViaCopyAndDelete(otherFile);
			return;
		}
		NativeFile otherNativeFile = (NativeFile)otherFile;
		log("moveTo: via JavaNIO...");
		boolean succeeded = moveViaJavaNIO(otherNativeFile);
		log("moveTo: via JavaNIO: succeeded: " + succeeded);
		if (succeeded) {
			return;
		}
		log("moveTo: via RenameTo...");
		succeeded = moveViaRenameTo(otherNativeFile);
		log("moveTo: via RenameTo: succeeded: " + succeeded);
		if (succeeded) {
			return;
		}

		log("moveTo: via copy and delete...");
		moveViaCopyAndDelete(otherFile);
	}


	private void log(String message) {
		if (LOGGING_ENABLED) {
			System.out.println("NativeFile> " + message);
		}
	}

	@Override
	public long getLength() {
		return file.length();
	}

	@Override
	public String listDirAsString(ListDirFormatting formatting) {
		return FSFFileUtil.listDirAsString(this, formatting, null);
	}

	@Override
	public byte[] readBytes() throws Exception {
		try {
			return Files.readAllBytes(file.toPath());
		} catch (NoSuchMethodError e) { //: File.toPath is not supported in all Java versions, e.g. not in Android before 8.0
			return FSFFileUtil.readAllBytes(this);
		}
	}

	@Override
	public void copyTo(FSFFile destFile) throws Exception {
		FSFFileUtil.verifyCopyPossible(this, destFile);
		FSFFileUtil.copyViaStreams(this, destFile);
		
//		if (!(destFile.getFSFSystem() instanceof NativeFS)){
//			throw new Exception("Cannot copy from Native File System to a different File System");
//		}
//		
//	 	try (FileInputStream in = new FileInputStream(file); FileOutputStream out = new FileOutputStream(destFile.getAbsolutePath())){
//		 	byte[] buf = new byte[10240];
//		 	int r;
//		 	while((r=in.read(buf)) != -1){
//		 		out.write(buf, 0, r);
//		 	}
// 		 	in.close();
// 		 	out.close();
//	 	} catch (IOException e){
//	 		throw new Exception("Error while coping file '" + file.getAbsolutePath() + "' to file '" + destFile.getAbsolutePath() + "'", e);
//	 	}
	}

	@Override
	public void copyFilesTree(FSFFile dest) throws Exception {
		FSFFileUtil.copyFilesTree(this, dest);
	}

	@Override
	public void deleteTree() throws Exception {
		FSFFileUtil.deleteTree(this);
	}

	@Override
	public List<FSFFile> listFilesTree() throws Exception {
		return FSFFileUtil.listFilesTree(this);
	}

	@Override
	public void setTimeLastModified(long timeLastModified) {
		file.setLastModified(timeLastModified);
	}

	@Override
	public FSFFile writeBytes(boolean append, byte[] bytes) throws Exception {
		FSFFileUtil.writeBytes(this, append, bytes);
		return this;
	}

	@Override
	public FSFFile writeString(String string) throws Exception {
		FSFFileUtil.writeString(this, string);
		return this;
	}

	@Override
	public String readString() throws Exception {
		return FSFFileUtil.readString(this);
	}

	@Override
	public SortedSet<Long> getHistoryTimes() {
		return new TreeSet<Long>();
	}

	@Override
	public void copyHistoryFilesTree(FSFFile dest, long version) throws Exception {
		throw new Exception("Versions are not supported in this file system type");
	}

	@Override
	public InputStream getHistoryInputStream(long version) throws Exception {
		throw new Exception("Versions are not supported in this file system type");
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
	public VersionedData<byte[]> readBytesAndVersion() throws Exception {
		return new VersionedData<byte[]>(0, readBytes());
	}

	@Override
	public FSFFile writeBytesForVersion(boolean append, byte[] bytes, long newVersion) throws WrongVersionException, Exception {
		return writeBytes(append, bytes);
	}

	@Override
	public long getVersion() {
		return 0;
	}

	@Override
	public long getVersion(boolean allowCache) throws Exception {
		return 0;
	}
	
	@Override
	public void setVersion(long version) throws Exception {
	}

	@Override
	public VersionedData<String> readStringAndVersion() throws Exception {
		return new VersionedData<String>(0, readString());
	}

	@Override
	public FSFFile writeStringForVersion(String string, long newVersion) throws WrongVersionException, Exception {
		return writeString(string);
	}

}
