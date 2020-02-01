package com.ken.kenutils.fileUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import com.ken.kenutils.constants.Constants;
import java.nio.file.OpenOption;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

public class KenFileUtils {
	

    public void initiateFileMonitor(String pathToFile, int logMode, Boolean filePathModeAppend)
    {
    	if(pathToFile==null)
    	{
    		pathToFile = "E:/Arun/trash";
    	}
    	
        FileAlterationObserver observer = new FileAlterationObserver(pathToFile);
        FileAlterationMonitor monitor = new FileAlterationMonitor(5*1000);
        
        FileAlterationListener listener = new FileAlterationListenerAdaptor() {
            @Override
            public void onFileCreate(File file) {
                try 
                {
                    String filePath = file.getCanonicalPath();
                    System.out.println("File created: "+ filePath);
                    writeLogToFile(null, filePath, logMode, filePathModeAppend,Constants.MODE_FILE_CREATED);
                } 
                catch (IOException e) {
                    e.printStackTrace(System.err);
                }
            }
 
            @Override
            public void onFileDelete(File file) {
                try 
                {
                    System.out.println("File removed: "+ file.getCanonicalPath());
                    
                } 
                catch (IOException e) {
                    e.printStackTrace(System.err);
                }
            }
            
            @Override
            public void onFileChange(File file) {
            	try 
            	{
                    String filePath = file.getCanonicalPath();
                    System.out.println("File changed: "+ filePath);                    
                    writeLogToFile(null, filePath, logMode, filePathModeAppend,Constants.MODE_FILE_UPDATED);
                } 
            	catch (IOException e) {
                    e.printStackTrace(System.err);
                }
            }
            
            @Override
            public void onDirectoryCreate(File directory) {
            	try 
            	{
                    System.out.println("Directory created: "+ directory.getCanonicalPath());
                } 
            	catch (IOException e) {
                    e.printStackTrace(System.err);
                }
            }
            
            @Override
            public void onDirectoryDelete(File directory) {
            	try 
            	{
                    System.out.println("Directory deleted: "+ directory.getCanonicalPath());
                } 
            	catch (IOException e) {
                    e.printStackTrace(System.err);
                }
            }
            
            @Override
            public void onDirectoryChange(File directory) {
            	try 
            	{
                    System.out.println("File created: "+ directory.getCanonicalPath());
                } 
            	catch (IOException e) {
                    e.printStackTrace(System.err);
                }
            }
            
        };
        
        observer.addListener(listener);
        monitor.addObserver(observer);
        try {
            monitor.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	
    public static void writeLogToFile(String logFilePath, String logData, int logMode,Boolean filePathModeAppend,String modeStr) throws IOException
    {
        if(logFilePath==null)
        {
            logFilePath = Paths.get(System.getProperty("user.dir"))
                            .resolve("logFile.txt").toString();
        }

        File logFile = new File(logFilePath);
        if(!logFile.exists())
        {
            try {
                Files.createFile(Paths.get(logFilePath));
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

        if(filePathModeAppend==true)
        {
            logData = modeStr+logData;
        }

        List<String> changeLogDataList = readTextData(logFilePath);

        if(logMode==Constants.MODE_LOG_ALL)
        {
            writeByteData(logFilePath, (logData+"\n").getBytes(), StandardOpenOption.APPEND);
        }
        else if(logMode==Constants.MODE_CREATED_OR_EDITED)
        {
            if(!listContainsFileMode(changeLogDataList, logData))
            {
                writeByteData(logFilePath, (logData+"\n").getBytes(), StandardOpenOption.APPEND);
            }
        }
		
    }
	
    public static boolean listContainsFileMode(List<String> list, String filePath)
    {
        if(list.contains(Constants.MODE_FILE_CREATED+filePath) || 
                        list.contains(Constants.MODE_FILE_UPDATED+filePath) || 
                        list.contains(Constants.MODE_FILE_DELETED+filePath) ||
                        list.contains(filePath))
        {
            return true;
        }

        return false;
    }
	
    public static List<String> readTextData(String pathToFile) throws IOException
    {
        return Files.readAllLines(Paths.get(pathToFile));
    }
	
    public static void writeTextData(String destionationPath, String textData, OpenOption... options) throws IOException
    {
        BufferedWriter buffWriter = Files.newBufferedWriter(Paths.get(destionationPath), options);
        buffWriter.write(textData);

    }
	
    public static void readByteData()
    {

    }
	
    public static void writeByteData(String destionationPath, byte[] byteData,OpenOption... options) throws IOException
    {
        File logFile = new File(destionationPath);
        if(!logFile.exists())
        {
            Files.createFile(Paths.get(destionationPath));
        }
        Files.write(Paths.get(destionationPath), byteData, options);
    }
	
    public static void copyOrMoveFiles(Path sourceFilePath, Path destinationDirPath, boolean inheritDirectoryStructure) throws IOException
    {
        Path desPath = null;
        Path destinationParent = null;
        Path rootPath = sourceFilePath.getRoot();
        Path desChild = sourceFilePath.getFileName();

        if(inheritDirectoryStructure)
        {
                Path relPath = rootPath.relativize(sourceFilePath);
        desPath = destinationDirPath.resolve(relPath);
        destinationParent = desPath.getParent();
        }
        else
        {
                desPath = destinationDirPath.resolve(desChild);
                destinationParent = desPath.getParent();
        }

        if(!(new File(destinationParent.toString())).exists())
        {
                Files.createDirectories(destinationParent);
        }

        Files.copy(sourceFilePath, desPath, StandardCopyOption.REPLACE_EXISTING);
    }
    
    public static List<File> getAllFilesInDirectory(File directory, String extensions){
    	List<File> files = null;
    	if(extensions!=null){
    		files = (List<File>) FileUtils.listFiles(directory, FileFilterUtils.
            		suffixFileFilter(extensions, IOCase.INSENSITIVE), TrueFileFilter.INSTANCE);
    	}
    	else{
    		files = (List<File>) FileUtils.listFiles(directory, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
    	}
        return files;
    }
    
    public static Map<String, Object> compareFileUsingMemoryMapping(String path1, String path2) throws IOException{
    	Map<String, Object> map = new HashMap<>();
    	try(FileChannel ch1 = new RandomAccessFile(path1, "r").getChannel();
            FileChannel ch2 = new RandomAccessFile(path2, "r").getChannel()) {
    		
            if (ch1.size() != ch2.size()) {
                map.put("differ", true);
                map.put("message", "Files differ in size.");
                return map;
            }
            long size = ch1.size();
            ByteBuffer m1 = ch1.map(FileChannel.MapMode.READ_ONLY, 0L, size);
            ByteBuffer m2 = ch2.map(FileChannel.MapMode.READ_ONLY, 0L, size);
            for (int pos = 0; pos < size; pos++) {
                if (m1.get(pos) != m2.get(pos)) {
                    map.put("differ", true);
                    map.put("message", "Files differ at position : "+ pos+".");
                    return map;
                }
            }
            
            map.put("differ", false);
            map.put("message", "Files are the same.");
            return map;
		} catch (IOException e) {
			throw new IOException(e);
		} 
    }
    
    public static Map<String, Object> compareFilesLineByLine(String path1, String path2) throws IOException{
    	Map<String, Object> map = new HashMap<>();
    	
    	String string1 = null;
    	String string2 = null;
    	int line = 1;
    	try(BufferedReader buffReader1 = new BufferedReader(new FileReader(path1));
	    	BufferedReader buffReader2 = new BufferedReader(new FileReader(path2))) {
			while((string1=buffReader1.readLine())!=null || (string2=buffReader2.readLine())!=null){
				if(string1!=null){
					string2=buffReader2.readLine();
				}

				if(string1==null){
					map.put("differ", true);
                    map.put("message", "No data in file-1 corresponding to file-2 at line : "+ line+".");
                    return map;
				}
				else if(string2==null){
					map.put("differ", true);
                    map.put("message", "No data in file-2 corresponding to file-1 at line : "+ line+".");
                    return map;
				}
				if(!string1.equals(string2)){
					map.put("differ", true);
                    map.put("message", "Files differ at line : "+ line+".");
                    return map;
				}
				line++;
			}
			
			map.put("differ", false);
            map.put("message", "Files are the same.");
            return map;
		} catch (Exception e) {
			throw new IOException(e);
		}
    }

}
