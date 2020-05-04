package de.bright_side.filesystemfacade.remotefs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import de.bright_side.filesystemfacade.util.FSFFileUtil;

/**
 * @author Philip Heyse
 *
 */
public class RemoteFSServletConnectionProvider implements RemoteFSConnectionProvider{
	private String servletURL;
	private URL url;
	private URLConnection connection;

	public RemoteFSServletConnectionProvider(String servletURL) throws Exception {
		this.servletURL = servletURL;
	    try {
	        url = new URL(servletURL);
	    } catch (MalformedURLException e) {
	        throw new Exception("Wrong URL: '" + servletURL + "'");
	    }
	}

	@Override
	public InputStream getInputStream() throws Exception {
		return connection.getInputStream();
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
	    try {
	    	connection = url.openConnection();
	    } catch (MalformedURLException e) {
	        throw new IOException("Could not access URL: '" + servletURL + "'");
	    }
		
		connection.setDoOutput(true);
		return connection.getOutputStream();
	}
	
	@Override
	public int compareLocation(RemoteFSConnectionProvider other) {
		if (other == null) {
			return 1;
		}
		if (!(other instanceof RemoteFSServletConnectionProvider)) {
			return getClass().getName().compareTo(other.getClass().getName());
		}
		RemoteFSServletConnectionProvider otherProvider = (RemoteFSServletConnectionProvider)other;
		
		int result = 0;
		result = FSFFileUtil.compareString(servletURL, otherProvider.servletURL);
		if (result != 0){
			return result;
		}
		return 0;
	}

//	@Override
//	public RemoteFSResponse handle(String command, InputStream payload, Object... parameters) throws Exception {
//	    URL url;
//	    try {
//	        url = new URL(servletURL);
//	    } catch (MalformedURLException e) {
//	        throw new Exception("Wrong URL: '" + servletURL + "'");
//	    }
//	
//	    try {
//	        URLConnection connection = url.openConnection();
//	        connection.setDoOutput(true);
//	        
//	        
//	        
//	        OutputStream outputStream = connection.getOutputStream();
//	        GZIPOutputStream zipOutputStream = new GZIPOutputStream(outputStream);
//
//	        FSFFileUtil.writeStringWithLengthInfo(zipOutputStream, app);
//	        FSFFileUtil.writeStringWithLengthInfo(zipOutputStream, tennant);
//	        FSFFileUtil.writeStringWithLengthInfo(zipOutputStream, username);
//	        FSFFileUtil.writeStringWithLengthInfo(zipOutputStream, password);
//	        
//	        RemoteFSUtil.writeHandleRequestToStream(zipOutputStream, command, payload, parameters);
//	        zipOutputStream.finish();
//	        zipOutputStream.flush();
//	        zipOutputStream.close();
//	
//	        InputStream inputStream = connection.getInputStream();
//	        GZIPInputStream gzipin = new GZIPInputStream(inputStream);
//	        
//	        RemoteFSResponse result = RemoteFSUtil.readRemoteFileResponseFromStream(gzipin);
//	        result.setByteResponseInputStream(gzipin);
//	        return result;
//	    } catch (UnknownHostException e) {
//	        throw e;
//	    }		
//	}
	

	
}
