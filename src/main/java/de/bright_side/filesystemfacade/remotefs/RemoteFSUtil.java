package de.bright_side.filesystemfacade.remotefs;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import de.bright_side.filesystemfacade.facade.FSFFile;
import de.bright_side.filesystemfacade.util.FSFFileUtil;

/**
 * @author Philip Heyse
 *
 */
public class RemoteFSUtil {
	public static List<FSFFile> toFileList(RemoteFS remoteFS, RemoteFSResponse response) {
		List<FSFFile> result = new ArrayList<>();
		if (response.getFileListResult() == null) {
			return null;
		}
		for (String i: response.getFileListResult()) {
			int posOfSeparator = i.indexOf(RemoteFS.VERSION_SEPARATOR);
			long version = Long.valueOf(i.substring(0, posOfSeparator));
			String absolutePath = i.substring(posOfSeparator + 1);
			result.add(new RemoteFile(remoteFS, absolutePath, version));
		}
		return result;
	}
	
	public static SortedSet<Long> toLongSortedSet(RemoteFSResponse response) {
		if (response.getNumberListResponse() == null) {
			return null;
		}
		return new TreeSet<Long>(response.getNumberListResponse());
	}
	
	public static void writeHandleRequestToStream(OutputStream outputStream, String absolutePath, String command, InputStream payload, Object ...parameters) throws Exception{
		byte[] requestBytes = FSFFileUtil.objectToByteArray(new RemoteFSRequest(1, absolutePath, command, parameters));
		outputStream.write(FSFFileUtil.convertTo4ByteArray(requestBytes.length));
		outputStream.write(requestBytes);
		if (payload != null) {
			FSFFileUtil.writeAllToOutputStream(payload, outputStream);
		}
	}

	public static byte[] writeHandleRequestToBytes(String absolutePath, String command, InputStream payload, Object ...parameters) throws Exception{
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		writeHandleRequestToStream(result, absolutePath, command, payload, parameters);
		return result.toByteArray();
	}

	public static void writeRemoteResponseToStream(OutputStream outputStream, RemoteFSResponse response, InputStream byteResponseInputStream) throws Exception{
		byte[] responseBytes = FSFFileUtil.objectToByteArray(response);
		outputStream.write(FSFFileUtil.convertTo4ByteArray(responseBytes.length));
		outputStream.write(responseBytes);
		if (byteResponseInputStream != null) {
			FSFFileUtil.writeAllToOutputStream(byteResponseInputStream, outputStream);
		}
	}
	
	/**
	 * returns the RemoteFileRequest. The rest of the inputStream is the payload
	 * @param intputStream
	 * @return 
	 * @throws Exception
	 */
	public static RemoteFSRequest readRemoteFileRequestFromStream(InputStream intputStream) throws Exception{
		byte[] lengthBytes = FSFFileUtil.readExactAmountOfBytes(intputStream, 4);
		int length = FSFFileUtil.convert4ByteArrayToInt(lengthBytes);
		
		byte[] objectBytes = FSFFileUtil.readExactAmountOfBytes(intputStream, length);
		RemoteFSRequest result = FSFFileUtil.objectFromByteArray(objectBytes, RemoteFSRequest.class);
		
		return result;
	}

	/**
	 * returns the RemoteFileRequest. The rest of the inputStream is the payload
	 * @param intputStream
	 * @return 
	 * @throws Exception
	 */
	public static RemoteFSResponse readRemoteFileResponseFromStream(InputStream intputStream) throws Exception{
		byte[] lengthBytes = FSFFileUtil.readExactAmountOfBytes(intputStream, 4);
		int length = FSFFileUtil.convert4ByteArrayToInt(lengthBytes);
		
		byte[] objectBytes = FSFFileUtil.readExactAmountOfBytes(intputStream, length);
		RemoteFSResponse result = FSFFileUtil.objectFromByteArray(objectBytes, RemoteFSResponse.class);
		
		
		
		return result;
	}

	

}
