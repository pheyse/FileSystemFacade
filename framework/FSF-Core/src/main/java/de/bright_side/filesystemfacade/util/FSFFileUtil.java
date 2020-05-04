package de.bright_side.filesystemfacade.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.bright_side.filesystemfacade.facade.FSFEnvironment;
import de.bright_side.filesystemfacade.facade.FSFFile;
import de.bright_side.filesystemfacade.facade.FSFSystem;
import de.bright_side.filesystemfacade.util.ListDirFormatting.Style;

/**
 * @author Philip Heyse
 *
 */
public class FSFFileUtil {
	private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.ssss");
	private static final String STRING_ENCODING = "UTF-8";
	private static final boolean LOGGING_ENABLED = false;

	/**
	 * 
	 * @param fs file system of which the files should be listed
	 * @return the file listing as a string
	 * @throws Exception on general error
	 */
	@SuppressWarnings("unused")
	private static String listDirFormattingSimple(FSFSystem fs) throws Exception{
		ListDirFormatting formatting = new ListDirFormatting();
		formatting.setStyle(Style.TREE);
		formatting.setAllSubItems(true);
		return fs.createByPath("").listDirAsString(formatting);
	}
	
	private static ListDirFormatting createDefaultListDirFormatting(){
		ListDirFormatting result = new ListDirFormatting();
		result.setAllSubItems(false);
		result.setIncludeSize(false);
		result.setIncludeTime(false);
		result.setStyle(Style.TREE);
		return result;
	}
	
	public static String listDirAsString(FSFFile file, ListDirFormatting formatting){
		return listDirAsString(file, formatting, null);
	}
	
	public static String listDirAsString(FSFFile file, ListDirFormatting formatting, Set<String> filenamesToSkip){
		return listDirAsString(file, formatting, filenamesToSkip, "").toString();
	}
	
	private static StringBuilder listDirAsString(FSFFile file, ListDirFormatting formatting, Set<String> filenamesToSkip, String indent){
		ListDirFormatting useFormatting = formatting;
		if (useFormatting == null){
			useFormatting = createDefaultListDirFormatting();
		}
		if (useFormatting.getStyle() == null){
			useFormatting.setStyle(Style.TREE);
		}
		
		StringBuilder result = new StringBuilder();
		List<FSFFile> files = file.listFiles();
		if (files == null){
			return new StringBuilder("(path does not exist: '" + file.getAbsolutePath() + "')");
		}
		
		SortedMap<String, FSFFile> filesOfTypeFile = new TreeMap<>();
		SortedMap<String, FSFFile> filesOfTypeDir = new TreeMap<>();
		
		for (FSFFile i: files) {
			if ((filenamesToSkip == null) || (!filenamesToSkip.contains(i.getName()))){
				if (i.isDirectory()) {
					filesOfTypeDir.put(i.getName(), i);
				} else {
					filesOfTypeFile.put(i.getName(), i);
				}
			}
		}
		
//		Collections.sort(filesOfTypeFile);
//		Collections.sort(filesOfTypeDir);
//		List<FSFFile> allFiles = new ArrayList<>(filesOfTypeDir);
//		allFiles.addAll(filesOfTypeFile);
		
		LinkedHashMap<String, FSFFile> allFiles = new LinkedHashMap<>();
		allFiles.putAll(filesOfTypeDir);
		allFiles.putAll(filesOfTypeFile);
		
		for (Map.Entry<String, FSFFile> i: allFiles.entrySet()){
			FSFFile fileItem = i.getValue();
			String filename = i.getKey();
			String type = fileItem.isDirectory() ? "<D>" : "<F>";
			switch (useFormatting.getStyle()) {
			case FULL_PATH:
				result.append(fileItem.getAbsolutePath() + " " + type);
				break;
			case TREE:
				result.append(indent + type + " " + filename);
				break;
			default:
				result.append("(unknown style: " + useFormatting.getStyle() + ")");
				break;
			}
			if (useFormatting.isIncludeSize()){
				result.append(" | " + fileItem.getLength());
			}
			if (useFormatting.isIncludeTime()){
				result.append(" | " + getTimeLastModifiedString(fileItem));
			}
			result.append("\n");
			
			if ((useFormatting.isAllSubItems()) && (fileItem.isDirectory())){
				result.append(listDirAsString(fileItem, useFormatting, filenamesToSkip, indent + "   "));
			}
		}
		return result;
	}
	
	private static String getTimeLastModifiedString(FSFFile file) {
		 try{
			 TIME_FORMAT.format(file.getTimeLastModified());
		 } catch (Exception e) {
			 return "?";
		 }
		 return null;
	}

	public static void verifyCopyPossible(FSFFile source, FSFFile dest) throws Exception{
		if (source == null){
			throw new Exception("Source file is null");
		}
		if (dest == null){
			throw new Exception("Dest file is null");
		}
		
		if (!source.exists()){
			throw new Exception("Source file does not exist: '" + source.getAbsolutePath() + "'");
		}

		if ((source.isDirectory()) && (!source.listFiles().isEmpty())){
			throw new Exception("Cannot copy to '" + dest.getAbsolutePath() + "' because source is non-empty directory. Do you want to use the copyTree method?");
		}
		
		if ((dest.exists()) && (dest.isDirectory()) && (!dest.listFiles().isEmpty())){
			throw new Exception("Cannot copy to '" + dest.getAbsolutePath() + "' because destination is non-empty directory.");
		}
	}

	public static void copyViaStreams(FSFFile source, FSFFile dest) throws Exception {
//		log("copyViaStreams: source = " + source + ", dest = " + dest);
//		log("copyViaStreams: source exists: " + source.exists());
		try(InputStream inputStream = source.getInputStream(); OutputStream outputStream = dest.getOutputStream(false)){
		 	byte[] buf = new byte[10240];
		 	int r;
//	 		log("copyViaStreams: starting to read");
		 	while((r=inputStream.read(buf)) != -1){
//		 		log("copyViaStreams: writing " + r + " bytes");
		 		outputStream.write(buf, 0, r);
		 	}
		 	
//		 	log("copyViaStreams: done");
		} catch (Exception e){
			throw new Exception("Could not copy from file '" + source.getAbsolutePath() + "' to '" + dest.getAbsolutePath() + "'", e);
		}
	}

	/**
	 * @param file file object which holds the directory of which the files tree should be listed
	 * @throws Exception on general error
	 * @return all sub-items recursively and as a list ordered by path
	 */
	public static List<FSFFile> listFilesTree(FSFFile file) throws Exception{
		return listFilesTree(file, null);
	}
	
	/**
	 * @param file file object that holds the directory of which the file tree should be listed
	 * @param filenamesToSkip list of filenames to be skipped (e.g. history directories)
	 * @return all sub-items recursively and as a list ordered by path
	 * @throws Exception on general error
	 */
	public static List<FSFFile> listFilesTree(FSFFile file, Set<String> filenamesToSkip) throws Exception{
		List<FSFFile> unsortedItems = new ArrayList<>();
		if (file.isDirectory()){
			unsortedItems = listFilesTreeUnsorted(file.listFiles(), filenamesToSkip);
		}
		
		//: don't use collections sort, because in the special case of encrypted files with encrypted file names
		//: there may be multiple file names that actually mean the same plain file name
		//: hence getAbsolutePath() must be used which created an uncrypted version of the path
		SortedMap<String, FSFFile> pathToItemMap = new TreeMap<>();
		for (FSFFile i: unsortedItems) {
			pathToItemMap.put(i.getAbsolutePath(), i);
		}
 		
		return new ArrayList<FSFFile>(pathToItemMap.values());
	}

	private static List<FSFFile> listFilesTreeUnsorted(List<FSFFile> file, Set<String> filenamesToSkip) throws Exception{
		List<FSFFile> result = new ArrayList<>();
		for (FSFFile i: file){
			if ((filenamesToSkip == null) || (!filenamesToSkip.contains(i.getName()))) {
				result.add(i);
				if (i.isDirectory()){
					result.addAll(listFilesTreeUnsorted(i.listFiles(), filenamesToSkip));
				}
			}
		}
		return result;
	}
	
	/**
	 * deletes the given item and all sub-items recursively
	 * @param file file object which represents the directory to be deleted
	 * @throws Exception on general error
	 */
	public static void deleteTree(FSFFile file) throws Exception{
		if (!file.exists()) {
			return;
		}
		if (file.isDirectory()){
			List<FSFFile> items = listFilesTree(file);
			Collections.reverse(items);
			
			for (FSFFile i: items){
				i.delete();
			}
		}
		
		file.delete();
	}
	
	public static void copyFilesTree(FSFFile source, FSFFile dest) throws Exception{
		copyFilesTree(source, dest, false);
	}
	
	/**
	 * copies all sub-items recursively to the dest directory
	 * @param source source directory to be copied
	 * @param dest dest directory to be copied to
	 * @param copyVersionData if true the version data is copied as well
	 * @throws Exception on general error
	 */
	public static void copyFilesTree(FSFFile source, FSFFile dest, boolean copyVersionData) throws Exception{
//		log("copyFilesTree: source = " + source + ", dest = " + dest + ", copyVersionData = " + copyVersionData);
		if (!source.exists()){
			throw new Exception("The source '" + source.getAbsolutePath() + "' does not exist");
		}
		
		if (source.isFile()){
//			log("copyFilesTree: source is file");
			source.copyTo(dest);
			if (copyVersionData) {
				dest.setVersion(source.getVersion(false));
			}
			return;
		}
		
		List<FSFFile> items = listFilesTree(source);
		dest.mkdirs();
		for (FSFFile i: items){
			FSFFile destFile = createDestFile(dest, source, i);
//			log("copyFilesTree: processing item " + i + ", destFile = " + destFile);
			if (i.isDirectory()){
				destFile.mkdir();
			} else {
				i.copyTo(destFile);
				if (copyVersionData) {
					destFile.setVersion(i.getVersion(false));
				}
			}
			destFile.setTimeLastModified(i.getTimeLastModified());
		}
	}

	private static void log(String message) {
		if (LOGGING_ENABLED) {
			System.out.println("FSFFileUtil> " + message);
		}
	}

	private static FSFFile createDestFile(FSFFile destRoot, FSFFile source, FSFFile sourceSubItem) throws Exception {
		FSFFile result = destRoot;
		for (String i: createSubPathList(source, sourceSubItem)){
			result = result.getChild(i);
		}
		result = result.getChild(sourceSubItem.getName());
		return result;
	}

	private static List<String> createSubPathList(FSFFile root, FSFFile subItem) throws Exception {
		List<String> result = new ArrayList<>();
		FSFFile file = subItem;
		while (!file.getAbsolutePath().equals(root.getAbsolutePath())){
			file = file.getParentFile();
			if (file == null){
				throw new Exception("Sub item '" + subItem.getAbsolutePath() + "' is not a child of root '" + root.getAbsolutePath() + "'");
			}
			result.add(file.getName());
		}
		Collections.reverse(result);
		if (!result.isEmpty()){
			result.remove(0);
		}
		
		log("createSubPathList. root = '" + root.getAbsolutePath() + "', subItem = '" + subItem.getAbsolutePath() + "', items: " + result);
		return result;
	}

	public static void writeBytes(FSFFile file, boolean append, byte[] bytes) throws Exception {
		OutputStream outputStream = file.getOutputStream(append);
		writeBytes(outputStream, bytes);
//		OutputStream outputStream = null;
//		try{
//			outputStream = file.getOutputStream(false);
//			outputStream.write(bytes);
//		} catch (Exception e) {
//			throw e;
//		} finally {
//			if (outputStream != null) {
//				outputStream.close();
//			}
//		}
	}

	public static void writeBytes(OutputStream outputStream, byte[] bytes) throws Exception {
		try{
			outputStream.write(bytes);
		} catch (Exception e) {
			throw e;
		} finally {
			if (outputStream != null) {
				outputStream.close();
			}
		}
	}
	
	public static FSFEnvironment createDefaultEnvironment() {
		return new FSFEnvironment() {
			@Override
			public long getCurrentTimeMillis() {
				return System.currentTimeMillis();
			}
		};
	}

	public static FSFFile writeString(FSFFile file, String string) throws Exception {
		writeBytes(file, false, string.getBytes(STRING_ENCODING));
		return file;
	}

	public static byte[] stringToByteArray(String string) throws Exception {
		return string.getBytes(STRING_ENCODING);
	}
	
	public static void writeString(OutputStream outputStream, String string) throws Exception {
		writeBytes(outputStream, string.getBytes(STRING_ENCODING));
	}
	
	public static String readString(FSFFile file) throws Exception{
		return new String(file.readBytes(), STRING_ENCODING);
	}

	public static String readString(InputStream inputStream) throws Exception{
		return new String(readAllBytes(inputStream), STRING_ENCODING);
	}
	
	public static String readString(byte[] bytes) throws Exception{
		return new String(bytes, STRING_ENCODING);
	}
	
	public static byte[] readAllBytes(FSFFile file) throws Exception {
		InputStream inputStream = null;
		try {
			inputStream = file.getInputStream();
			return readAllBytes(inputStream);
		} catch (Exception e) {
			throw e;
		} finally {
			if (inputStream != null) {
				inputStream.close();
			}
		}
	}

	public static void writeAllToOutputStream(InputStream inputStream, OutputStream outputStream) throws Exception {
		try {
			byte[] buffer = new byte[10240];
			int len;
			while ((len = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, len);
			}
		} catch (Exception e) {
			throw e;
		}
	}
	
	public static byte[] readAllBytes(InputStream inputStream) throws Exception {
		try {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			byte[] buffer = new byte[10240];
			int len;
			while ((len = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, len);
			}
			return outputStream.toByteArray();
		} catch (Exception e) {
			throw e;
		} finally {
			inputStream.close();
		}
	}

	public static byte[] objectToByteArray(Object object) throws Exception{
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String json = gson.toJson(object);
		BufferedWriter writer = null;
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try{
			writer = new BufferedWriter(new OutputStreamWriter(outputStream, STRING_ENCODING));
			writer.append(json);
		}catch (IOException e){
			throw new Exception("Could not write data to byte array", e);
		} finally {
			if (writer != null){
				writer.close();
			}
		}
		byte[] result = outputStream.toByteArray();
		return result;
	}

	public static <K> K objectFromByteArray(byte[] byteArray, Class<K> classType) throws Exception{
		BufferedReader reader = null;
		GsonBuilder gsonBuilder = new GsonBuilder();
		Gson gson = gsonBuilder.create();
		ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArray);
		try{
			reader = new BufferedReader(new InputStreamReader(inputStream, STRING_ENCODING));
			return gson.fromJson(reader, classType);
		} catch (Exception e){
			throw e;
		} finally {
			if (reader != null){
				reader.close();
			}
		}
	}
	

	public static byte[] readExactAmountOfBytes(InputStream fileInputStream, int length) throws Exception{
		byte[] data = new byte[length];
		int readLength = 0;
		int readLengthTotal = fileInputStream.read(data, 0, length);
		if (readLengthTotal < 0) throw new Exception("Cannot read any bytes read from file (input stream type: " + fileInputStream.getClass().getName() + ")");
		if (readLengthTotal >= length) return data;
		while (readLengthTotal < length){
			int numberOfBytesToRead = length - readLengthTotal;
			try{
				readLength = fileInputStream.read(data, readLengthTotal, numberOfBytesToRead);
			} catch (Exception e){
				throw new Exception("Could not read " + numberOfBytesToRead + " byte(s). buffer = " + data.length + " bytes. readLengthTotal = " + readLengthTotal, e);
			}
			if (readLength > 0) readLengthTotal += readLength;
			if (readLengthTotal >= length) return data;

			if (readLength <= 0) throw new Exception("Could not read as much data as requested,"
					+ " becuase the end of the file was reached. (Wanted size: "
					+ length + ", read size total: " + readLengthTotal + ", read size in step: " + readLength + ")");
		}
		return data;
	}

	/**
	 * @param b byte array
	 * @return An integer that contains the values of the byte array
	 */
	public static final int convert4ByteArrayToInt(byte[] b) {
		return b[0]<<24 | (b[1]&0xff)<<16 | (b[2]&0xff)<<8 | (b[3]&0xff);
	}
	
	/**
	 * @param b1 first byte
	 * @param b2 second byte
	 * @param b3 third byte
	 * @param b4 fourth byte
	 * @return An integer that contains the values of the byte array
	 */
	public static final int convertToInt(byte b1, byte b2, byte b3, byte b4) {
		return b1<<24 | (b2&0xff)<<16 | (b3&0xff)<<8 | (b4&0xff);
	}

	/**
	 * @param i integer
	 * @return A byte array that contains the value of the integer
	 */
	public static final byte[] convertTo4ByteArray(int i) {
		return new byte[] { (byte)(i>>24), (byte)(i>>16), (byte)(i>>8), (byte)i };
	}

	/**
	 * @param bytes bytes to be converted
	 * @return A long value that contains the values of the byte array
	 */
	public static final long convert8ByteArrayToLong(byte[] bytes) {
		return new BigInteger(bytes).longValue();
	}

	/**
	 * @param longValue value to be converted
	 * @return A byte array that contains the value of the long-value
	 */
	public static final byte[] convertTo8ByteArray(long longValue) {
		byte [] bytes = new byte[8];
		for(int i= 0; i < 8; i++){
			bytes[7-i] = (byte)(longValue >>> (i * 8));
		}		

		return bytes;
	}

	public static void writeStringWithLengthInfo(OutputStream outputStream, String string) throws Exception {
		byte[] bytes = string.getBytes(STRING_ENCODING); 
		outputStream.write(convertTo4ByteArray(bytes.length));
		outputStream.write(bytes);
	}

	public static int compareString(String string1, String string2){
		if (string1 == null){
			if (string2 == null) return 0;
			else return -1;
		}
		if (string2 == null) return 1;
		return string1.compareTo(string2);
	}

	public static String readStringWithLengthInfo(InputStream input) throws Exception {
		byte[] lengthBytes = readExactAmountOfBytes(input, 4);
		int length = convert4ByteArrayToInt(lengthBytes);
		
		byte[] stringBytes = readExactAmountOfBytes(input, length);
		return new String(stringBytes, STRING_ENCODING);
	}

	public static String toString(Throwable exception){
		if (exception == null) return "null";
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		exception.printStackTrace(new PrintWriter(byteArrayOutputStream, true));
		return new String(byteArrayOutputStream.toByteArray());
	}
	
	public static String removeIfStartsWith(String string, String optionalStart) {
		if (string == null) {
			return null;
		}
		if (optionalStart == null) {
			return string;
		}
		
		if (string.startsWith(optionalStart)) {
			return string.substring(optionalStart.length());
		}
		return string;
	}

	public static String removeIfEndsWith(String string, String optionalEnd) {
		if (string == null) {
			return null;
		}
		if (optionalEnd == null) {
			return string;
		}
		
		if (string.endsWith(optionalEnd)) {
			return string.substring(0, string.length() - optionalEnd.length());
		}
		return string;
	}

	
	
}
