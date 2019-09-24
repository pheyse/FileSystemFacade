# FileSystemFacade
A facade to different file system types such as Native (like java.io.File), in memory, database, server, history, sub-directory and encrypted as well as combinations of these.

Created 2017-2019 by Philip Heyse

## License
Apace V2

## Featuers
 - FileSystemFacade (FSF) allows you to write unit tests simulating the regular file system in memory. All tests can run in parallel without any interference. With the change of one line of code / calling a different init-method the actual native file system (java.io.File) is used
 - Instead of a normal local file system, the files may also be located in a database on a different server (RemoteFS) which is called via a Java servlet.  The behavior of the file system stays the same
 - Additional features can be added to the file system by basing one file system onto another.
 - File systems may be encrypted by using EnryptionFS with a different inner file system (e.g. NativeFS, MemoryFS, DatabaseFS, RemoteFS)
 - File systems may have version logic by using HistroyFS with a different inner file system. Each file then has a version property and you can enforce that only the next version of a file may be written - avoiding cases where two different instances each attempt to write the next version causing data inconsistencies.
 - File systems may have a limited or infinite number of history entries each time a file or directory is changed or deleted, also by using HistoryFS.
 - File systems may be limited to only a specific sub-directory to provide an app from writing "outside" of it's designated data folder in case of a malfunction or an attack. 
 - It is possible to copy from one file system to another. Examples: copy from MemoryFS to the NativeFS after a failed unit test, copy from RemoteFS or DatabaseFS to a NativeFS for backup.

## Usage
### Native file system with behavior similar to java.io.File
```java
FSFSystem fs = new NativeFS();
FSFFile myDir = fs.createByPath("C:\data\my_dir");
FSFFile myTextFile = myDir.getChild("myText.txt");
String text = myTextFile.readString(); //: read file as UTF-8 text
```

### In-memory file system 
```java
FSFSystem fs = new MemoryFS();
FSFFile myDir = fs.createByPath("/data/my_dir");
FSFFile myTextFile = myDir.getChild("myText.txt");
String text = myTextFile.readString(); //: read file as UTF-8 text
```

### Operations with different file systems
```java
private static fianl int FSF_TYPE = 3;

public FSFFile getAppDir(int type) throws Exception {
	String password = "myPassword";
	switch (type) {
	case 1:
		return new NativeFS().createByPath("C:\\my_data\\app1");
	case 2:
		return new NativeFS().createByPath("/usr/local/app1");
	case 3:
		return new MemoryFS().createByPath("/data");
	case 4:
		FSFSystem innerFS = new MemoryFS();
		innerFS.createByPath("/data").mkdirs();
		return new EncryptedFS(innerFS, password, "/data").createByPath("/encrypetd");
	case 5:
		new RemoteFS("/", "myApp", "myTennant", "myUser", "myPassword", "https://myserver.com/remoteFS").createByPath("/data");
	case 6:
		new DatabaseFS(createDatabaseConfig()).createByPath("/data");
	case 7:
		new HistoryFS(new MemoryFS(), true).createByPath("/data");
	case 8:
		new HistoryFS(new NativeFS(), true).createByPath("C:\\my_data\\app1");
	case 9:
		new SubDirFS(new NativeFS(), "/usr/local/app1").createByPath("/");
	default:
		throw new Exception("Unexpected type: " + type);
	}
}

public void static void main(String[] args){
	// depending on the type constant a different file system type is returned. 
	FSFFile appDir = getAppDir(FSF_TYPE);
	
	// all following actions can be performed whichever file system type was chosen:

	appDir.mkDirs();

	appDir.getChild("textOutput").mkDir().getChild("mytext.txt").writeString("hello!");

	FSFFile file = appDir.getChild("MyFile.txt").writeString("nice");
	file.renameTo("newName.txt");

	List<FSFFile> allFiles = appDir.listFilesTree();
	for (FSFFile i: allFiles){
		System.out.println("Name = '" + i.getName() + "'");
		System.out.println("Path = '" + i.getAbsolutePath() + "'");
		System.out.println("Length = '" + i.getLength() + "'");
	}
	
	file.delete();
}
```

## Including via Maven
```xml
[...]
		<dependency>
			<groupId>de.bright-side.filesystemfacade</groupId>
			<artifactId>filesystemfacade</artifactId>
			<version>2.6.0</version>
		</dependency>
[...]
```

 
## Change History
Version 2.3.0 (2019-08-03)
 - encryption via the BEAM module
 
Version 2.4.0 (2019-08-07)
 - added feature SubDirFS
 
Version 2.4.1 (2019-08-20)
 - with RemoteFS allows copyTo to a different FS
 
Version 2.4.2 (2019-08-20)
 - bugfix copy files tree for history FS
 
Version 2.5.0 (2019-09-06)
 - EncryptedFS allows versions and works with inner RemoteFS
 
Version 2.5.1 (2019-09-07)
 - EncryptedFS performance improvement - especially for RemoteFS
 
Version 2.5.2 (2019-09-12)
 - updated to BEAM 1.0.1 which does not need external XML library to read/write hex-string and works with Android
 
Version 2.6.0 (2019-09-13)
 - RemoteFS allow reading file history
