package de.bright_side.filesystemfacade.remotefs;

import java.io.InputStream;
import java.util.List;

/**
 * @author Philip Heyse
 *
 */
public class RemoteFSResponse {
	private List<String> fileListResult;
	private String fileResult;
	private String remoteGeneralException;
	private String remoteAuthenticationException;
	private String remoteWrongVersionException;
	private String localException;
	private long numberResponse;
	private List<Long> numberListResponse;
	private boolean booleanResponse;
	private InputStream byteResponseInputStream;
	
	public List<String> getFileListResult() {
		return fileListResult;
	}
	public void setFileListResult(List<String> fileListResult) {
		this.fileListResult = fileListResult;
	}
	public String getFileResult() {
		return fileResult;
	}
	public void setFileResult(String fileResult) {
		this.fileResult = fileResult;
	}
	public String getLocalException() {
		return localException;
	}
	public void setLocalException(String localException) {
		this.localException = localException;
	}
	
	public long getNumberResponse() {
		return numberResponse;
	}
	public void setNumberResponse(long numberResponse) {
		this.numberResponse = numberResponse;
	}
	public boolean isBooleanResponse() {
		return booleanResponse;
	}
	public void setBooleanResponse(boolean booleanResponse) {
		this.booleanResponse = booleanResponse;
	}

	public String getRemoteGeneralException() {
		return remoteGeneralException;
	}
	public void setRemoteGeneralException(String remoteGeneralException) {
		this.remoteGeneralException = remoteGeneralException;
	}
	public String getRemoteAuthenticationException() {
		return remoteAuthenticationException;
	}
	public void setRemoteAuthenticationException(String remoteAuthenticationException) {
		this.remoteAuthenticationException = remoteAuthenticationException;
	}
	public InputStream getByteResponseInputStream() {
		return byteResponseInputStream;
	}
	public void setByteResponseInputStream(InputStream byteResponseInputStream) {
		this.byteResponseInputStream = byteResponseInputStream;
	}
	
	public List<Long> getNumberListResponse() {
		return numberListResponse;
	}
	public void setNumberListResponse(List<Long> numberListResponse) {
		this.numberListResponse = numberListResponse;
	}
	public String getRemoteWrongVersionException() {
		return remoteWrongVersionException;
	}
	public void setRemoteWrongVersionException(String remoteWrongVersionException) {
		this.remoteWrongVersionException = remoteWrongVersionException;
	}
	@Override
	public String toString() {
		return "RemoteFSResponse [fileListResult=" + fileListResult + ", fileResult=" + fileResult
				+ ", remoteGeneralException=" + remoteGeneralException + ", remoteAuthenticationException="
				+ remoteAuthenticationException + ", remoteWrongVersionException=" + remoteWrongVersionException
				+ ", localException=" + localException + ", numberResponse=" + numberResponse + ", numberListResponse="
				+ numberListResponse + ", booleanResponse=" + booleanResponse + "]";
	}
	
}
