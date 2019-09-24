package de.bright_side.filesystemfacade.encryptedfs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import de.bright_side.beam.Beam;
import de.bright_side.beam.BeamProgressListener;
import de.bright_side.filesystemfacade.facade.FSFEnvironment;
import de.bright_side.filesystemfacade.facade.FSFFile;
import de.bright_side.filesystemfacade.facade.FSFSystem;
import de.bright_side.filesystemfacade.util.FSFFileUtil;


/**
 * A file system which resides inside a sub-directory within another file system such as NativeFS ("normal" file system on the disk), 
 * DataBaseFS, on a data base, MemoryFS in Memory only (e.g. for unit tests), etc.
 * All files and filenames are encrypted by a password.
 * The used encryption module is BEAM (Basic Encryption Algorithm Module). The level of security is that regular users cannot read the data while hackers or government agencies are likely to
 * be able to decrypt the data.  Sample usage:
 * <code>
 *		FSFSystem fs = EncryptedFS(new NativeFS(), "This-is-the-password!", "/home/me/encrypted_dir");
 *		fs.createByPath("/myFile.txt").writeString("My secret text");
 *      String readText = fs.createByPath("/myFile.txt").readString();
 * </code>
 * 
 * @author Philip Heyse
 *
 */
public class EncryptedFS implements FSFSystem{
	private static final int HEX_DIGITS_PER_BYTE = 2;
	private static final int MAXIMUM_FILENAME_LENGTH_IN_FILE_SYSTEM = 128;
	private static final int FILENAME_ENCRYPTION_BLOCK_SIZE = 32;
	private static final String ENCODING = "UTF-8";
	private static final String FILE_LENGTH_SEPARATOR = "_";
	
	protected static final String SEPARATOR = "/";
	protected static final int MAXIMUM_FILENAME_LENGTH = MAXIMUM_FILENAME_LENGTH_IN_FILE_SYSTEM / HEX_DIGITS_PER_BYTE;

	private Beam dataProcessor;
	private Beam filenameProcessor;
	private String basePath;
	private FSFFile baseDir;
	private FSFSystem innerFS;
	
	/**
	 * 
	 * @param innerFS inner file system on which the encrypted file system is based
	 * @param password password that is used to encrypt and decrypt the data
	 * @param basePath the path where the encrypted dir is located, e.g. "C:\\data\\myEncryptedDir"
	 * @throws Exception on general error
	 */
	public EncryptedFS(FSFSystem innerFS, String password, String basePath) throws Exception {
		this (innerFS, password.getBytes(ENCODING), basePath, FSFFileUtil.createDefaultEnvironment());
	}
	
	/**
	 * 
	 * @param innerFS inner file system on which the encrypted file system is based
	 * @param password password that is used to encrypt and decrypt the data
	 * @param basePath the path where the encrypted dir is located, e.g. "C:\\data\\myEncryptedDir"
	 * @param environment instance to e.g. provide the current time
	 * @throws Exception on general error
	 */
	public EncryptedFS(FSFSystem innerFS, String password, String basePath, FSFEnvironment environment) throws Exception {
		this (innerFS, password.getBytes(ENCODING), basePath, environment);
	}
	
	/**
	 * 
	 * @param innerFS inner file system on which the encrypted file system is based
	 * @param password password that is used to encrypt and decrypt the data
	 * @param basePath the path where the encrypted dir is located, e.g. "C:\\data\\myEncryptedDir"
	 * @throws Exception on general error
	 */
	public EncryptedFS(FSFSystem innerFS, byte[] password, String basePath) throws Exception {
		this(innerFS, password, basePath, FSFFileUtil.createDefaultEnvironment());
	}
	
	/**
	 * 
	 * @param innerFS inner file system on which the encrypted file system is based
	 * @param password password that is used to encrypt and decrypt the data
	 * @param basePath the path where the encrypted dir is located, e.g. "C:\\data\\myEncryptedDir"
	 * @param environment instance to e.g. provide the current time
	 * @throws Exception on general error
	 */
	public EncryptedFS(FSFSystem innerFS, byte[] password, String basePath, FSFEnvironment environment) throws Exception {
		this.innerFS = innerFS;
		this.basePath = FSFFileUtil.removeIfEndsWith(basePath, innerFS.getSeparator()) + innerFS.getSeparator(); //: let path end with separator (but of course only once)
		dataProcessor = new Beam(password);
		filenameProcessor = new Beam(password, FILENAME_ENCRYPTION_BLOCK_SIZE);
		baseDir = innerFS.createByPath(this.basePath);
		if (baseDir.getAbsolutePath().replace(innerFS.getSeparator(), "").isEmpty()){
			throw new Exception("The base dir '" + this.basePath + "' may not be the root ('/') of the file system");
		}
		if (baseDir.getParentFile() == null) {
			throw new Exception("The base dir '" + this.basePath + "' must have a parent directory in the file system");
		}
		
		if (!baseDir.exists()){
			throw new Exception("Provided base path '" + basePath + "' does not exist in inner file system");
		}
	}
	
	@Override
	public List<FSFFile> listRoots() {
		List<FSFFile> result = new ArrayList<>();
		result.add(new EncryptedFile(this, baseDir));
		return result;
	}

	@Override
	public FSFFile createByPath(String path) throws Exception {
		if ((path.isEmpty()) || (path.equals(SEPARATOR))) {
			return new EncryptedFile(this, baseDir);
		}
		
		if (new String(SEPARATOR + path).contains(SEPARATOR + ".." + SEPARATOR)){
			throw new Exception("Path may not contain dir-up-sequence " + SEPARATOR + "'..'" + SEPARATOR);
		}
		
		if (!path.startsWith(SEPARATOR)) {
			throw new Exception("Path must start with '" + SEPARATOR + "', but was '" + path + "'");
		}
		
		String internalPath = path.substring(SEPARATOR.length());
		internalPath = FSFFileUtil.removeIfEndsWith(internalPath, SEPARATOR);
		
		String[] items = internalPath.split(SEPARATOR);
		FSFFile innerFile = baseDir;
		for (String filename: items) {
			try {
				validateFilenameWithException(filename);
			} catch (Exception e) {
				throw new Exception("Wrong item in path '" + path + "'", e);
			}
			
			innerFile = toExistingInnerFileIfPossibleByParentAndName(innerFile, filename, null);
		}
		
		EncryptedFile result = new EncryptedFile(this, innerFile); 
		
		return result;
	}

	@Override
	public String getSeparator() {
		return SEPARATOR;
	}

	protected String createEncodedFilename(String filename, long plainDataLength) {
		try {
			String result = filenameProcessor.encrypt(filename);
			if (plainDataLength > 0) {
				result += FILE_LENGTH_SEPARATOR + Long.toHexString(plainDataLength);
			}
			
			return result;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	protected String readPlainFilename(String encodedFilename) {
		try {
			String[] items = encodedFilename.split(FILE_LENGTH_SEPARATOR);
			String result = filenameProcessor.decrypt(items[0]);
			return result;
		} catch (Exception e) {
			throw new RuntimeException("Could not read plain file name from encoded filenam '" + encodedFilename + "'", e);
		}
	}
	
	protected long readPlainDataLength(String filename) {
		try {
			String[] items = filename.split(FILE_LENGTH_SEPARATOR);
			long result = 0; 
			if (items.length > 1) {
				result = Long.parseLong(items[1], 16); 
			}
			return result;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private byte[] stringToBytes(String string) {
		try {
			return string.getBytes(ENCODING);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	protected void validateNotRootWithException(EncryptedFile file) throws Exception {
		if (isRoot(file)) {
			throw new Exception("Operation not allowed for root of encrypted file system");
		}
	}

	protected boolean isRoot(EncryptedFile file){
		return file.getParentFile() == null;
	}
	

	protected void validateFilenameWithException(String filename) throws Exception{
		if (stringToBytes(filename).length > MAXIMUM_FILENAME_LENGTH) {
			throw new Exception("Filename '" + filename + "' name exceeds maximum length of " + MAXIMUM_FILENAME_LENGTH + " bytes in UTF-8");
		}
		if (filename.contains(SEPARATOR)) {
			throw new Exception("Filename '" + filename + "' contains illegal char '" + SEPARATOR + "'");
		}
	}
	
	protected void validateFilenameWithRuntimeException(String filename) {
		try {
			validateFilenameWithException(filename);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	protected boolean isExternalPathInBasePath(String path) {
		String usePath = path + innerFS.getSeparator();
		boolean result = usePath.startsWith(basePath);
		return result;
	}

	protected String getBasePath() {
		return basePath;
	}
	
	protected byte[] decryptData(byte[] bytes) throws Exception {
		return dataProcessor.decrypt(bytes);
	}

	protected byte[] encryptData(byte[] bytes) throws Exception {
		return dataProcessor.encrypt(bytes);
	}

	protected InputStream createDecryptedInputStream(InputStream inputStream) {
		return dataProcessor.getDecryptedInputStream(inputStream, null);
	}

	protected OutputStream createEncryptedOutputStream(OutputStream outputStream, final boolean append, final EncryptedFile file) {
		long appendedLength = 0;
		if (append) {
			appendedLength = file.getLength();
		}
		final long useAppendedLength = appendedLength;
		
		//: once the output stream is closed, make sure that the filename of the inner file which also contains the decryped length in the name is correct
		return dataProcessor.getEncryptedOutputStream(outputStream, new BeamProgressListener() {
			@Override
			public void finishedSuccessfully(long amountPlain, long amountEncrypted) throws IOException{
				renameAccordingToLength(file, useAppendedLength, amountPlain);
			}

			@Override
			public void bytesProcessed(long amountPlain, long amountEncrypted) {
			}
		});
	}

	public FSFSystem getInnerFS() {
		return innerFS;
	}

	/**
	 * for each plain file name there are multiple encrypted file names. This is because the encryption is done block-wise and if a block isn't full 
	 * (which is usually the case for the last block) the remaining bytes are filled with random data. Hence each time an encrypted file name is generated, it is different 
	 * (unless the length matches exactly 1 block).
	 * @param plainDataLength: null if unknown (e.g. because createByPath was called an no length was provided)
	 * @param innerParentFile the inner parent file
	 * @param plainFilename the filename in a not encrypted form
	 * @param plainDataLength the length of the data if it wasn't encrypted
	 * @return the existing inner file if found 
	 */
	protected FSFFile toExistingInnerFileIfPossibleByParentAndName(FSFFile innerParentFile, String plainFilename, Long plainDataLength) {
		if (innerParentFile.exists()) {
			for (FSFFile i: innerParentFile.listFiles()) {
				if (readPlainFilename(i.getName()).equals(plainFilename)) {
					//: found an existing file that has the same plainName as the innerFile provided as the parameter, but it exists
					return i;
				}
			}
		}
		
		
		//: no existing file found, create a new one
		long useLength = 0;
		if (plainDataLength != null) {
			useLength = plainDataLength.longValue();
		}
		String encodedFilename = createEncodedFilename(plainFilename, useLength);
		return innerParentFile.getChild(encodedFilename);
	}
	
	/**
	 * for each plain file name there are multiple encrypted file names. This is because the encryption is done block-wise and if a block isn't full 
	 * (which is usually the case for the last block) the remaining bytes are filled with random data. Hence each time an encrypted file name is generated, it is different 
	 * (unless the length matches exactly 1 block).
	 * @param innerFile the current inner file
	 * @return the new inner file
	 */
	protected FSFFile toExistingInnerFileIfPossible(FSFFile innerFile) {
		if (innerFile == null) {
			return null;
		}
		if (innerFile.exists()) {
			return innerFile;
		}
		FSFFile parent = innerFile.getParentFile();
		if (parent == null) {
			throw new RuntimeException("inner file has no parent: " + innerFile);
		}
		if (!parent.exists()) {
			//: if the parent file of the inner file doesn't exist, it doesn't make sense to look for an existing file in the (non-existent) parent dir
			return innerFile;
		}
		
		String plainName = readPlainFilename(innerFile.getName());
		List<FSFFile> filesInParentDir = parent.listFiles();
		if (filesInParentDir == null) {
			String message = "parent of inner file returns null on listFiles. Parent: " + parent + "; inner file: " + innerFile;
			throw new RuntimeException(message);
		}
		
		for (FSFFile i: filesInParentDir) {
			if (readPlainFilename(i.getName()).equals(plainName)) {
				//: found an existing file that has the same plainName as the innerFile provided as the parameter, but it exists
				return i;
			}
		}
		
		//: no match found
		return innerFile;
	}

	/**
	 * The file size is stored in the filename so that it can be returned without any processing logic. However the filename needs to be adjusted whenever the length of the file changes.
	 * This method performs that operation.
	 * @param file the file object which should be renamed
	 * @param appendedLength if appended, the length before the appending. Otherwise 0
	 * @param writtenBytes number of written bytes
	 * @throws IOException on general errors
	 */
	protected void renameAccordingToLength(EncryptedFile file, long appendedLength, long writtenBytes) throws IOException {
		String plainFilename = file.getName();
		long newLength = appendedLength + writtenBytes;
		String newEncodedFilename = createEncodedFilename(plainFilename, newLength);
		
		try {
			file.getInnerFile().rename(newEncodedFilename);
		} catch (Exception e) {
			throw new IOException("Could not rename file to contain the correct plain data length. Old inner file name: '" + file.getInnerFile().getName() 
					+ "', new inner file name: '" + newEncodedFilename + "'. Encrypted file: " + file + ", encrypted file absolute path: " + file.getAbsolutePath(), e);
		}
		FSFFile newInnerFile = file.getInnerFile().getParentFile().getChild(newEncodedFilename);
		file.setInnerFile(newInnerFile);
	}

	protected static String getParentPath(String path){
		int pos = path.lastIndexOf(EncryptedFS.SEPARATOR);
		if (pos < 0){
			return "";
		}
		return path.substring(0, pos);
	}

}
