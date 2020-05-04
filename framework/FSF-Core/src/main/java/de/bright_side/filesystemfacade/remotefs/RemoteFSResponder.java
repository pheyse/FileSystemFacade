package de.bright_side.filesystemfacade.remotefs;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import de.bright_side.filesystemfacade.facade.FSFFile;
import de.bright_side.filesystemfacade.facade.FSFSystem;
import de.bright_side.filesystemfacade.facade.VersionedData;
import de.bright_side.filesystemfacade.facade.WrongVersionException;
import de.bright_side.filesystemfacade.util.FSFFileUtil;

/**
 * @author Philip Heyse
 *
 */
public class RemoteFSResponder {
	public void respond(InputStream input, OutputStream output, RemoteFSFSystemProvider fsProvider) throws Exception {
        GZIPInputStream gzipin = new GZIPInputStream(input);

		
		String app = FSFFileUtil.readStringWithLengthInfo(gzipin);
		String tennant = FSFFileUtil.readStringWithLengthInfo(gzipin);
		String username = FSFFileUtil.readStringWithLengthInfo(gzipin);
		String password = FSFFileUtil.readStringWithLengthInfo(gzipin);

		RemoteFSRequest request = RemoteFSUtil.readRemoteFileRequestFromStream(gzipin);

		RemoteFSResponse response = null;
		try {
			FSFSystem fsfSystem = fsProvider.getFSFFystem(app, tennant, username, password);
			if (request.getVersion() != 1) {
				throw new Exception("Unexpected version: " + request.getVersion());
			}
			response = process(fsfSystem, request, gzipin);
		} catch (WrongVersionException e) {
			response = createWrongVersionExceptionResponse(e);
		} catch (RemoteFSAuthenticationException e) {
			response = createAuthenticationExceptionResponse(e);
		} catch (Exception e) {
			response = createGeneralExceptionResponse(e);
		}

		GZIPOutputStream zipOutputStream = new GZIPOutputStream(output);
		InputStream byteResponseInputStream = response.getByteResponseInputStream();
		response.setByteResponseInputStream(null); //: must be set to null before "sending" because InputStream cannot be "serialized" as JSON
		RemoteFSUtil.writeRemoteResponseToStream(zipOutputStream, response, byteResponseInputStream);
        zipOutputStream.finish();
        zipOutputStream.flush();
        zipOutputStream.close();
	}

	private RemoteFSResponse createGeneralExceptionResponse(Exception e) {
		RemoteFSResponse result = new RemoteFSResponse();
		result.setRemoteGeneralException(FSFFileUtil.toString(e));
		return result;
	}

	private RemoteFSResponse createAuthenticationExceptionResponse(RemoteFSAuthenticationException e) {
		RemoteFSResponse result = new RemoteFSResponse();
		result.setRemoteAuthenticationException("Authentication failed for app '" + e.getApp() + "', tenant '"
				+ e.getTenant() + "', user '" + e.getUsername() + "'");
		return result;
	}

	private RemoteFSResponse createWrongVersionExceptionResponse(WrongVersionException e) {
		RemoteFSResponse result = new RemoteFSResponse();
		result.setRemoteWrongVersionException("" + e);
		return result;
	}
	
	private List<String> toVersionAndAbsolutePathList(List<FSFFile> files) throws Exception {
		List<String> result = new ArrayList<>();
		if (files == null) {
			return null;
		}
		for (FSFFile i: files){
			result.add("" + i.getVersion() + RemoteFS.VERSION_SEPARATOR + i.getAbsolutePath());
		}
		
		return result;
	}
	
	private RemoteFSResponse process(FSFSystem fsfSystem, RemoteFSRequest request, InputStream payload) throws Exception {
		String command = "?";
		String absolutePath = "?";
		try {
			RemoteFSResponse result = new RemoteFSResponse();
			command = request.getCommand();
			if (command.equals(RemoteFS.COMMAND_LIST_ROOTS)) {
				result.setFileListResult(toVersionAndAbsolutePathList(fsfSystem.listRoots()));
				return result;
			}
			
			Object[] parameters = request.getParameters();
			absolutePath = request.getAbsolutePath();
			FSFFile file = fsfSystem.createByPath(absolutePath);
			
			byte[] bytes;
			
			switch (command) {
			case RemoteFS.COMMAND_LIST_FILES:
				result.setFileListResult(toVersionAndAbsolutePathList(file.listFiles()));
				break;
			case RemoteFS.COMMAND_GET_TIME_LAST_MODIFIED:
				result.setNumberResponse(file.getTimeLastModified());
				break;
			case RemoteFS.COMMAND_SET_TIME_LAST_MODIFIED:
				file.setTimeLastModified((Long.valueOf("" + parameters[0])));
				break;
			case RemoteFS.COMMAND_SET_TIME_CREATED:
				file.setTimeCreated((Long.valueOf("" + parameters[0])));
				break;
			case RemoteFS.COMMAND_GET_TIME_CREATED:
				result.setNumberResponse(file.getTimeCreated());
				break;
			case RemoteFS.COMMAND_GET_LENGTH:
				result.setNumberResponse(file.getLength());
				break;
			case RemoteFS.COMMAND_IS_FILE:
				result.setBooleanResponse(file.isFile());
				break;
			case RemoteFS.COMMAND_MKDIR:
				file.mkdir();
				break;
			case RemoteFS.COMMAND_DELETE:
				file.delete();
				break;
			case RemoteFS.COMMAND_DELETE_TREE:
				file.deleteTree();
				break;
			case RemoteFS.COMMAND_GET_VERSION:
				result.setNumberResponse(file.getVersion(false));
				break;
			case RemoteFS.COMMAND_SET_VERSION:
				file.setVersion((Long.valueOf("" + parameters[0])));
				break;
			case RemoteFS.COMMAND_MKDIRS:
				file.mkdirs();
				break;
			case RemoteFS.COMMAND_IS_DIRECTORY:
				result.setBooleanResponse(file.isDirectory());
				break;
			case RemoteFS.COMMAND_EXISTS:
				result.setBooleanResponse(file.exists());
				break;
			case RemoteFS.COMMAND_READ_BYTES:
				result.setByteResponseInputStream(new ByteArrayInputStream(file.readBytes()));
				break;
			case RemoteFS.COMMAND_READ_HISTORY_BYTES:
				result.setByteResponseInputStream(new ByteArrayInputStream(readHistoryBytes(file, Long.valueOf("" + parameters[0]))));
				break;
			case RemoteFS.COMMAND_WRITE_BYTES:
				bytes = FSFFileUtil.readAllBytes(payload);
				file.writeBytes((Boolean)parameters[0], bytes);
				break;
			case RemoteFS.COMMAND_READ_BYTES_AND_VERSION:
				VersionedData<byte[]> commandResult = file.readBytesAndVersion();
				result.setNumberResponse(commandResult.getVersion());
				result.setByteResponseInputStream(new ByteArrayInputStream(commandResult.getData()));
				break;
			case RemoteFS.COMMAND_WRITE_BYTES_FOR_VERSION:
				bytes = FSFFileUtil.readAllBytes(payload);
				file.writeBytesForVersion((Boolean)parameters[0], bytes, Long.valueOf("" + parameters[1]));
				break;
			case RemoteFS.COMMAND_RENAME:
				file.rename((String)parameters[0]);
				break;
			case RemoteFS.COMMAND_MOVE_TO:
				file.moveTo(fsfSystem.createByPath((String)parameters[0]));
				break;
			case RemoteFS.COMMAND_GET_HISTORY_TIMES:
				result.setNumberListResponse(new ArrayList<>(file.getHistoryTimes()));
				break;
			default:
				throw new Exception("Unknown command: '" + request.getCommand() + "'");
			}
			return result;
		} catch (WrongVersionException t) {
			throw t;
		} catch (Throwable t) {
			throw new Exception("Could not execute command '" + command + "' for absolute path '" + absolutePath + "'", t);
		}
	}

	private byte[] readHistoryBytes(FSFFile file, long historyTime) throws Exception {
		return FSFFileUtil.readAllBytes(file.getHistoryInputStream(historyTime));
	}

}
